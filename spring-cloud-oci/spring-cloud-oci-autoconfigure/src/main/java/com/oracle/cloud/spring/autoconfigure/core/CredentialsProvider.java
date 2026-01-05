/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;

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
