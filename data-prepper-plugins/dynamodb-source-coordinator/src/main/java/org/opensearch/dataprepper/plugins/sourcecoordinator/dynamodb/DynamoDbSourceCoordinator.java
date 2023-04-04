/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.sourcecoordinator.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.source.coordinator.PartitionIdentifier;
import org.opensearch.dataprepper.model.source.coordinator.SourceCoordinator;
import org.opensearch.dataprepper.model.source.coordinator.SourcePartition;
import org.opensearch.dataprepper.plugins.sourcecoordinator.dynamodb.common.PartitionManager;
import org.opensearch.dataprepper.plugins.sourcecoordinator.dynamodb.common.SourcePartitionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link SourceCoordinator} when DynamoDB is used at the distributed store
 * for source_coordination
 * @since 2.2
 */

@DataPrepperPlugin(name = "dynamodb-source-coordinator", pluginType = SourceCoordinator.class, pluginConfigurationType = DynamoStoreSettings.class )
public class DynamoDbSourceCoordinator<T> implements SourceCoordinator<T>, SourceCoordinationStore {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbSourceCoordinator.class);
    private static final Duration DEFAULT_LEASE_TIMEOUT = Duration.ofMinutes(8);
    private static final int MAX_CLOSED_COUNT = 5;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ownerId;

    static {
        try {
            ownerId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private final DynamoStoreSettings dynamoStoreSettings;
    private final PluginMetrics pluginMetrics;
    private final PartitionManager<T> partitionManager;
    private final DynamoDbClientWrapper dynamoDbClientWrapper;

    private Class<T> partitionProgressStateClass;

    @DataPrepperPluginConstructor
    public DynamoDbSourceCoordinator(final DynamoStoreSettings dynamoStoreSettings, final PluginMetrics pluginMetrics, final Class<T> clazz) {
        this.dynamoStoreSettings = dynamoStoreSettings;
        this.pluginMetrics = pluginMetrics;
        this.partitionManager = new PartitionManager<>();
        this.dynamoDbClientWrapper = new DynamoDbClientWrapper(dynamoStoreSettings.getRegion(), dynamoStoreSettings.getStsRoleArn());

       this.partitionProgressStateClass = this.getProgressStateType();
    }

    @Override
    public void createPartitions(final List<PartitionIdentifier> partitionIdentifiers) {
        for (final PartitionIdentifier partitionIdentifier : partitionIdentifiers) {
            final String partitionKey = partitionIdentifier.getPartitionKey();
            // Probably don't need this lookup, can lower to only 1 RCU throughput and creation should just fail if it exists
            final Optional<DynamoDbSourcePartitionItem> optionalPartitionItem = dynamoDbClientWrapper.getSourcePartitionItem(partitionKey);
            if (optionalPartitionItem.isEmpty()) {
                final DynamoDbSourcePartitionItem newPartitionItem = initializeItemForPartition(partitionKey);
                dynamoDbClientWrapper.tryCreatePartitionItem(newPartitionItem);
            }
        }
    }

    @Override
    public Optional<SourcePartition<T>> getNextPartition() {
        if (partitionManager.getActivePartition().isPresent()) {
            final boolean renewedPartitionTimeout = renewPartitionOwnershipTimeout(partitionManager.getActivePartition().get().getPartitionKey());
            // Do anything if we aren't able to renew the partition ownership? Should we even do that here
            if (!renewedPartitionTimeout) {
                LOG.error("Unable to renew partition ownership for owner {} on source partition key {}",
                        ownerId, partitionManager.getActivePartition().get().getPartitionKey());
            }

            return partitionManager.getActivePartition();
        }

        final PageIterable<DynamoDbSourcePartitionItem> dynamoDbSourcePartitionItemPageIterable =
                dynamoDbClientWrapper.getSourcePartitionItems(getFilterExpressionForUnownedPartitions());

        for (final DynamoDbSourcePartitionItem item : dynamoDbSourcePartitionItemPageIterable.items()) {
            final boolean acquired = tryAcquireItem(item);

            if (acquired) {
                final SourcePartition<T> sourcePartition = new SourcePartition<>(item.getSourcePartitionKey(), convertPartitionProgressStateFromString(item.getPartitionProgressState()));
                partitionManager.setActivePartition(sourcePartition);
                return Optional.of(sourcePartition);
            }
        }

        return Optional.empty();
    }

    @Override
    public void completePartition(final String partitionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closePartition(final String partitionKey, final Duration reopenAfter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> void saveProgressStateForPartition(final String partitionKey, final S partitionState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void giveUpPartitions() {
        throw new UnsupportedOperationException();
    }

    private ProvisionedThroughput constructProvisionedThroughput(final Long readCapacityUnits,
                                                                 final Long writeCapacityUnits) {
        return ProvisionedThroughput.builder()
                .readCapacityUnits(readCapacityUnits)
                .writeCapacityUnits(writeCapacityUnits)
                .build();
    }

    private boolean tryAcquireItem(final DynamoDbSourcePartitionItem item) {
        item.setPartitionOwner(ownerId);
        item.setSourcePartitionStatus(SourcePartitionStatus.ASSIGNED);
        item.setPartitionOwnershipTimeout(Instant.now().plus(DEFAULT_LEASE_TIMEOUT));

        return dynamoDbClientWrapper.updatePartitionItem(item);
    }

    private DynamoDbSourcePartitionItem initializeItemForPartition(final String partitionKey) {
        final DynamoDbSourcePartitionItem item = new DynamoDbSourcePartitionItem();
        item.setSourcePartitionKey(partitionKey);
        item.setSourcePartitionStatus(SourcePartitionStatus.UNASSIGNED);
        item.setClosedCount(0L);
        item.setPartitionProgressState(null);
        item.setVersion(0L);

        return item;
    }

    private boolean isActivelyOwnedPartition(final String sourcePartitionKey) {
        final Optional<SourcePartition<T>> activePartition = partitionManager.getActivePartition();
        return activePartition.isPresent() && activePartition.get().getPartitionKey().equals(sourcePartitionKey);
    }

    private boolean renewPartitionOwnershipTimeout(final String sourcePartitionKey) {
        final Optional<DynamoDbSourcePartitionItem> item = dynamoDbClientWrapper.getSourcePartitionItem(sourcePartitionKey);

        if (item.isPresent()) {
            final DynamoDbSourcePartitionItem updateItem = item.get();
            updateItem.setPartitionOwnershipTimeout(Instant.now().plus(DEFAULT_LEASE_TIMEOUT));
            return dynamoDbClientWrapper.updatePartitionItem(updateItem);
        }

        return false;
    }

    private Expression getFilterExpressionForUnownedPartitions() {
        return Expression.builder()
                .expressionValues(Map.of(
                        ":s", AttributeValue.builder().s(SourcePartitionStatus.UNASSIGNED.name()).build(),
                        ":t", AttributeValue.builder().s(Instant.now().toString()).build(),
                        ":ro", AttributeValue.builder().s(Instant.now().toString()).build(),
                        ":null", AttributeValue.builder().nul(true).build()))
                .expression("contains(sourcePartitionStatus, :s) " +
                        "and (attribute_not_exists(reOpenAt) or reOpenAt = :null or reOpenAt <= :ro) " +
                        "and (attribute_not_exists(partitionOwnershipTimeout) or partitionOwnershipTimeout = :null or partitionOwnershipTimeout <= :t)")
                .build();
    }

    private T convertPartitionProgressStateFromString(final String partitionProgressState) {
        return objectMapper.convertValue(partitionProgressState, partitionProgressStateClass);
    }
}
