/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.test.performance.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Session;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class Templates {
    public static final String APACHE_COMMON_LOG_DATETIME_PATTERN = "d/LLL/uuuu:HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(APACHE_COMMON_LOG_DATETIME_PATTERN);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String now() {
        return formatter.format(LocalDateTime.now()) + " -0700";
    }
    
    public static Function<Session, String> apacheCommonLogTemplate(final int batchSize) {
        return session -> {
            final String log = "{\"log\": \"127.0.0.1 - frank [" + now() + "] \\\"GET /apache_pb.gif HTTP/1.0\\\" 200 2326\"}";
            final List<String> logs = Collections.nCopies(batchSize, log);
            final String logArray = String.join(",", logs);
            return "[" + logArray + "]";
        };
    }

    public static Function<Session, String> customFileTemplate(final int batchSize, final String logFilePath) {
        return session -> {
            String result = null;
            try {
                final List<Object> logs = OBJECT_MAPPER.readValue(Paths.get(logFilePath).toFile(), new TypeReference<List<Object>>(){});
                final int numLogsInFile = logs.size();

                for (int i = 0; i < batchSize - numLogsInFile; i++) {
                    logs.add(logs.get(i % numLogsInFile));
                }
                result = OBJECT_MAPPER.writeValueAsString(logs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        };
    }

    private Templates() {
    }
}
