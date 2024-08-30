// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.json;

import com.oracle.database.spring.json.jsonb.JSONB;
import com.oracle.database.spring.json.jsonb.SODA;
import jakarta.json.bind.JsonbBuilder;
import oracle.soda.OracleDocument;
import oracle.soda.rdbms.OracleRDBMSClient;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JsonCollectionsAutoConfiguration {
    @Bean
    @ConditionalOnClass(OracleJsonFactory.class)
    OracleJsonFactory oracleJsonFactory() {
        return new OracleJsonFactory();
    }

    @Bean
    @ConditionalOnClass(YassonJsonb.class)
    YassonJsonb yassonJsonb() {
        return (YassonJsonb) JsonbBuilder.create();
    }

    @Bean
    @ConditionalOnClass(OracleRDBMSClient.class)
    OracleRDBMSClient oracleRDBMSClient() {
        return new OracleRDBMSClient();
    }

    @Bean
    @ConditionalOnClass({OracleJsonFactory.class, YassonJsonb.class})
    public JSONB jsonb(OracleJsonFactory oracleJsonFactory, YassonJsonb yassonJsonb) {
        return new JSONB(oracleJsonFactory, yassonJsonb);
    }

    @Bean
    @ConditionalOnClass({OracleJsonFactory.class, YassonJsonb.class, OracleDocument.class})
    public SODA soda(OracleJsonFactory oracleJsonFactory, YassonJsonb yassonJsonb) {
        return new SODA(oracleJsonFactory, yassonJsonb);
    }
}
