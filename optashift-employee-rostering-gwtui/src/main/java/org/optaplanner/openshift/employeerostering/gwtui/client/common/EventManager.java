package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.ShiftRosterView;

// This can probably also be done with @Observes, but then we need to create classes/use annotations
// for a bunch of events, most of which carry either one or no values
@ApplicationScoped
public class EventManager {

    public <T> void subscribeToEvent(Event<T> event, Handler<T> handler) {
        ErraiBus.get().subscribe(event.getEventName(), (m) -> {
            handler.handleEvent(m.getValue(event.getEventClass()));
        });
    }

    public <T> void fireEvent(Event<T> event, T data) {
        MessageBuilder.createMessage()
                      .toSubject(event.getEventName())
                      .withValue(data)
                      .noErrorHandling()
                      .sendNowWith(ErraiBus.getDispatcher());
    }

    public void fireEvent(Event<Void> event) {
        MessageBuilder.createMessage()
                      .toSubject(event.getEventName())
                      .noErrorHandling()
                      .sendNowWith(ErraiBus.getDispatcher());
    }

    public static interface Handler<T> {

        void handleEvent(T value);
    }

    // This is really an enum, but enums cannot have generics
    public static final class Event<T> {

        // Solving events
        public static Event<Integer> SOLVE_TIME_UPDATE = new Event<>("SolveTimeUpdate", Integer.class);
        public static Event<Void> SOLVE_START = new Event<>("SolveStart", Void.class);
        public static Event<Void> SOLVE_END = new Event<>("SolveEnd", Void.class);

        // Spot Roster Events
        public static Event<ShiftRosterView> SHIFT_ROSTER_UPDATE = new Event<>("ShiftRosterUpdate", ShiftRosterView.class);
        public static Event<Void> SHIFT_ROSTER_INVALIDATE = new Event<>("ShiftRosterInvalidate", Void.class);
        public static Event<Pagination> SHIFT_ROSTER_PAGINATION = new Event<>("ShiftRosterPagination", Pagination.class);

        // Employee Roster Events
        public static Event<AvailabilityRosterView> AVAILABILITY_ROSTER_UPDATE = new Event<>("AvailabilityRosterUpdate", AvailabilityRosterView.class);
        public static Event<Void> AVAILABILITY_ROSTER_INVALIDATE = new Event<>("AvailabilityRosterInvalidate", Void.class);
        public static Event<Pagination> AVAILABILITY_ROSTER_PAGINATION = new Event<>("AvailabilityRosterPagination", Pagination.class);

        // Rotation Events
        public static Event<Void> ROTATION_SAVE = new Event<>("RotationSave", Void.class);

        private final String eventName;
        private final Class<? extends T> eventClass;

        private Event(String eventName, Class<? extends T> eventClass) {
            this.eventName = eventName;
            this.eventClass = eventClass;
        }

        public final String getEventName() {
            return eventName;
        }

        public final Class<? extends T> getEventClass() {
            return eventClass;
        }
    }
}
