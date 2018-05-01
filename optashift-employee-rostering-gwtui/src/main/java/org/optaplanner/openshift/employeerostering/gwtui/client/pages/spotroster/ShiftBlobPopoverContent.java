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

import javax.inject.Inject;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopover;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopoverContent;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
public class ShiftBlobPopoverContent implements BlobPopoverContent {

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("close-button")
    private HTMLButtonElement closeButton;

    @Inject
    @DataField("from")
    private LocalDateTimePicker from;

    @Inject
    @DataField("to")
    private LocalDateTimePicker to;

    @Inject
    @DataField("spot")
    private ListBox spotSelect; //FIXME: Don't use GWT widget

    @Inject
    @DataField("employee")
    private ListBox employeeSelect; //FIXME: Don't use GWT widget

    @Inject
    @DataField("pinned")
    private HTMLInputElement pinned;

    @Inject
    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @Inject
    @DataField("cancel-button")
    private HTMLButtonElement cancelButton;

    @Inject
    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    @Inject
    private TenantStore tenantStore;

    private BlobPopover popover;

    private ShiftBlobView blobView;

    @Override
    public void init(final BlobView<?, ?> blobView) {

        this.blobView = (ShiftBlobView) blobView;
        final ShiftBlob blob = (ShiftBlob) blobView.getBlob();
        final ShiftView shift = blob.getShiftView();

        employeeSelect.clear();
        employeeSelect.addItem("Unassigned", "-1"); //FIXME: i18n

        // TODO: Do rest call
        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            spotSelect.setSelectedIndex(spots.indexOf(blob.getSpot()));
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            employeeSelect.setSelectedIndex((blob.getEmployee() == null) ? 0 : employees.indexOf(blob.getEmployee()) + 1);
        }));

        from.setValue(shift.getStartDateTime());
        to.setValue(shift.getEndDateTime());
        pinned.checked = shift.isPinnedByUser();
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        popover.hide();
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        popover.hide();
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {

        final ShiftBlob blob = (ShiftBlob) blobView.getBlob();
        final ShiftView shiftView = blob.getShiftView();

        final boolean oldLockedByUser = shiftView.isPinnedByUser();
        final Long oldEmployee = shiftView.getEmployeeId();
        final LocalDateTime oldStartDateTime = shiftView.getStartDateTime();
        final LocalDateTime oldEndDateTime = shiftView.getEndDateTime();

        try {
            shiftView.setPinnedByUser(pinned.checked);
            shiftView.setEmployeeId(parseId(employeeSelect.getSelectedValue()));
            shiftView.setStartDateTime(from.getValue());
            shiftView.setEndDateTime(to.getValue());
        } catch (ValidationException invalidField) {
            shiftView.setPinnedByUser(oldLockedByUser);
            shiftView.setEmployeeId(oldEmployee);
            shiftView.setStartDateTime(oldStartDateTime);
            shiftView.setEndDateTime(oldEndDateTime);
            return;
        }

        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView, onSuccess((final Shift updatedShift) -> {
            blob.setShiftView(new ShiftView(updatedShift));
            blobView.refresh();
            popover.hide();
        }).onFailure(i -> {
            shiftView.setPinnedByUser(oldLockedByUser);
            shiftView.setEmployeeId(oldEmployee);
            shiftView.setStartDateTime(oldStartDateTime);
            shiftView.setEndDateTime(oldEndDateTime);
        }).onError(i -> {
            shiftView.setPinnedByUser(oldLockedByUser);
            shiftView.setEmployeeId(oldEmployee);
            shiftView.setStartDateTime(oldStartDateTime);
            shiftView.setEndDateTime(oldEndDateTime);
        }));

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        blobView.remove();
        popover.hide();
        e.stopPropagation();
    }

    @Override
    public BlobPopoverContent withPopover(final BlobPopover popover) {
        this.popover = popover;
        return this;
    }

    private Long parseId(String text) {
        Long id = Long.parseLong(text);
        if (id < 0) {
            return null;
        }
        return id;
    }
}
