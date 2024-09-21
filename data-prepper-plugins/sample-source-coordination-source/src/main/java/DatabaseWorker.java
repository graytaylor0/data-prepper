import org.opensearch.dataprepper.model.buffer.Buffer;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.event.JacksonEvent;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourceCoordinator;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourcePartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import partitions.DatabaseFilePartition;
import partitions.DatabaseFilePartitionProgressState;
import partitions.LeaderPartition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DatabaseWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SampleSourceConfig.class);

    private final EnhancedSourceCoordinator sourceCoordinator;
    private final SampleSourceConfig sampleSourceConfig;

    private LeaderPartition leaderPartition;

    private final Buffer<Record<Event>> buffer;

    public DatabaseWorker(final EnhancedSourceCoordinator sourceCoordinator,
                          final SampleSourceConfig sampleSourceConfig,
                          final Buffer<Record<Event>> buffer) {
        this.sourceCoordinator = sourceCoordinator;
        this.sampleSourceConfig = sampleSourceConfig;
        this.buffer = buffer;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {

                // 1 - Check if this node is already the leader. If it is not, then try to acquire leadership in case the leader node has crashed
                if (leaderPartition == null) {
                    final Optional<EnhancedSourcePartition> sourcePartition = sourceCoordinator.acquireAvailablePartition(LeaderPartition.PARTITION_TYPE);
                    if (sourcePartition.isPresent()) {
                        LOG.info("Running as a LEADER that will discover new database files and create partitions");
                        leaderPartition = (LeaderPartition) sourcePartition.get();
                    }
                }

                // 2- If this node is the leader, run discovery of new database files and create partitions
                if (leaderPartition != null) {
                    final List<EnhancedSourcePartition<DatabaseFilePartitionProgressState>> databaseFilePartitions = discoverDatabaseFilePartitions();
                    LOG.info("Discovered {} new database file partitions", databaseFilePartitions.size());

                    databaseFilePartitions.forEach(databaseFilePartition -> {
                        sourceCoordinator.createPartition(databaseFilePartition);
                    });

                    LOG.info("Created {} database file partitions in the source coordination store", databaseFilePartitions.size());
                }

                // 3 - Grab a database file partition, process it by writing to the buffer, and mark that database file partition as completed
                final Optional<EnhancedSourcePartition> databaseFilePartition = sourceCoordinator.acquireAvailablePartition(DatabaseFilePartition.PARTITION_TYPE);

                // 4 - If it's empty that means there are no more database files to process for now. If it's not empty, the database file is processed and then marked as COMPLETED in the source coordination store
                if (databaseFilePartition.isPresent()) {
                    processDataFile(databaseFilePartition.get().getPartitionKey());
                    sourceCoordinator.completePartition(databaseFilePartition.get());
                }

            } catch (final Exception e) {
                LOG.error("Received an exception in DatabaseWorker loop, retrying");
            }
        }
    }

    private List<EnhancedSourcePartition<DatabaseFilePartitionProgressState>> discoverDatabaseFilePartitions() {
        return listNewDatabaseFiles().stream()
                .map(filePath -> new DatabaseFilePartition(filePath))
                .collect(Collectors.toList());
    }

    private List<String> listNewDatabaseFiles() {
        // TODO: This would be implemented to connect to the database and list the files, and return the file names
        return Collections.emptyList();
    }

    /**
     * This method would read the database file and create Events out of each database item, and write them to the buffer
     * @param filePath
     */
    private void processDataFile(final String filePath) {
        final List<Map<String, Object>> databaseItems = getDatabaseFileItems(filePath);

        for (final Map<String, Object> databaseItemMap : databaseItems) {

            // Event is the base model for Data Prepper items that all processors and sinks use
            final Record<Event> databaseItemEvent = new Record<>(JacksonEvent.builder()
                    .withData(databaseItemMap)
                    .build());

            try {
                buffer.write(databaseItemEvent, 5000);
            } catch (final TimeoutException e) {
                LOG.error("Timed out writing database item {} to the buffer", databaseItemMap);
            }
        }
    }

    private List<Map<String, Object>> getDatabaseFileItems(final String filePath) {
        return Collections.emptyList();
    }
}
