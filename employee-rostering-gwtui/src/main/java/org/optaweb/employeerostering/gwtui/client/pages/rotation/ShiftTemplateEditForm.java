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
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.RotationRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.ShiftTemplate;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_INVALIDATE;;

@Templated
public class ShiftTemplateEditForm extends AbstractFormPopup {

    @DataField("from")
    private RotationTimeSelector from;

    @DataField("to")
    private RotationTimeSelector to;

    @DataField("spot")
    private ListBox spotSelect; //FIXME: Don't use GWT widget

    @DataField("employee")
    private ListBox employeeSelect; //FIXME: Don't use GWT widget

    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    private TenantStore tenantStore;

    private EventManager eventManager;

    private TranslationService translationService;

    private ShiftTemplateGridObject shiftTemplateGridObject;

    private static final String SELECTED_CLASS = "selected";

    @Inject
    public ShiftTemplateEditForm(PopupFactory popupFactory, HTMLDivElement root, @Named("span") HTMLElement popupTitle, HTMLButtonElement closeButton,
                                 HTMLButtonElement cancelButton, RotationTimeSelector from, RotationTimeSelector to,
                                 ListBox spotSelect, ListBox employeeSelect,
                                 HTMLButtonElement deleteButton, HTMLButtonElement applyButton,
                                 TenantStore tenantStore, EventManager eventManager,
                                 TranslationService translationService) {
        super(popupFactory, root, popupTitle, closeButton, cancelButton);
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
        this.shiftTemplateGridObject = (ShiftTemplateGridObject) shiftTemplateGridObject;
        shiftTemplateGridObject.getElement().classList.add(SELECTED_CLASS);
        final ShiftTemplateView template = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();
        setup(template);
        showFor(shiftTemplateGridObject);
    }

    public void createNewShiftTemplate() {
        setup(new ShiftTemplateView(tenantStore.getCurrentTenantId(), new ShiftTemplate(tenantStore.getCurrentTenantId(), new Spot(), 0, LocalTime.MIDNIGHT, 1, LocalTime.MIDNIGHT)));
        spotSelect.setEnabled(true);
        deleteButton.remove();
        setTitle(translationService.format(I18nKeys.ShiftRosterToolbar_createShift));
        show();
    }

    private void setup(ShiftTemplateView shiftTemplateView) {
        employeeSelect.clear();
        employeeSelect.addItem(translationService.format(I18nKeys.Shift_unassigned), "-1");

        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            Optional<Spot> mySpot = spots.stream().filter(s -> s.getId().equals(shiftTemplateView.getSpotId())).findAny();
            if (mySpot.isPresent()) {
                spotSelect.setSelectedIndex(spots.indexOf(mySpot.get()));
            } else {
                spotSelect.setSelectedIndex(0);
            }
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            Optional<Employee> myEmployee = employees.stream().filter(s -> s.getId().equals(shiftTemplateView.getRotationEmployeeId())).findAny();
            if (myEmployee.isPresent()) {
                employeeSelect.setSelectedIndex(employees.indexOf(myEmployee.get()) + 1);
            } else {
                employeeSelect.setSelectedIndex(0);
            }
        }));

        long startDayOffset = shiftTemplateView.getDurationBetweenReferenceAndStart().toDays();
        from.setDayOffset((int) startDayOffset);
        from.setTime(LocalTime.MIDNIGHT.plusSeconds(shiftTemplateView.getDurationBetweenReferenceAndStart().minusDays(startDayOffset).getSeconds()));
        long endDayOffset = shiftTemplateView.getDurationBetweenReferenceAndStart().plus(shiftTemplateView.getShiftTemplateDuration()).toDays();
        to.setDayOffset((int) endDayOffset);
        to.setTime(LocalTime.MIDNIGHT.plusSeconds(shiftTemplateView.getDurationBetweenReferenceAndStart().plus(shiftTemplateView.getShiftTemplateDuration()).minusDays(endDayOffset).getSeconds()));
    }

    @Override
    protected void onClose() {
        if (shiftTemplateGridObject != null) {
            shiftTemplateGridObject.getElement().classList.remove(SELECTED_CLASS);
        }
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        if (shiftTemplateGridObject != null) {

            final ShiftTemplateView template = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();

            final Long oldEmployee = template.getRotationEmployeeId();
            final Duration oldDurationFromReference = template.getDurationBetweenRotationStartAndTemplateStart();
            final Duration oldDurationOfTemplate = template.getShiftTemplateDuration();

            try {
                template.setRotationEmployeeId(parseId(employeeSelect.getSelectedValue()));
                Duration newDurationFromReference = Duration.ofDays(from.getDayOffset()).plusSeconds(from.getTime().toSecondOfDay());
                Duration newDurationOfTemplate;
                if (to.getDayOffset() >= from.getDayOffset()) {
                    newDurationOfTemplate = Duration.ofDays(to.getDayOffset()).plusSeconds(to.getTime().toSecondOfDay()).minus(newDurationFromReference);
                } else {
                    newDurationOfTemplate = Duration.ofDays(to.getRotationLength()).minus(newDurationFromReference).plusDays(to.getDayOffset()).plusSeconds(to.getTime().toSecondOfDay());
                }
                template.setDurationBetweenRotationStartAndTemplateStart(newDurationFromReference);
                template.setShiftTemplateDuration(newDurationOfTemplate);
            } catch (ValidationException invalidField) {
                template.setRotationEmployeeId(oldEmployee);
                template.setDurationBetweenRotationStartAndTemplateStart(oldDurationFromReference);
                template.setShiftTemplateDuration(oldDurationOfTemplate);
                return;
            }

            RotationRestServiceBuilder.updateShiftTemplate(template.getTenantId(), template, FailureShownRestCallback.onSuccess((final ShiftTemplateView updatedShiftTemplate) -> {
                shiftTemplateGridObject.getShiftTemplateModel().withShiftTemplateView(updatedShiftTemplate);
                shiftTemplateGridObject.getElement().classList.remove(SELECTED_CLASS);
                eventManager.fireEvent(ROTATION_INVALIDATE);
                hide();
            }).onFailure(i -> {
                template.setRotationEmployeeId(oldEmployee);
                template.setDurationBetweenRotationStartAndTemplateStart(oldDurationFromReference);
                template.setShiftTemplateDuration(oldDurationOfTemplate);
            }).onError(i -> {
                template.setRotationEmployeeId(oldEmployee);
                template.setDurationBetweenRotationStartAndTemplateStart(oldDurationFromReference);
                template.setShiftTemplateDuration(oldDurationOfTemplate);
            }));
        } else {
            final ShiftTemplateView template = new ShiftTemplateView();

            template.setRotationEmployeeId(parseId(employeeSelect.getSelectedValue()));
            Duration newDurationFromReference = Duration.ofDays(from.getDayOffset()).plusSeconds(from.getTime().toSecondOfDay());
            Duration newDurationOfTemplate;
            if (to.getDayOffset() >= from.getDayOffset()) {
                newDurationOfTemplate = Duration.ofDays(to.getDayOffset()).plusSeconds(to.getTime().toSecondOfDay()).minus(newDurationFromReference);
            } else {
                newDurationOfTemplate = Duration.ofDays(to.getRotationLength()).minus(newDurationFromReference).plusDays(to.getDayOffset()).plusSeconds(to.getTime().toSecondOfDay());
            }
            template.setDurationBetweenRotationStartAndTemplateStart(newDurationFromReference);
            template.setShiftTemplateDuration(newDurationOfTemplate);
            template.setTenantId(tenantStore.getCurrentTenantId());

            RotationRestServiceBuilder.addShiftTemplate(tenantStore.getCurrentTenantId(), template, FailureShownRestCallback.onSuccess(v -> {
                hide();
                eventManager.fireEvent(ROTATION_INVALIDATE);
            }));
        }

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        final ShiftTemplateView shiftTemplateView = shiftTemplateGridObject.getShiftTemplateModel().getShiftTemplateView();

        RotationRestServiceBuilder.removeShiftTemplate(shiftTemplateView.getTenantId(), shiftTemplateView.getId(), FailureShownRestCallback.onSuccess(v -> {
            shiftTemplateGridObject.getLane().removeGridObject(shiftTemplateGridObject.getShiftTemplateModel());
            hide();
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
