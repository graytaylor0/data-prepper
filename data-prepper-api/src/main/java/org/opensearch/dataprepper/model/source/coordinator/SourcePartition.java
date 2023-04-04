/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.model.source.coordinator;

import java.util.Objects;

/**
 * The class that will be provided to {@link org.opensearch.dataprepper.model.source.Source} plugins
 * that implement {@link UsesSourceCoordination} to identify the partition of
 * data that the source should process
 * @since 2.2
 */
public class SourcePartition<T> {

    private final String partitionKey;
    private final T partitionState;

    public SourcePartition(final String partitionKey, final T partitionState) {
        Objects.requireNonNull(partitionKey);
        this.partitionKey = partitionKey;
        this.partitionState = partitionState;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public T getPartitionState() {
        return partitionState;
    }
}
