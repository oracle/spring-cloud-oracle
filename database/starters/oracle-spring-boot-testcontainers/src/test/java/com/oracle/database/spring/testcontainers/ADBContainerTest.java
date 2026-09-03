// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import oracle.jdbc.pool.OracleDataSource;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration coverage for all externally exposed ADB Free APIs. */
@Testcontainers(disabledWithoutDocker = true)
class ADBContainerTest {

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String ADMIN_PASSWORD = "SecurePass1234";
    private static final String WALLET_PASSWORD = "WalletPassword1";
    private static final String MONGO_USERNAME = "TC_MONGO";
    private static final String MONGO_PASSWORD = "Mongo'Pass1234";
    private static final SSLContext INSECURE_TLS_CONTEXT = insecureTlsContext();

    private static final ADBContainer ADB_CONTAINER = new ADBContainer()
            .withAdminPassword(ADMIN_PASSWORD)
            .withWalletPassword(WALLET_PASSWORD)
            .withAppUser(MONGO_USERNAME, MONGO_PASSWORD)
            .withAppUserRoles("DWROLE", "SODA_APP");

    private static ADBContainer.Wallet wallet;

    @BeforeAll
    static void startContainer() throws IOException, SQLException {
        ADB_CONTAINER.start();
        wallet = ADB_CONTAINER.copyWalletTo(Files.createTempDirectory("adb-free-wallet-"));
        enableMongoSchema();
    }

    @AfterAll
    static void stopContainer() throws IOException {
        try {
            ADB_CONTAINER.stop();
        } finally {
            if (wallet != null) {
                Path walletPath = wallet.getDirectory();
                wallet.close();
                assertFalse(Files.exists(walletPath), "Expected copied wallet directory to be removed");
            }
        }
    }

    @Test
    void connectsOverMutualTls() throws SQLException {
        assertDatabaseConnection(dataSource(ADB_CONTAINER.getMtlsServiceAlias()).getConnection());
    }

    @Test
    void connectsAsConfiguredAppUser() throws SQLException {
        assertDatabaseConnection(dataSource(
                ADB_CONTAINER.getMtlsServiceAlias(), MONGO_USERNAME, MONGO_PASSWORD).getConnection());
    }

    @Test
    void servesOrdsOverHttps() throws IOException, InterruptedException {
        HttpResponse<String> response = insecureHttpClient().send(
                HttpRequest.newBuilder(URI.create(
                                "https://%s:%d/ords/".formatted(ADB_CONTAINER.getHost(), ADB_CONTAINER.getHttpsPort())))
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() < 400, "Expected ORDS to respond successfully over HTTPS");
    }

    @Test
    void supportsMongoDbCrudOperations() {
        try (MongoClient client = MongoClients.create(mongoClientSettings())) {
            MongoDatabase database = client.getDatabase(MONGO_USERNAME);
            MongoCollection<Document> collection = database.getCollection("testcontainers");
            String documentId = "adb-free-integration";

            collection.deleteOne(Filters.eq("_id", documentId));
            collection.insertOne(new Document("_id", documentId).append("value", 1));

            Document insertedDocument = collection.find(Filters.eq("_id", documentId)).first();
            assertNotNull(insertedDocument, "Expected inserted MongoDB document");
            assertEquals(1, insertedDocument.getInteger("value"));

            assertEquals(1, collection.updateOne(
                    Filters.eq("_id", documentId), Updates.set("value", 2)).getModifiedCount());
            assertEquals(1, collection.deleteOne(Filters.eq("_id", documentId)).getDeletedCount());
            assertNull(collection.find(Filters.eq("_id", documentId)).first());
        }
    }

    private static void assertDatabaseConnection(Connection connection) throws SQLException {
        try (connection;
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 FROM DUAL")) {
            assertTrue(resultSet.next(), "Expected database query result");
            assertEquals(1, resultSet.getInt(1));
        }
    }

    private static OracleDataSource dataSource(String serviceAlias) throws SQLException {
        return dataSource(serviceAlias, ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private static OracleDataSource dataSource(String serviceAlias, String username, String password)
            throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("jdbc:oracle:thin:@" + serviceAlias);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setConnectionProperty("oracle.net.tns_admin", wallet.getDirectory().toString());
        return dataSource;
    }

    private static void enableMongoSchema() throws SQLException {
        try (Connection connection = dataSource(ADB_CONTAINER.getMtlsServiceAlias()).getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    BEGIN
                        ORDS.ENABLE_SCHEMA(
                            p_enabled => TRUE,
                            p_schema => 'TC_MONGO',
                            p_url_mapping_type => 'BASE_PATH',
                            p_url_mapping_pattern => 'tc_mongo',
                            p_auto_rest_auth => TRUE);
                        COMMIT;
                    END;
                    """);
        }
    }

    private static MongoClientSettings mongoClientSettings() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://%s:%s@%s:%d/admin?authMechanism=PLAIN&authSource=%%24external&tls=true&retryWrites=false&loadBalanced=true"
                        .formatted(
                                MONGO_USERNAME,
                                MONGO_PASSWORD,
                                ADB_CONTAINER.getHost(),
                                ADB_CONTAINER.getMongoDbApiPort()));

        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> builder
                        .enabled(true)
                        .invalidHostNameAllowed(true)
                        .context(INSECURE_TLS_CONTEXT))
                .build();
    }

    private static HttpClient insecureHttpClient() {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm(null);
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .sslContext(INSECURE_TLS_CONTEXT)
                .sslParameters(sslParameters)
                .build();
    }

    private static SSLContext insecureTlsContext() {
        try {
            TrustManager[] trustAllManagers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllManagers, new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize test TLS context", e);
        }
    }

}
