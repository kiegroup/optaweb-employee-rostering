package org.optaplanner.openshift.employeerostering.shared.rotation;

import java.time.LocalTime;
import java.util.Collections;

import org.junit.Test;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static org.junit.Assert.assertEquals;

public class ShiftTemplateTest {

    private static final int ROTATION_LENGTH = 7;

    @Test
    public void testConversionFromToView() {
        Spot testSpot = new Spot(1, "Test", Collections.emptySet());
        Employee testEmployee = new Employee(1, "Employee");
        testConversion(new ShiftTemplate(1, testSpot, 0, LocalTime.of(9, 0),
                2, LocalTime.of(17, 0)), ROTATION_LENGTH);
        testConversion(new ShiftTemplate(1, testSpot, 0, LocalTime.of(9, 0),
                2, LocalTime.of(17, 0), testEmployee), ROTATION_LENGTH);
        testConversion(new ShiftTemplate(1, testSpot, 2, LocalTime.of(11, 0),
                4, LocalTime.of(14, 0)), ROTATION_LENGTH);
        testConversion(new ShiftTemplate(1, testSpot, 6, LocalTime.of(19, 0),
                0, LocalTime.of(6, 0)), ROTATION_LENGTH);
    }

    public void testConversion(final ShiftTemplate template, int rotationLength) {
        ShiftTemplateView view = new ShiftTemplateView(rotationLength, template);
        ShiftTemplate templateFromView = new ShiftTemplate(rotationLength, view, template.getSpot(), template.getRotationEmployee());
        assertEquals(template.getStartDayOffset(), templateFromView.getStartDayOffset());
        assertEquals(template.getEndDayOffset(), templateFromView.getEndDayOffset());
        assertEquals(template.getStartTime(), templateFromView.getStartTime());
        assertEquals(template.getEndTime(), templateFromView.getEndTime());
        assertEquals(template.getSpot(), templateFromView.getSpot());
        assertEquals(template.getRotationEmployee(), templateFromView.getRotationEmployee());
        assertEquals(template.getTenantId(), templateFromView.getTenantId());
        assertEquals(template.getId(), templateFromView.getId());
    }

}
