/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.AbstractFormPopup;
import org.optaweb.employeerostering.gwtui.client.common.CallbackFactory;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;

@Templated
public class ShiftEditForm extends AbstractFormPopup {

    @DataField("from")
    private LocalDateTimePicker from;

    @DataField("to")
    private LocalDateTimePicker to;

    @DataField("spot")
    private ListBox spotSelect; //TODO: Replace with widget that has search capabilities

    @DataField("employee")
    private ListBox employeeSelect; //TODO: Replace with widget that has search capabilities

    @DataField("pinned")
    private HTMLInputElement pinned;

    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    private TenantStore tenantStore;

    private EventManager eventManager;

    private TranslationService translationService;

    private CallbackFactory callbackFactory;

    private NotificationFactory notificationFactory;

    private ShiftGridObject shiftGridObject;

    @Inject
    public ShiftEditForm(PopupFactory popupFactory,
                         CallbackFactory callbackFactory,
                         NotificationFactory notificationFactory,
                         HTMLDivElement root,
                         @Named("span") HTMLElement popupTitle,
                         HTMLButtonElement closeButton,
                         HTMLButtonElement cancelButton,
                         LocalDateTimePicker from,
                         LocalDateTimePicker to,
                         ListBox spotSelect,
                         ListBox employeeSelect,
                         HTMLInputElement pinned,
                         HTMLButtonElement deleteButton,
                         HTMLButtonElement applyButton,
                         TenantStore tenantStore,
                         EventManager eventManager,
                         TranslationService translationService) {
        super(popupFactory, root, popupTitle, closeButton, cancelButton);
        this.from = from;
        this.to = to;
        this.spotSelect = spotSelect;
        this.employeeSelect = employeeSelect;
        this.pinned = pinned;
        this.deleteButton = deleteButton;
        this.applyButton = applyButton;
        this.tenantStore = tenantStore;
        this.eventManager = eventManager;
        this.translationService = translationService;
        this.callbackFactory = callbackFactory;
        this.notificationFactory = notificationFactory;
    }

    public void init(final ShiftGridObject shiftGridObject) {

        this.shiftGridObject = (ShiftGridObject) shiftGridObject;
        shiftGridObject.setSelected(true);
        final ShiftView shift = shiftGridObject.getShiftView();
        setup(shift);
        showFor(shiftGridObject);
    }

    public void createNewShift() {
        setup(new ShiftView(tenantStore.getCurrentTenantId(), new Spot(), LocalDateTime.now(), LocalDateTime.now().plusHours(9)));
        spotSelect.setEnabled(true);
        deleteButton.remove();
        setTitle(translationService.format(I18nKeys.ShiftRosterToolbar_createShift));
        show();
    }

    private void setup(ShiftView shiftView) {
        employeeSelect.clear();
        employeeSelect.addItem(translationService.format(I18nKeys.Shift_unassigned), "-1");

        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), callbackFactory.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            if (shiftView.getSpotId() != null) {
                spotSelect.setSelectedIndex(spots.indexOf(shiftGridObject.getSpot()));
            } else {
                spotSelect.setSelectedIndex(0);
            }
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), callbackFactory.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            if (shiftView.getEmployeeId() != null) {
                employeeSelect.setSelectedIndex((shiftGridObject.getEmployee() == null) ?
                                                        0 : employees.indexOf(shiftGridObject.getEmployee()) + 1);
            } else {
                employeeSelect.setSelectedIndex(0);
            }
        }));

        from.setValue(shiftView.getStartDateTime());
        to.setValue(shiftView.getEndDateTime());
        pinned.checked = shiftView.isPinnedByUser();
    }

    @Override
    protected void onClose() {
        if (shiftGridObject != null) {
            shiftGridObject.setSelected(false);
        }
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        if (shiftGridObject != null) {

            final ShiftView shiftView = shiftGridObject.getShiftView();

            if (updateShiftFromWidgetsIfValid(shiftView)) {

                ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView, callbackFactory.onSuccess((final ShiftView updatedShift) -> {
                    shiftGridObject.withShiftView(updatedShift);
                    shiftGridObject.setSelected(false);
                    eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
                    hide();
                }));
            }
        } else {
            final ShiftView shiftView = new ShiftView();
            shiftView.setTenantId(tenantStore.getCurrentTenantId());

            if (updateShiftFromWidgetsIfValid(shiftView)) {
                ShiftRestServiceBuilder.addShift(tenantStore.getCurrentTenantId(), shiftView, callbackFactory.onSuccess(v -> {
                    hide();
                    eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
                }));
            }
        }

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        final ShiftView shiftView = shiftGridObject.getShiftView();

        ShiftRestServiceBuilder.removeShift(shiftView.getTenantId(), shiftView.getId(), callbackFactory.onSuccess(success -> {
            if (success) {
                shiftGridObject.getLane().removeGridObject(shiftGridObject);
                hide();
            }
        }));
        e.stopPropagation();
    }

    public boolean updateShiftFromWidgetsIfValid(ShiftView shiftView) {
        final boolean oldLockedByUser = shiftView.isPinnedByUser();
        final Long oldEmployee = shiftView.getEmployeeId();
        final Long oldSpot = shiftView.getSpotId();
        final LocalDateTime oldStartDateTime = shiftView.getStartDateTime();
        final LocalDateTime oldEndDateTime = shiftView.getEndDateTime();

        try {
            if (!from.reportValidity()) {
                throw new ValidationException("Shift's start time need to be set.");
            }
            if (!to.reportValidity()) {
                throw new ValidationException("Shift's end time need to be set.");
            }

            shiftView.setPinnedByUser(pinned.checked);
            shiftView.setSpotId(parseId(spotSelect.getSelectedValue()));
            shiftView.setEmployeeId(parseId(employeeSelect.getSelectedValue()));
            shiftView.setStartDateTime(from.getValue());
            shiftView.setEndDateTime(to.getValue());

            if (!shiftView.getStartDateTime().isBefore(shiftView.getEndDateTime())) {
                throw new ValidationException("Shift's end time need to be after shift's start time.");
            }
            return true;
        } catch (ValidationException invalidField) {
            shiftView.setPinnedByUser(oldLockedByUser);
            shiftView.setSpotId(oldSpot);
            shiftView.setEmployeeId(oldEmployee);
            shiftView.setStartDateTime(oldStartDateTime);
            shiftView.setEndDateTime(oldEndDateTime);
            notificationFactory.showError(invalidField);
            return false;
        }
    }

    private Long parseId(String text) {
        Long id = Long.parseLong(text);
        if (id < 0) {
            return null;
        }
        return id;
    }
}
