/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.LocalDateTime;
import java.util.Map;

import org.optaplanner.openshift.employeerostering.gwtui.client.pages.TimeslotBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeBlob extends TimeslotBlob {

    private EmployeeAvailabilityView employeeAvailabilityView;
    private ShiftView shiftView;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    public EmployeeBlob(final LinearScale<LocalDateTime> scale, Map<Long, Spot> spotIdToSpotMap,
                        Map<Long, Employee> employeeIdToEmployeeMap, final ShiftView shiftView) {
        this(scale, spotIdToSpotMap, employeeIdToEmployeeMap, null, shiftView);
    }

    public EmployeeBlob(final LinearScale<LocalDateTime> scale, Map<Long, Spot> spotIdToSpotMap,
                        Map<Long, Employee> employeeIdToEmployeeMap, final EmployeeAvailabilityView employeeAvailabilityView) {
        this(scale, spotIdToSpotMap, employeeIdToEmployeeMap, employeeAvailabilityView, null);
    }

    private EmployeeBlob(final LinearScale<LocalDateTime> scale, Map<Long, Spot> spotIdToSpotMap,
                         Map<Long, Employee> employeeIdToEmployeeMap, final EmployeeAvailabilityView employeeAvailabilityView, final ShiftView shiftView) {
        this.employeeAvailabilityView = employeeAvailabilityView;
        this.shiftView = shiftView;
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
        setScale(scale);
    }

    public String getLabel() {
        if (shiftView != null) {
            return getSpot().toString() + ": " + shiftView.getStartDateTime() + "-" + shiftView.getEndDateTime();
        } else {
            return employeeAvailabilityView.getState().toString();
        }
    }

    public ShiftView getShiftView() {
        return shiftView;
    }

    public void setShiftView(final ShiftView shiftView) {
        this.shiftView = shiftView;
    }

    public EmployeeAvailabilityView getEmployeeAvailabilityView() {
        return employeeAvailabilityView;
    }

    public void setEmployeeAvailabilityView(final EmployeeAvailabilityView employeeAvailabilityView) {
        this.employeeAvailabilityView = employeeAvailabilityView;
    }

    public Spot getSpot() {
        return spotIdToSpotMap.get(shiftView.getSpotId());
    }

    public Employee getEmployee() {
        if (shiftView != null) {
            return employeeIdToEmployeeMap.get(shiftView.getEmployeeId());
        } else {
            return employeeIdToEmployeeMap.get(employeeAvailabilityView.getEmployeeId());
        }
    }

    @Override
    public void setPositionInScaleUnits(LocalDateTime position) {
        if (shiftView != null) {
            shiftView.setStartDateTime(position);
        } else {
            employeeAvailabilityView.setStartDateTime(position);
        }
    }

    @Override
    public void setSizeInGridPixels(double sizeInGridPixels) {
        LocalDateTime newEndDateTime = getScale().toScaleUnits(getScale()
                .toGridPixels(getPositionInScaleUnits()) + sizeInGridPixels);
        if (shiftView != null) {
            shiftView.setEndDateTime(newEndDateTime);
        } else {
            employeeAvailabilityView.setEndDateTime(newEndDateTime);
        }
    }

    @Override
    public HasTimeslot getTimeslot() {
        return (shiftView != null) ? shiftView : employeeAvailabilityView;
    }

}
