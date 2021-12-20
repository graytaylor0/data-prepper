package com.amazon.dataprepper.plugins.prepper.aggregate;

import com.amazon.dataprepper.model.event.Event;

import java.util.Map;
import java.util.Optional;

public interface AggregateAction {
    /**
     * Handles an event as part of aggregation.
     *
     * @param event The current event
     * @param groupState An arbitrary map for the current group
     * @return The Event to return. Empty if this event should be removed from processing.
     */
    default Optional<Event> handleEvent(Event event, Map<Object, Object> groupState, Map<String, Object> identificationKeysHash) {
        return Optional.of(event);
    }

    /**
     * Concludes a group of Events
     *
     * @param groupState The groupState map from previous calls to handleEvent
     * @return The final Event to return. Return empty if the aggregate processor
     * should not pass an event
     */
    default Optional<Event> concludeGroup(Map<Object, Object> groupState) {
        return Optional.empty();
    }
}
