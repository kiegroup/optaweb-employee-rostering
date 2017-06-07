/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.shared.roster.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class SpotRosterView extends AbstractRosterView {

    @NotNull
    protected Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToSpotIdToShiftViewListMap;

    @SuppressWarnings("unused")
    public SpotRosterView() {
    }

    public SpotRosterView(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public SpotRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        this(tenantId);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Map<Long, Map<Long, List<ShiftView>>> getTimeSlotIdToSpotIdToShiftViewListMap() {
        return timeSlotIdToSpotIdToShiftViewListMap;
    }

    public void setTimeSlotIdToSpotIdToShiftViewListMap(Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToSpotIdToShiftViewListMap) {
        this.timeSlotIdToSpotIdToShiftViewListMap = timeSlotIdToSpotIdToShiftViewListMap;
    }

}
