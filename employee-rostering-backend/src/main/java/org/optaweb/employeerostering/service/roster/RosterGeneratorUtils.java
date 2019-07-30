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

package org.optaweb.employeerostering.service.roster;

import java.time.ZoneId;
import java.util.List;

import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.springframework.stereotype.Component;

import static org.optaweb.employeerostering.service.roster.RosterGenerator.EXTRA_SHIFT_THRESHOLDS;

@Component
public class RosterGeneratorUtils {

    @Transactional
    public Roster generateRoster(int spotListSize,
                                 int lengthInDays,
                                 RosterGenerator.GeneratorType generatorType,
                                 ZoneId zoneId, RosterGenerator rosterGenerator) {
        int maxShiftSizePerDay = generatorType.timeslotRangeList.size() + EXTRA_SHIFT_THRESHOLDS.length;
        // The average employee works 5 days out of 7
        int employeeListSize = spotListSize * maxShiftSizePerDay * 7 / 5;
        int skillListSize = (spotListSize + 4) / 5;

        Tenant tenant = rosterGenerator.createTenant(generatorType, employeeListSize);
        Integer tenantId = tenant.getId();
        RosterParametrization rosterParametrization = rosterGenerator.createTenantConfiguration(generatorType,
                                                                                                tenantId, zoneId);
        RosterState rosterState = rosterGenerator.createRosterState(generatorType, tenant, zoneId, lengthInDays);

        List<Skill> skillList = rosterGenerator.createSkillList(generatorType, tenantId, skillListSize);
        List<Spot> spotList = rosterGenerator.createSpotList(generatorType, tenantId, spotListSize, skillList);
        List<Contract> contractList = rosterGenerator.createContractList(tenantId);
        List<Employee> employeeList = rosterGenerator.createEmployeeList(generatorType, tenantId, employeeListSize,
                                                                         contractList, skillList);
        List<ShiftTemplate> shiftTemplateList = rosterGenerator.createShiftTemplateList(generatorType, tenantId,
                                                                                        rosterState, spotList,
                                                                                        employeeList);
        List<Shift> shiftList = rosterGenerator.createShiftList(generatorType, tenantId, rosterParametrization,
                                                                rosterState, spotList, shiftTemplateList);
        List<EmployeeAvailability> employeeAvailabilityList = rosterGenerator.createEmployeeAvailabilityList(
                generatorType, tenantId, rosterParametrization, rosterState, employeeList, shiftList);

        return new Roster((long) tenantId, tenantId, skillList, spotList, employeeList, employeeAvailabilityList,
                          rosterParametrization, rosterState, shiftList);
    }
}
