/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.aws.httprequestinterceptor;

import org.opensearch.dataprepper.aws.api.AwsHttpRequestInterceptorSupplier;
import org.opensearch.dataprepper.model.plugin.ExtensionProvider;

import java.util.Optional;

public class AwsHttpRequestInterceptorExtensionProvider implements ExtensionProvider<AwsHttpRequestInterceptorSupplier> {

    private final AwsHttpRequestInterceptorSupplier awsHttpRequestInterceptorSupplier;

    public AwsHttpRequestInterceptorExtensionProvider(final AwsHttpRequestInterceptorSupplier awsHttpRequestInterceptorSupplier) {
        this.awsHttpRequestInterceptorSupplier = awsHttpRequestInterceptorSupplier;
    }

    @Override
    public Optional<AwsHttpRequestInterceptorSupplier> provideInstance(final Context context) {
        return Optional.of(awsHttpRequestInterceptorSupplier);
    }

    @Override
    public Class<AwsHttpRequestInterceptorSupplier> supportedClass() {
        return AwsHttpRequestInterceptorSupplier.class;
    }
}
