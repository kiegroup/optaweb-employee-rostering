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

package org.optaplanner.openshift.employeerostering.shared.roster;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@PlanningSolution
public class Roster extends AbstractPersistable {

    @ProblemFactCollectionProperty
    private List<Skill> skillList;
    @ProblemFactCollectionProperty
    private List<Spot> spotList;
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "employeeRange")
    private List<Employee> employeeList;
    @ProblemFactCollectionProperty
    private List<TimeSlot> timeSlotList;
    @ProblemFactCollectionProperty
    private List<EmployeeAvailability> employeeAvailabilityList;

    @PlanningEntityCollectionProperty
    private List<Shift> shiftList;

    @PlanningScore
    private HardSoftScore score = null;

    @SuppressWarnings("unused")
    public Roster() {
    }

    public Roster(Long id, Integer tenantId, List<Skill> skillList, List<Spot> spotList, List<Employee> employeeList, List<TimeSlot> timeSlotList,
            List<EmployeeAvailability> employeeAvailabilityList, List<Shift> shiftList) {
        super(id, tenantId);
        this.skillList = skillList;
        this.spotList = spotList;
        this.employeeList = employeeList;
        this.timeSlotList = timeSlotList;
        this.employeeAvailabilityList = employeeAvailabilityList;
        this.shiftList = shiftList;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

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

    public List<TimeSlot> getTimeSlotList() {
        return timeSlotList;
    }

    public void setTimeSlotList(List<TimeSlot> timeSlotList) {
        this.timeSlotList = timeSlotList;
    }

    public List<EmployeeAvailability> getEmployeeAvailabilityList() {
        return employeeAvailabilityList;
    }

    public void setEmployeeAvailabilityList(List<EmployeeAvailability> employeeAvailabilityList) {
        this.employeeAvailabilityList = employeeAvailabilityList;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

}
