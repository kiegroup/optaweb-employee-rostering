package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotData implements HasTimeslot<SpotId> {

    private Shift shift;
    private SpotId spotId;
    private Indictment indictment;

    public SpotData(Shift shift) {
        this.shift = shift;
        this.spotId = new SpotId(shift.getSpot());
        this.indictment = null;
    }

    public SpotData(Shift shift, Indictment indictment) {
        this.shift = shift;
        this.spotId = new SpotId(shift.getSpot());
        this.indictment = indictment;
    }

    public Spot getSpot() {
        return shift.getSpot();
    }

    public Employee getAssignedEmployee() {
        return shift.getEmployee();
    }

    public boolean isLocked() {
        return shift.isLockedByUser();
    }

    public Shift getShift() {
        return shift;
    }

    public Indictment getIndictment() {
        return indictment;
    }

    public Set<ConstraintMatch> getRulesMatched() {
        return (null != indictment) ? indictment.getConstraintMatchSet() : Collections.emptySet();
    }

    @Override
    public LocalDateTime getStartTime() {
        return shift.getTimeSlot().getStartDateTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return shift.getTimeSlot().getEndDateTime();
    }

    @Override
    public SpotId getGroupId() {
        return spotId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SpotData) {
            SpotData other = (SpotData) o;
            return this.shift.equals(other.shift);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return shift.hashCode();
    }

}
