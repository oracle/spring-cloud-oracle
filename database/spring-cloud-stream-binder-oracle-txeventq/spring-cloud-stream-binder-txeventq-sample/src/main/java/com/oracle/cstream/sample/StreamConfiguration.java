// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.cstream.sample;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamConfiguration {
    private @Value("${phrase}") String phrase;

    @Bean
    public Function<String, String> toUpperCase() {
        return String::toUpperCase;
    }

    @Bean
    public Consumer<String> stdoutConsumer() {
        return s -> System.out.println("Consumed: " + s);
    }

    @Bean
    public WordSupplier wordSupplier() {
        return new WordSupplier(phrase);
    }
}
