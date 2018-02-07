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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftBlob implements Blob {

    private final Shift shift;

    public ShiftBlob(final Shift shift) {
        this.shift = shift;
    }

    @Override
    public String getLabel() {
        return shift.getEmployee() == null ? "Unassigned" : shift.getEmployee().getName();
    }

    @Override
    public Integer getSize() {
        return 1 - getPosition(); // Minutes representing the ending time
    }

    @Override
    public Integer getPosition() {
        return 0; // Minutes representing starting time
    }

    @Override
    public void setLabel(final String label) {
        // no-op
    }

    @Override
    public void setPosition(final Integer position) {
//        shift.setTimeSlot(new TimeSlot(shift.getTenantId(),
//                                       new LocalDateTime(), // get time from position
//                                       shift.getTimeSlot().getEndDateTime()));
    }

    @Override
    public void setSize(final Integer size) {
//        shift.setTimeSlot(new TimeSlot(shift.getTenantId(),
//                                       shift.getTimeSlot().getStartDateTime(),
//                                       new LocalDateTime())); // get time from size
    }
}
