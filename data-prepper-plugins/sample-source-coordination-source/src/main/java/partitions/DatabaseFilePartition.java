package partitions;

import org.opensearch.dataprepper.model.source.coordinator.SourcePartitionStoreItem;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourcePartition;

import java.util.Optional;

public class DatabaseFilePartition extends EnhancedSourcePartition<DatabaseFilePartitionProgressState> {

    public static final String PARTITION_TYPE = "DATABASE_FILE";

    private final DatabaseFilePartitionProgressState state;

    private final String filePath;



    public DatabaseFilePartition(final String filePath) {
        this.state = new DatabaseFilePartitionProgressState();
        this.filePath = filePath;
    }

    public DatabaseFilePartition(final SourcePartitionStoreItem sourcePartitionStoreItem) {
        setSourcePartitionStoreItem(sourcePartitionStoreItem);

        // The source partition key is file path, this uniquely identifies the partition
        this.filePath = sourcePartitionStoreItem.getSourcePartitionKey();
        this.state = convertStringToPartitionProgressState(DatabaseFilePartitionProgressState.class, sourcePartitionStoreItem.getPartitionProgressState());
    }

    @Override
    public String getPartitionType() {
        return PARTITION_TYPE;
    }

    /**
     * File path is used the partition key
     * @return the file path as the partition key
     */
    @Override
    public String getPartitionKey() {
        return filePath;
    }

    @Override
    public Optional<DatabaseFilePartitionProgressState> getProgressState() {
        return Optional.of(state);
    }

    public String getFilePath() { return filePath; }

}
