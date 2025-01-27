package org.opensearch.dataprepper.plugins.processor.otel.attributes.actions;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.plugin.InvalidPluginConfigurationException;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.OtelAttributeActionConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashAction implements OtelAction {

    private MessageDigest messageDigest;

    public HashAction() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new InvalidPluginConfigurationException("SHA-1 algorithm not found");
        }
    }

    @Override
    public Event performActionOnEvent(final Event event, final OtelAttributeActionConfig actionConfig) {
        final String key = actionConfig.getKey();
        final Pattern hashPattern = actionConfig.getPattern();

        if (key != null) {
            final Object value = event.get(key, Object.class);
            final String hashedValue = getHashedValue(value.toString());
            event.put(key, hashedValue);
        }

        if (actionConfig.getPattern() == null) {
            return event;
        }

        final Map<String, Object> attributes = event.get(actionConfig.getAttributesKey(), Map.class);
        for (final String attributeKey : attributes.keySet()) {
            final Matcher patternMatcher = hashPattern.matcher(attributeKey);
            if (patternMatcher.matches()) {
                final Object value = event.get(actionConfig.getAttributesKey() + "/" + key, Object.class);
                final String hashedValue = getHashedValue(value.toString());
                event.put(key, hashedValue);
            }
        }

        return event;
    }

    private String getHashedValue(final String value) {
        messageDigest.update(value.getBytes(StandardCharsets.UTF_8));

        byte[] hashBytes = messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
