package org.optaplanner.openshift.employeerostering.gwtui.client.viewport;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalWeekDatePicker;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.view.AbstractRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_TIME_UPDATE;

public abstract class RosterToolbar {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    @DataField("scores")
    protected HTMLDivElement scores;

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
    protected HTMLElement remainingTime;

    @Inject
    @DataField("week-picker")
    protected LocalWeekDatePicker weekPicker;

    @Inject
    protected TenantStore tenantStore;

    @Inject
    protected EventManager eventManager;

    protected Pagination pagenation;

    protected int timeRemaining;

    protected abstract Event<? extends AbstractRosterView> getViewRefreshEvent();

    protected abstract Event<Void> getViewInvalidateEvent();

    protected abstract Event<Pagination> getPageChangeEvent();

    protected abstract Event<LocalDateRange> getDateRangeEvent();

    @PostConstruct
    private void init() {
        pagenation = Pagination.of(0, 10);
        remainingTime.classList.add("hidden");

        eventManager.subscribeToEvent(getViewRefreshEvent(), (view) -> {
            onViewRefresh(view);
        });
        eventManager.subscribeToEvent(SOLVE_TIME_UPDATE, (timeRemaining) -> {
            remainingTime.innerHTML = timeRemaining + "s";
        });
        eventManager.subscribeToEvent(SOLVE_START, (v) -> {
            remainingTime.classList.remove("hidden");
        });
        eventManager.subscribeToEvent(SOLVE_END, (v) -> {
            remainingTime.classList.add("hidden");
        });
        eventManager.subscribeToEvent(getPageChangeEvent(), (pagenation) -> {
            this.pagenation = pagenation;
        });
        eventManager.subscribeToEvent(getDateRangeEvent(), (dateRange) -> {
            weekPicker.setValue(dateRange.getStartDate());
        });
        weekPicker.addValueChangeHandler((dateChangedEvent) -> {
            eventManager.fireEvent(getDateRangeEvent(), new LocalDateRange(dateChangedEvent.getValue(), dateChangedEvent.getValue().plusDays(7)));
        });
    }

    protected void onViewRefresh(AbstractRosterView view) {
        final Optional<HardMediumSoftLongScore> score = Optional.ofNullable(view.getScore());

        if (score.isPresent()) {
            scores.classList.remove("hidden");
            hardScore.textContent = score.get().getHardScore() + "";
            mediumScore.textContent = score.get().getMediumScore() + "";
            softScore.textContent = score.get().getSoftScore() + "";
        } else {
            scores.classList.add("hidden");
        }
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

}
