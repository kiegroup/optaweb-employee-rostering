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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.OffsetDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopover;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
@ApplicationScoped
public class EmployeeRosterPage implements Page {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    @DataField("viewport-frame")
    private HTMLDivElement viewportFrame;

    @Inject
    @DataField("viewport")
    private ViewportView<OffsetDateTime> viewportView;

    @Inject
    @DataField("next-page-button")
    private HTMLAnchorElement nextPageButton;

    @Inject
    @DataField("previous-page-button")
    private HTMLAnchorElement previousPageButton;

    @Inject
    @DataField("back-in-time-button")
    private HTMLAnchorElement backInTimeButton;

    @Inject
    @DataField("forward-in-time-button")
    private HTMLAnchorElement forwardInTimeButton;

    @Inject
    @DataField("availability-blob-popover")
    private BlobPopover availabilityBlobPopover;

    @Inject
    private EmployeeAvailabilityBlobPopoverContent availabilityBlobPopoverContent;

    @Inject
    private EmployeeRosterViewportFactory employeeRosterViewportFactory;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private PromiseUtils promiseUtils;

    private EmployeeRosterViewport viewport;
    private Pagination employeePagination = Pagination.of(0, 10);
    private EmployeeRosterView currentEmployeeRosterView;

    @PostConstruct
    public void init() {
        availabilityBlobPopover.init(this, availabilityBlobPopoverContent);
    }

    public BlobPopover getBlobPopover() {
        return availabilityBlobPopover;
    }

    public EmployeeRosterView getCurrentEmployeeRosterView() {
        return currentEmployeeRosterView;
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refreshWithLoadingSpinner();
    }

    public void onTenantChanged(@Observes final TenantStore.TenantChange tenant) {
        refreshWithLoadingSpinner();
    }

    private Promise<Void> refreshWithoutLoadingSpinner() {
        return fetchEmployeeRosterView().then(employeeRosterView -> {
            if (employeeRosterView.getEmployeeList().isEmpty()) {
                employeePagination = employeePagination.previousPage();
                return promiseUtils.resolve();
            } else {
                viewport = employeeRosterViewportFactory.getViewport(employeeRosterView);
                viewportView.setViewport(viewport);
                return promiseUtils.resolve();
            }
        });
    }

    private Promise<Void> refreshWithLoadingSpinner() {

        loadingSpinner.showFor("refresh-employee-roster");

        return refreshWithoutLoadingSpinner().then(i -> {
            loadingSpinner.hideFor("refresh-employee-roster");
            return promiseUtils.resolve();
        }).catch_(e -> {
            loadingSpinner.hideFor("refresh-employee-roster");
            return promiseUtils.resolve();
        });
    }

    //Events
    @EventHandler("refresh-button")
    public void onRefreshButtonClicked(@ForEvent("click") final MouseEvent e) {
        refreshWithLoadingSpinner();
    }

    @EventHandler("previous-page-button")
    public void onPreviousPageButtonClicked(@ForEvent("click") final MouseEvent e) {

        if (employeePagination.isOnFirstPage()) {
            return;
        }

        employeePagination = employeePagination.previousPage();
        refreshWithLoadingSpinner();
    }

    @EventHandler("next-page-button")
    public void onNextPageButtonClicked(@ForEvent("click") final MouseEvent e) {
        employeePagination = employeePagination.nextPage();
        refreshWithLoadingSpinner();
    }

    //FIXME: Improve horizontal navigation. Probably snap to fixed dates with animation.
    private static final Integer TIME_SCROLL_SIZE = 300;

    @EventHandler("forward-in-time-button")
    public void onForwardInTimeButtonClicked(@ForEvent("click") final MouseEvent e) {
        viewportFrame.scrollLeft += TIME_SCROLL_SIZE;
    }

    @EventHandler("back-in-time-button")
    public void onBackInTimeButtonClicked(@ForEvent("click") final MouseEvent e) {
        viewportFrame.scrollLeft -= TIME_SCROLL_SIZE;
    }

    //API calls

    private Promise<EmployeeRosterView> fetchEmployeeRosterView() {
        return new Promise<>((resolve, reject) -> {
            RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantStore.getCurrentTenantId(), employeePagination.getPageNumber(), employeePagination.getNumberOfItemsPerPage(),
                    onSuccess(v -> {
                        currentEmployeeRosterView = v;
                        resolve.onInvoke(v);
                    }));
        });
    }
}
