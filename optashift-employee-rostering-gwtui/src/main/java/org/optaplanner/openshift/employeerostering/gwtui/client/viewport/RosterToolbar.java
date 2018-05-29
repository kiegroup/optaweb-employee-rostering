package org.optaplanner.openshift.employeerostering.gwtui.client.viewport;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.user.client.Timer;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.AbstractRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_TIME_UPDATE;

public abstract class RosterToolbar {

    @Inject
    @DataField("solve-button")
    private HTMLButtonElement solveButton;
    
    @Inject
    @DataField("publish-button")
    private HTMLButtonElement publishButton;
    
    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    @DataField("terminate-early-button")
    private HTMLButtonElement terminateEarlyButton;

    @Inject
    @DataField("scores")
    private HTMLDivElement scores;

    @Inject
    @Named("span")
    @DataField("hard-score")
    private HTMLElement hardScore;
    
    @Inject
    @Named("span")
    @DataField("medium-score")
    private HTMLElement mediumScore;

    @Inject
    @Named("span")
    @DataField("soft-score")
    private HTMLElement softScore;

    @Inject
    @DataField("previous-page-button")
    private HTMLButtonElement prevPage;

    @Inject
    @DataField("next-page-button")
    private HTMLButtonElement nextPage;

    @Inject
    @Named("span")
    @DataField("remaining-time")
    private HTMLElement remainingTime;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private EventManager eventManager;

    private Pagination pagenation;

    private Timer updateSolvingTimeTimer;
    private Timer terminateSolvingTimer;

    private int timeRemaining;

    protected abstract Event<? extends AbstractRosterView> getViewRefreshEvent();
    
    protected abstract Event<Void> getViewInvalidateEvent();

    protected abstract Event<Pagination> getPageChangeEvent();

    @PostConstruct
    private void init() {
        pagenation = Pagination.of(0, 10);
        terminateEarlyButton.classList.add("hidden");
        remainingTime.classList.add("hidden");
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
        eventManager.subscribeToEvent(getViewRefreshEvent(), (view) -> {
            final Optional<HardMediumSoftLongScore> score = Optional.ofNullable(view.getScore());

            if (score.isPresent()) {
                scores.classList.remove("hidden");
                hardScore.textContent = score.get().getHardScore() + "";
                mediumScore.textContent = score.get().getMediumScore() + "";
                softScore.textContent = score.get().getSoftScore() + "";
            } else {
                scores.classList.add("hidden");
            }
        });
        eventManager.subscribeToEvent(SOLVE_TIME_UPDATE, (timeRemaining) -> {
            remainingTime.innerHTML = timeRemaining + "s";
        });
        eventManager.subscribeToEvent(SOLVE_START, (v) -> {
            solveButton.classList.add("hidden");
            terminateEarlyButton.classList.remove("hidden");
            remainingTime.classList.remove("hidden");
        });
        eventManager.subscribeToEvent(SOLVE_END, (v) -> {
            terminateEarlyButton.classList.add("hidden");
            remainingTime.classList.add("hidden");
            solveButton.classList.remove("hidden");
        });
        eventManager.subscribeToEvent(getPageChangeEvent(), (pagenation) -> {
            this.pagenation = pagenation;
        });
    }

    @EventHandler("solve-button")
    public void onSolveButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantStore.getCurrentTenantId(),
                                             FailureShownRestCallback.onSuccess(a -> {
                                                 timeRemaining = 30;
                                                 eventManager.fireEvent(SOLVE_START);
                                                 updateSolvingTimeTimer.scheduleRepeating(1000);
                                                 terminateSolvingTimer.schedule(30000);
                                             }));

    }

    @EventHandler("terminate-early-button")
    public void onTerminateEarlyButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.terminateRosterEarly(tenantStore.getCurrentTenantId(),
                                                      FailureShownRestCallback.onSuccess(a -> {
                                                          terminateSolvingTimer.cancel();
                                                          terminateSolving();
                                                      }));

    }
    
    @EventHandler("publish-button")
    public void onPublishButtonClick(@ForEvent("click") MouseEvent e) {
        RosterRestServiceBuilder.publishAndProvision(tenantStore.getCurrentTenantId(),
                                             FailureShownRestCallback.onSuccess(a -> {
                                                 eventManager.fireEvent(getViewInvalidateEvent());
                                             }));

    }
    
    @EventHandler("refresh-button")
    public void onRefreshButtonClick(@ForEvent("click") MouseEvent e) {
         eventManager.fireEvent(getViewInvalidateEvent());

    }

    @EventHandler("previous-page-button")
    public void onPreviousPageButtonClick(@ForEvent("click") MouseEvent e) {
        pagenation = pagenation.previousPage();
        eventManager.fireEvent(getPageChangeEvent(), pagenation);
    }

    @EventHandler("next-page-button")
    public void onNextPageButtonClick(@ForEvent("click") MouseEvent e) {
        pagenation = pagenation.nextPage();
        eventManager.fireEvent(getPageChangeEvent(), pagenation);
    }

    private void terminateSolving() {
        remainingTime.innerHTML = "";
        updateSolvingTimeTimer.cancel();

        scores.classList.add("hidden");
        terminateEarlyButton.classList.add("hidden");
        solveButton.classList.remove("hidden");
        eventManager.fireEvent(SOLVE_END);
    }
}
