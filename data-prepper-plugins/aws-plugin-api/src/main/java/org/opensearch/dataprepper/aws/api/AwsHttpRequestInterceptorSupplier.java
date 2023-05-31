/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.aws.api;

import org.apache.http.HttpRequestInterceptor;

public interface AwsHttpRequestInterceptorSupplier {
    HttpRequestInterceptor getAwsRequestInterceptor(final AwsHttpRequestInterceptorOptions awsHttpRequestInterceptorOptions);
}
