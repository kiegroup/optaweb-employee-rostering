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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.pages.TimeslotBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class ShiftBlob extends TimeslotBlob {

    private ShiftView shift;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    ShiftBlob(final LinearScale<LocalDateTime> scale,
              Map<Long, Spot> spotIdToSpotMap,
              Map<Long, Employee> employeeIdToEmployeeMap,
              final ShiftView shift) {
        this.shift = shift;
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
        setScale(scale);
    }

    @Override
    public void setPositionInScaleUnits(final LocalDateTime start) {
        shift.setStartDateTime(start);
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        shift.setEndDateTime(getScale().toScaleUnits(getScale()
                .toGridPixels(getPositionInScaleUnits()) + sizeInGridPixels));
    }

    public String getLabel() {
        return Optional.ofNullable(getEmployee())
                .map(Employee::getName)
                .orElse("Unassigned");
    }

    public ShiftView getShiftView() {
        return shift;
    }

    public void setShiftView(final ShiftView shift) {
        this.shift = shift;
    }

    public Spot getSpot() {
        return spotIdToSpotMap.get(shift.getSpotId());
    }

    public Employee getEmployee() {
        return employeeIdToEmployeeMap.get(shift.getEmployeeId());
    }

    public Employee getRotationEmployee() {
        return employeeIdToEmployeeMap.get(shift.getRotationEmployeeId());
    }

    @Override
    public HasTimeslot getTimeslot() {
        return shift;
    }

}
