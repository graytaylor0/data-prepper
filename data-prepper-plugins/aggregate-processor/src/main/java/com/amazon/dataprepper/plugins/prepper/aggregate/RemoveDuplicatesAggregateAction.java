package com.amazon.dataprepper.plugins.prepper.aggregate;

import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.event.Event;

import java.util.Map;
import java.util.Optional;

@DataPrepperPlugin(name = "remove_duplicates", pluginType = AggregateAction.class)
public class RemoveDuplicatesAggregateAction implements AggregateAction {
    @Override
    public Optional<Event> handleEvent(Event event, Map<Object, Object> groupState, Map<String, Object> identificationKeysHash) {
        if (groupState.containsKey(identificationKeysHash)) {
            return Optional.empty();
        }

        groupState.put(identificationKeysHash, event);
        return Optional.of(event);
    }
}
