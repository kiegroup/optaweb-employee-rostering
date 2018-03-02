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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

@Templated
public class ShiftBlobPopoverContent implements BlobPopoverContent {

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("close-button")
    private HTMLButtonElement closeButton;

    @Inject
    @DataField("from-day")
    private HTMLInputElement fromDay;

    @Inject
    @DataField("from-hour")
    private HTMLInputElement fromHour;

    @Inject
    @DataField("to-day")
    private HTMLInputElement toDay;

    @Inject
    @DataField("to-hour")
    private HTMLInputElement toHour;

    @Inject
    @DataField("spot")
    private HTMLInputElement spot;

    @Inject
    @DataField("employee")
    private HTMLInputElement employee;

    @Inject
    @DataField("pinned")
    private HTMLInputElement pinned;

    @Inject
    @Named("p")
    @DataField("rotation-employee")
    private HTMLElement rotationEmployee;

    @Inject
    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @Inject
    @DataField("cancel-button")
    private HTMLButtonElement cancelButton;

    @Inject
    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    private BlobPopover parent;

    private ShiftBlobView blobView;

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        parent.hide();
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        parent.hide();
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        blobView.remove();
        parent.hide();
        e.stopPropagation();
    }

    @Override
    public BlobPopoverContent withParent(final BlobPopover parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public void setBlobView(final BlobView<?, ?> blobView) {
        this.blobView = (ShiftBlobView) blobView;

        final ShiftBlob blob = (ShiftBlob) blobView.getBlob();
        final Shift shift = blob.getShift();

        final LocalDateTime start = shift.getTimeSlot().getStartDateTime();
        fromDay.value = start.getMonth().toString() + " " + start.getDayOfMonth();
        fromHour.value = start.toLocalTime() + "";

        final LocalDateTime end = shift.getTimeSlot().getEndDateTime();
        toDay.value = end.getMonth().toString() + " " + end.getDayOfMonth();
        toHour.value = end.toLocalTime() + "";

        spot.value = shift.getSpot().getName();
        employee.value = shift.getEmployee().getName();
        pinned.checked = shift.isLockedByUser();

        rotationEmployee.textContent = Optional.ofNullable(shift.getRotationEmployee()).map(Employee::getName).orElse("-");
    }
}
