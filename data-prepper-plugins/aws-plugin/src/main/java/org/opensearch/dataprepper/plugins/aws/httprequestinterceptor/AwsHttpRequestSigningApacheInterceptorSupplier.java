/*
 * Copyright OpenSearch Contributors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.opensearch.dataprepper.plugins.aws.httprequestinterceptor;

import org.apache.http.HttpRequestInterceptor;
import org.opensearch.dataprepper.aws.api.AwsHttpRequestInterceptorOptions;
import org.opensearch.dataprepper.aws.api.AwsHttpRequestInterceptorSupplier;

public class AwsHttpRequestSigningApacheInterceptorSupplier implements AwsHttpRequestInterceptorSupplier {

    private final AwsHttpRequestInterceptorFactory awsHttpRequestInterceptorFactory;

    public AwsHttpRequestSigningApacheInterceptorSupplier(final AwsHttpRequestInterceptorFactory awsHttpRequestInterceptorFactory) {
        this.awsHttpRequestInterceptorFactory = awsHttpRequestInterceptorFactory;
    }

    @Override
    public HttpRequestInterceptor getAwsRequestInterceptor(final AwsHttpRequestInterceptorOptions awsHttpRequestInterceptorOptions) {
        return awsHttpRequestInterceptorFactory.createInterceptorFromOptions(awsHttpRequestInterceptorOptions);
    }
}
