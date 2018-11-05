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

package org.optaweb.employeerostering.shared.rotation;

import java.time.LocalTime;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.junit.Assert.assertEquals;

public class ShiftTemplateTest {

    private static final int ROTATION_LENGTH = 7;

    @Test
    public void testConversionFromToView() {
        Contract contract = new Contract(1, "Contract");
        Spot testSpot = new Spot(1, "Test", Collections.emptySet());
        Employee testEmployee = new Employee(1, "Employee", contract, Collections.emptySet());
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
        Assert.assertEquals(template.getRotationEmployee(), templateFromView.getRotationEmployee());
        assertEquals(template.getTenantId(), templateFromView.getTenantId());
        assertEquals(template.getId(), templateFromView.getId());
    }
}
