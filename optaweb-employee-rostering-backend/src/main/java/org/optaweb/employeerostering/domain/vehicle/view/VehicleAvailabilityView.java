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

package org.optaweb.employeerostering.domain.vehicle.view;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.common.DateTimeUtils;
import org.optaweb.employeerostering.domain.vehicle.Vehicle;
import org.optaweb.employeerostering.domain.vehicle.VehicleAvailability;
import org.optaweb.employeerostering.domain.vehicle.VehicleAvailabilityState;

public class VehicleAvailabilityView extends AbstractPersistable {

    @NotNull
    private Long vehicleId;

    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;

    private VehicleAvailabilityState state;

    @SuppressWarnings("unused")
    public VehicleAvailabilityView() {}

    public VehicleAvailabilityView(Integer tenantId, Vehicle vehicle, LocalDateTime startDateTime,
                                    LocalDateTime endDateTime, VehicleAvailabilityState state) {
        super(tenantId);
        this.vehicleId = vehicle.getId();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.state = state;
    }

    public VehicleAvailabilityView(ZoneId zoneId, VehicleAvailability vehicleAvailability) {
        super(vehicleAvailability);
        this.vehicleId = vehicleAvailability.getVehicle().getId();
        this.startDateTime = DateTimeUtils.toLocalDateTimeInZone(vehicleAvailability.getStartDateTime(), zoneId);
        this.endDateTime = DateTimeUtils.toLocalDateTimeInZone(vehicleAvailability.getEndDateTime(), zoneId);
        this.state = vehicleAvailability.getState();
    }

    @Override
    public String toString() {
        return vehicleId + ":" + startDateTime + "-" + endDateTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public VehicleAvailabilityState getState() {
        return state;
    }

    public void setState(VehicleAvailabilityState state) {
        this.state = state;
    }
}
