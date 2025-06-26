// Copyright (c) 2024, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json;

import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.kafka.OSONKafkaSerializationFactory;
import jakarta.json.bind.JsonbBuilder;
import oracle.sql.json.OracleJsonFactory;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.eclipse.yasson.YassonJsonb;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguration for the JSON Collections beans.
 * OKafka-related beans are only instantiated if the required interfaces
 * are on the classpath.
 */
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

    @Bean
    @ConditionalOnClass(value = {
            Deserializer.class,
            Serializer.class
    })
    public OSONKafkaSerializationFactory osonSerializationFactory(JSONB jsonb) {
        return new OSONKafkaSerializationFactory(jsonb);
    }
}
