// Copyright (c) 2024, Oracle and/or its affiliates.
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

@SpringBootApplication
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
                Connection connection = dataSource.getConnection();
                System.out.println("Connection is : " + connection);

                PreparedStatement stmt = connection.prepareStatement("SELECT 'Hello World!' FROM dual");
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }

                stmt = connection.prepareStatement("SELECT BANNER_FULL FROM V$VERSION");
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(OracleSpringBootSampleWalletApplication.class, args);
    }
}

