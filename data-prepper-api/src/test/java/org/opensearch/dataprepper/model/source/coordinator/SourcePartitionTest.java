/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.model.source.coordinator;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SourcePartitionTest {

    @Test
    void sourcePartitionBWithNullPartitionKeyThrowsNullPointerException() {

        assertThrows(NullPointerException.class, () -> {
            new SourcePartition<>(null, UUID.randomUUID().toString());
        });
    }

    @Test
    void sourcePartitionBuilder_returns_expected_SourcePartition() {
        final String partitionKey = UUID.randomUUID().toString();
        final String partitionState = UUID.randomUUID().toString();

        final SourcePartition<String> sourcePartition = new SourcePartition<>(partitionKey, partitionState);

        assertThat(sourcePartition, notNullValue());
        assertThat(sourcePartition.getPartitionKey(), equalTo(partitionKey));
        assertThat(sourcePartition.getPartitionState(), equalTo(partitionState));
    }
}
