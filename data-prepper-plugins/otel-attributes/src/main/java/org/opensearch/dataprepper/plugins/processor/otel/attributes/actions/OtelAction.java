package org.opensearch.dataprepper.plugins.processor.otel.attributes.actions;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.OtelAttributeActionConfig;

public interface OtelAction {
    Event performActionOnEvent(final Event event, final OtelAttributeActionConfig actionConfig);
}
