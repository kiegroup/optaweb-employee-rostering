package org.optaweb.employeerostering.domain.shift;

import org.optaplanner.core.api.domain.entity.PinningFilter;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;

public class PinningShiftFilter implements PinningFilter<Roster, Shift> {

    @Override
    public boolean accept(Roster roster, Shift shift) {
        RosterState rosterState = roster.getRosterState();

        if (roster.isNondisruptivePlanning()) {
            return shift.getStartDateTime().isBefore(roster.getNondisruptiveReplanFrom());
        } else {
            return !rosterState.isDraft(shift);
        }
    }
}
