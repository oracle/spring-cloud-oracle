// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class VaultPropertyLoader implements AutoCloseable {
    private static Timer timer;

    private final VaultTemplate vaultTemplate;
    private Map<String, String> properties = new LinkedHashMap<>();

    public VaultPropertyLoader(VaultTemplate vaultTemplate, Duration refresh) {
        this.vaultTemplate = vaultTemplate;
        reload();
        long refreshMillis = Optional.ofNullable(refresh)
                .orElse(Duration.ofMinutes(10))
                .toMillis();
        if (refreshMillis > 0) {
            synchronized (VaultPropertyLoader.class) {
                if (timer == null) {
                    timer = new Timer(true);
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            reload();
                        }
                    }, refreshMillis, refreshMillis);
                }
            }
        }
    }

    boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    Object getProperty(String key) {
        return properties.get(key);
    }

    String[] getPropertyNames() {
        return properties.keySet().toArray(String[]::new);
    }

    private void reload() {
        properties = vaultTemplate.getAllSecrets();
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
