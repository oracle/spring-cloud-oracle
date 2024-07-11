// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;

public class VaultPropertyLoader implements AutoCloseable {
    private static Timer timer;

    private final Secrets secrets;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public VaultPropertyLoader(Secrets secrets, List<String> secretIds, Duration refresh) {
        this.secrets = secrets;

        for (String secretId : secretIds) {
            properties.put(secretId, "");
        }

        synchronized (VaultPropertyLoader.class) {
            if (timer == null) {
                timer = new Timer(true);
                long refreshMillis = refresh.toMillis();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        reload();
                    }
                }, refreshMillis, refreshMillis);
            }
        }
    }

    boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    String getProeprty(String key) {
        return properties.get(key).toString();
    }

    String[] getPropertyNames() {
        return properties.keySet().toArray(String[]::new);
    }

    private void reload() {
        Set<String> secretIds = properties.keySet();
        for (String secretId : secretIds) {
            Object value = getSecretValue(secretId);
            properties.put(secretId, value);
        }
    }

    private Object getSecretValue(String secretId) {
        GetSecretBundleRequest request = GetSecretBundleRequest.builder()
                .secretId(secretId)
                .build();
        GetSecretBundleResponse response = secrets.getSecretBundle(request);
        SecretBundleContentDetails content = response.getSecretBundle().getSecretBundleContent();
        if (content instanceof Base64SecretBundleContentDetails) {
            Base64SecretBundleContentDetails encoded = (Base64SecretBundleContentDetails) content;
            return new String(Base64.getDecoder().decode(encoded.getContent()), StandardCharsets.UTF_8);
        }
        return content.toString();
    }

    @Override
    public void close() throws Exception {
        synchronized (VaultPropertyLoader.class) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }
}
