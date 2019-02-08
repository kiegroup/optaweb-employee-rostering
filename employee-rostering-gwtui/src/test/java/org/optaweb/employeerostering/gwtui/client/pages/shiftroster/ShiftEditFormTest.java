package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.optaweb.employeerostering.gwtui.client.common.CallbackFactory;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateTimePicker;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
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
    
    private ShiftEditForm testedShiftEditForm;
    
    private Queue<Object> restCallbackAnswers;
    
    private List<Spot> spotList;
    
    private List<Employee> employeeList;
    

    @Before
    public void setUp() throws Exception {
        restCallbackAnswers = new LinkedList();
        spotList = getSpotList();
        employeeList = getEmployeeList();
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
        verify(testedShiftEditForm).setup(shiftView);
        
        InOrder order = inOrder(spotSelect);
        for (Spot spot : spotList) {
            order.verify(spotSelect).addItem(spot.getName(), spot.getId().toString());
        }
        order.verify(spotSelect).setSelectedIndex(2);
        
        verify(from).setValue(shiftView.getStartDateTime());
        verify(to).setValue(shiftView.getEndDateTime());
        
        assertThat(pinned.checked).isEqualTo(shiftView.isPinnedByUser());
    }
    
}
