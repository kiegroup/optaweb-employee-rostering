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

package org.optaweb.employeerostering.domain.rotation.view;

import org.optaweb.employeerostering.domain.rotation.Seat;

public class SeatView {
    private Integer dayInRotation;
    private Long employeeId;

    public SeatView() {
    }

    public SeatView(Seat seat) {
        this.dayInRotation = seat.getDayInRotation();
        this.employeeId = (seat.getEmployee() != null) ? seat.getEmployee().getId() : null;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getDayInRotation() {
        return dayInRotation;
    }

    public void setDayInRotation(Integer dayInRotation) {
        this.dayInRotation = dayInRotation;
    }
}
