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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.optaweb.employeerostering.gwtui.client.common.CallbackFactory;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
    private Lane<LocalDateTime, ShiftRosterMetadata> mockLane;

    private ShiftEditForm testedShiftEditForm;

    private Queue<Object> restCallbackAnswers;

    private List<Spot> spotList;

    private List<Employee> employeeList;

    @Before
    public void setUp() throws Exception {
        restCallbackAnswers = new LinkedList();
        spotList = getSpotList();
        employeeList = getEmployeeList();
        doReturn(Optional.of(formPopup)).when(popupFactory).getFormPopup(any());
        doAnswer(v -> {
            Consumer c = v.getArgument(0);
            c.accept(restCallbackAnswers.poll());
            return null;
        }).when(callbackFactory).onSuccess(any());
        testedShiftEditForm = spy(new ShiftEditForm(popupFactory,
                                                    callbackFactory,
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
    }

    private List<Spot> getSpotList() {
        final String[] SPOT_NAMES = {"Spot1", "Spot2", "Spot3", "Spot4"};
        List<Spot> out = new ArrayList<>(SPOT_NAMES.length);
        for (int i = 0; i < SPOT_NAMES.length; i++) {
            Spot spot = new Spot(0, SPOT_NAMES[i], Collections.emptySet());
            spot.setId((long) i);
            out.add(spot);
        }
        return out;
    }

    private List<Employee> getEmployeeList() {
        final String[] EMPLOYEE_NAMES = {"Employee1", "Employee2", "Employee3", "Employee4"};
        List<Employee> out = new ArrayList<>(EMPLOYEE_NAMES.length);
        for (int i = 0; i < EMPLOYEE_NAMES.length; i++) {
            Employee employee = new Employee(0, EMPLOYEE_NAMES[i]);
            employee.setId((long) i);
            out.add(employee);
        }
        return out;
    }

    @Test
    public void testInit() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        doReturn(shiftView).when(mockShiftGridObject).getShiftView();
        doReturn(spotList.get(2)).when(mockShiftGridObject).getSpot();
        testedShiftEditForm.init(mockShiftGridObject);
        verify(testedShiftEditForm).showFor(mockShiftGridObject);
        verify(mockShiftGridObject).setSelected(true);

        InOrder order = inOrder(spotSelect);
        for (Spot spot : spotList) {
            order.verify(spotSelect).addItem(spot.getName(), spot.getId().toString());
        }
        order.verify(spotSelect).setSelectedIndex(2);

        order = inOrder(employeeSelect);
        for (Employee employee : employeeList) {
            order.verify(employeeSelect).addItem(employee.getName(), employee.getId().toString());
        }
        order.verify(employeeSelect).setSelectedIndex(0);

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
        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        doReturn(shiftView).when(mockShiftGridObject).getShiftView();
        doReturn(spotList.get(2)).when(mockShiftGridObject).getSpot();
        testedShiftEditForm.init(mockShiftGridObject);

        testedShiftEditForm.onClose();
        verify(mockShiftGridObject).setSelected(false);
    }

    @Test
    public void testInvalidUpdateShiftFromWidgetsIfValid() {
        ShiftView oldShiftView = new ShiftView(0, spotList.get(2),
                                               LocalDateTime.of(2012, 10, 1, 9, 0),
                                               LocalDateTime.of(2012, 10, 1, 14, 0));
        ShiftView newShiftView = new ShiftView(0, spotList.get(2),
                                               LocalDateTime.of(2012, 10, 1, 9, 0),
                                               LocalDateTime.of(2012, 10, 1, 14, 0));

        pinned.checked = true;
        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 14, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 9, 0)).when(to).getValue();

        boolean out = testedShiftEditForm.updateShiftFromWidgetsIfValid(newShiftView);
        assertThat(newShiftView).isEqualTo(oldShiftView);
        assertThat(out).isFalse();
    }

    @Test
    public void testValidUpdateShiftFromWidgetsIfValid() {
        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));

        pinned.checked = true;
        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 9, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 14, 0)).when(to).getValue();

        boolean out = testedShiftEditForm.updateShiftFromWidgetsIfValid(shiftView);

        verify(spotSelect).getSelectedValue();
        verify(employeeSelect).getSelectedValue();
        verify(from).getValue();
        verify(to).getValue();

        assertThat(shiftView.isPinnedByUser()).isTrue();
        assertThat(shiftView.getSpotId()).isEqualTo(spotList.get(3).getId());
        assertThat(shiftView.getEmployeeId()).isEqualTo(employeeList.get(1).getId());
        assertThat(shiftView.getRotationEmployeeId()).isNull();
        assertThat(shiftView.getStartDateTime()).isEqualTo(LocalDateTime.of(2012, 10, 1, 9, 0));
        assertThat(shiftView.getEndDateTime()).isEqualTo(LocalDateTime.of(2012, 10, 1, 14, 0));
        assertThat(out).isTrue();
    }

    @Test
    public void testAddNewShiftFromForm() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.createNewShift();

        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 9, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 14, 0)).when(to).getValue();

        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        restCallbackAnswers.add(shiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).hide();
        verify(eventManager).fireEvent(EventManager.Event.SHIFT_ROSTER_INVALIDATE);
    }

    @Test
    public void testInvalidAddNewShiftFromForm() {
        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.createNewShift();

        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 14, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 1, 9, 0)).when(to).getValue();

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(eventManager, VerificationModeFactory.noMoreInteractions()).fireEvent(any());
    }

    @Test
    public void testUpdateShiftFromForm() {
        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        doReturn(shiftView).when(mockShiftGridObject).getShiftView();
        doReturn(spotList.get(2)).when(mockShiftGridObject).getSpot();

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 3, 9, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 3, 14, 0)).when(to).getValue();

        restCallbackAnswers.add(shiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).updateShiftFromWidgetsIfValid(shiftView);
        verify(testedShiftEditForm).hide();
        verify(eventManager).fireEvent(EventManager.Event.SHIFT_ROSTER_INVALIDATE);
    }

    @Test
    public void testInvalidUpdateShiftFromForm() {
        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        doReturn(shiftView).when(mockShiftGridObject).getShiftView();
        doReturn(spotList.get(2)).when(mockShiftGridObject).getSpot();

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        doReturn(spotList.get(3).getId().toString()).when(spotSelect).getSelectedValue();
        doReturn(employeeList.get(1).getId().toString()).when(employeeSelect).getSelectedValue();
        doReturn(LocalDateTime.of(2012, 10, 3, 14, 0)).when(from).getValue();
        doReturn(LocalDateTime.of(2012, 10, 3, 9, 0)).when(to).getValue();

        restCallbackAnswers.add(shiftView);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedShiftEditForm.onApplyButtonClick(mouseEvent);
        verify(testedShiftEditForm).updateShiftFromWidgetsIfValid(shiftView);
        verify(eventManager, VerificationModeFactory.noMoreInteractions()).fireEvent(any());
    }

    @Test
    public void testDelete() {
        doReturn(mockLane).when(mockShiftGridObject).getLane();

        ShiftView shiftView = new ShiftView(0, spotList.get(2),
                                            LocalDateTime.of(2012, 10, 1, 9, 0),
                                            LocalDateTime.of(2012, 10, 1, 14, 0));
        doReturn(shiftView).when(mockShiftGridObject).getShiftView();
        doReturn(spotList.get(2)).when(mockShiftGridObject).getSpot();

        restCallbackAnswers.add(spotList);
        restCallbackAnswers.add(employeeList);
        testedShiftEditForm.init(mockShiftGridObject);

        MouseEvent mouseEvent = mock(MouseEvent.class);
        restCallbackAnswers.add(null);

        testedShiftEditForm.onDeleteButtonClick(mouseEvent);
        verify(mockLane).removeGridObject(mockShiftGridObject);
        verify(testedShiftEditForm).hide();
    }
}
