/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.model.StorageTier;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.retrier.RetryConfiguration;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holding class to define Object Metadata
 */
public class StorageObjectMetadata {

    @Nullable
    private String contentType;

    @Nullable
    private String contentLanguage;

    @Nullable
    private String contentEncoding;

    @Nullable
    private Map<String, String> opcMeta = new HashMap<>();

    @Nullable
    private String cacheControl;

    @Nullable
    private String contentDisposition;

    @Nullable
    private Long contentLength;

    @Nullable
    private String contentMD5;

    @Nullable
    private String expect;

    @Nullable
    private String ifMatch;

    @Nullable
    private String ifNoneMatch;

    @Nullable
    private String opcClientRequestId;

    @Nullable
    private String opcSseCustomerAlgorithm;

    @Nullable
    private String opcSseCustomerKey;

    @Nullable
    private String opcSseCustomerKeySha256;

    @Nullable
    private String opcSseKmsKeyId;

    @Nullable
    private RetryConfiguration retryConfiguration;

    @Nullable
    private StorageTier storageTier;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public Map<String, String> getOpcMeta() {
        return opcMeta;
    }

    public void setOpcMeta(Map<String, String> opcMeta) {
        this.opcMeta = opcMeta;
    }

    @Nullable
    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(@Nullable String cacheControl) {
        this.cacheControl = cacheControl;
    }

    @Nullable
    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(@Nullable String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    @Nullable
    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(@Nullable Long contentLength) {
        this.contentLength = contentLength;
    }

    @Nullable
    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(@Nullable String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    @Nullable
    public String getExpect() {
        return expect;
    }

    public void setExpect(@Nullable String expect) {
        this.expect = expect;
    }

    @Nullable
    public String getIfMatch() {
        return ifMatch;
    }

    public void setIfMatch(@Nullable String ifMatch) {
        this.ifMatch = ifMatch;
    }

    @Nullable
    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public void setIfNoneMatch(@Nullable String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
    }

    @Nullable
    public String getOpcClientRequestId() {
        return opcClientRequestId;
    }

    public void setOpcClientRequestId(@Nullable String opcClientRequestId) {
        this.opcClientRequestId = opcClientRequestId;
    }

    @Nullable
    public String getOpcSseCustomerAlgorithm() {
        return opcSseCustomerAlgorithm;
    }

    public void setOpcSseCustomerAlgorithm(@Nullable String opcSseCustomerAlgorithm) {
        this.opcSseCustomerAlgorithm = opcSseCustomerAlgorithm;
    }

    @Nullable
    public String getOpcSseCustomerKey() {
        return opcSseCustomerKey;
    }

    public void setOpcSseCustomerKey(@Nullable String opcSseCustomerKey) {
        this.opcSseCustomerKey = opcSseCustomerKey;
    }

    @Nullable
    public String getOpcSseCustomerKeySha256() {
        return opcSseCustomerKeySha256;
    }

    public void setOpcSseCustomerKeySha256(@Nullable String opcSseCustomerKeySha256) {
        this.opcSseCustomerKeySha256 = opcSseCustomerKeySha256;
    }

    @Nullable
    public String getOpcSseKmsKeyId() {
        return opcSseKmsKeyId;
    }

    public void setOpcSseKmsKeyId(@Nullable String opcSseKmsKeyId) {
        this.opcSseKmsKeyId = opcSseKmsKeyId;
    }

    @Nullable
    public RetryConfiguration getRetryConfiguration() {
        return retryConfiguration;
    }

    public void setRetryConfiguration(@Nullable RetryConfiguration retryConfiguration) {
        this.retryConfiguration = retryConfiguration;
    }

    @Nullable
    public StorageTier getStorageTier() {
        return storageTier;
    }

    public void setStorageTier(@Nullable StorageTier storageTier) {
        this.storageTier = storageTier;
    }

    void apply(PutObjectRequest.Builder builder) {
        if (contentEncoding != null) {
            builder.contentEncoding(contentEncoding);
        }

        if (contentLanguage != null) {
            builder.contentLanguage(contentLanguage);
        }

        if (opcMeta != null) {
            builder.opcMeta(opcMeta);
        }

        if (contentType != null) {
            builder.contentType(contentType);
        }

        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }

        if (contentDisposition != null) {
            builder.contentDisposition(contentDisposition);
        }

        if (contentLength != null) {
            builder.contentLength(contentLength);
        }

        if (contentMD5 != null) {
            builder.contentMD5(contentMD5);
        }

        if (expect != null) {
            builder.expect(expect);
        }

        if (ifMatch != null) {
            builder.ifMatch(ifMatch);
        }

        if (ifNoneMatch != null) {
            builder.ifNoneMatch(ifNoneMatch);
        }

        if (opcClientRequestId != null) {
            builder.opcClientRequestId(opcClientRequestId);
        }

        if (opcSseCustomerAlgorithm != null) {
            builder.opcSseCustomerAlgorithm(opcSseCustomerAlgorithm);
        }

        if (opcSseCustomerKey != null) {
            builder.opcSseCustomerKey(opcSseCustomerKey);
        }

        if (opcSseCustomerKeySha256 != null) {
            builder.opcSseCustomerKeySha256(opcSseCustomerKeySha256);
        }

        if (opcSseKmsKeyId != null) {
            builder.opcSseKmsKeyId(opcSseKmsKeyId);
        }

        if (retryConfiguration != null) {
            builder.retryConfiguration(retryConfiguration);
        }

        if (storageTier != null) {
            builder.storageTier(storageTier);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        @Nullable
        private String contentType;

        @Nullable
        private String contentLanguage;

        @Nullable
        private String contentEncoding;

        private final Map<String, String> opcMeta = new HashMap<>();

        @Nullable
        private String cacheControl;

        @Nullable
        private String contentDisposition;

        @Nullable
        private Long contentLength;

        @Nullable
        private String contentMD5;

        @Nullable
        private String expect;

        @Nullable
        private String ifMatch;

        @Nullable
        private String ifNoneMatch;

        @Nullable
        private String opcClientRequestId;

        @Nullable
        private String opcSseCustomerAlgorithm;

        @Nullable
        private String opcSseCustomerKey;

        @Nullable
        private String opcSseCustomerKeySha256;

        @Nullable
        private String opcSseKmsKeyId;

        @Nullable
        private RetryConfiguration retryConfiguration;

        @Nullable
        private StorageTier storageTier;

        public Builder contentType(@Nullable String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder contentLanguage(@Nullable String contentLanguage) {
            this.contentLanguage = contentLanguage;
            return this;
        }

        public Builder contentEncoding(@Nullable String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }

        public Builder metadata(String key, String value) {
            opcMeta.put(key, value);
            return this;
        }

        public Builder cacheControl(@Nullable String cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        public Builder contentDisposition(@Nullable String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

        public Builder contentLength(@Nullable Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentMD5(@Nullable String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public Builder expect(@Nullable String expect) {
            this.expect = expect;
            return this;
        }

        public Builder ifMatch(@Nullable String ifMatch) {
            this.ifMatch = ifMatch;
            return this;
        }

        public Builder ifNoneMatch(@Nullable String ifNoneMatch) {
            this.ifNoneMatch = ifNoneMatch;
            return this;
        }

        public Builder opcClientRequestId(@Nullable String opcClientRequestId) {
            this.opcClientRequestId = opcClientRequestId;
            return this;
        }

        public Builder opcSseCustomerAlgorithm(@Nullable String opcSseCustomerAlgorithm) {
            this.opcSseCustomerAlgorithm = opcSseCustomerAlgorithm;
            return this;
        }

        public Builder opcSseCustomerKey(@Nullable String opcSseCustomerKey) {
            this.opcSseCustomerKey = opcSseCustomerKey;
            return this;
        }

        public Builder opcSseCustomerKeySha256(@Nullable String opcSseCustomerKeySha256) {
            this.opcSseCustomerKeySha256 = opcSseCustomerKeySha256;
            return this;
        }

        public Builder opcSseKmsKeyId(@Nullable String opcSseKmsKeyId) {
            this.opcSseKmsKeyId = opcSseKmsKeyId;
            return this;
        }

        public Builder retryConfiguration(@Nullable RetryConfiguration retryConfiguration) {
            this.retryConfiguration = retryConfiguration;
            return this;
        }

        public Builder storageTier(@Nullable StorageTier storageTier) {
            this.storageTier = storageTier;
            return this;
        }

        public StorageObjectMetadata build() {
            StorageObjectMetadata som = new StorageObjectMetadata();

            som.setContentEncoding(contentEncoding);
            som.setOpcMeta(opcMeta);
            som.setContentType(contentType);
            som.setContentLanguage(contentLanguage);
            som.setCacheControl(cacheControl);
            som.setContentDisposition(contentDisposition);
            som.setContentLength(contentLength);
            som.setContentMD5(contentMD5);
            som.setExpect(expect);
            som.setIfMatch(ifMatch);
            som.setIfNoneMatch(ifNoneMatch);
            som.setOpcClientRequestId(opcClientRequestId);
            som.setOpcSseCustomerAlgorithm(opcSseCustomerAlgorithm);
            som.setOpcSseCustomerKey(opcSseCustomerKey);
            som.setOpcSseCustomerKeySha256(opcSseCustomerKeySha256);
            som.setOpcSseKmsKeyId(opcSseKmsKeyId);
            som.setRetryConfiguration(retryConfiguration);
            som.setStorageTier(storageTier);

            return som;
        }
    }
}
