/*
 ** Copyright (c) 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.oracle.cloud.spring.storage.Storage;
import com.oracle.cloud.spring.storage.StorageObjectMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ImageControllerTests {

    @Test
    void uploadUsesGeneratedObjectNameInsteadOfClientFilename() throws Exception {
        AtomicReference<String> capturedBucketName = new AtomicReference<>();
        AtomicReference<String> capturedObjectName = new AtomicReference<>();
        AtomicReference<StorageObjectMetadata> capturedMetadata = new AtomicReference<>();
        Storage storage = (Storage) Proxy.newProxyInstance(
                Storage.class.getClassLoader(),
                new Class<?>[] {Storage.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "capturingStorage";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    if ("upload".equals(method.getName()) && method.getParameterCount() == 4) {
                        capturedBucketName.set((String) args[0]);
                        capturedObjectName.set((String) args[1]);
                        capturedMetadata.set((StorageObjectMetadata) args[3]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        ImageController controller = new ImageController();
        controller.storage = storage;
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "../../etc/passwd?.PNG",
                "image/png",
                "payload".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<?> response = controller.upload(multipartFile, "images");

        assertThat(capturedBucketName.get()).isEqualTo("images");
        assertThat(capturedObjectName.get())
                .startsWith("upload-")
                .endsWith("-passwd.png")
                .doesNotContain("/")
                .doesNotContain("\\")
                .doesNotContain("..")
                .doesNotContain("?");
        assertThat(response.getBody()).isEqualTo(Map.of("objectName", capturedObjectName.get()));
        assertThat(capturedMetadata.get().getContentType()).isEqualTo("image/png");
    }

    @Test
    void createStorageObjectNameFallsBackWhenFilenameIsMissing() {
        assertThat(ImageController.createStorageObjectName(null))
                .startsWith("upload-")
                .endsWith("-file");
    }
}
