/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.model.source.coordinator.exceptions;

import org.opensearch.dataprepper.model.source.coordinator.SourceCoordinator;

/**
 * This exception is thrown by a {@link SourceCoordinator} when the source makes a call to perform an action on a
 * partition, but that partition could not be found in the distributed store.
 */
public class PartitionNotFoundException extends RuntimeException {
    public PartitionNotFoundException(final String message) {
        super(message);
    }
}
