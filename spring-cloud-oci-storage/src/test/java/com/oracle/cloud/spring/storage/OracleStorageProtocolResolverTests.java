/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;

import static org.mockito.Mockito.*;

public class OracleStorageProtocolResolverTests {
    final BeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    final OracleStorageProtocolResolver oracleStorageProtocolResolver = new OracleStorageProtocolResolver();

    @Test
    public void testPostProcessBeanFactory() {
        oracleStorageProtocolResolver.postProcessBeanFactory((ConfigurableListableBeanFactory) beanFactory);
    }

    @Test
    public void testSetResourceLoader() {
        oracleStorageProtocolResolver.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    public void testGetStorageClient() {
        oracleStorageProtocolResolver.getStorageClient();
    }

    @Test
    public void testResolve() {
        try (MockedStatic mock = mockStatic(OracleStorageResource.class)) {
            oracleStorageProtocolResolver.resolve("ocs://test", new DefaultResourceLoader());
        }
    }
}
