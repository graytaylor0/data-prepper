/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.sourcecoordination;

import org.opensearch.dataprepper.model.source.coordinator.PartitionIdentifier;
import org.opensearch.dataprepper.model.source.coordinator.SourceCoordinator;
import org.opensearch.dataprepper.model.source.coordinator.SourcePartition;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class DefaultSourceCoordinator<T> implements SourceCoordinator<T> {
    private SourceCoordinatorStore sourceCoordinatorStore;

    private Class<T> clazz;

    public DefaultSourceCoordinator(final Class<T> clazz, final SourceCoorinatorStore store) {

    }


    @Override
    public void createPartitions(List<PartitionIdentifier> partitionIdentifiers) {
        for (final PartitionIdentifier partitionIdentifier : partitionIdentifiers) {
            final String partitionKey = partitionIdentifier.getPartitionKey();
            // Probably don't need this lookup, can lower to only 1 RCU throughput and creation should just fail if it exists


            final Optional<SourcePartitionItem> optionalPartitionItem = sourceCoordinatorStore.getSourcePartitionItem(partitionKey);
            if (optionalPartitionItem.isEmpty()) {
                final DynamoDbSourcePartitionItem newPartitionItem = sourceCoordinatorStore.initializeItemForPartition(partitionKey);
                dynamoDbClientWrapper.tryCreatePartitionItem(newPartitionItem);
            }
        }
    }

    @Override
    public Optional<SourcePartition<T>> getNextPartition() {
        return Optional.empty();
    }

    @Override
    public void completePartition(String partitionKey) {

    }

    @Override
    public void closePartition(String partitionKey, Duration reopenAfter) {

    }

    @Override
    public <S extends T> void saveProgressStateForPartition(String partitionKey, S partitionProgressState) {

    }

    @Override
    public void giveUpPartitions() {

    }

    @Override
    public Class<T> getProgressStateType() {
        return SourceCoordinator.super.getProgressStateType();
    }

    @Override
    public void setClass(Class<T> clazz) {

    }

}
