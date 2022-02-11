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
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AggregateProcessorIT {
    @Rule
    public ConcurrentRule rule = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatingRule = new RepeatingRule();

    @Mock
    private PluginFactory pluginFactory;

    private static final int NUM_EVENTS = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    private AggregateProcessor aggregateProcessor;
    private AggregateProcessorConfig aggregateProcessorConfig;
    private AggregateAction aggregateAction;
    private PluginMetrics pluginMetrics;

    private Map<String, Object> aggregateConfigMap;

    private Collection<Record<Event>> eventBatch;

    private static Set<Map<String, Object>> set;

    @Before
    public void setup() {
        final List<String> identificationKeys = new ArrayList<>();
        identificationKeys.add("firstRandomNumber");
        identificationKeys.add("secondRandomNumber");
        identificationKeys.add("thirdRandomNumber");

        aggregateConfigMap = new HashMap<>();
        aggregateConfigMap.put("window_duration", 10);
        aggregateConfigMap.put("identification_keys", identificationKeys);
        aggregateConfigMap.put("action", new PluginModel("aggregateAction", Collections.emptyMap()));
        aggregateAction = new RemoveDuplicatesAggregateAction();

        eventBatch = getBatchOfEvents();

        pluginMetrics = PluginMetrics.fromNames("aggregate", "aggregate-pipeline");

        aggregateProcessorConfig = OBJECT_MAPPER.convertValue(aggregateConfigMap, AggregateProcessorConfig.class);
        when(pluginFactory.loadPlugin(eq(AggregateAction.class), any(PluginSetting.class)))
                .thenReturn(aggregateAction);
        aggregateProcessor = new AggregateProcessor(aggregateProcessorConfig, pluginMetrics, pluginFactory);

        set = new HashSet<>();
    }

    /*@Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testCombineActionGivesCorrectResults() {
    }*/

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testRemoveDuplicatesActionGivesCorrectResults() {
        final List<Record<Event>> recordsOut = (List<Record<Event>>) aggregateProcessor.doExecute(eventBatch);

        for (final Record<Event> record : recordsOut) {
            final Map<String, Object> map = record.getData().toMap();
            set.add(map);
        }
    }

    @AfterClass
    public static void testAfterAllThreadsRun() {
        assertThat(set.size(), equalTo(8));
    }

    private List<Record<Event>> getBatchOfEvents() {
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

    private Map<String, Object> getGeneratedEventMap() {
        final Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("firstRandomNumber", new Random().nextInt(2));
        eventMap.put("secondRandomNumber", new Random().nextInt(2));
        eventMap.put("thirdRandomNumber", new Random().nextInt(2));
        return eventMap;
    }
}
