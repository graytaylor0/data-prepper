import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.buffer.Buffer;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.model.source.coordinator.SourcePartitionStoreItem;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourceCoordinator;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.EnhancedSourcePartition;
import org.opensearch.dataprepper.model.source.coordinator.enhanced.UsesEnhancedSourceCoordination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import partitions.LeaderPartition;

import javax.xml.transform.Source;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@DataPrepperPlugin(name = "sample_source", pluginType = Source.class, pluginConfigurationType = SampleSource.class)
public class SampleSource implements org.opensearch.dataprepper.model.source.Source<Record<Event>>, UsesEnhancedSourceCoordination {

    private static final Logger LOG = LoggerFactory.getLogger(SampleSourceConfig.class);

    private final ExecutorService executor;
    private EnhancedSourceCoordinator sourceCoordinator;

    private SampleSourceConfig sampleSourceConfig;

    @DataPrepperPluginConstructor
    public SampleSource(final PluginMetrics pluginMetrics,
                          final SampleSourceConfig sampleSourceConfig) {
        LOG.info("Created Sample Source");

        this.sampleSourceConfig = sampleSourceConfig;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(final Buffer<Record<Event>> buffer) {
        sourceCoordinator.createPartition(new LeaderPartition());

        final Runnable databaseWorker = new DatabaseWorker(sourceCoordinator, sampleSourceConfig, buffer);

        // Start the database worker asynchronously
        executor.submit(databaseWorker);
    }

    @Override
    public void stop() {
        // force shutdown the database worker thread
        executor.shutdownNow();
    }

    @Override
    public void setEnhancedSourceCoordinator(final EnhancedSourceCoordinator sourceCoordinator) {
        this.sourceCoordinator = sourceCoordinator;
        this.sourceCoordinator.initialize();
    }

    @Override
    public Function<SourcePartitionStoreItem, EnhancedSourcePartition> getPartitionFactory() {
        return new PartitionFactory();
    }
}
