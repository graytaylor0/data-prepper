/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.processor.otel.attributes;

import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.processor.AbstractProcessor;
import org.opensearch.dataprepper.model.processor.Processor;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.actions.OtelAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.opensearch.dataprepper.logging.DataPrepperMarkers.NOISY;

@DataPrepperPlugin(name = "otel_attributes", pluginType = Processor.class, pluginConfigurationType = OtelAttributesProcessorConfig.class)
public class OtelAttributesProcessor extends AbstractProcessor<Record<Event>, Record<Event>> {

    private static final Logger LOG = LoggerFactory.getLogger(OtelAttributesProcessor.class);

    private final OtelAttributesProcessorConfig otelAttributesProcessorConfig;

    private final PluginMetrics pluginMetrics;

    @DataPrepperPluginConstructor
    public OtelAttributesProcessor(final OtelAttributesProcessorConfig otelAttributesProcessorConfig,
                                   final PluginMetrics pluginMetrics) {
        super(pluginMetrics);
        this.pluginMetrics = pluginMetrics;
        this.otelAttributesProcessorConfig = otelAttributesProcessorConfig;
    }
    @Override
    public Collection<Record<Event>> doExecute(final Collection<Record<Event>> records) {
        for (final Record<Event> record : records) {

            try {
                final Event event = record.getData();

                for (final OtelAttributeActionConfig actionConfig : otelAttributesProcessorConfig.getActions()) {

                    try {
                        final OtelAction otelAction = actionConfig.getOtelAttributeActions().getTargetAction();
                        otelAction.performActionOnEvent(event, actionConfig);
                    } catch (final Exception e) {
                        LOG.error(NOISY, "An error occurred while processing an Event with the {} action with action configuration {}",
                                actionConfig.getOtelAttributeActions().getActionType(), actionConfig, e);
                    }
                }
            } catch (final Exception e) {
                LOG.error(NOISY, "An error occurred while processing an Event with the otel_attributes processor", e);
            }
        }

        return records;
    }

    @Override
    public void prepareForShutdown() {

    }

    @Override
    public boolean isReadyForShutdown() {
        return true;
    }

    @Override
    public void shutdown() {

    }
}
