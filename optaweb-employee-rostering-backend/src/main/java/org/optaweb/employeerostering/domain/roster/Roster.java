package org.optaweb.employeerostering.domain.roster;

import java.time.OffsetDateTime;
import java.util.List;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

@PlanningSolution
public class Roster extends AbstractPersistable {

    @ConstraintConfigurationProvider
    private RosterConstraintConfiguration rosterConstraintConfiguration;

    @ProblemFactCollectionProperty
    private List<Skill> skillList;
    @ProblemFactCollectionProperty
    private List<Spot> spotList;
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "employeeRange")
    private List<Employee> employeeList;
    @ProblemFactCollectionProperty
    private List<EmployeeAvailability> employeeAvailabilityList;

    @ProblemFactProperty
    private RosterState rosterState;

    @PlanningEntityCollectionProperty
    private List<Shift> shiftList;

    @PlanningScore
    private HardMediumSoftLongScore score = null;

    private boolean isNondisruptivePlanning;
    private OffsetDateTime nondisruptiveReplanFrom;

    @SuppressWarnings("unused")
    public Roster() {
    }

    public Roster(Long id, Integer tenantId, RosterConstraintConfiguration rosterConstraintConfiguration,
            List<Skill> skillList, List<Spot> spotList, List<Employee> employeeList,
            List<EmployeeAvailability> employeeAvailabilityList,
            RosterState rosterState, List<Shift> shiftList) {
        this(id, tenantId, rosterConstraintConfiguration, skillList, spotList, employeeList, employeeAvailabilityList,
                rosterState, shiftList, false, null);
    }

    public Roster(Long id, Integer tenantId, RosterConstraintConfiguration rosterConstraintConfiguration,
            List<Skill> skillList, List<Spot> spotList, List<Employee> employeeList,
            List<EmployeeAvailability> employeeAvailabilityList,
            RosterState rosterState, List<Shift> shiftList, boolean isNondisruptivePlanning,
            OffsetDateTime nondisruptiveReplanFrom) {
        super(id, tenantId);
        this.rosterConstraintConfiguration = rosterConstraintConfiguration;
        this.skillList = skillList;
        this.spotList = spotList;
        this.employeeList = employeeList;
        this.employeeAvailabilityList = employeeAvailabilityList;
        this.rosterState = rosterState;
        this.shiftList = shiftList;
        this.isNondisruptivePlanning = isNondisruptivePlanning;
        this.nondisruptiveReplanFrom = nondisruptiveReplanFrom;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public RosterConstraintConfiguration getRosterConstraintConfiguration() {
        return rosterConstraintConfiguration;
    }

    public void setRosterConstraintConfiguration(RosterConstraintConfiguration rosterConstraintConfiguration) {
        this.rosterConstraintConfiguration = rosterConstraintConfiguration;
    }

    public List<Skill> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
    }

    public List<Spot> getSpotList() {
        return spotList;
    }

    public void setSpotList(List<Spot> spotList) {
        this.spotList = spotList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<EmployeeAvailability> getEmployeeAvailabilityList() {
        return employeeAvailabilityList;
    }

    public void setEmployeeAvailabilityList(List<EmployeeAvailability> employeeAvailabilityList) {
        this.employeeAvailabilityList = employeeAvailabilityList;
    }

    public RosterState getRosterState() {
        return rosterState;
    }

    public void setRosterState(RosterState rosterState) {
        this.rosterState = rosterState;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

    public boolean isNondisruptivePlanning() {
        return isNondisruptivePlanning;
    }

    public void setNondisruptivePlanning(boolean isNondisruptivePlanning) {
        this.isNondisruptivePlanning = isNondisruptivePlanning;
    }

    public OffsetDateTime getNondisruptiveReplanFrom() {
        return nondisruptiveReplanFrom;
    }

    public void setNondisruptiveReplanFrom(OffsetDateTime undistruptiveReplanFrom) {
        this.nondisruptiveReplanFrom = undistruptiveReplanFrom;
    }
}
