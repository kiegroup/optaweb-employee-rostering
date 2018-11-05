/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.common;

import java.util.Map;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;

// This can probably also be done with @Observes, but then we need to create classes/use annotations
// for a bunch of events, most of which carry either one or no values
@ApplicationScoped
public class EventManager {

    public <T> void subscribeToEvent(Event<T> event, Handler<T> handler) {
        handler.setupUnsubscribeListener(ErraiBus.get().subscribe(event.getEventName(), (m) -> {
            handler.handleEvent(m.getValue(event.getEventClass()));
        }));
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

    public <T> void subscribeToEventForever(Event<T> event, Consumer<T> handler) {
        subscribeToEvent(event, new Handler<T>() {

            @Override
            public void setupUnsubscribeListener(Subscription subscription) {
            }

            @Override
            public void handleEvent(T value) {
                handler.accept(value);
            }
        });
    }

    public <T> void subscribeToEventForElement(Event<T> event, IsElement element, Consumer<T> handler) {
        subscribeToEvent(event, new Handler<T>() {

            @Override
            public void setupUnsubscribeListener(Subscription subscription) {
                element.getElement().addEventListener("unload", (e) -> {
                    subscription.remove();
                });
            }

            @Override
            public void handleEvent(T value) {
                handler.accept(value);
            }
        });
    }

    public <T> void subscribeToEventForElement(Event<T> event, org.jboss.errai.ui.client.local.api.IsElement element, Consumer<T> handler) {
        subscribeToEvent(event, new Handler<T>() {

            @SuppressWarnings("deprecation")
            @Override
            public void setupUnsubscribeListener(Subscription subscription) {
                element.getElement().addEventListener("unload", (e) -> {
                    subscription.remove();
                }, true);
            }

            @Override
            public void handleEvent(T value) {
                handler.accept(value);
            }
        });
    }

    public static interface Handler<T> {

        /**
         * Called when subscribed to an event. Use it to set up unsubscribe listeners.
         */
        void setupUnsubscribeListener(Subscription subscription);

        void handleEvent(T value);
    }

    // This is really an enum, but enums cannot have generics
    public static final class Event<T> {

        // Solving events
        public static final Event<Integer> SOLVE_TIME_UPDATE = new Event<>("SolveTimeUpdate", Integer.class);
        public static final Event<Void> SOLVE_START = new Event<>("SolveStart", Void.class);
        public static final Event<Void> SOLVE_END = new Event<>("SolveEnd", Void.class);

        // Spot Roster Events
        public static final Event<ShiftRosterView> SHIFT_ROSTER_UPDATE = new Event<>("ShiftRosterUpdate", ShiftRosterView.class);
        public static final Event<Void> SHIFT_ROSTER_INVALIDATE = new Event<>("ShiftRosterInvalidate", Void.class);
        public static final Event<Pagination> SHIFT_ROSTER_PAGINATION = new Event<>("ShiftRosterPagination", Pagination.class);
        public static final Event<LocalDateRange> SHIFT_ROSTER_DATE_RANGE = new Event<>("ShiftRosterDateRange", LocalDateRange.class);

        // Employee Roster Events
        public static final Event<AvailabilityRosterView> AVAILABILITY_ROSTER_UPDATE = new Event<>("AvailabilityRosterUpdate", AvailabilityRosterView.class);
        public static final Event<Void> AVAILABILITY_ROSTER_INVALIDATE = new Event<>("AvailabilityRosterInvalidate", Void.class);
        public static final Event<Pagination> AVAILABILITY_ROSTER_PAGINATION = new Event<>("AvailabilityRosterPagination", Pagination.class);
        public static final Event<LocalDateRange> AVAILABILITY_ROSTER_DATE_RANGE = new Event<>("AvailabilityRosterDateRange", LocalDateRange.class);

        // Rotation Events
        public static final Event<Void> ROTATION_SAVE = new Event<>("RotationSave", Void.class);
        public static final Event<Void> ROTATION_INVALIDATE = new Event<>("RotationInvalidate", Void.class);

        // Invalidation Events
        @SuppressWarnings("rawtypes")
        public static final Event<Class> DATA_INVALIDATION = new Event<>("DataInvalidation", Class.class);
        @SuppressWarnings("rawtypes")
        public static final Event<Map> SKILL_MAP_INVALIDATION = new Event<>("SkillMapInvalidation", Map.class);
        @SuppressWarnings("rawtypes")
        public static final Event<Map> CONTRACT_MAP_INVALIDATION = new Event<>("ContractMapInvalidation", Map.class);

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
