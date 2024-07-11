// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import org.springframework.core.env.EnumerablePropertySource;

public class VaultPropertySource extends EnumerablePropertySource<VaultPropertyLoader> {
    public VaultPropertySource(String name, VaultPropertyLoader source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        return source.getPropertyNames();
    }

    @Override
    public Object getProperty(String name) {
        return source.getProperty(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return source.containsProperty(name);
    }
}
