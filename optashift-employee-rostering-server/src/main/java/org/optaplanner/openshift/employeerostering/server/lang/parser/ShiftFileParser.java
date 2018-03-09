package org.optaplanner.openshift.employeerostering.server.lang.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;

@Singleton
public class ShiftFileParser {

    public ParserOut parse(Integer tenantId,
            TenantConfiguration tenantConfiguration,
            RosterState rosterState,
            int lengthInDays,
            Collection<ShiftTemplate> shifts) {
        List<Shift> shiftOutputList = new ArrayList<>();

        for (LocalDate currDay = rosterState.getFirstDraftDate(); currDay.isBefore(rosterState.getFirstDraftDate()
                .plusDays(lengthInDays)); currDay = currDay.plusDays(1), rosterState.setUnplannedOffset((rosterState
                        .getUnplannedOffset() + 1) % rosterState.getRotationLength())) {
            List<ShiftTemplate> shiftsToAdd = shifts.stream().filter((s) -> s.getOffsetStartDay() == rosterState
                    .getUnplannedOffset()).collect(Collectors.toList());
            for (ShiftTemplate shiftTemplate : shiftsToAdd) {
                LocalDateTime startDateTime = currDay.atTime(shiftTemplate.getStartTime());
                LocalDateTime endDateTime = currDay.plusDays(shiftTemplate.getOffsetEndDay() - shiftTemplate
                        .getOffsetStartDay()).atTime(shiftTemplate.getEndTime());

                // TODO: How to handle start/end time in transitions? Current is the Offset BEFORE the transition
                OffsetDateTime startOffsetDateTime = startDateTime.atOffset(tenantConfiguration.getTimeZone().getRules()
                        .getOffset(startDateTime));
                OffsetDateTime endOffsetDateTime = endDateTime.atOffset(tenantConfiguration.getTimeZone().getRules()
                        .getOffset(endDateTime));

                shiftOutputList.add(new Shift(tenantId, shiftTemplate.getSpot(), startOffsetDateTime, endOffsetDateTime,
                        shiftTemplate.getRotationEmployee()));
            }
        }

        ParserOut out = new ParserOut();
        out.newRosterState = rosterState;
        out.shiftOutputList = shiftOutputList;

        // TODO: Actually generate the employee avaliability output (need design and info from users first)
        out.employeeAvailabilityOutputList = new ArrayList<>();

        return out;
    }

    public static class ParserOut {

        RosterState newRosterState;
        List<Shift> shiftOutputList;
        List<EmployeeAvailability> employeeAvailabilityOutputList;

        public RosterState getNewRosterState() {
            return newRosterState;
        }

        public List<Shift> getShiftOutputList() {
            return shiftOutputList;
        }

        public List<EmployeeAvailability> getEmployeeAvailabilityOutputList() {
            return employeeAvailabilityOutputList;
        }
    }
}
