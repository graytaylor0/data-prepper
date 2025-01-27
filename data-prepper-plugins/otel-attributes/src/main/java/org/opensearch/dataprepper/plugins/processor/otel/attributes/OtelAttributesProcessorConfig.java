package org.opensearch.dataprepper.plugins.processor.otel.attributes;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OtelAttributesProcessorConfig {

    @JsonProperty("actions")
    @Valid
    @NotNull
    @NotEmpty
    private List<OtelAttributeActionConfig> actions;

    public List<OtelAttributeActionConfig> getActions() {
        return actions;
    }
}
