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

package org.optaweb.employeerostering.gwtui.client.pages.rotation;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
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
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.RotationRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_INVALIDATE;

@Templated
public class ShiftTemplateEditForm extends AbstractFormPopup {

    @DataField("from")
    private RotationTimeSelector from;

    @DataField("to")
    private RotationTimeSelector to;

    @DataField("spot")
    private ListBox spotSelect; //TODO: Replace with widget that has search capabilities

    @DataField("employee")
    private ListBox employeeSelect; //TODO: Replace with widget that has search capabilities

    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    private TenantStore tenantStore;

    private EventManager eventManager;

    private TranslationService translationService;

    private CallbackFactory callbackFactory;

    private NotificationFactory notificationFactory;

    private ShiftTemplateGridObject shiftTemplateGridObject;

    @Inject
    public ShiftTemplateEditForm(PopupFactory popupFactory,
                                 CallbackFactory callbackFactory,
                                 NotificationFactory notificationFactory,
                                 HTMLDivElement root,
                                 @Named("span") HTMLElement popupTitle,
                                 HTMLButtonElement closeButton,
                                 HTMLButtonElement cancelButton,
                                 RotationTimeSelector from,
                                 RotationTimeSelector to,
                                 ListBox spotSelect,
                                 ListBox employeeSelect,
                                 HTMLButtonElement deleteButton,
                                 HTMLButtonElement applyButton,
                                 TenantStore tenantStore,
                                 EventManager eventManager,
                                 TranslationService translationService) {
        super(popupFactory, root, popupTitle, closeButton, cancelButton);
        this.callbackFactory = callbackFactory;
        this.notificationFactory = notificationFactory;
        this.from = from;
        this.to = to;
        this.spotSelect = spotSelect;
        this.employeeSelect = employeeSelect;
        this.deleteButton = deleteButton;
        this.applyButton = applyButton;
        this.tenantStore = tenantStore;
        this.eventManager = eventManager;
        this.translationService = translationService;
    }

    public void init(final ShiftTemplateGridObject shiftTemplateGridObject) {
        this.shiftTemplateGridObject = shiftTemplateGridObject;
        shiftTemplateGridObject.setSelected(true);
        final ShiftTemplateView template = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();
        setup(template);
        showFor(shiftTemplateGridObject);
    }

    private void setup(ShiftTemplateView shiftTemplateView) {
        employeeSelect.clear();
        employeeSelect.addItem(translationService.format(I18nKeys.Shift_unassigned), "-1");

        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), callbackFactory.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            Optional<Spot> mySpot = spots.stream().filter(s -> s.getId().equals(shiftTemplateView.getSpotId())).findAny();
            if (mySpot.isPresent()) {
                spotSelect.setSelectedIndex(spots.indexOf(mySpot.get()));
            } else {
                spotSelect.setSelectedIndex(0);
            }
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), callbackFactory.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            Optional<Employee> myEmployee = employees.stream().filter(s -> s.getId()
                    .equals(shiftTemplateView
                                    .getRotationEmployeeId())).findAny();
            if (myEmployee.isPresent()) {
                employeeSelect.setSelectedIndex(employees.indexOf(myEmployee.get()) + 1);
            } else {
                employeeSelect.setSelectedIndex(0);
            }
        }));

        long startDayOffset = shiftTemplateView.getDurationBetweenReferenceAndStart().toDays();
        from.setDayOffset((int) startDayOffset);
        from.setTime(LocalTime.MIDNIGHT.plusSeconds(shiftTemplateView.getDurationBetweenReferenceAndStart()
                                                            .minusDays(startDayOffset).getSeconds()));
        long endDayOffset = shiftTemplateView.getDurationBetweenReferenceAndStart()
                .plus(shiftTemplateView.getShiftTemplateDuration()).toDays();
        to.setDayOffset((int) endDayOffset);
        to.setTime(LocalTime.MIDNIGHT.plusSeconds(shiftTemplateView.getDurationBetweenReferenceAndStart().
                plus(shiftTemplateView.getShiftTemplateDuration())
                                                          .minusDays(endDayOffset).getSeconds()));
    }

    @Override
    protected void onClose() {
        if (shiftTemplateGridObject != null) {
            shiftTemplateGridObject.setSelected(false);
        }
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        if (shiftTemplateGridObject != null) {

            final ShiftTemplateView template = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();

            if (updateShiftTemplateFromWidgetsIfValid(template)) {
                RotationRestServiceBuilder.updateShiftTemplate(template.getTenantId(), template, callbackFactory.onSuccess((final ShiftTemplateView updatedShiftTemplate) -> {
                    shiftTemplateGridObject.getShiftTemplateModel().withShiftTemplateView(updatedShiftTemplate);
                    shiftTemplateGridObject.setSelected(false);
                    eventManager.fireEvent(ROTATION_INVALIDATE);
                    hide();
                }));
            }
        } else {
            throw new IllegalStateException("shiftTemplateGridObject is null.");
        }

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        final ShiftTemplateView shiftTemplateView = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();

        RotationRestServiceBuilder.removeShiftTemplate(shiftTemplateView.getTenantId(), shiftTemplateView.getId(), callbackFactory.onSuccess(success -> {
            if (success) {
                shiftTemplateGridObject.getLane().removeGridObject(shiftTemplateGridObject.getShiftTemplateModel());
                hide();
            }
        }));
        e.stopPropagation();
    }

    public boolean updateShiftTemplateFromWidgetsIfValid(ShiftTemplateView template) {
        final Long oldSpot = template.getSpotId();
        final Long oldEmployee = template.getRotationEmployeeId();
        final Duration oldDurationFromReference = template.getDurationBetweenRotationStartAndTemplateStart();
        final Duration oldDurationOfTemplate = template.getShiftTemplateDuration();

        try {
            if (!from.reportValidity()) {
                throw new ValidationException("Rotation shift's start day offset/time is invalid.");
            }
            if (!to.reportValidity()) {
                throw new ValidationException("Rotation shift's end day offset/time is invalid.");
            }
            template.setSpotId(parseId(spotSelect.getSelectedValue()));
            template.setRotationEmployeeId(parseId(employeeSelect.getSelectedValue()));
            Duration newDurationFromReference = fromDayOffsetAndTimeToDuration(from.getDayOffset(), from.getTime());
            Duration newDurationOfTemplate = getDurationOfShiftTemplate(from.getDayOffset(), from.getTime(),
                                                                        to.getDayOffset(), to.getTime(),
                                                                        to.getRotationLength());
            template.setDurationBetweenRotationStartAndTemplateStart(newDurationFromReference);
            template.setShiftTemplateDuration(newDurationOfTemplate);
            return true;
        } catch (ValidationException invalidField) {
            template.setSpotId(oldSpot);
            template.setRotationEmployeeId(oldEmployee);
            template.setDurationBetweenRotationStartAndTemplateStart(oldDurationFromReference);
            template.setShiftTemplateDuration(oldDurationOfTemplate);
            notificationFactory.showError(invalidField);
            return false;
        }
    }

    public Duration fromDayOffsetAndTimeToDuration(int dayOffset, LocalTime time) {
        return Duration.ofDays(dayOffset).plusSeconds(time.toSecondOfDay());
    }

    public Duration getDurationOfShiftTemplate(int startDayOffset, LocalTime startTime,
                                               int endDayOffset, LocalTime endTime,
                                               int rotationLength) {
        Duration durationFromStart = fromDayOffsetAndTimeToDuration(startDayOffset, startTime);
        if (endDayOffset >= startDayOffset) {
            return Duration.ofDays(endDayOffset).plusSeconds(endTime.toSecondOfDay())
                    .minus(durationFromStart);
        } else {
            return Duration.ofDays(rotationLength).minus(durationFromStart)
                    .plusDays(endDayOffset).plusSeconds(endTime.toSecondOfDay());
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
