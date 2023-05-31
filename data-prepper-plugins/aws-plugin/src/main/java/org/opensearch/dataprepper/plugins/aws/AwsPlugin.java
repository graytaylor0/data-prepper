/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.aws;

import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.plugin.ExtensionPlugin;
import org.opensearch.dataprepper.model.plugin.ExtensionPoints;
import org.opensearch.dataprepper.plugins.aws.httprequestinterceptor.AwsHttpRequestInterceptorExtensionProvider;
import org.opensearch.dataprepper.plugins.aws.httprequestinterceptor.AwsHttpRequestInterceptorFactory;
import org.opensearch.dataprepper.plugins.aws.httprequestinterceptor.AwsHttpRequestSigningApacheInterceptorSupplier;

/**
 * The {@link ExtensionPlugin} class which adds the AWS Plugin to
 * Data Prepper as an extension plugin. Everything starts from here.
 */
public class AwsPlugin implements ExtensionPlugin {
    private final DefaultAwsCredentialsSupplier defaultAwsCredentialsSupplier;
    private final AwsHttpRequestSigningApacheInterceptorSupplier awsHttpRequestSigningApacheInterceptorSupplier;

    @DataPrepperPluginConstructor
    public AwsPlugin() {
        final CredentialsProviderFactory credentialsProviderFactory = new CredentialsProviderFactory();
        final CredentialsCache credentialsCache = new CredentialsCache();
        defaultAwsCredentialsSupplier = new DefaultAwsCredentialsSupplier(credentialsProviderFactory, credentialsCache);

        final AwsHttpRequestInterceptorFactory awsHttpRequestInterceptorFactory = new AwsHttpRequestInterceptorFactory();
        awsHttpRequestSigningApacheInterceptorSupplier = new AwsHttpRequestSigningApacheInterceptorSupplier(awsHttpRequestInterceptorFactory);
    }

    @Override
    public void apply(final ExtensionPoints extensionPoints) {
        extensionPoints.addExtensionProvider(new AwsExtensionProvider(defaultAwsCredentialsSupplier));
        extensionPoints.addExtensionProvider(new AwsHttpRequestInterceptorExtensionProvider(awsHttpRequestSigningApacheInterceptorSupplier));
    }
}
