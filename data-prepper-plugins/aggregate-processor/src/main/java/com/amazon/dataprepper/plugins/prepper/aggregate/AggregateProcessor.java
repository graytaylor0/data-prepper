/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package com.amazon.dataprepper.plugins.prepper.aggregate;

import com.amazon.dataprepper.metrics.PluginMetrics;
import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.annotations.DataPrepperPluginConstructor;
import com.amazon.dataprepper.model.annotations.SingleThread;
import com.amazon.dataprepper.model.configuration.PipelineDescription;
import com.amazon.dataprepper.model.configuration.PluginModel;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.event.Event;
import com.amazon.dataprepper.model.plugin.PluginFactory;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.model.processor.AbstractProcessor;
import com.amazon.dataprepper.model.record.RecordMetadata;
import com.amazon.dataprepper.plugins.prepper.state.MapDbPrepperState;
import com.amazon.dataprepper.model.processor.Processor;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

@SingleThread
@DataPrepperPlugin(name = "aggregate", pluginType = Processor.class, pluginConfigurationType = AggregateProcessorConfig.class)
public class AggregateProcessor extends AbstractProcessor<Record<Event>, Record<Event>> {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateProcessor.class);
    private static final AtomicInteger processorsCreated = new AtomicInteger(0);

    private final AggregateProcessorConfig aggregateProcessorConfig;
    private final AggregateAction aggregateAction;
    private static volatile MapDbPrepperState<Event> currentWindow;
    private static final Map<Object, Object> groupState = Maps.newConcurrentMap();
    private static Clock clock;
    private static long lastConcludedGroupTimestamp;
    private final int processorId;
    private final int numProcessWorkers;
    private static CyclicBarrier allThreadsCyclicBarrier;

    @DataPrepperPluginConstructor
    public AggregateProcessor(final AggregateProcessorConfig aggregateProcessorConfig, final PluginMetrics pluginMetrics, final PluginFactory pluginFactory, final PipelineDescription pipelineDescription) {
        super(pluginMetrics);
        aggregateProcessorConfig.validate();
        this.aggregateProcessorConfig = aggregateProcessorConfig;
        this.processorId = processorsCreated.getAndIncrement();
        this.numProcessWorkers = pipelineDescription.getNumProcessWorkers();
        aggregateAction = getAggregateAction(pluginFactory);
        clock = Clock.systemUTC();


        if (isMasterInstance()) {
            lastConcludedGroupTimestamp = clock.millis();
            allThreadsCyclicBarrier = new CyclicBarrier(numProcessWorkers);
        }
    }

    private AggregateAction getAggregateAction(final PluginFactory pluginFactory) {
        final PluginModel actionConfiguration = aggregateProcessorConfig.getAction();
        final PluginSetting actionPluginSetting = new PluginSetting(actionConfiguration.getPluginName(), actionConfiguration.getPluginSettings());
        return pluginFactory.loadPlugin(AggregateAction.class, actionPluginSetting);
    }

    @Override
    public Collection<Record<Event>> doExecute(Collection<Record<Event>> records) {
        List<Record<Event>> recordsOut = new LinkedList<>();
        if (windowDurationHasPassed()) {
            try {
                allThreadsCyclicBarrier.await();
                if (isMasterInstance()) {
                    concludeGroupAndResetWindow(recordsOut);
                }
                allThreadsCyclicBarrier.await();
            } catch(InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        for (final Record<Event> record : records) {
            final Event event = record.getData();
            final Map<String, Object> identificationKeysHash = getEventKeyHashFromIdentificationKeys(event);
            final Optional<Event> handledEvent = aggregateAction.handleEvent(event, groupState, identificationKeysHash);
            handledEvent.ifPresent(handledEventValue -> recordsOut.add(new Record<>(handledEventValue, record.getMetadata())));
        }
        return recordsOut;
    }

    private void concludeGroupAndResetWindow(List<Record<Event>> recordsOut) {
        LOG.info("Concluding group state for current window at " + clock.instant().toString());
        final Optional<Event> concludedEvent = aggregateAction.concludeGroup(groupState);
        groupState.clear();
        concludedEvent.ifPresent(event -> recordsOut.add(new Record<>(event, RecordMetadata.defaultMetadata())));
        lastConcludedGroupTimestamp = clock.millis();
    }

    private Map<String, Object> getEventKeyHashFromIdentificationKeys(final Event event) {
        final Map<String, Object> identificationKeysHash = new HashMap<>();
        for (final String identificationKey : aggregateProcessorConfig.getIdentificationKeys()) {
            if (event.containsKey(identificationKey)) {
                identificationKeysHash.put(identificationKey, event.get(identificationKey, Object.class));
            }
        }
        return identificationKeysHash;
    }

    @Override
    public void prepareForShutdown() {

    }

    @Override
    public boolean isReadyForShutdown() {
        return false;
    }

    @Override
    public void shutdown() {

    }


    /**
     * @return Boolean indicating whether the window duration has lapsed
     */
    private boolean windowDurationHasPassed() {
        if ((clock.millis() - lastConcludedGroupTimestamp) >= aggregateProcessorConfig.getWindowDuration() * 1000L) {
            return true;
        }
        return false;
    }

    /**
     * Master instance is needed to do things like window rotation that should only be done once
     *
     * @return Boolean indicating whether this object is the master ServiceMapStatefulPrepper instance
     */
    private boolean isMasterInstance() {
        return processorId == 0;
    }
}
