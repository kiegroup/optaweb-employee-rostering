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

package org.optaweb.employeerostering.domain.roster.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.optaweb.employeerostering.domain.vehicle.view.VehicleAvailabilityView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;

public class AvailabilityRosterViewVehicle extends AbstractRosterView {

    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<ShiftView>> vehicleIdToShiftViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<VehicleAvailabilityView>> vehicleIdToAvailabilityViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private List<ShiftView> unassignedShiftViewList;

    @SuppressWarnings("unused")
    public AvailabilityRosterViewVehicle() {}

    public AvailabilityRosterViewVehicle(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public AvailabilityRosterViewVehicle(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        this(tenantId);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Map<Long, List<ShiftView>> getVehicleIdToShiftViewListMap() {
        return vehicleIdToShiftViewListMap;
    }

    public void setVehicleIdToShiftViewListMap(Map<Long, List<ShiftView>> vehicleIdToShiftViewListMap) {
        this.vehicleIdToShiftViewListMap = vehicleIdToShiftViewListMap;
    }

    public Map<Long, List<VehicleAvailabilityView>> getVehicleIdToAvailabilityViewListMap() {
        return vehicleIdToAvailabilityViewListMap;
    }

    public void setVehicleIdToAvailabilityViewListMap(Map<Long, List<VehicleAvailabilityView>>
                                                               vehicleIdToAvailabilityViewListMap) {
        this.vehicleIdToAvailabilityViewListMap = vehicleIdToAvailabilityViewListMap;
    }

    public List<ShiftView> getUnassignedShiftViewList() {
        return unassignedShiftViewList;
    }

    public void setUnassignedShiftViewList(List<ShiftView> unassignedShiftViewList) {
        this.unassignedShiftViewList = unassignedShiftViewList;
    }

}
