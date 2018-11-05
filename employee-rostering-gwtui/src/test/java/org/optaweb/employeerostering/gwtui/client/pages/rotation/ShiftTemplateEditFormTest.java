/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.optaweb.employeerostering.gwtui.client.common.CallbackFactory;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ShiftTemplateEditFormTest {

    @Mock
    private RotationTimeSelector from;

    @Mock
    private RotationTimeSelector to;

    @Mock
    private ListBox spotSelect;

    @Mock
    private ListBox employeeSelect;

    @Mock
    private HTMLButtonElement deleteButton;

    @Mock
    private HTMLButtonElement applyButton;

    @Mock
    private TenantStore tenantStore;

    @Mock
    private EventManager eventManager;

    @Mock
    private TranslationService translationService;

    @Mock
    private CallbackFactory callbackFactory;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private HTMLDivElement root;

    @Mock
    private HTMLElement popupTitle;

    @Mock
    private HTMLButtonElement closeButton;

    @Mock
    private HTMLButtonElement cancelButton;

    @Mock
    private PopupFactory popupFactory;

    @Mock
    private FormPopup formPopup;

    @Mock
    private ShiftTemplateGridObject mockShiftTemplateGridObject;

    @Mock
    private ShiftTemplateModel mockShiftTemplateModel;

    @Mock
    private Lane<LocalDateTime, RotationMetadata> mockLane;

    private ShiftTemplateEditForm testedShiftTemplateEditForm;

    private Queue<Object> restCallbackAnswers;

    private List<Spot> spotList;

    private List<Employee> employeeList;

    private ShiftTemplateView shiftTemplateView;

    private static final int TENANT_ID = 0;

    private static final int NUM_OF_SPOTS = 4;

    private static final int NUM_OF_EMPLOYEES = 4;

    private static final int UNASSIGNED_EMPLOYEE = 0;

    private static final int SELECTED_SPOT = 2; // A random index between 0 and (NUM_OF_SPOTS - 1)

    private static final int EARILER_DAY_OFFSET = 2;
    private static final int LATER_DAY_OFFSET = 3;

    private static final LocalTime EARILER_LOCAL_TIME = LocalTime.of(9, 0);
    private static final LocalTime LATER_LOCAL_TIME = LocalTime.of(18, 0);

    private static final Duration DURATION_FROM_ROTATION_START = Duration.ofDays(EARILER_DAY_OFFSET)
            .plusSeconds(EARILER_LOCAL_TIME.toSecondOfDay());
    private static final Duration DURATION_OF_SHIFT = Duration.ofDays(LATER_DAY_OFFSET)
            .plusSeconds(LATER_LOCAL_TIME.toSecondOfDay())
            .minus(DURATION_FROM_ROTATION_START);

    @Before
    public void setUp() throws Exception {
        restCallbackAnswers = new LinkedList<>();
        spotList = getSpotList();
        employeeList = getEmployeeList();
        when(popupFactory.getFormPopup(any())).thenReturn(Optional.of(formPopup));
        when(callbackFactory.onSuccess(any())).thenAnswer(v -> {
            Consumer<Object> c = v.getArgument(0);
            c.accept(restCallbackAnswers.poll());
            return null;
        });
        testedShiftTemplateEditForm = spy(new ShiftTemplateEditForm(popupFactory,
                                                                    callbackFactory,
                                                                    notificationFactory,
                                                                    root,
                                                                    popupTitle,
                                                                    closeButton,
                                                                    cancelButton,
                                                                    from,
                                                                    to,
                                                                    spotSelect,
                                                                    employeeSelect,
                                                                    deleteButton,
                                                                    applyButton,
                                                                    tenantStore,
                                                                    eventManager,
                                                                    translationService));
        shiftTemplateView = new ShiftTemplateView(TENANT_ID, (long) SELECTED_SPOT,
                                                  DURATION_FROM_ROTATION_START,
                                                  DURATION_OF_SHIFT, null);
        when(mockShiftTemplateGridObject.getShiftTemplateModel()).thenReturn(mockShiftTemplateModel);
        when(mockShiftTemplateModel.getShiftTemplateView()).thenReturn(shiftTemplateView);
    }

    private List<Spot> getSpotList() {
        List<Spot> out = new ArrayList<>(NUM_OF_SPOTS);
        for (long i = 0; i < NUM_OF_SPOTS; i++) {
            Spot spot = new Spot(TENANT_ID, "Spot" + i, Collections.emptySet());
            spot.setId(i);
            out.add(spot);
        }
        return out;
    }

    private List<Employee> getEmployeeList() {
        List<Employee> out = new ArrayList<>(NUM_OF_EMPLOYEES);
        Contract mockContract = Mockito.mock(Contract.class);
        for (long i = 0; i < NUM_OF_EMPLOYEES; i++) {
            Employee employee = new Employee(NUM_OF_SPOTS, "Employee" + i, mockContract, Collections.emptySet());
            employee.setId(i);
            out.add(employee);
        }
        return out;
    }

    @Test
    public void testInit() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);

        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);
        verify(testedShiftTemplateEditForm).showFor(mockShiftTemplateGridObject);
        verify(mockShiftTemplateGridObject).setSelected(true);

        InOrder order = inOrder(spotSelect);
        for (Spot spot : spotList) {
            order.verify(spotSelect).addItem(spot.getName(), spot.getId().toString());
        }
        order.verify(spotSelect).setSelectedIndex(SELECTED_SPOT);

        order = inOrder(employeeSelect);
        for (Employee employee : employeeList) {
            order.verify(employeeSelect).addItem(employee.getName(), employee.getId().toString());
        }
        order.verify(employeeSelect).setSelectedIndex(UNASSIGNED_EMPLOYEE);

        verify(from).setDayOffset(EARILER_DAY_OFFSET);
        verify(from).setTime(EARILER_LOCAL_TIME);

        verify(to).setDayOffset(LATER_DAY_OFFSET);
        verify(to).setTime(LATER_LOCAL_TIME);
    }

    @Test
    public void testOnClose() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);

        testedShiftTemplateEditForm.onClose();
        verify(mockShiftTemplateGridObject).setSelected(false);
    }

    @Test
    public void testInvalidWidgetUpdateShiftFromWidgetsIfValid() {
        ShiftTemplateView oldShiftTemplateView = shiftTemplateView;

        // Clone the shift view since this one will may be modified
        ShiftTemplateView newShiftTemplateView = new ShiftTemplateView(TENANT_ID, (long) SELECTED_SPOT,
                                                                       DURATION_FROM_ROTATION_START,
                                                                       DURATION_OF_SHIFT, null);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.reportValidity()).thenReturn(false);
        when(to.reportValidity()).thenReturn(true);

        boolean out = testedShiftTemplateEditForm.updateShiftTemplateFromWidgetsIfValid(newShiftTemplateView);

        // Assert any changes are rolled back
        assertThat(newShiftTemplateView).isEqualTo(oldShiftTemplateView);
        assertThat(out).isFalse();
        verify(notificationFactory).showError(any());
    }

    @Test
    public void testValidUpdateShiftTemplateFromWidgetsIfValid() {
        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getDayOffset()).thenReturn(EARILER_DAY_OFFSET + 2);
        when(to.getDayOffset()).thenReturn(LATER_DAY_OFFSET + 2);
        when(from.getTime()).thenReturn(EARILER_LOCAL_TIME);
        when(to.getTime()).thenReturn(LATER_LOCAL_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);
        when(from.getRotationLength()).thenReturn(7);
        when(to.getRotationLength()).thenReturn(7);

        boolean out = testedShiftTemplateEditForm.updateShiftTemplateFromWidgetsIfValid(shiftTemplateView);

        verify(spotSelect).getSelectedValue();
        verify(employeeSelect).getSelectedValue();

        verify(from).reportValidity();
        verify(to).reportValidity();
        verify(from, atLeastOnce()).getDayOffset();
        verify(from, atLeastOnce()).getTime();
        verify(to, atLeastOnce()).getDayOffset();
        verify(to, atLeastOnce()).getTime();
        verify(testedShiftTemplateEditForm, atLeastOnce()).fromDayOffsetAndTimeToDuration(from.getDayOffset(), from.getTime());
        verify(testedShiftTemplateEditForm, atLeastOnce()).getDurationOfShiftTemplate(from.getDayOffset(), from.getTime(),
                                                                                      to.getDayOffset(), to.getTime(),
                                                                                      to.getRotationLength());

        assertThat(shiftTemplateView.getSpotId()).isEqualTo(spotList.get(NEW_SELECTED_SPOT).getId());
        assertThat(shiftTemplateView.getRotationEmployeeId()).isEqualTo(employeeList.get(NEW_SELECTED_EMPLOYEE).getId());
        assertThat(shiftTemplateView.getDurationBetweenRotationStartAndTemplateStart())
                .isEqualTo(DURATION_FROM_ROTATION_START.plusDays(2));
        assertThat(shiftTemplateView.getShiftTemplateDuration()).isEqualTo(DURATION_OF_SHIFT);
        assertThat(out).isTrue();
    }

    @Test
    public void testFromDayOffsetAndTimeToDuration() {
        assertThat(testedShiftTemplateEditForm.fromDayOffsetAndTimeToDuration(3, LocalTime.NOON))
                .isEqualTo(Duration.ofDays(3).plusSeconds(LocalTime.NOON.toSecondOfDay()));
    }

    @Test
    public void testGetDurationOfShiftTemplate() {
        assertThat(testedShiftTemplateEditForm.getDurationOfShiftTemplate(3, LocalTime.NOON, 5, LocalTime.NOON, 7))
                .isEqualTo(Duration.ofDays(2));
        assertThat(testedShiftTemplateEditForm.getDurationOfShiftTemplate(3, LocalTime.MIN, 5, LocalTime.NOON, 7))
                .isEqualTo(Duration.ofDays(2).plusSeconds(LocalTime.NOON.toSecondOfDay()));
        assertThat(testedShiftTemplateEditForm.getDurationOfShiftTemplate(3, LocalTime.NOON, 5, LocalTime.NOON, 7))
                .isEqualTo(Duration.ofDays(2));
        assertThat(testedShiftTemplateEditForm.getDurationOfShiftTemplate(6, LocalTime.NOON, 0, LocalTime.of(3, 0), 7))
                .isEqualTo(Duration.ofHours(15));
    }

    @Test
    public void testUpdateShiftTemplateFromForm() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getDayOffset()).thenReturn(EARILER_DAY_OFFSET + 2);
        when(to.getDayOffset()).thenReturn(LATER_DAY_OFFSET + 2);
        when(from.getTime()).thenReturn(EARILER_LOCAL_TIME);
        when(to.getTime()).thenReturn(LATER_LOCAL_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);
        ShiftTemplateView expectedShiftTemplateView = new ShiftTemplateView(TENANT_ID, (long) SELECTED_SPOT,
                                                                            DURATION_FROM_ROTATION_START.plusDays(2),
                                                                            DURATION_OF_SHIFT, (long) NEW_SELECTED_EMPLOYEE);

        restCallbackAnswers.add(expectedShiftTemplateView);

        MouseEvent mouseEvent = mock(MouseEvent.class);

        testedShiftTemplateEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftTemplateEditForm).updateShiftTemplateFromWidgetsIfValid(shiftTemplateView);
        verify(testedShiftTemplateEditForm).hide();
        verify(eventManager).fireEvent(EventManager.Event.ROTATION_INVALIDATE);

        ArgumentCaptor<ShiftTemplateView> updatedShiftViewCaptor = ArgumentCaptor.forClass(ShiftTemplateView.class);
        verify(mockShiftTemplateModel).withShiftTemplateView(updatedShiftViewCaptor.capture());

        ShiftTemplateView updatedShiftView = updatedShiftViewCaptor.getValue();
        assertThat(updatedShiftView).isEqualTo(expectedShiftTemplateView);
    }

    @Test
    public void testInvalidUpdateShiftTemplateFromForm() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.reportValidity()).thenReturn(false);
        when(to.reportValidity()).thenReturn(false);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftTemplateEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftTemplateEditForm).updateShiftTemplateFromWidgetsIfValid(shiftTemplateView);
        verify(eventManager, never()).fireEvent(any());
    }

    @Test
    public void testSuccessfulDelete() {
        when(mockShiftTemplateGridObject.getLane()).thenReturn(mockLane);

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        restCallbackAnswers.add(Boolean.TRUE);

        testedShiftTemplateEditForm.onDeleteButtonClick(mouseEvent);
        verify(mockLane).removeGridObject(mockShiftTemplateModel);
        verify(testedShiftTemplateEditForm).hide();
    }

    @Test
    public void testUnsuccessfulDelete() {
        when(mockShiftTemplateGridObject.getLane()).thenReturn(mockLane);

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftTemplateEditForm.init(mockShiftTemplateGridObject);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        restCallbackAnswers.add(Boolean.FALSE);

        testedShiftTemplateEditForm.onDeleteButtonClick(mouseEvent);
        verify(mockLane, never()).removeGridObject(any());
        verify(testedShiftTemplateEditForm, never()).hide();
    }
}
