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

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import java.time.LocalDateTime;
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
import elemental2.dom.HTMLInputElement;
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
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ShiftEditFormTest {

    @Mock
    private LocalDateTimePicker from;

    @Mock
    private LocalDateTimePicker to;

    @Mock
    private ListBox spotSelect;

    @Mock
    private ListBox employeeSelect;

    @Mock
    private HTMLInputElement pinned;

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
    private ShiftGridObject mockShiftGridObject;

    @Mock
    private HTMLElement mockShiftGridObjectElement;

    @Mock
    private Lane<LocalDateTime, ShiftRosterMetadata> mockLane;

    private ShiftEditForm testedShiftEditForm;

    private Queue<Object> restCallbackAnswers;

    private List<Spot> spotList;

    private List<Employee> employeeList;

    private ShiftView shiftView;

    private static final int TENANT_ID = 0;

    private static final int NUM_OF_SPOTS = 4;

    private static final int NUM_OF_EMPLOYEES = 4;

    private static final int UNASSIGNED_EMPLOYEE = 0;

    private static final int SELECTED_SPOT = 2; // A random index between 0 and (NUM_OF_SPOTS - 1)

    private static final LocalDateTime EARILER_LOCAL_DATE_TIME = LocalDateTime.of(2012, 10, 1, 9, 0);
    private static final LocalDateTime LATER_LOCAL_DATE_TIME = LocalDateTime.of(2012, 10, 1, 14, 0);

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
        testedShiftEditForm = spy(new ShiftEditForm(popupFactory,
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
                                                    pinned,
                                                    deleteButton,
                                                    applyButton,
                                                    tenantStore,
                                                    eventManager,
                                                    translationService));
        shiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                  EARILER_LOCAL_DATE_TIME,
                                  LATER_LOCAL_DATE_TIME);
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
            Employee employee = new Employee(TENANT_ID, "Employee" + i, mockContract, Collections.emptySet());
            employee.setId(i);
            out.add(employee);
        }
        return out;
    }

    @Test
    public void testInit() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));
        testedShiftEditForm.init(mockShiftGridObject);
        verify(testedShiftEditForm).showFor(mockShiftGridObject);
        verify(mockShiftGridObject).setSelected(true);

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

        verify(from).setValue(shiftView.getStartDateTime());
        verify(to).setValue(shiftView.getEndDateTime());

        assertThat(pinned.checked).isEqualTo(shiftView.isPinnedByUser());
    }

    @Test
    public void testCreateNewShift() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.createNewShift();
        verify(testedShiftEditForm).show();

        InOrder order = inOrder(spotSelect);
        for (Spot spot : spotList) {
            order.verify(spotSelect).addItem(spot.getName(), spot.getId().toString());
        }
        order = inOrder(employeeSelect);
        for (Employee employee : employeeList) {
            order.verify(employeeSelect).addItem(employee.getName(), employee.getId().toString());
        }
        verify(spotSelect).setEnabled(true);
    }

    @Test
    public void testOnClose() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));
        testedShiftEditForm.init(mockShiftGridObject);

        testedShiftEditForm.onClose();
        verify(mockShiftGridObject).setSelected(false);
    }

    @Test
    public void testInvalidDataUpdateShiftFromWidgetsIfValid() {
        ShiftView oldShiftView = shiftView;

        // Clone the shift view since this one will may be modified
        ShiftView newShiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                               EARILER_LOCAL_DATE_TIME,
                                               LATER_LOCAL_DATE_TIME);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        pinned.checked = true;
        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(LATER_LOCAL_DATE_TIME);
        when(to.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);

        boolean out = testedShiftEditForm.updateShiftFromWidgetsIfValid(newShiftView);

        // Assert any changes are rolled back
        assertThat(newShiftView).isEqualTo(oldShiftView);
        assertThat(out).isFalse();
        verify(from).reportValidity();
        verify(to).reportValidity();
        verify(notificationFactory).showError(any());
    }

    @Test
    public void testInvalidWidgetUpdateShiftFromWidgetsIfValid() {
        ShiftView oldShiftView = shiftView;

        // Clone the shift view since this one will may be modified
        ShiftView newShiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                               EARILER_LOCAL_DATE_TIME,
                                               LATER_LOCAL_DATE_TIME);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        pinned.checked = true;
        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(null);
        when(to.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(false);
        when(to.reportValidity()).thenReturn(true);

        boolean out = testedShiftEditForm.updateShiftFromWidgetsIfValid(newShiftView);

        // Assert any changes are rolled back
        assertThat(newShiftView).isEqualTo(oldShiftView);
        assertThat(out).isFalse();
        verify(notificationFactory).showError(any());
    }

    @Test
    public void testValidUpdateShiftFromWidgetsIfValid() {
        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        pinned.checked = true;
        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME.plusDays(2));
        when(to.getValue()).thenReturn(LATER_LOCAL_DATE_TIME.plusDays(2));
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);

        boolean out = testedShiftEditForm.updateShiftFromWidgetsIfValid(shiftView);

        verify(from).reportValidity();
        verify(to).reportValidity();

        assertThat(shiftView.isPinnedByUser()).isTrue();
        assertThat(shiftView.getSpotId()).isEqualTo(spotList.get(NEW_SELECTED_SPOT).getId());
        assertThat(shiftView.getEmployeeId()).isEqualTo(employeeList.get(NEW_SELECTED_EMPLOYEE).getId());
        assertThat(shiftView.getRotationEmployeeId()).isNull();
        assertThat(shiftView.getStartDateTime()).isEqualTo(EARILER_LOCAL_DATE_TIME.plusDays(2));
        assertThat(shiftView.getEndDateTime()).isEqualTo(LATER_LOCAL_DATE_TIME.plusDays(2));
        assertThat(out).isTrue();
    }

    @Test
    public void testAddNewShiftFromForm() {
        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.createNewShift();

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(to.getValue()).thenReturn(LATER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);

        ShiftView updatedShiftView = new ShiftView(TENANT_ID, spotList.get(NEW_SELECTED_SPOT),
                                                   EARILER_LOCAL_DATE_TIME.plusDays(2),
                                                   LATER_LOCAL_DATE_TIME.plusDays(2));
        updatedShiftView.setEmployeeId(employeeList.get(NEW_SELECTED_EMPLOYEE).getId());

        restCallbackAnswers.add(updatedShiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).hide();
        verify(eventManager).fireEvent(EventManager.Event.SHIFT_ROSTER_INVALIDATE);
    }

    @Test
    public void testInvalidAddNewShiftFromForm() {
        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.createNewShift();

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(LATER_LOCAL_DATE_TIME);
        when(to.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(eventManager, never()).fireEvent(any());
    }

    @Test
    public void testUpdateShiftFromForm() {
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(to.getValue()).thenReturn(LATER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);
        ShiftView expectedShiftView = new ShiftView(TENANT_ID, spotList.get(NEW_SELECTED_SPOT),
                                                    EARILER_LOCAL_DATE_TIME.plusDays(2),
                                                    LATER_LOCAL_DATE_TIME.plusDays(2));
        expectedShiftView.setEmployeeId(employeeList.get(NEW_SELECTED_EMPLOYEE).getId());

        restCallbackAnswers.add(expectedShiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);

        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).updateShiftFromWidgetsIfValid(shiftView);
        verify(testedShiftEditForm).hide();
        verify(eventManager).fireEvent(EventManager.Event.SHIFT_ROSTER_INVALIDATE);

        ArgumentCaptor<ShiftView> updatedShiftViewCaptor = ArgumentCaptor.forClass(ShiftView.class);
        verify(mockShiftGridObject).withShiftView(updatedShiftViewCaptor.capture());

        ShiftView updatedShiftView = updatedShiftViewCaptor.getValue();
        assertThat(updatedShiftView).isEqualTo(expectedShiftView);
    }

    @Test
    public void testInvalidUpdateShiftFromForm() {
        ShiftView shiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                            EARILER_LOCAL_DATE_TIME,
                                            LATER_LOCAL_DATE_TIME);
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        final int NEW_SELECTED_SPOT = 3;
        final int NEW_SELECTED_EMPLOYEE = 1;

        when(spotSelect.getSelectedValue()).thenReturn(spotList.get(NEW_SELECTED_SPOT).getId().toString());
        when(employeeSelect.getSelectedValue()).thenReturn(employeeList.get(NEW_SELECTED_EMPLOYEE).getId().toString());
        when(from.getValue()).thenReturn(LATER_LOCAL_DATE_TIME);
        when(to.getValue()).thenReturn(EARILER_LOCAL_DATE_TIME);
        when(from.reportValidity()).thenReturn(true);
        when(to.reportValidity()).thenReturn(true);

        restCallbackAnswers.add(shiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).updateShiftFromWidgetsIfValid(shiftView);
        verify(eventManager, never()).fireEvent(any());
    }

    @Test
    public void testSuccessfulDelete() {
        when(mockShiftGridObject.getLane()).thenReturn(mockLane);

        ShiftView shiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                            EARILER_LOCAL_DATE_TIME,
                                            LATER_LOCAL_DATE_TIME);
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        restCallbackAnswers.add(Boolean.TRUE);

        testedShiftEditForm.onDeleteButtonClick(mouseEvent);
        verify(mockLane).removeGridObject(mockShiftGridObject);
        verify(testedShiftEditForm).hide();
    }

    @Test
    public void testUnsuccessfulDelete() {
        when(mockShiftGridObject.getLane()).thenReturn(mockLane);

        ShiftView shiftView = new ShiftView(TENANT_ID, spotList.get(SELECTED_SPOT),
                                            EARILER_LOCAL_DATE_TIME,
                                            LATER_LOCAL_DATE_TIME);
        when(mockShiftGridObject.getShiftView()).thenReturn(shiftView);
        when(mockShiftGridObject.getSpot()).thenReturn(spotList.get(SELECTED_SPOT));

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        restCallbackAnswers.add(Boolean.FALSE);

        testedShiftEditForm.onDeleteButtonClick(mouseEvent);
        verify(mockLane, never()).removeGridObject(any());
        verify(testedShiftEditForm, never()).hide();
    }
}
