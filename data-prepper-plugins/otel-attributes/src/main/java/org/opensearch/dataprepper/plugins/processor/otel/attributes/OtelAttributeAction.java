package org.opensearch.dataprepper.plugins.processor.otel.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.actions.DeleteAction;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.actions.HashAction;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.actions.InsertUpdateUpsertAction;
import org.opensearch.dataprepper.plugins.processor.otel.attributes.actions.OtelAction;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OtelAttributeAction {
    INSERT("insert", new InsertUpdateUpsertAction()),
    UPDATE("update", new InsertUpdateUpsertAction()),
    UPSERT("upsert", new InsertUpdateUpsertAction()),
    DELETE("delete", new DeleteAction()),

    HASH("hash", new HashAction());

    private static final Map<String, OtelAttributeAction> OPTIONS_MAP = Arrays.stream(OtelAttributeAction.values())
            .collect(Collectors.toMap(
                    value -> value.actionType,
                    value -> value
            ));

    private final String actionType;
    private final OtelAction targetAction;

    OtelAttributeAction(final String actionType, final OtelAction targetAction) {
        this.actionType = actionType;
        this.targetAction = targetAction;
    }

    public OtelAction getTargetAction() {
        return targetAction;
    }

    String getActionType() {
        return actionType;
    }

    @JsonCreator
    public static OtelAttributeAction fromOptionValue(final String option) {
        return OPTIONS_MAP.get(option);
    }

    @JsonValue
    public String getOptionValue() {
        return actionType;
    }
}
