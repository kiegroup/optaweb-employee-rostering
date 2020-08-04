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

package org.optaweb.employeerostering.service.rotation;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.rotation.Seat;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.roster.RosterService;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.optaweb.employeerostering.service.tenant.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class RotationService extends AbstractRestService {

    private final TimeBucketRepository timeBucketRepository;
    private final RosterService rosterService;
    private final TenantService tenantService;
    private final SpotService spotService;
    private final SkillService skillService;
    private final EmployeeService employeeService;

    public RotationService(Validator validator,
            TimeBucketRepository timeBucketRepository, RosterService rosterService,
            TenantService tenantService, SpotService spotService, SkillService skillService,
            EmployeeService employeeService) {
        super(validator);
        this.timeBucketRepository = timeBucketRepository;

        this.tenantService = tenantService;
        Assert.notNull(tenantService, "tenantService must not be null");

        this.rosterService = rosterService;
        Assert.notNull(rosterService, "rosterService must not be null.");

        this.spotService = spotService;
        Assert.notNull(spotService, "spotService must not be null.");

        this.skillService = skillService;
        Assert.notNull(skillService, "skillService must not be null.");

        this.employeeService = employeeService;
        Assert.notNull(employeeService, "employeeService must not be null.");
    }

    private Set<Skill> getRequiredSkillSet(Integer tenantId, TimeBucketView timeBucketView) {
        return timeBucketView.getAdditionalSkillSetIdList()
                .stream().map(id -> skillService.getSkill(tenantId, id))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public List<TimeBucketView> getTimeBucketList(@Min(0) Integer tenantId) {
        return timeBucketRepository.findAllByTenantId(tenantId)
                .stream()
                .map(TimeBucketView::new)
                .collect(Collectors.toList());
    }

    public TimeBucketView getTimeBucket(@Min(0) Integer tenantId, @Min(0) Long id) {
        TimeBucket timeBucket = timeBucketRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No TimeBucket entity found with ID (" + id + ")."));

        validateBean(tenantId, timeBucket);
        return new TimeBucketView(timeBucket);
    }

    @Transactional
    public Boolean deleteTimeBucket(@Min(0) Integer tenantId, @Min(0) Long id) {
        Optional<TimeBucket> timeBucketOptional = timeBucketRepository.findById(id);

        if (!timeBucketOptional.isPresent()) {
            return false;
        }

        validateBean(tenantId, timeBucketOptional.get());
        timeBucketRepository.deleteById(id);
        return true;
    }

    @Transactional
    public TimeBucketView createTimeBucket(@Min(0) Integer tenantId, @Valid TimeBucketView timeBucketView) {
        Spot spot = spotService.getSpot(tenantId, timeBucketView.getSpotId());
        Set<Skill> additionalSkillSet = getRequiredSkillSet(tenantId, timeBucketView);
        Integer rotationLength = rosterService.getRosterState(tenantId).getRotationLength();

        Set<DayOfWeek> repeatOnDaySet = timeBucketView.getRepeatOnDaySetList().stream().collect(Collectors.toSet());
        TimeBucket timeBucket;

        if (timeBucketView.getSeatList() != null) {
            List<Seat> seatList = timeBucketView.getSeatList().stream()
                    .map(seat -> {
                        if (seat.getEmployeeId() != null) {
                            Employee employee = employeeService.getEmployee(tenantId, seat.getEmployeeId());
                            return new Seat(seat.getDayInRotation(), employee);
                        } else {
                            return new Seat(seat.getDayInRotation(), null);
                        }
                    }).collect(Collectors.toList());
            timeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, seatList);
        } else {
            DayOfWeek startOfWeek = tenantService.getRosterConstraintConfiguration(tenantId).getWeekStartDay();
            timeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, startOfWeek, rotationLength);
        }

        validateBean(tenantId, timeBucket);
        timeBucketRepository.save(timeBucket);
        return new TimeBucketView(timeBucket);
    }

    @Transactional
    public TimeBucketView updateTimeBucket(@Min(0) Integer tenantId, @Valid TimeBucketView timeBucketView) {
        Spot spot = spotService.getSpot(tenantId, timeBucketView.getSpotId());
        Set<Skill> additionalSkillSet = getRequiredSkillSet(tenantId, timeBucketView);
        Integer rotationLength = rosterService.getRosterState(tenantId).getRotationLength();

        Set<DayOfWeek> repeatOnDaySet = timeBucketView.getRepeatOnDaySetList().stream().collect(Collectors.toSet());
        TimeBucket newTimeBucket;

        if (timeBucketView.getSeatList() != null) {
            List<Seat> seatList = timeBucketView.getSeatList().stream()
                    .map(seat -> {
                        if (seat.getEmployeeId() != null) {
                            Employee employee = employeeService.getEmployee(tenantId, seat.getEmployeeId());
                            return new Seat(seat.getDayInRotation(), employee);
                        } else {
                            return new Seat(seat.getDayInRotation(), null);
                        }
                    }).collect(Collectors.toList());
            newTimeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, seatList);
            newTimeBucket.setId(timeBucketView.getId());
            newTimeBucket.setVersion(timeBucketView.getVersion());
        } else {
            DayOfWeek startOfWeek = tenantService.getRosterConstraintConfiguration(tenantId).getWeekStartDay();
            newTimeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, startOfWeek, rotationLength);
            newTimeBucket.setId(timeBucketView.getId());
            newTimeBucket.setVersion(timeBucketView.getVersion());
        }

        validateBean(tenantId, newTimeBucket);

        TimeBucket oldTimeBucket = timeBucketRepository
                .findById(newTimeBucket.getId())
                .orElseThrow(() -> new EntityNotFoundException("TimeBucket entity with ID (" +
                        newTimeBucket.getId() + ") not found."));

        if (!oldTimeBucket.getTenantId().equals(newTimeBucket.getTenantId())) {
            throw new IllegalStateException("TimeBucket entity with tenantId (" + oldTimeBucket.getTenantId() +
                    ") cannot change tenants.");
        }

        oldTimeBucket.setValuesFromTimeBucket(newTimeBucket);

        // Flush to increase version number before we duplicate it to TimeBucketView
        TimeBucket updatedTimeBucket = timeBucketRepository.saveAndFlush(oldTimeBucket);

        return new TimeBucketView(updatedTimeBucket);
    }
}
