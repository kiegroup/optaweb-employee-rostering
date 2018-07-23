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

package org.optaweb.employeerostering.gwtui.client.viewport.shiftroster;

import java.time.LocalDateTime;
import javax.inject.Inject;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.*;

@Templated
public class ShiftGridObjectPopup implements IsElement {

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

    @Inject
    private PopupFactory popupFactory;

    @Inject
    private EventManager eventManager;

    private FormPopup formPopup;

    private ShiftGridObject shiftGridObject;

    public void init(final ShiftGridObject shiftGridObject) {

        this.shiftGridObject = (ShiftGridObject) shiftGridObject;
        shiftGridObject.getElement().classList.add("selected");
        final ShiftView shift = shiftGridObject.getShiftView();

        employeeSelect.clear();
        employeeSelect.addItem("Unassigned", "-1"); //FIXME: i18n

        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            spotSelect.setSelectedIndex(spots.indexOf(shiftGridObject.getSpot()));
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            employeeSelect.setSelectedIndex((shiftGridObject.getEmployee() == null) ? 0 : employees.indexOf(shiftGridObject.getEmployee()) + 1);
        }));

        from.setValue(shift.getStartDateTime());
        to.setValue(shift.getEndDateTime());
        pinned.checked = shift.isPinnedByUser();

        popupFactory.getFormPopup(this).ifPresent((fp) -> {
            formPopup = fp;
            formPopup.showFor(shiftGridObject);
        });

    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        shiftGridObject.getElement().classList.remove("selected");
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        shiftGridObject.getElement().classList.remove("selected");
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {

        final ShiftView shiftView = shiftGridObject.getShiftView();

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

        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView, FailureShownRestCallback.onSuccess((final ShiftView updatedShift) -> {
            shiftGridObject.withShiftView(updatedShift);
            shiftGridObject.getElement().classList.remove("selected");
            eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
            formPopup.hide();
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
        final ShiftView shiftView = shiftGridObject.getShiftView();

        ShiftRestServiceBuilder.removeShift(shiftView.getTenantId(), shiftView.getId(), FailureShownRestCallback.onSuccess(v -> {
            shiftGridObject.getLane().removeGridObject(shiftGridObject);
            formPopup.hide();
        }));
        e.stopPropagation();
    }

    private Long parseId(String text) {
        Long id = Long.parseLong(text);
        if (id < 0) {
            return null;
        }
        return id;
    }
}
