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

package org.optaplanner.openshift.employeerostering.shared.timeslot;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.jackson.LocalDateTimeDeserializer;
import org.optaplanner.openshift.employeerostering.shared.jackson.LocalDateTimeSerializer;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "TimeSlot.findAllOrderedByStartDate",
                query = " select t from TimeSlot t " +
                        " where t.tenantId = :tenantId " +
                        " order by t.startDateTime ")
})
public class TimeSlot extends AbstractPersistable {

    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    private TimeSlotState timeSlotState;

    @SuppressWarnings("unused")
    public TimeSlot() {
    }

    public TimeSlot(Integer tenantId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        super(tenantId);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    @Override
    public String toString() {
        return startDateTime + "-" + endDateTime.toLocalTime();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public TimeSlotState getTimeSlotState() {
        return timeSlotState;
    }

    public void setTimeSlotState(TimeSlotState timeSlotState) {
        this.timeSlotState = timeSlotState;
    }
}
