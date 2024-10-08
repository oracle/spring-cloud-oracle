// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json;

import com.oracle.spring.json.jsonb.JSONB;
import jakarta.json.bind.JsonbBuilder;
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
    @ConditionalOnClass({OracleJsonFactory.class, YassonJsonb.class})
    public JSONB jsonb(OracleJsonFactory oracleJsonFactory, YassonJsonb yassonJsonb) {
        return new JSONB(oracleJsonFactory, yassonJsonb);
    }
}
