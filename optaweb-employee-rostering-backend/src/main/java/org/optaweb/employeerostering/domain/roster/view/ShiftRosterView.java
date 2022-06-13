package org.optaweb.employeerostering.domain.roster.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.optaweb.employeerostering.domain.shift.view.ShiftView;

public class ShiftRosterView extends AbstractRosterView {

    @NotNull
    // The list in each entry is sorted by startTime
    protected Map<Long, List<ShiftView>> spotIdToShiftViewListMap;

    @SuppressWarnings("unused")
    public ShiftRosterView() {
    }

    public ShiftRosterView(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public ShiftRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
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
