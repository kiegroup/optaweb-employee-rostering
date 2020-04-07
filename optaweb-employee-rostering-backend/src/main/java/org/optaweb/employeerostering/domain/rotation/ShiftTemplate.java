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

package org.optaweb.employeerostering.domain.rotation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;

@Entity
public class ShiftTemplate extends AbstractPersistable {

    @NotNull
    @ManyToOne
    private Spot spot;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ShiftTemplateRequiredSkillSet",
            joinColumns = @JoinColumn(name = "shiftTemplateId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id")
    )
    private Set<Skill> requiredSkillSet;

    @NotNull
    private Integer startDayOffset;
    @NotNull
    private LocalTime startTime;

    @NotNull
    private Integer endDayOffset;
    @NotNull
    private LocalTime endTime;

    @ManyToOne
    private Employee rotationEmployee;

    @SuppressWarnings("unused")
    public ShiftTemplate() {
    }

    public ShiftTemplate(Integer tenantId, Spot spot, int startDayOffset, LocalTime startTime, int endDayOffset,
                         LocalTime endTime) {
        this(tenantId, spot, startDayOffset, startTime, endDayOffset, endTime, null);
    }

    public ShiftTemplate(Integer tenantId, Spot spot,
                         int startDayOffset, LocalTime startTime, int endDayOffset, LocalTime endTime,
                         Employee rotationEmployee) {
        this(tenantId, spot, startDayOffset, startTime, endDayOffset, endTime, null, Collections.emptySet());
    }

    public ShiftTemplate(Integer tenantId, Spot spot,
                         int startDayOffset, LocalTime startTime, int endDayOffset, LocalTime endTime,
                         Employee rotationEmployee, Collection<Skill> requiredSkillSet) {
        super(tenantId);
        this.rotationEmployee = rotationEmployee;
        this.spot = spot;
        this.startDayOffset = startDayOffset;
        this.endDayOffset = endDayOffset;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredSkillSet = new HashSet<>(requiredSkillSet);
    }

    public ShiftTemplate(Integer rotationLength, ShiftTemplateView shiftTemplateView, Spot spot,
                         Employee rotationEmployee, Collection<Skill> requiredSkillSet) {
        super(shiftTemplateView);
        this.spot = spot;
        this.rotationEmployee = rotationEmployee;
        this.startDayOffset = (int) (shiftTemplateView
                .getDurationBetweenRotationStartAndTemplateStart()
                .toDays());
        this.startTime = LocalTime.ofSecondOfDay(shiftTemplateView
                                                         .getDurationBetweenRotationStartAndTemplateStart()
                                                         .minusDays(startDayOffset)
                                                         .getSeconds());
        int endDayAfterStartDay = ((int) (shiftTemplateView
                .getDurationBetweenRotationStartAndTemplateStart()
                .plus(shiftTemplateView.getShiftTemplateDuration())
                .toDays()));
        this.endTime = LocalTime.ofSecondOfDay(shiftTemplateView
                                                       .getDurationBetweenRotationStartAndTemplateStart()
                                                       .plus(shiftTemplateView.getShiftTemplateDuration())
                                                       .minusDays(endDayAfterStartDay)
                                                       .getSeconds());
        this.endDayOffset = endDayAfterStartDay % rotationLength;
        this.requiredSkillSet = new HashSet<>(requiredSkillSet);
    }

    public Shift createShiftOnDate(LocalDate startDate, int rotationLength, ZoneId zoneId,
                                   boolean defaultToRotationEmployee) {
        LocalDateTime startDateTime = startDate.atTime(getStartTime());

        LocalDate endDate;
        if (getStartDayOffset() <= getEndDayOffset()) {
            endDate = startDate.plusDays(getEndDayOffset() - getStartDayOffset());
        } else {
            // Happens for shifts that "wrap around" in the rotation (ex: start on last day of the rotation,
            // end on first day of the rotation)
            endDate = startDate.plusDays(rotationLength + getEndDayOffset() - getStartDayOffset());
        }
        LocalDateTime endDateTime = endDate.atTime(getEndTime());

        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime,
                                                               zoneId.getRules().getOffset(startDateTime));
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime, zoneId.getRules().getOffset(endDateTime));
        Shift shift = new Shift(getTenantId(), getSpot(), startOffsetDateTime, endOffsetDateTime, rotationEmployee,
                                new HashSet<>(requiredSkillSet), null);
        if (defaultToRotationEmployee) {
            shift.setEmployee(rotationEmployee);
        }
        return shift;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public Integer getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(Integer offsetStartDay) {
        this.startDayOffset = offsetStartDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getEndDayOffset() {
        return endDayOffset;
    }

    public void setEndDayOffset(Integer offsetEndDay) {
        this.endDayOffset = offsetEndDay;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Employee getRotationEmployee() {
        return rotationEmployee;
    }

    public void setRotationEmployee(Employee rotationEmployee) {
        this.rotationEmployee = rotationEmployee;
    }

    public Set<Skill> getRequiredSkillSet() {
        return requiredSkillSet;
    }

    public void setRequiredSkillSet(Set<Skill> requiredSkillSet) {
        this.requiredSkillSet = requiredSkillSet;
    }
}
