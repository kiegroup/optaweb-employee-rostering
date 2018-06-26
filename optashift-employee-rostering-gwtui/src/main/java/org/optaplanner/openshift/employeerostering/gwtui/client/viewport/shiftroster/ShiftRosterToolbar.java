package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.client.Timer;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.RosterToolbar;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.ShiftRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_DATE_RANGE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_UPDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_TIME_UPDATE;

@Templated
public class ShiftRosterToolbar extends RosterToolbar implements IsElement {

    @Inject
    @DataField("solve-button")
    private HTMLButtonElement solveButton;

    @Inject
    @DataField("terminate-early-button")
    private HTMLButtonElement terminateEarlyButton;

    protected Timer updateSolvingTimeTimer;
    protected Timer terminateSolvingTimer;

    @PostConstruct
    public void initTimers() {
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

    @EventHandler("solve-button")
    public void onSolveButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantStore.getCurrentTenantId(),
                                             FailureShownRestCallback.onSuccess(a -> {
                                                 timeRemaining = 30;
                                                 scores.classList.remove("hidden");
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
                                                     FailureShownRestCallback.onSuccess(a -> {
                                                         eventManager.fireEvent(getViewInvalidateEvent());
                                                     }));

    }

    private void terminateSolving() {
        remainingTime.innerHTML = "";
        updateSolvingTimeTimer.cancel();

        scores.classList.add("hidden");
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
