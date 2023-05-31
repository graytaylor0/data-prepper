/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.aws.api;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.signer.Signer;
import software.amazon.awssdk.regions.Region;

import java.util.Objects;

public class AwsHttpRequestInterceptorOptions {

    private final AwsCredentialsProvider credentialsProvider;
    private final String serviceName;
    private final Signer signer;
    private final Region region;

    private AwsHttpRequestInterceptorOptions(final Builder builder) {
        this.credentialsProvider = builder.credentialsProvider;
        this.signer = builder.signer;
        this.serviceName = builder.serviceName;
        this.region = builder.region;

        Objects.requireNonNull(credentialsProvider);
        Objects.requireNonNull(region);
        Objects.requireNonNull(signer);
        Objects.requireNonNull(serviceName);
    }

    /**
     * Constructs a new {@link AwsHttpRequestInterceptorOptions.Builder} to build the credentials
     * options.
     *
     * @return A new builder.
     */
    public static AwsHttpRequestInterceptorOptions.Builder builder() {
        return new AwsHttpRequestInterceptorOptions.Builder();
    }

    public String getServiceName() {
        return serviceName;
    }

    public Region getRegion() {
        return region;
    }

    public Signer getSigner() {
        return signer;
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    /**
     * Builder class for {@link AwsHttpRequestInterceptorOptions}.
     */
    public static class Builder {

        private AwsCredentialsProvider credentialsProvider;
        private Region region;
        private String serviceName;
        private Signer signer;

        /**
         * Sets the credentials provider for the interceptor to use
         *
         * @param credentialsProvider The credentials provider that will be sent in the http request
         * @return The {@link AwsHttpRequestInterceptorOptions.Builder} for continuing to build
         */
        public AwsHttpRequestInterceptorOptions.Builder withCredentialsProvider(final AwsCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        /**
         * Sets the AWS region using the model class from the AWS SDK.
         *
         * @param region The AWS region
         * @return The {@link AwsHttpRequestInterceptorOptions.Builder} for continuing to build
         */
        public AwsHttpRequestInterceptorOptions.Builder withRegion(final Region region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the AWS region from a string.
         *
         * @param region The AWS region
         * @return The {@link AwsHttpRequestInterceptorOptions.Builder} for continuing to build
         */
        public AwsHttpRequestInterceptorOptions.Builder withRegion(final String region) {
            this.region = Region.of(region);
            return this;
        }

        /**
         * Sets the AWS service name from a string.
         *
         * @param serviceName The service name for the aws service that is being called in the http request
         * @return The {@link AwsHttpRequestInterceptorOptions.Builder} for continuing to build
         */
        public AwsHttpRequestInterceptorOptions.Builder withServiceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * @param signer The {@link Signer} to sign the http requests
         * @return The {@link AwsHttpRequestInterceptorOptions.Builder} for continuing to build
         */
        public AwsHttpRequestInterceptorOptions.Builder withSigner(final Signer signer) {
            this.signer = signer;
            return this;
        }

        /**
         * Builds the {@link AwsHttpRequestInterceptorOptions}.
         *
         * @return A new {@link AwsHttpRequestInterceptorOptions}.
         */
        public AwsHttpRequestInterceptorOptions build() {
            return new AwsHttpRequestInterceptorOptions(this);
        }
    }
}
