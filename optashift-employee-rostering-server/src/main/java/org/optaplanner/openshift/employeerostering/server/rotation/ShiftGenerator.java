package org.optaplanner.openshift.employeerostering.server.rotation;

import java.time.LocalDate;
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
public class ShiftGenerator {

    public ParserOut parse(Integer tenantId,
            TenantConfiguration tenantConfiguration,
            RosterState rosterState,
            int lengthInDays,
            Collection<ShiftTemplate> shifts) {
        List<Shift> shiftOutputList = new ArrayList<>();

        LocalDate oldLastDraftDate = rosterState.getLastDraftDate();
        LocalDate newLastDraftDate = oldLastDraftDate.plusDays(lengthInDays);

        for (LocalDate currDay = oldLastDraftDate.plusDays(1); !currDay.isAfter(newLastDraftDate); currDay = currDay
                .plusDays(1)) {
            List<ShiftTemplate> shiftsToAdd = shifts.stream().filter((s) -> s.getOffsetStartDay() == rosterState
                    .getUnplannedRotationOffset()).collect(Collectors.toList());
            for (ShiftTemplate shiftTemplate : shiftsToAdd) {
                shiftOutputList.add(shiftTemplate.asShiftOnDate(currDay, tenantConfiguration.getTimeZone()));
            }
            rosterState.setUnplannedRotationOffset((rosterState.getUnplannedRotationOffset() + 1) % rosterState
                    .getRotationLength());
        }
        rosterState.setDraftLength(rosterState.getDraftLength() + lengthInDays);

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
