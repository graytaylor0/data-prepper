package org.opensearch.dataprepper.plugins.processor.otel.attributes.actions;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.OtelAttributeAction;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.OtelAttributeActionConfig;

public class InsertUpdateUpsertAction implements OtelAction {

    @Override
    public Event performActionOnEvent(final Event event, final OtelAttributeActionConfig actionConfig) {

            final Object value = getValue(event, actionConfig);

            if (OtelAttributeAction.INSERT.equals(actionConfig.getOtelAttributeActions())) {
                if (event.containsKey(actionConfig.getKey())) {
                    return event;
                }
            } else if (OtelAttributeAction.UPDATE.equals(actionConfig.getOtelAttributeActions())) {
                if (!event.containsKey(actionConfig.getKey())) {
                    return event;
                }
            }

            event.put(actionConfig.getKey(), value);
            return event;
    }

    private Object getValue(final Event event, final OtelAttributeActionConfig actionConfig) {
        if (actionConfig.getValue() != null) {
            return actionConfig.getValue();
        } else if (actionConfig.getFromAttribute() != null) {
            return event.get(actionConfig.getFromAttribute(), Object.class);
        } else {
            // handle from_context
        }

        return null;
    }
}
