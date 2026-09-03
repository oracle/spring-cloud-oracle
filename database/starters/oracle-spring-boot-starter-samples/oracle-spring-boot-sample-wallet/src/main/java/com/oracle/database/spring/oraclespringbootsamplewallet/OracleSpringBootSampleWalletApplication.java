// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.oraclespringbootsamplewallet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SpringBootApplication
@SuppressFBWarnings(value = "EI2",
        justification = "The DataSource is an application-managed Spring dependency used by this sample runner.")
public class OracleSpringBootSampleWalletApplication {

    final DataSource dataSource;

    public OracleSpringBootSampleWalletApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            try {
                System.out.println("Datasource is : " + dataSource.getClass().getName());
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement helloStatement = connection.prepareStatement("SELECT 'Hello World!' FROM dual");
                     ResultSet helloResultSet = helloStatement.executeQuery()) {
                System.out.println("Connection is : " + connection);

                    while (helloResultSet.next()) {
                        System.out.println(helloResultSet.getString(1));
                    }

                    try (PreparedStatement versionStatement = connection.prepareStatement("SELECT BANNER_FULL FROM V$VERSION");
                         ResultSet versionResultSet = versionStatement.executeQuery()) {
                        while (versionResultSet.next()) {
                            System.out.println(versionResultSet.getString(1));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(OracleSpringBootSampleWalletApplication.class, args);
    }
}
