/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.source;

import org.opensearch.dataprepper.model.source.coordinator.PartitionIdentifier;
import org.opensearch.dataprepper.model.source.coordinator.SourceCoordinator;
import org.opensearch.dataprepper.model.source.coordinator.SourcePartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class S3Service {
    private static final Logger LOG = LoggerFactory.getLogger(S3Service.class);

    private final S3ObjectHandler s3ObjectHandler;

    private static final String bucketName = "source-coordination-test-bucket";

    private final SourceCoordinator<?> sourceCoordinator;
    private final S3Client s3Client;
    private final ScheduledExecutorService scheduledExecutorService;

    S3Service(final S3ObjectHandler s3ObjectHandler, final SourceCoordinator<?> sourceCoordinator, final S3Client s3Client) {
        this.s3ObjectHandler = s3ObjectHandler;
        this.sourceCoordinator = sourceCoordinator;
        this.s3Client = s3Client;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::pullObjects, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::processPartition, 30, 30, TimeUnit.SECONDS);
    }

    void addS3Object(final S3ObjectReference s3ObjectReference) {
        try {
            s3ObjectHandler.parseS3Object(s3ObjectReference);
        } catch (final IOException e) {
            LOG.error("Unable to read S3 object from S3ObjectReference = {}", s3ObjectReference, e);
        }
    }

    private void pullObjects() {
        final List<PartitionIdentifier> objectKeys = getS3Objects();
        sourceCoordinator.createPartitions(objectKeys);
    }

    private void processPartition() {
        final Optional<? extends SourcePartition<?>> activePartition = sourceCoordinator.getNextPartition();
        activePartition.ifPresent(sourcePartition -> {
            addS3Object(S3ObjectReference.bucketAndKey(bucketName, sourcePartition.getPartitionKey()).build());
            sourceCoordinator.completePartition(sourcePartition.getPartitionKey());
        });
    }

    private List<PartitionIdentifier> getS3Objects() {
        final ListObjectsResponse response = s3Client.listObjects(ListObjectsRequest.builder()
                .bucket(bucketName)
                .build());

        return response.contents().stream().map(S3Object::key).map(key -> PartitionIdentifier.builder().withPartitionKey(key).build()).collect(Collectors.toList());
    }
}
