/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.source.dynamodb.converter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExportRecordDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        final ObjectCodec objectCodec = p.getCodec();
        final JsonNode jsonNode = objectCodec.readTree(p);

        return convertToMap(jsonNode);
    }

    private Map<String, Object> convertToMap(final JsonNode rootNode) {
        final Map<String, Object> result = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> field = it.next();

            final JsonNode value = field.getValue();
            if (value.isBigDecimal()) {
                result.put(field.getKey(), new BigDecimal(value.decimalValue().toPlainString()));
            } else {
                result.put(field.getKey(), field.getValue());
            }
        }

        return result;
    }
}
