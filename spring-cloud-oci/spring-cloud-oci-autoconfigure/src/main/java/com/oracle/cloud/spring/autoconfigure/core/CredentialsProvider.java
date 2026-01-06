/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;

import java.io.IOException;

/**
 * Provider to wrap AuthenticationDetailsProvider for beans initialization
 */
public class CredentialsProvider {
    private BasicAuthenticationDetailsProvider authenticationDetailsProvider;

    public CredentialsProvider(BasicAuthenticationDetailsProvider authenticationDetailsProvider) throws IOException {
        this.authenticationDetailsProvider = authenticationDetailsProvider;
    }

    public BasicAuthenticationDetailsProvider getAuthenticationDetailsProvider() {
        return authenticationDetailsProvider;
    }
}
