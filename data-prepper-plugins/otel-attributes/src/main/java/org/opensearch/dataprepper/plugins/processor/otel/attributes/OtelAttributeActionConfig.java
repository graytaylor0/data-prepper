package org.opensearch.dataprepper.plugins.processor.otel.attributes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.regex.Pattern;

public class OtelAttributeActionConfig {

    @NotNull
    @JsonProperty("key")
    private String key;

    @NotNull
    @JsonProperty("action")
    private String action;

    @JsonIgnore
    private OtelAttributeAction otelAttributeAction;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("from_attribute")
    private String fromAttribute;

    @JsonProperty("from_context")
    private String fromContext;

    @JsonProperty("pattern")
    private String pattern;

    @JsonIgnore
    private Pattern regexPattern;

    @JsonProperty("attributes_key")
    private String attributesKey = "attributes";

    public String getKey() {
        return key;
    }

    public OtelAttributeAction getOtelAttributeActions() {
        return otelAttributeAction;
    }

    public Object getValue() {
        return value;
    }

    public String getFromAttribute() {
        return fromAttribute;
    }

    public Pattern getPattern() {
        return regexPattern;
    }

    public String getAttributesKey() {
        return attributesKey;
    }

    @AssertTrue(message = "Valid actions include [ 'insert', 'update', 'upsert', 'delete', 'hash' ]")
    boolean isValidAction() {
        try {
            otelAttributeAction = OtelAttributeAction.fromOptionValue(action);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    @AssertTrue(message = "When using the insert, update, or upsert actions, exactly one of value, from_attribute, or from_context is required")
    boolean isValidValueOptionsInsertUpdateUpsert() {
        if (isInsertUpdateUpsertAction()) {
            return value != null ^ fromAttribute != null;
        }

        return true;
    }

    @AssertTrue(message = "When using the hash or delete actions, key and/or pattern is required")
    boolean isValidDeleteHashOption() {
        if (OtelAttributeAction.DELETE.equals(otelAttributeAction) ||
                OtelAttributeAction.HASH.equals(otelAttributeAction)) {
            return key != null || pattern != null;
        }

        return true;
    }

    @AssertTrue(message = "pattern must be a valid regex pattern")
    boolean isPatternValid() {
        try {
            if (pattern == null) {
                return true;
            }

            regexPattern = Pattern.compile(pattern);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }



    private boolean isInsertUpdateUpsertAction() {
        return OtelAttributeAction.INSERT.equals(otelAttributeAction) ||
                OtelAttributeAction.UPDATE.equals(otelAttributeAction) ||
                OtelAttributeAction.UPSERT.equals(otelAttributeAction);
    }
}
