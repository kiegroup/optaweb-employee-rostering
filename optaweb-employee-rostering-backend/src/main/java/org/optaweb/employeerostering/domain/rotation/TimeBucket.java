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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;

@Entity
public class TimeBucket extends AbstractPersistable {
    private LocalTime startTime;
    private LocalTime endTime;
    
    @ManyToOne
    private Spot spot;
    
    @OneToMany
    private Set<Skill> additionalSkillSet;
    
    @ElementCollection
    private Set<DayOfWeek> repeatOnDaySet;
    
    @ElementCollection
    private List<Seat> seatList;
    
    public TimeBucket() {}
    
    public TimeBucket(Integer tenantId, Spot spot, LocalTime startTime, LocalTime endTime,
                      Set<Skill> additionalSkillSet,
                      Set<DayOfWeek> repeatOnDaySet, DayOfWeek startOfWeek, int rotationLength) {
        super(tenantId);
        this.spot = spot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.additionalSkillSet = additionalSkillSet;
        this.repeatOnDaySet = repeatOnDaySet;
        this.seatList = new ArrayList<>(rotationLength);
        DayOfWeek rotationDay = startOfWeek;
        for (int i = 0; i < rotationLength; i++) {
            if (repeatOnDaySet.contains(rotationDay)) {
                seatList.add(new Seat(i,null));
            }
            rotationDay = rotationDay.plus(1);
        }
    }
    
    public TimeBucket(Integer tenantId, Spot spot, LocalTime startTime, LocalTime endTime,
                      Set<Skill> additionalSkillSet,
                      Set<DayOfWeek> repeatOnDaySet, List<Seat> seatList) {
        super(tenantId);
        this.spot = spot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.additionalSkillSet = additionalSkillSet;
        this.repeatOnDaySet = repeatOnDaySet;
        this.seatList = new ArrayList<>(seatList);
    }
    
    
    public Spot getSpot() {
        return spot;
    }
    
    
    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    
    public LocalTime getEndTime() {
        return endTime;
    }

    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    
    public Set<Skill> getAdditionalSkillSet() {
        return additionalSkillSet;
    }

    
    public void setAdditionalSkillSet(Set<Skill> additionalSkillSet) {
        this.additionalSkillSet = additionalSkillSet;
    }

    
    public Set<DayOfWeek> getRepeatOnDaySet() {
        return repeatOnDaySet;
    }

    
    public void setRepeatOnDaySet(Set<DayOfWeek> repeatOnDaySet) {
        this.repeatOnDaySet = repeatOnDaySet;
    }

    
    public List<Seat> getSeatList() {
        return seatList;
    }

    
    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public void setValuesFromTimeBucket(TimeBucket updatedTimeBucket) {
        setStartTime(updatedTimeBucket.getStartTime());
        setEndTime(updatedTimeBucket.getEndTime());
        setAdditionalSkillSet(updatedTimeBucket.getAdditionalSkillSet());
        setRepeatOnDaySet(updatedTimeBucket.getRepeatOnDaySet());
        setSeatList(updatedTimeBucket.getSeatList());   
    }
    
    public Optional<Shift> createShiftForOffset(LocalDate startDate, int offset, ZoneId zoneId,
                                                boolean defaultToRotationEmployee) {
        Optional<Seat> maybeSeat = seatList.stream().filter(seat -> seat.getDayInRotation() == offset).findAny();
        
        if (!maybeSeat.isPresent()) {
            return Optional.empty();
        }
        Seat seat = maybeSeat.get();
        LocalDateTime startDateTime = startDate.atTime(getStartTime());
        LocalDate endDate;
        
        if (getStartTime().isBefore(getEndTime())) {
            // Ex: 9am-5pm
            endDate = startDate;
        } else {
            // Ex: 9pm-6am
            endDate = startDate.plusDays(1);
        }
        LocalDateTime endDateTime = endDate.atTime(getEndTime());

        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime,
                                                               zoneId.getRules().getOffset(startDateTime));
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime, zoneId.getRules().getOffset(endDateTime));
        Shift shift = new Shift(getTenantId(), getSpot(), startOffsetDateTime, endOffsetDateTime, seat.getEmployee(),
                                new HashSet<>(additionalSkillSet), null);
        if (defaultToRotationEmployee) {
            shift.setEmployee(seat.getEmployee());
        }
        return Optional.of(shift);
    }
}
