/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import java.io.InputStream;

/**
 * Interface to convert Java POJOs to byte array stored on OCI storage service and InputStream of an object to Java POJO.
 */
public interface StorageObjectConverter {
    /**
     * Converts Java POJO to byte array.
     * @param object Java POJO
     * @param <T> Type of the Java POJO
     * @return byte[]
     */
    <T> byte[] write(T object);

    /**
     * Read InputStream and create Java POJO instance of the class T.
     * @param is InputStream to read data from.
     * @param clazz Class instance of the type of Java POJO
     * @param <T> Type of the Java POJO
     * @return Instance of Java POJO
     */
    <T> T read(InputStream is, Class<T> clazz);

    /**
     * Returns the storage content type.
     * @return String
     */
    String contentType();
}
