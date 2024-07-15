/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * Default implementation of ProtocolResolver to resolve Object URIs starting with specific protocol prefix.
 */
public class OracleStorageProtocolResolver implements ProtocolResolver, ResourceLoaderAware, BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleStorageProtocolResolver.class);

    @Nullable
    private ObjectStorageClient osClient;

    @Nullable
    private BeanFactory beanFactory;

    /**
     * Resolves OCI storage location URI to Resource.
     * @param location URI starting with protocol prefix.
     * @param resourceLoader Instance of ResourceLoader
     * @return Spring Resource.
     */
    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        ObjectStorageClient osClient = getStorageClient();
        if (osClient == null) {
            LOGGER.warn("Could not resolve ObjectStorageClient. Resource {} could not be resolved", location);
            return null;
        }

        return OracleStorageResource.create(location, osClient);
    }

    /**
     * postProcessBeanFactory implementation
     * @param beanFactory instance of ConfigurableListableBeanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Sets the {@link ResourceLoader}
     * @param resourceLoader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("The provided delegate resource loader is not an implementation "
                    + "of DefaultResourceLoader. Custom OCI Object Storage prefix will not be enabled.");
        }
    }

    /**
     * Get {@link ObjectStorageClient}
     * @return ObjectStorageClient
     */
    @Nullable
    public ObjectStorageClient getStorageClient() {
        if (osClient != null) {
            return osClient;
        }

        if (beanFactory != null) {
            ObjectStorageClient osClient = beanFactory.getBean(ObjectStorageClient.class);
            this.osClient = osClient;
            return osClient;
        }

        return null;
    }

}
