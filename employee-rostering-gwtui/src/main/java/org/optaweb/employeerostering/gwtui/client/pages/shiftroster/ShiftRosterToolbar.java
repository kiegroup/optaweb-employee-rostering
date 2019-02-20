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

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.client.Timer;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.RosterToolbar;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.PublishResult;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_DATE_RANGE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_PAGINATION;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_UPDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_TIME_UPDATE;

@Templated
public class ShiftRosterToolbar extends RosterToolbar
        implements
        IsElement {

    @Inject
    @DataField("solve-button")
    private HTMLButtonElement solveButton;

    @Inject
    @DataField("terminate-early-button")
    private HTMLButtonElement terminateEarlyButton;

    @Inject
    private NotificationFactory notificationFactory;

    @Inject
    private DateTimeUtils dateTimeUtils;

    @Inject
    private ManagedInstance<ShiftEditForm> shiftEditFormInstance;

    protected Timer updateSolvingTimeTimer;
    protected Timer terminateSolvingTimer;

    @PostConstruct
    public void init() {
        eventManager.subscribeToEventForever(Event.DATA_INVALIDATION, clazz -> {
            if (clazz.equals(Spot.class)) {
                updateRowCount();
            }
        });
        updateSolvingTimeTimer = new Timer() {

            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                }
                eventManager.fireEvent(SOLVE_TIME_UPDATE, timeRemaining);
            }
        };
        terminateSolvingTimer = new Timer() {

            @Override
            public void run() {
                terminateSolving();
            }
        };
        terminateEarlyButton.classList.add("hidden");
        updateRowCount();
    }

    @Override
    protected Event<ShiftRosterView> getViewRefreshEvent() {
        return SHIFT_ROSTER_UPDATE;
    }

    @Override
    protected Event<Pagination> getPageChangeEvent() {
        return SHIFT_ROSTER_PAGINATION;
    }

    @Override
    protected Event<Void> getViewInvalidateEvent() {
        return SHIFT_ROSTER_INVALIDATE;
    }

    @Override
    protected Event<LocalDateRange> getDateRangeEvent() {
        return SHIFT_ROSTER_DATE_RANGE;
    }

    private void updateRowCount() {
        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spotList -> {
            setRowCount(spotList.size());
        }));
    }

    @EventHandler("solve-button")
    public void onSolveButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantStore.getCurrentTenantId(),
                                             FailureShownRestCallback.onSuccess(a -> {
                                                 timeRemaining = 30;
                                                 scoresDisplay.classList.remove("hidden");
                                                 terminateEarlyButton.classList.remove("hidden");
                                                 solveButton.classList.add("hidden");
                                                 eventManager.fireEvent(SOLVE_START);
                                                 updateSolvingTimeTimer.scheduleRepeating(1000);
                                                 terminateSolvingTimer.schedule(30000);
                                             }));
    }

    @EventHandler("publish-button")
    public void onPublishButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.publishAndProvision(tenantStore.getCurrentTenantId(),
                                                     FailureShownRestCallback.onSuccess((PublishResult pr) -> {
                                                         notificationFactory.showSuccessMessage(I18nKeys.Notifications_publishResult, dateTimeUtils.translateLocalDate(pr.getPublishedFromDate()), dateTimeUtils
                                                                 .translateLocalDate(pr.getPublishedToDate()));
                                                         eventManager.fireEvent(getViewInvalidateEvent());
                                                     }));
    }

    @EventHandler("add-shift-button")
    public void onCreateShiftClick(@ForEvent("click") MouseEvent e) {
        shiftEditFormInstance.get().createNewShift();
    }

    private void terminateSolving() {
        remainingTimeDisplay.innerHTML = "";
        updateSolvingTimeTimer.cancel();

        scoresDisplay.classList.add("hidden");
        terminateEarlyButton.classList.add("hidden");
        solveButton.classList.remove("hidden");
        eventManager.fireEvent(SOLVE_END);
    }

    @EventHandler("terminate-early-button")
    public void onTerminateEarlyButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.terminateRosterEarly(tenantStore.getCurrentTenantId(),
                                                      FailureShownRestCallback.onSuccess(a -> {
                                                          terminateSolvingTimer.cancel();
                                                          terminateSolving();
                                                      }));
    }
}
