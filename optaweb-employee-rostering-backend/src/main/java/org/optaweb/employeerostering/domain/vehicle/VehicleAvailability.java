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

package org.optaweb.employeerostering.domain.vehicle;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.vehicle.view.VehicleAvailabilityView;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "vehicle_id", "startDateTime", "endDateTime"}))
// TODO: Single Responsibility Principle - acts as both domain entity and JSON-serializable entity
public class VehicleAvailability extends AbstractPersistable {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Vehicle vehicle;

    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;

    @NotNull
    private VehicleAvailabilityState state;

    @SuppressWarnings("unused")
    public VehicleAvailability() {
    }

    public VehicleAvailability(Integer tenantId, Vehicle vehicle, OffsetDateTime startDateTime,
                                OffsetDateTime endDateTime) {
        super(tenantId);
        this.vehicle = vehicle;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public VehicleAvailability(ZoneId zoneId, VehicleAvailabilityView vehicleAvailabilityView, Vehicle vehicle) {
        super(vehicleAvailabilityView);
        this.vehicle = vehicle;
        this.startDateTime = OffsetDateTime.of(vehicleAvailabilityView.getStartDateTime(),
                                             zoneId.getRules().getOffset(vehicleAvailabilityView.getStartDateTime()));
        this.endDateTime = OffsetDateTime.of(vehicleAvailabilityView.getEndDateTime(),
                                             zoneId.getRules().getOffset(vehicleAvailabilityView.getEndDateTime()));
        this.state = vehicleAvailabilityView.getState();
    }

    @AssertTrue
    @JsonIgnore
    public boolean isValid() {
        return getDuration().getSeconds() / (60 * 60) < 28;
    }

    @JsonIgnore
    public Duration getDuration() {
        return Duration.between(startDateTime, endDateTime);
    }

    @Override
    public String toString() {
        return vehicle + ":" + startDateTime + "-" + endDateTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public VehicleAvailabilityState getState() {
        return state;
    }

    public void setState(VehicleAvailabilityState state) {
        this.state = state;
    }

    public VehicleAvailability inTimeZone(ZoneId zoneId) {
        return new VehicleAvailability(zoneId, new VehicleAvailabilityView(zoneId, this), getVehicle());
    }
}
