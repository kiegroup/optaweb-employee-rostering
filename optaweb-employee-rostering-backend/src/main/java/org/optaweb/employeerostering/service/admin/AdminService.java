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

package org.optaweb.employeerostering.service.admin;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.service.contract.ContractRepository;
import org.optaweb.employeerostering.service.employee.EmployeeAvailabilityRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.roster.RosterGenerator;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.optaweb.employeerostering.service.rotation.TimeBucketRepository;
import org.optaweb.employeerostering.service.shift.ShiftRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.service.tenant.RosterConstraintConfigurationRepository;
import org.optaweb.employeerostering.service.tenant.TenantRepository;

@ApplicationScoped
public class AdminService {

    private ShiftRepository shiftRepository;
    private EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private TimeBucketRepository timeBucketRepository;
    private EmployeeRepository employeeRepository;
    private ContractRepository contractRepository;
    private SpotRepository spotRepository;
    private SkillRepository skillRepository;
    private RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository;
    private RosterStateRepository rosterStateRepository;
    private TenantRepository tenantRepository;

    private RosterGenerator rosterGenerator;

    @Inject
    public AdminService(ShiftRepository shiftRepository,
            EmployeeAvailabilityRepository employeeAvailabilityRepository,
            TimeBucketRepository timeBucketRepository,
            EmployeeRepository employeeRepository,
            ContractRepository contractRepository,
            SpotRepository spotRepository,
            SkillRepository skillRepository,
            RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository,
            RosterStateRepository rosterStateRepository,
            TenantRepository tenantRepository,
            RosterGenerator rosterGenerator) {
        this.shiftRepository = shiftRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.timeBucketRepository = timeBucketRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.spotRepository = spotRepository;
        this.skillRepository = skillRepository;
        this.rosterConstraintConfigurationRepository = rosterConstraintConfigurationRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.tenantRepository = tenantRepository;
        this.rosterGenerator = rosterGenerator;
    }

    @Transactional
    public void resetApplication() {
        deleteAllEntities();
        rosterGenerator.setUpGeneratedData();
    }

    private void deleteAllEntities() {
        // IMPORTANT: Delete entries that has Many-to-One relations first, otherwise we break referential integrity
        shiftRepository.deleteAll();
        employeeAvailabilityRepository.deleteAll();
        timeBucketRepository.deleteAll();
        employeeRepository.deleteAll();
        contractRepository.deleteAll();
        spotRepository.deleteAll();
        skillRepository.deleteAll();
        rosterConstraintConfigurationRepository.deleteAll();
        rosterStateRepository.deleteAll();
        tenantRepository.deleteAll();
    }
}
