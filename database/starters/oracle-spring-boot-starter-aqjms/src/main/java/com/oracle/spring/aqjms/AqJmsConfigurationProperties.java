// Copyright (c) 2023, 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.aqjms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Oracle Spring Boot Starter for AQ/TxEventQ JMS
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class AqJmsConfigurationProperties {
    /**
     * Database URL, e.g. `jdbc:oracle:thin:@//172.17.0.2:1521/pdb1`
     */
    private String url;

    /**
     * Username to connect to database, e.g. `dave`
     */
    private String username;

    /**
     * Password to connect to database.
     */
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
