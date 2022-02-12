package com.amazon.dataprepper.plugins.processor.aggregate;

import com.amazon.dataprepper.metrics.PluginMetrics;
import com.amazon.dataprepper.model.configuration.PluginModel;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.event.Event;
import com.amazon.dataprepper.model.event.JacksonEvent;
import com.amazon.dataprepper.model.plugin.PluginFactory;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.plugins.processor.aggregate.actions.RemoveDuplicatesAggregateAction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.testing.threadtester.AnnotatedTestRunner;
import com.google.testing.threadtester.ThreadedAfter;
import com.google.testing.threadtester.ThreadedBefore;
import com.google.testing.threadtester.ThreadedMain;
import com.google.testing.threadtester.ThreadedSecondary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AggregateProcessorIT {

    @Mock
    private static PluginFactory pluginFactory;

    private static final int NUM_EVENTS = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    private AggregateProcessor aggregateProcessor;
    private AggregateProcessorConfig aggregateProcessorConfig;
    private AggregateAction aggregateAction;
    private PluginMetrics pluginMetrics;

    private Map<String, Object> aggregateConfigMap;

    private Collection<Record<Event>> eventBatch;

    private ConcurrentLinkedDeque<Map<String, Object>> aggregatedResult;

    @ThreadedBefore
    public void setup() {
        final List<String> identificationKeys = new ArrayList<>();
        identificationKeys.add("firstRandomNumber");
        identificationKeys.add("secondRandomNumber");
        identificationKeys.add("thirdRandomNumber");

        aggregateConfigMap = new HashMap<>();
        aggregateConfigMap.put("window_duration", 180);
        aggregateConfigMap.put("identification_keys", identificationKeys);
        aggregateConfigMap.put("action", new PluginModel("aggregateAction", Collections.emptyMap()));
        aggregateAction = new RemoveDuplicatesAggregateAction();

        eventBatch = getBatchOfEvents();

        pluginMetrics = PluginMetrics.fromNames("aggregate", "aggregate-pipeline");

        aggregateProcessorConfig = OBJECT_MAPPER.convertValue(aggregateConfigMap, AggregateProcessorConfig.class);
        when(pluginFactory.loadPlugin(eq(AggregateAction.class), any(PluginSetting.class)))
                .thenReturn(aggregateAction);
        aggregateProcessor = new AggregateProcessor(aggregateProcessorConfig, pluginMetrics, pluginFactory);

        aggregatedResult = new ConcurrentLinkedDeque<>();
    }

    @ThreadedMain
    public void testRemoveDuplicatesActionGivesCorrectResults() {
        final List<Record<Event>> recordsOut = (List<Record<Event>>) aggregateProcessor.doExecute(eventBatch);
        for (final Record<Event> record : recordsOut) {
            final Map<String, Object> map = record.getData().toMap();
            aggregatedResult.add(map);
        }
    }

    @ThreadedSecondary
    public void testRemoveDuplicatesActionGivesCorrectResults2() {
        final List<Record<Event>> recordsOut = (List<Record<Event>>) aggregateProcessor.doExecute(eventBatch);
        for (final Record<Event> record : recordsOut) {
            final Map<String, Object> map = record.getData().toMap();
            aggregatedResult.add(map);
        }
    }

    @ThreadedAfter
    public void testAfterAllThreadsRun() {
        assertThat(aggregatedResult.size(), equalTo(8));
    }

    private static List<Record<Event>> getBatchOfEvents() {
        final List<Record<Event>> events = new ArrayList<>();

        for (int i = 0; i < NUM_EVENTS; i++) {
            final Map<String, Object> eventMap = getGeneratedEventMap();
            final Event event = JacksonEvent.builder()
                            .withEventType("event")
                            .withData(eventMap)
                            .build();

            events.add(new Record<>(event));
        }
        return events;
    }

    private static Map<String, Object> getGeneratedEventMap() {
        final Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("firstRandomNumber", new Random().nextInt(2));
        eventMap.put("secondRandomNumber", new Random().nextInt(2));
        eventMap.put("thirdRandomNumber", new Random().nextInt(2));
        return eventMap;
    }

    @Test
    public void testPutIfAbsent() throws Exception {
        System.out.printf("In testPutIfAbsent\n");
        // Create an AnnotatedTestRunner that will run the threaded tests defined in this
        // class. We want to test the behaviour of the private method "putIfAbsentInternal" so
        // we need to specify it by name using runner.setMethodOption()
        AnnotatedTestRunner runner = new AnnotatedTestRunner();
        runner.setDebug(true);
        try {
            runner.runTests(this.getClass(), AggregateProcessor.class);
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }

    }
}
