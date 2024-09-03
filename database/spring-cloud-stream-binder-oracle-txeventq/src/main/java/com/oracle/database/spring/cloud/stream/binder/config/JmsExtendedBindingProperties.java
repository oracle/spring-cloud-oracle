/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.spring.cloud.stream.binder.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;

@ConfigurationProperties("spring.cloud.stream.txeventq")
public class JmsExtendedBindingProperties
        extends
        AbstractExtendedBindingProperties<JmsConsumerProperties, JmsProducerProperties, JmsBindingProperties> {

    private static final String DEFAULT_PREFIX = "spring.cloud.stream.txeventq.default";

    @Override
    public Map<String, JmsBindingProperties> getBindings() {
        return this.doGetBindings();
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULT_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return JmsBindingProperties.class;
    }
}
