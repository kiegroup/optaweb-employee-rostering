/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.NotificationSystem;
import org.optaplanner.openshift.employeerostering.gwtui.client.header.HeaderView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopover;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

import static elemental2.dom.DomGlobal.setInterval;
import static elemental2.dom.DomGlobal.setTimeout;
import static java.lang.Math.max;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
@ApplicationScoped
public class SpotRosterPage implements Page {

    private static final Integer SOLVE_TIME_IN_SECONDS = 30;

    @Inject
    @DataField("toolbar")
    private HTMLDivElement toolbar;

    @Inject
    @DataField("solve-button")
    private HTMLButtonElement solveButton;

    @Inject
    @DataField("terminate-early-button")
    private HTMLButtonElement terminateEarlyButton;

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    @DataField("publish-button")
    private HTMLButtonElement publishButton;

    @Inject
    @DataField("viewport")
    private ViewportView<LocalDateTime> viewportView;

    @Inject
    @DataField("scores")
    private HTMLDivElement scores;

    @Inject
    @Named("span")
    @DataField("hard-score")
    private HTMLElement hardScore;

    @Inject
    @Named("span")
    @DataField("soft-score")
    private HTMLElement softScore;

    @Inject
    @Named("span")
    @DataField("remaining-time")
    private HTMLElement remainingTime;

    @Inject
    @DataField("next-page-button")
    private HTMLAnchorElement nextPageButton;

    @Inject
    @DataField("previous-page-button")
    private HTMLAnchorElement previousPageButton;

    @Inject
    @DataField("shift-blob-popover")
    private BlobPopover shiftBlobPopover;

    @Inject
    private ShiftBlobPopoverContent shiftBlobPopoverContent;

    @Inject
    private SpotRosterViewportFactory spotRosterViewportFactory;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private HeaderView headerView;

    @Inject
    @DataField("current-pagination-range")
    @Named("span")
    private HTMLElement currentRange;

    @Inject
    @DataField("num-of-spots")
    @Named("span")
    private HTMLElement rowCount;
    @Inject
    private NotificationSystem notificationSystem;

    private double solveTaskId;
    private double updateRemainingTimeTaskId;
    private double stopSolvingTaskId;

    private SpotRosterViewport viewport;
    private Pagination spotsPagination = Pagination.of(0, 10);
    private SpotRosterView currentSpotRosterView;

    @PostConstruct
    public void init() {
        shiftBlobPopover.init(this, shiftBlobPopoverContent);
        currentRange.innerHTML = new SafeHtmlBuilder().appendEscaped(spotsPagination.toString())
                .toSafeHtml().asString();
    }

    public BlobPopover getBlobPopover() {
        return shiftBlobPopover;
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refreshWithLoadingSpinner();
    }

    @Override
    public Promise<Void> onOpen() {
        headerView.addStickyElement(() -> toolbar);
        return promiseUtils.resolve();
    }

    public void onTenantChanged(@Observes final TenantStore.TenantChange tenant) {
        refreshWithLoadingSpinner();
    }

    private Promise<Void> refreshWithoutLoadingSpinner() {
        return promiseUtils.manage(getSpotList().then(spotList -> {
            return promiseUtils.manage(fetchSpotRosterView().then(spotRosterView -> {
                currentSpotRosterView = spotRosterView;
                final Optional<HardSoftScore> score = Optional.ofNullable(spotRosterView.getScore());

                if (score.isPresent()) {
                    scores.classList.remove("hidden");
                    hardScore.textContent = score.get().getHardScore() + "";
                    softScore.textContent = score.get().getSoftScore() + "";
                    terminateEarlyButton.classList.remove("hidden");
                    solveButton.classList.add("hidden");
                } else {
                    scores.classList.add("hidden");
                    terminateEarlyButton.classList.add("hidden");
                    solveButton.classList.remove("hidden");
                }

                if (spotRosterView.getSpotList().isEmpty()) {
                    spotsPagination = spotsPagination.previousPage();
                } else {
                    viewport = spotRosterViewportFactory.getViewport(spotRosterView);
                    viewportView.setViewport(viewport);
                }
                rowCount.innerHTML = Integer.toString(spotList.size());
                currentRange.innerHTML = new SafeHtmlBuilder().append(spotsPagination.getFirstResultIndex() + 1)
                        .append('-').append(Math.min(spotsPagination.getFirstResultIndex() + spotsPagination.getNumberOfItemsPerPage(), spotList.size()))
                        .toSafeHtml().asString();
                return promiseUtils.resolve();
            }));
        }));
    }

    private Promise<Void> refreshWithLoadingSpinner() {

        loadingSpinner.showFor("refresh-spot-roster");

        return refreshWithoutLoadingSpinner().then(i -> {
            loadingSpinner.hideFor("refresh-spot-roster");
            return promiseUtils.resolve();
        }).catch_(e -> {
            loadingSpinner.hideFor("refresh-spot-roster");
            return promiseUtils.resolve();
        });
    }

    private void swapSolveAndTerminateEarlyButtons() {
        solveButton.classList.toggle("hidden");
        terminateEarlyButton.classList.toggle("hidden");
    }

    private void updateRemainingTime(final long start) {
        final long elapsed = (System.currentTimeMillis() - start) / 1000;
        final long remaining = SOLVE_TIME_IN_SECONDS - elapsed;
        remainingTime.textContent = Long.toString(max(0, remaining));
    }

    private void unlockRosterViewport() {
        viewportView.getElement().classList.remove("locked-for-interaction");
    }

    private void lockRosterViewport() {
        viewportView.getElement().classList.add("locked-for-interaction");
    }

    //Events

    @EventHandler("solve-button")
    public void onSolveButtonClicked(@ForEvent("click") final MouseEvent e) {

        triggerRosterSolve().then(i -> {

            lockRosterViewport();
            swapSolveAndTerminateEarlyButtons();

            final long start = System.currentTimeMillis();

            updateRemainingTimeTaskId = setInterval(a -> updateRemainingTime(start), 1000);
            solveTaskId = setInterval(a -> refreshWithoutLoadingSpinner(), 2000);
            stopSolvingTaskId = setTimeout(a -> stopSolving(), (SOLVE_TIME_IN_SECONDS + 1) * 1000);

            return promiseUtils.resolve();
        });
    }

    @EventHandler("terminate-early-button")
    public void onTerminateEarlyButtonClicked(@ForEvent("click") final MouseEvent e) {
        triggerTerminateEarly().then(i -> {
            swapSolveAndTerminateEarlyButtons();
            return stopSolving();
        });
    }

    private Promise<Void> stopSolving() {

        DomGlobal.clearInterval(stopSolvingTaskId);
        DomGlobal.clearInterval(solveTaskId);
        DomGlobal.clearInterval(updateRemainingTimeTaskId);

        unlockRosterViewport();

        return refreshWithLoadingSpinner();
    }

    @EventHandler("refresh-button")
    public void onRefreshButtonClicked(@ForEvent("click") final MouseEvent e) {
        refreshWithLoadingSpinner();
    }

    @EventHandler("publish-button")
    public void onPublishButtonClicked(@ForEvent("click") final MouseEvent e) {
        RosterRestServiceBuilder.publishAndProvision(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(pr -> {
            notificationSystem.notify("Published Week of " + pr.getPublishedFromDate().toString(),
                    "Shifts started after " + pr.getPublishedFromDate().toString() + " and before " +
                            pr.getPublishedToDate().toString() + " published.");
            refreshWithLoadingSpinner();
        }));
    }

    @EventHandler("previous-page-button")
    public void onPreviousPageButtonClicked(@ForEvent("click") final MouseEvent e) {

        if (spotsPagination.isOnFirstPage()) {
            return;
        }

        spotsPagination = spotsPagination.previousPage();
        refreshWithLoadingSpinner();
    }

    @EventHandler("next-page-button")
    public void onNextPageButtonClicked(@ForEvent("click") final MouseEvent e) {
        spotsPagination = spotsPagination.nextPage();
        refreshWithLoadingSpinner();
    }

    public SpotRosterView getCurrentSpotRosterView() {
        return currentSpotRosterView;
    }

    //API calls

    private Promise<Void> triggerRosterSolve() {
        return promiseUtils.promise((resolve, reject) -> {
            RosterRestServiceBuilder.solveRoster(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<Void> triggerTerminateEarly() {
        return promiseUtils.promise((resolve, reject) -> {
            RosterRestServiceBuilder.terminateRosterEarly(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<SpotRosterView> fetchSpotRosterView() {
        return promiseUtils.promise((resolve, reject) -> {
            RosterRestServiceBuilder.getCurrentSpotRosterView(tenantStore.getCurrentTenantId(),
                    spotsPagination.getPageNumber(),
                    spotsPagination.getNumberOfItemsPerPage(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<List<Spot>> getSpotList() {
        return promiseUtils.promise((resolve, reject) -> {
            SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }
}
