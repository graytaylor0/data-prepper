package partitions;

import org.opensearch.dataprepper.model.source.coordinator.SourcePartitionStoreItem;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourcePartition;

import java.util.ArrayList;
import java.util.Optional;

public class LeaderPartition extends EnhancedSourcePartition<LeaderProgressState> {

    public static final String PARTITION_TYPE = "LEADER";

    private static final String DEFAULT_PARTITION_KEY = "GLOBAL";

    private final LeaderProgressState state;

    public LeaderPartition() {
        this.state = new LeaderProgressState();
        this.state.setInitialized(false);
    }

    public LeaderPartition(SourcePartitionStoreItem sourcePartitionStoreItem) {
        setSourcePartitionStoreItem(sourcePartitionStoreItem);
        this.state = convertStringToPartitionProgressState(LeaderProgressState.class, sourcePartitionStoreItem.getPartitionProgressState());
    }

    @Override
    public String getPartitionType() {
        return PARTITION_TYPE;
    }

    @Override
    public String getPartitionKey() {
        return DEFAULT_PARTITION_KEY;
    }

    @Override
    public Optional<LeaderProgressState> getProgressState() {
        return Optional.of(state);
    }

}
