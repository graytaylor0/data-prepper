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

import com.amazon.dataprepper.model.configuration.PluginModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.List;

public class AggregateProcessorConfig {

    static int DEFAULT_WINDOW_DURATION = 180;
    static String DEFAULT_DB_PATH = "data/aggregate";

    @JsonProperty("identification_keys")
    private List<String> identificationKeys;

    @JsonProperty("window_duration")
    private int windowDuration = DEFAULT_WINDOW_DURATION;

    @JsonProperty("db_path")
    private String dbPath = DEFAULT_DB_PATH;

    private PluginModel action;

    public List<String> getIdentificationKeys() {
        return identificationKeys;
    }

    public int getWindowDuration() {
        return windowDuration;
    }

    public String getDbPath() {
        return dbPath;
    }

    public PluginModel getAction() {
        return action;
    }

    void validate() {
        Preconditions.checkArgument(windowDuration > 0, "Window duration must be greater than 0");
        Preconditions.checkArgument(identificationKeys.size() > 0, "Identification keys cannot be empty");
        Preconditions.checkArgument(action != null, "An aggregate action is required.");
    }
}
