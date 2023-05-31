/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.source.opensearch;

import org.opensearch.dataprepper.model.source.coordinator.SourceCoordinator;
import org.opensearch.dataprepper.plugins.source.opensearch.worker.PitWorker;
import org.opensearch.dataprepper.plugins.source.opensearch.worker.ScrollWorker;
import org.opensearch.dataprepper.plugins.source.opensearch.worker.client.SearchAccessor;

public class OpenSearchService {

    private final SearchAccessor searchAccessor;
    private final OpenSearchSourceConfiguration openSearchSourceConfiguration;
    private final SourceCoordinator<OpenSearchIndexProgressState> sourceCoordinator;

    private Thread searchWorkerThread;

    public OpenSearchService(final SearchAccessor searchAccessor,
                             final SourceCoordinator<OpenSearchIndexProgressState> sourceCoordinator,
                             final OpenSearchSourceConfiguration openSearchSourceConfiguration) {
        this.searchAccessor = searchAccessor;
        this.openSearchSourceConfiguration = openSearchSourceConfiguration;
        this.sourceCoordinator = sourceCoordinator;
        this.sourceCoordinator.initialize();
    }

    public void start() {
        switch(searchAccessor.getSearchContextType()) {
            case POINT_IN_TIME:
                searchWorkerThread = new Thread(new PitWorker(searchAccessor, openSearchSourceConfiguration, sourceCoordinator));
                break;
            case SCROLL:
                searchWorkerThread = new Thread(new ScrollWorker(searchAccessor, openSearchSourceConfiguration, sourceCoordinator));
                break;
        }

        searchWorkerThread.start();
    }

    public void stop() {
        // todo: to implement
    }
}
