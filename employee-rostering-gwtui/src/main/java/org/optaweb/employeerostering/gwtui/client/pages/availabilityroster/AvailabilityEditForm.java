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

package org.optaweb.employeerostering.gwtui.client.pages.availabilityroster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;

@Templated
public class AvailabilityEditForm
        implements
        IsElement {

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("popup-title")
    @Named("span")
    private HTMLElement popupTitle;

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

    @Inject
    private PopupFactory popupFactory;

    @Inject
    private TranslationService translationService;

    @Inject
    private EventManager eventManager;

    private FormPopup formPopup;

    private AvailabilityGridObject availabilityGridObject;

    private List<Employee> employeeList;

    private static final List<EmployeeAvailabilityState> EMPLOYEE_AVAILABILITY_STATE_LIST = Arrays.asList(EmployeeAvailabilityState.DESIRED,
                                                                                                          EmployeeAvailabilityState.UNDESIRED,
                                                                                                          EmployeeAvailabilityState.UNAVAILABLE);

    public void init(final AvailabilityGridObject availabilityGridObject) {

        this.availabilityGridObject = availabilityGridObject;
        availabilityGridObject.getElement().classList.add("selected");
        final EmployeeAvailabilityView availabilityView = availabilityGridObject.getEmployeeAvailabilityView();

        setup(availabilityView);

        popupFactory.getFormPopup(this).ifPresent((fp) -> {
            formPopup = fp;
            formPopup.showFor(availabilityGridObject);
        });
    }

    public void createNewAvailability() {
        setup(new EmployeeAvailabilityView(tenantStore.getCurrentTenantId(),
                                           new Employee(),
                                           LocalDate.now().atTime(0, 0),
                                           LocalDate.now().plusDays(1).atTime(0, 0),
                                           EmployeeAvailabilityState.UNAVAILABLE));
        employeeSelect.setEnabled(true);
        deleteButton.remove();
        popupTitle.innerHTML = translationService.format(I18nKeys.AvailabilityRosterToolbar_createAvailability);

        popupFactory.getFormPopup(this).ifPresent((fp) -> {
            formPopup = fp;
            formPopup.center();
        });
    }

    private void setup(EmployeeAvailabilityView availabilityView) {
        employeeSelect.clear();
        availabilitySelect.clear();

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            employeeList = employees;
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            if (availabilityView.getEmployeeId() != null) {
                employeeSelect.setSelectedIndex(employeeList.stream().map(e -> e.getId()).collect(Collectors.toList())
                                                        .indexOf(availabilityView.getEmployeeId()));
            } else {
                employeeSelect.setSelectedIndex(0);
            }
        }));

        availabilitySelect.addItem(translationService.format(I18nKeys.EmployeeAvailabilityState_DESIRED));
        availabilitySelect.addItem(translationService.format(I18nKeys.EmployeeAvailabilityState_UNDESIRED));
        availabilitySelect.addItem(translationService.format(I18nKeys.EmployeeAvailabilityState_UNAVAILABLE));

        int availabilityIndex = EMPLOYEE_AVAILABILITY_STATE_LIST.indexOf(availabilityView.getState());
        availabilitySelect.setSelectedIndex((availabilityIndex > -1) ? availabilityIndex : 0);
        employeeSelect.setEnabled(false);

        from.setValue(availabilityView.getStartDateTime());
        to.setValue(availabilityView.getEndDateTime());
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        if (availabilityGridObject != null) {
            availabilityGridObject.getElement().classList.remove("selected");
        }
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        if (availabilityGridObject != null) {
            availabilityGridObject.getElement().classList.remove("selected");
        }
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        if (availabilityGridObject != null) {
            final EmployeeAvailabilityView availabilityView = availabilityGridObject.getEmployeeAvailabilityView();

            final EmployeeAvailabilityState oldState = availabilityView.getState();
            final Long oldEmployeeId = availabilityView.getEmployeeId();
            final LocalDateTime oldStartDateTime = availabilityView.getStartDateTime();
            final LocalDateTime oldEndDateTime = availabilityView.getEndDateTime();

            try {
                availabilityView.setState(EMPLOYEE_AVAILABILITY_STATE_LIST.get(availabilitySelect.getSelectedIndex()));
                availabilityView.setEmployeeId(employeeList.get(employeeSelect.getSelectedIndex()).getId());
                availabilityView.setStartDateTime(from.getValue());
                availabilityView.setEndDateTime(to.getValue());
            } catch (ValidationException invalidField) {
                availabilityView.setState(oldState);
                availabilityView.setEmployeeId(oldEmployeeId);
                availabilityView.setStartDateTime(oldStartDateTime);
                availabilityView.setEndDateTime(oldEndDateTime);
                return;
            }

            EmployeeRestServiceBuilder.updateEmployeeAvailability(availabilityView.getTenantId(), availabilityView, FailureShownRestCallback.onSuccess((EmployeeAvailabilityView updatedView) -> {
                availabilityGridObject.withEmployeeAvailabilityView(updatedView);
                availabilityGridObject.getElement().classList.remove("selected");
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
        } else {
            final EmployeeAvailabilityView availabilityView = new EmployeeAvailabilityView(tenantStore.getCurrentTenantId(), employeeList.get(employeeSelect.getSelectedIndex()), from.getValue(), to.getValue(),
                                                                                           EMPLOYEE_AVAILABILITY_STATE_LIST.get(availabilitySelect.getSelectedIndex()));
            EmployeeRestServiceBuilder.addEmployeeAvailability(tenantStore.getCurrentTenantId(), availabilityView, FailureShownRestCallback.onSuccess(v -> {
                formPopup.hide();
                eventManager.fireEvent(Event.AVAILABILITY_ROSTER_INVALIDATE);
            }));
        }

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {

        final EmployeeAvailabilityView availabilityView = availabilityGridObject.getEmployeeAvailabilityView();

        EmployeeRestServiceBuilder.removeEmployeeAvailability(availabilityView.getTenantId(), availabilityView.getId(), FailureShownRestCallback.onSuccess((Boolean v) -> {
            availabilityGridObject.getLane().removeGridObject(availabilityGridObject);
            formPopup.hide();
        }));

        e.stopPropagation();
    }
}
