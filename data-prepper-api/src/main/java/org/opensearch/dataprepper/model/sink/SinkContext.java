/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.model.sink;

import java.util.Collection;

/**
 * Data Prepper Sink Context class. This the class for keeping global
 * sink configuration as context so that individual sinks may use them.
 */
public class SinkContext {
    private final String tagsTargetKey;
    private final Collection<String> routes;
    private final Collection<String> excludeKeys;

    public SinkContext(final String tagsTargetKey, final Collection<String> routes, final Collection<String> excludeKeys) {
        this.tagsTargetKey = tagsTargetKey;
        this.routes = routes;
        this.excludeKeys = excludeKeys;
    }
    
    /**
     * returns the target key name for tags if configured for a given sink
     * @return tags target key
     */
    public String getTagsTargetKey() {
        return tagsTargetKey;
    }

    /**
     * returns routes if configured for a given sink
     * @return routes
     */
    public Collection<String> getRoutes() {
        return routes;
    }

    /**
     * returns a List of keys to remove from the Event before sending to the sink
     */
    public Collection<String> getExcludeKeys() { return excludeKeys; }
}

