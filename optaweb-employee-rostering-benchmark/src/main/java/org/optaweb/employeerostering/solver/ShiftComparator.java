package org.optaweb.employeerostering.solver;

import java.util.Comparator;

import org.optaweb.employeerostering.domain.shift.Shift;

public class ShiftComparator implements Comparator<Shift> {

    private static final Comparator<Shift> DATE_TIME_COMPARATOR = Comparator.comparing(Shift::getStartDateTime)
            .thenComparing(Shift::getEndDateTime);

    @Override
    public int compare(Shift a, Shift b) {
        return DATE_TIME_COMPARATOR.compare(a, b);
    }
}
