/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.aws.httprequestinterceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.dataprepper.aws.api.AwsHttpRequestInterceptorSupplier;
import org.opensearch.dataprepper.model.plugin.ExtensionProvider;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(MockitoExtension.class)
public class AwsHttpRequestInterceptorExtensionProviderTest {

    @Mock
    private AwsHttpRequestInterceptorSupplier awsHttpRequestSigningApacheInterceptorSupplier;

    @Mock
    private ExtensionProvider.Context context;

    private AwsHttpRequestInterceptorExtensionProvider createObjectUnderTest() {
        return new AwsHttpRequestInterceptorExtensionProvider(awsHttpRequestSigningApacheInterceptorSupplier);
    }

    @Test
    void supportedClass_returns_awsHttpRequestSigningApacheInterceptorSupplier() {
        assertThat(createObjectUnderTest().supportedClass(), equalTo(AwsHttpRequestInterceptorSupplier.class));
    }

    @Test
    void provideInstance_returns_the_AwsHttpRequestSigningApacheInterceptorSupplier_from_the_constructor() {
        final AwsHttpRequestInterceptorExtensionProvider objectUnderTest = createObjectUnderTest();

        final Optional<AwsHttpRequestInterceptorSupplier> optionalInterceptorSupplier = objectUnderTest.provideInstance(context);

        assertThat(optionalInterceptorSupplier, notNullValue());
        assertThat(optionalInterceptorSupplier.isPresent(), equalTo(true));
        assertThat(optionalInterceptorSupplier.get(), equalTo(awsHttpRequestSigningApacheInterceptorSupplier));
    }

}
