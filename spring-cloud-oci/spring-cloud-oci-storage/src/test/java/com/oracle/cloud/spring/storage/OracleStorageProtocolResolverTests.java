/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OracleStorageProtocolResolverTests {

    final OracleStorageProtocolResolver oracleStorageProtocolResolver = new OracleStorageProtocolResolver();

    @Test
    void testPostProcessBeanFactory() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("objectStorageClient", StorageTestSupport.newObjectStorageClient());
        oracleStorageProtocolResolver.postProcessBeanFactory(beanFactory);
        assertNotNull(oracleStorageProtocolResolver.getStorageClient());
    }

    @Test
    void testSetResourceLoader() {
        oracleStorageProtocolResolver.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    void testGetStorageClient() {
        assertNull(oracleStorageProtocolResolver.getStorageClient());
    }

    @Test
    void testResolve() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("objectStorageClient", StorageTestSupport.newObjectStorageClient());
        oracleStorageProtocolResolver.postProcessBeanFactory(beanFactory);

        Resource resource = oracleStorageProtocolResolver.resolve(
                "https://objectstorage.us-chicago-1.oraclecloud.com/n/namespace/b/mybucket/o/myobject",
                new DefaultResourceLoader());
        assertNotNull(resource);
    }
}
