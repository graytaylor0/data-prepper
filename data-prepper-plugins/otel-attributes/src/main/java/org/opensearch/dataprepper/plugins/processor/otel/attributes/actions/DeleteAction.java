package org.opensearch.dataprepper.plugins.processor.otel.attributes.actions;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.OtelAttributeActionConfig;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteAction implements OtelAction {
    @Override
    public Event performActionOnEvent(final Event event, final OtelAttributeActionConfig actionConfig) {

        final String key = actionConfig.getKey();
        final Pattern deletePattern = actionConfig.getPattern();

        if (key != null) {
            event.delete(key);
        }

        if (actionConfig.getPattern() == null) {
            return event;
        }

        final Map<String, Object> attributes = event.get(actionConfig.getAttributesKey(), Map.class);
        for (final String attributeKey : attributes.keySet()) {
            final Matcher patternMatcher = deletePattern.matcher(attributeKey);
            if (patternMatcher.matches()) {
                event.delete(actionConfig.getAttributesKey() + "/" + attributeKey);
            }
        }

        return event;
    }
}
