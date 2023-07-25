/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.common.util;

import java.io.File;
import java.io.FileWriter;

public class FileUtils {

    /**
     * File Utility to help file operations like create and delete
     */
    public static void createFile(String filePath, String fileContent) throws Exception {
        File file = new File(filePath);
        if(!file.exists() || file.isDirectory()) {
            String directoryPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
            File directory = new File(directoryPath);
            boolean bool = directory.mkdirs();
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(fileContent);
            myWriter.close();
        }
    }

    public static boolean deleteFile(String filePath) {
        File f = new File(filePath);
        return f.delete();
    }

}
