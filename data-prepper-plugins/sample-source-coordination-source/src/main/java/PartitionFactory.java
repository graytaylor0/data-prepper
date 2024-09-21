import org.opensearch.dataprepper.model.source.coordinator.SourcePartitionStoreItem;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourcePartition;
import partitions.DatabaseFilePartition;
import partitions.LeaderPartition;

import java.util.function.Function;

public class PartitionFactory implements Function<SourcePartitionStoreItem, EnhancedSourcePartition> {


    @Override
    public EnhancedSourcePartition apply(final SourcePartitionStoreItem partitionStoreItem) {
        String sourceIdentifier = partitionStoreItem.getSourceIdentifier();

        // Partition source identifiers contain the format of "pipelineName|partitionType"
        String partitionType = sourceIdentifier.substring(sourceIdentifier.lastIndexOf('|') + 1);

        if (LeaderPartition.PARTITION_TYPE.equals(partitionType)) {
            return new LeaderPartition(partitionStoreItem);
        } else if (DatabaseFilePartition.PARTITION_TYPE.equals(partitionType)) {
            return new DatabaseFilePartition(partitionStoreItem);
        } else {
            throw new IllegalArgumentException(
                    String.format("Partition type %s is not valid", partitionType));
        }
    }


}
