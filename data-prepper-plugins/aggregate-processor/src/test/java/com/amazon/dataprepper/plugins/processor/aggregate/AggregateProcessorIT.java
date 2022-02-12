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
import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CyclicBarrier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AggregateProcessorIT {
    @Rule
    public ConcurrentRule rule = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatingRule = new RepeatingRule();

    @Mock
    private static PluginFactory pluginFactory;

    private static final int NUM_EVENTS = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    private static AggregateProcessor aggregateProcessor;
    private static AggregateProcessorConfig aggregateProcessorConfig;
    private static AggregateAction aggregateAction;
    private static PluginMetrics pluginMetrics;

    private static Map<String, Object> aggregateConfigMap;

    private static Collection<Record<Event>> eventBatch;

    private static ConcurrentLinkedDeque<Map<String, Object>> aggregatedResult;
    private static CyclicBarrier cyclicBarrier;

    @BeforeClass
    public static void setup() {
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

    /*@Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testCombineActionGivesCorrectResults() {
    }*/

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 1)
    public void testRemoveDuplicatesActionGivesCorrectResults() {
        final List<Record<Event>> recordsOut = (List<Record<Event>>) aggregateProcessor.doExecute(eventBatch);
        for (final Record<Event> record : recordsOut) {
            final Map<String, Object> map = record.getData().toMap();
            aggregatedResult.add(map);
        }
    }

    @AfterClass
    public static void testAfterAllThreadsRun() {
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
}
