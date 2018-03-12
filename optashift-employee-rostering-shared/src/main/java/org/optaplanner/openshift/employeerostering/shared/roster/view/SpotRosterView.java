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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

public class SpotRosterView extends AbstractRosterView {

    @NotNull
    // The list in each entry is sorted by startTime
    protected Map<Long, List<ShiftView>> spotIdToShiftViewListMap;

    @SuppressWarnings("unused")
    public SpotRosterView() {}

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

    public Map<Long, List<ShiftView>> getSpotIdToShiftViewListMap() {
        return spotIdToShiftViewListMap;
    }

    public void setSpotIdToShiftViewListMap(Map<Long, List<ShiftView>> spotIdToShiftViewListMap) {
        this.spotIdToShiftViewListMap = spotIdToShiftViewListMap;
    }

}
