/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.viewport;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaweb.employeerostering.gwtui.client.common.LocalWeekDatePicker;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.view.AbstractRosterView;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_TIME_UPDATE;

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
    @Named("span")
    @DataField("current-range")
    private HTMLElement currentRange;

    @Inject
    @Named("span")
    @DataField("row-count")
    private HTMLElement rowCountDisplay;

    @Inject
    @DataField("previous-page-button")
    private HTMLAnchorElement prevPage;

    @Inject
    @DataField("next-page-button")
    private HTMLAnchorElement nextPage;

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

    @Inject
    protected TranslationService translationService;

    protected Pagination pagenation;

    protected Integer rowCount;

    protected int timeRemaining;

    protected abstract Event<? extends AbstractRosterView> getViewRefreshEvent();

    protected abstract Event<Void> getViewInvalidateEvent();

    protected abstract Event<Pagination> getPageChangeEvent();

    protected abstract Event<LocalDateRange> getDateRangeEvent();

    @PostConstruct
    private void init() {
        pagenation = Pagination.of(0, 10);
        rowCount = 0;
        setCurrentRange(pagenation);
        ((HTMLElement) remainingTime.parentNode).classList.add("hidden");

        eventManager.subscribeToEventForever(getViewRefreshEvent(), (view) -> {
            onViewRefresh(view);
        });
        eventManager.subscribeToEventForever(SOLVE_TIME_UPDATE, (timeRemaining) -> {
            remainingTime.innerHTML = translationService.format(I18nKeys.Solver_secondsRemaining, timeRemaining);
        });
        eventManager.subscribeToEventForever(SOLVE_START, (v) -> {
            ((HTMLElement) remainingTime.parentNode).classList.remove("hidden");
        });
        eventManager.subscribeToEventForever(SOLVE_END, (v) -> {
            ((HTMLElement) remainingTime.parentNode).classList.add("hidden");
        });
        eventManager.subscribeToEventForever(getPageChangeEvent(), (pagenation) -> {
            this.pagenation = pagenation;
            setCurrentRange(pagenation);
        });
        eventManager.subscribeToEventForever(getDateRangeEvent(), (dateRange) -> {
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
            hardScore.textContent = translationService.format(I18nKeys.Indictment_hardScore, score.get().getHardScore());
            mediumScore.textContent = translationService.format(I18nKeys.Indictment_mediumScore, score.get().getMediumScore());
            softScore.textContent = translationService.format(I18nKeys.Indictment_softScore, score.get().getSoftScore());
        } else {
            scores.classList.add("hidden");
        }
    }

    protected void setRowCount(Integer rowCount) {
        rowCountDisplay.innerHTML = rowCount + "";
        this.rowCount = rowCount;
        setCurrentRange(pagenation);
    }

    protected void setCurrentRange(Pagination pagination) {
        currentRange.innerHTML = (pagination.getFirstResultIndex() + 1) + "-" + Math.min(pagination.getFirstResultIndex() + pagination.getNumberOfItemsPerPage(), rowCount);
        if (pagenation.getNumberOfItemsPerPage() + pagenation.getFirstResultIndex() >= rowCount) {
            nextPage.classList.add("btn", "disabled");
        } else {
            nextPage.classList.remove("btn", "disabled");
        }

        if (pagenation.isOnFirstPage()) {
            prevPage.classList.add("btn", "disabled");
        } else {
            prevPage.classList.remove("btn", "disabled");
        }
    }

    @EventHandler("refresh-button")
    public void onRefreshButtonClick(@ForEvent("click") MouseEvent e) {
        eventManager.fireEvent(getViewInvalidateEvent());
    }

    @EventHandler("previous-page-button")
    public void onPreviousPageButtonClick(@ForEvent("click") MouseEvent e) {
        if (!pagenation.isOnFirstPage()) {
            pagenation = pagenation.previousPage();
            eventManager.fireEvent(getPageChangeEvent(), pagenation);
        }
    }

    @EventHandler("next-page-button")
    public void onNextPageButtonClick(@ForEvent("click") MouseEvent e) {
        if (pagenation.getNumberOfItemsPerPage() + pagenation.getFirstResultIndex() < rowCount) {
            pagenation = pagenation.nextPage();
            eventManager.fireEvent(getPageChangeEvent(), pagenation);
        }
    }
}
