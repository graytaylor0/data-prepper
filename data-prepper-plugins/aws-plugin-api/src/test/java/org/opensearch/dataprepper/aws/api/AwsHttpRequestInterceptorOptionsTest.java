/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.aws.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.signer.Signer;
import software.amazon.awssdk.regions.Region;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AwsHttpRequestInterceptorOptionsTest {

    @Mock
    private Signer signer;

    @Mock
    private AwsCredentialsProvider awsCredentialsProvider;

    private String serviceName;
    private Region region;

    @BeforeEach
    void setup() {
        this.serviceName = UUID.randomUUID().toString();
        this.region = Region.US_EAST_1;
    }

    @Test
    void test_with_all_required_options() {
        final AwsHttpRequestInterceptorOptions options = AwsHttpRequestInterceptorOptions.builder()
                .withServiceName(serviceName)
                .withRegion(region)
                .withSigner(signer)
                .withCredentialsProvider(awsCredentialsProvider)
                .build();

        assertThat(options, notNullValue());
        assertThat(options.getServiceName(), equalTo(serviceName));
        assertThat(options.getRegion(), equalTo(region));
        assertThat(options.getSigner(), equalTo(signer));
        assertThat(options.getCredentialsProvider(), equalTo(awsCredentialsProvider));
    }

    @Test
    void null_region_throws_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            AwsHttpRequestInterceptorOptions.builder()
                    .withServiceName(serviceName)
                    .withSigner(signer)
                    .withCredentialsProvider(awsCredentialsProvider)
                    .build();
        });
    }

    @Test
    void null_serviceName_throws_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            AwsHttpRequestInterceptorOptions.builder()
                    .withServiceName(serviceName)
                    .withRegion(region.toString())
                    .withCredentialsProvider(awsCredentialsProvider)
                    .build();
        });
    }

    @Test
    void null_credentialsProvider_throws_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            AwsHttpRequestInterceptorOptions.builder()
                    .withServiceName(serviceName)
                    .withSigner(signer)
                    .withRegion(region)
                    .build();
        });
    }

    @Test
    void null_signer_throws_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            AwsHttpRequestInterceptorOptions.builder()
                    .withServiceName(serviceName)
                    .withCredentialsProvider(awsCredentialsProvider)
                    .withRegion(region)
                    .build();
        });
    }

}
