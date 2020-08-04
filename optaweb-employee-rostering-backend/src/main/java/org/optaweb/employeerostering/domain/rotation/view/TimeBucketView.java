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

package org.optaweb.employeerostering.domain.rotation.view;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.skill.Skill;

public class TimeBucketView extends AbstractPersistable {

    private Long spotId;
    private LocalTime startTime;
    private LocalTime endTime;

    private List<Long> additionalSkillSetIdList;
    private List<DayOfWeek> repeatOnDaySetList;
    private List<SeatView> seatList;

    public TimeBucketView() {
    }

    public TimeBucketView(TimeBucket timeBucket) {
        super(timeBucket.getTenantId());
        setId(timeBucket.getId());
        setVersion(timeBucket.getVersion());
        this.spotId = timeBucket.getSpot().getId();
        this.startTime = timeBucket.getStartTime();
        this.endTime = timeBucket.getEndTime();
        this.additionalSkillSetIdList = timeBucket.getAdditionalSkillSet().stream()
                .map(Skill::getId).sorted().collect(Collectors.toList());
        this.repeatOnDaySetList = timeBucket.getRepeatOnDaySet().stream()
                .sorted().collect(Collectors.toList());
        this.seatList = timeBucket.getSeatList().stream()
                .map(SeatView::new)
                .collect(Collectors.toList());
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
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

    public List<Long> getAdditionalSkillSetIdList() {
        return additionalSkillSetIdList;
    }

    public void setAdditionalSkillSetIdList(List<Long> additionalSkillSetIdList) {
        this.additionalSkillSetIdList = additionalSkillSetIdList;
    }

    public List<DayOfWeek> getRepeatOnDaySetList() {
        return repeatOnDaySetList;
    }

    public void setRepeatOnDaySetList(List<DayOfWeek> repeatOnDaySetList) {
        this.repeatOnDaySetList = repeatOnDaySetList;
    }

    public List<SeatView> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<SeatView> seatList) {
        this.seatList = seatList;
    }
}
