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

import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;

public class AvailabilityRosterView extends AbstractRosterView {

    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<ShiftView>> employeeIdToShiftViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private List<ShiftView> unassignedShiftViewList;

    @SuppressWarnings("unused")
    public AvailabilityRosterView() {}

    public AvailabilityRosterView(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public AvailabilityRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        this(tenantId);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Map<Long, List<ShiftView>> getEmployeeIdToShiftViewListMap() {
        return employeeIdToShiftViewListMap;
    }

    public void setEmployeeIdToShiftViewListMap(Map<Long, List<ShiftView>> employeeIdToShiftViewListMap) {
        this.employeeIdToShiftViewListMap = employeeIdToShiftViewListMap;
    }

    public Map<Long, List<EmployeeAvailabilityView>> getEmployeeIdToAvailabilityViewListMap() {
        return employeeIdToAvailabilityViewListMap;
    }

    public void setEmployeeIdToAvailabilityViewListMap(Map<Long, List<EmployeeAvailabilityView>>
                                                               employeeIdToAvailabilityViewListMap) {
        this.employeeIdToAvailabilityViewListMap = employeeIdToAvailabilityViewListMap;
    }

    public List<ShiftView> getUnassignedShiftViewList() {
        return unassignedShiftViewList;
    }

    public void setUnassignedShiftViewList(List<ShiftView> unassignedShiftViewList) {
        this.unassignedShiftViewList = unassignedShiftViewList;
    }

}
