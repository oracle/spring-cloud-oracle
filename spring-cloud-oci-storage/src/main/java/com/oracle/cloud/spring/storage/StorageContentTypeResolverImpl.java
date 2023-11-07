/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Properties;

/**
 * Default implementation of StorageContentTypeResolver.
 */
public class StorageContentTypeResolverImpl implements StorageContentTypeResolver {
    private static final String PROPERTIES_FILE_LOCATION = "/com/oracle/cloud/spring/storage/StorageContentTypeResolver.properties";

    static final String ERROR_PROPS_FILE_REQUIRED = "properties are required";
    static final String ERROR_FILE_NAME_REQUIRED = "fileName is required";

    private final Properties properties;

    public StorageContentTypeResolverImpl() {
        this(loadProperties());
    }

    private static Properties loadProperties() {
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource(PROPERTIES_FILE_LOCATION));
        } catch (IOException e) {
            throw new StorageException(
                    "Error when loading properties from " + PROPERTIES_FILE_LOCATION + " for content type resolution",
                    e);
        }
    }

    public StorageContentTypeResolverImpl(Properties properties) {
        Assert.notNull(properties, ERROR_PROPS_FILE_REQUIRED);
        this.properties = properties;
    }

    /**
     * Identifies and returns the content type by matching the fileName with StorageContentTypeResolver.properties.
     * @param fileName name of the file object.
     * @return Content Type
     */
    @Override
    public String resolveContentType(String fileName) {
        Assert.notNull(fileName, ERROR_FILE_NAME_REQUIRED);

        String extension = resolveExtension(fileName);
        return extension == null ? null : properties.getProperty(extension);
    }

    @Nullable
    public String resolveExtension(String fileName) {
        Assert.notNull(fileName, ERROR_FILE_NAME_REQUIRED);

        return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : null;
    }
}

