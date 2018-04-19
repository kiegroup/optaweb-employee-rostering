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

package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.employeeroster;

import java.time.LocalDateTime;
import java.util.Arrays;

import javax.inject.Inject;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
public class EmployeeAvailabilityBlobPopoverContent implements IsElement {

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
    @DataField("employee")
    private ListBox employeeSelect; //FIXME: Don't use GWT widget

    @Inject
    @DataField("availability")
    private ListBox availabilitySelect; //FIXME: Don't use GWT widget

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

    private FormPopup formPopup;

    private EmployeeAvailabilityGridObject employeeAvailabilityGridObject;

    public void init(final EmployeeAvailabilityGridObject employeeAvailabilityGridObject) {

        this.employeeAvailabilityGridObject = employeeAvailabilityGridObject;
        employeeAvailabilityGridObject.getElement().classList.add("selected");
        final EmployeeAvailabilityView availabilityView = employeeAvailabilityGridObject.getEmployeeAvailabilityView();

        employeeSelect.clear();
        availabilitySelect.clear();
        employeeSelect.addItem("Unassigned", "-1"); //FIXME: i18n

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            employeeSelect.setSelectedIndex(employees.indexOf(employeeAvailabilityGridObject.getEmployee()) + 1);
        }));

        // TODO: Indifferent = NULL case
        Arrays.asList(EmployeeAvailabilityState.values()).forEach((e) -> {
            availabilitySelect.addItem(e.toString());
        });
        int availabilityIndex = Arrays.asList(EmployeeAvailabilityState.values()).indexOf(availabilityView.getState());
        availabilitySelect.setSelectedIndex((availabilityIndex > -1) ? availabilityIndex : 0);
        employeeSelect.setEnabled(false);

        from.setValue(availabilityView.getStartDateTime());
        to.setValue(availabilityView.getEndDateTime());

        formPopup = FormPopup.getFormPopup(this);
        formPopup.showFor(employeeAvailabilityGridObject);
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        employeeAvailabilityGridObject.getElement().classList.remove("selected");
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        employeeAvailabilityGridObject.getElement().classList.remove("selected");
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        final EmployeeAvailabilityView availabilityView = employeeAvailabilityGridObject.getEmployeeAvailabilityView();

        final EmployeeAvailabilityState oldState = availabilityView.getState();
        final LocalDateTime oldStartDateTime = availabilityView.getStartDateTime();
        final LocalDateTime oldEndDateTime = availabilityView.getEndDateTime();

        try {
            availabilityView.setState(EmployeeAvailabilityState.values()[availabilitySelect.getSelectedIndex()]);
            availabilityView.setStartDateTime(from.getValue());
            availabilityView.setEndDateTime(to.getValue());
        } catch (ValidationException invalidField) {
            availabilityView.setState(oldState);
            availabilityView.setStartDateTime(oldStartDateTime);
            availabilityView.setEndDateTime(oldEndDateTime);
            return;
        }

        EmployeeRestServiceBuilder.updateEmployeeAvailability(availabilityView.getTenantId(), availabilityView, onSuccess((EmployeeAvailabilityView updatedView) -> {
            employeeAvailabilityGridObject.withEmployeeAvailabilityView(updatedView);
            employeeAvailabilityGridObject.getElement().classList.remove("selected");
            formPopup.hide();
        }).onFailure(i -> {
            availabilityView.setState(oldState);
            availabilityView.setStartDateTime(oldStartDateTime);
            availabilityView.setEndDateTime(oldEndDateTime);
        }).onError(i -> {
            availabilityView.setState(oldState);
            availabilityView.setStartDateTime(oldStartDateTime);
            availabilityView.setEndDateTime(oldEndDateTime);
        }));

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {

        final EmployeeAvailabilityView availabilityView = employeeAvailabilityGridObject.getEmployeeAvailabilityView();

        EmployeeRestServiceBuilder.removeEmployeeAvailability(availabilityView.getTenantId(), availabilityView.getId(), onSuccess((Boolean v) -> {
            employeeAvailabilityGridObject.getLane().removeGridObject(employeeAvailabilityGridObject);
            formPopup.hide();
        }));

        e.stopPropagation();
    }

}
