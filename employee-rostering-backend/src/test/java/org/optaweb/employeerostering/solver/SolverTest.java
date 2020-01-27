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

package org.optaweb.employeerostering.solver;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.test.impl.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreVerifier;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.roster.RosterGenerator;
import org.optaweb.employeerostering.service.solver.WannabeSolverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class SolverTest {

    private static final int TENANT_ID = 0;
    private static final LocalDate START_DATE = LocalDate.of(2019, 5, 13);
    private static final RosterParametrization ROSTER_PARAMETRIZATION = new RosterParametrization();

    @Autowired
    private TestRestTemplate restTemplate;

    private final String rosterPathURI = "http://localhost:8080/rest/tenant/{tenantId}/roster/";

    private ResponseEntity<Void> solveForTenant(Integer tenantId) {
        return restTemplate.postForEntity(rosterPathURI + "solve", null, Void.class, tenantId);
    }

    private ResponseEntity<Void> terminateSolver(Integer tenantId) {
        return restTemplate.postForEntity(rosterPathURI + "terminate", null, Void.class, tenantId);
    }

    private SolverFactory<Roster> getSolverFactory() {
        SolverFactory<Roster> solverFactory = SolverFactory.createFromXmlResource(WannabeSolverManager.SOLVER_CONFIG);
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig()
                                                                     .withScoreCalculationCountLimit(10000L));
        return solverFactory;
    }

    private HardMediumSoftLongScoreVerifier<Roster> getScoreVerifier() {
        return new HardMediumSoftLongScoreVerifier<Roster>(getSolverFactory());
    }

    @Test
    public void testTerminateNonExistentSolver() {
        try {
            ResponseEntity<Void> terminateResponse = terminateSolver(0);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("The roster with tenantId (0) is not being solved currently.");
        }
    }

    // A solver "integration" test that verify that our constraints can create a feasible
    // solution on our demo data set
    @Test(timeout = 600000)
    public void testFeasibleSolution() {
        Solver<Roster> solver = getSolverFactory().buildSolver();

        RosterGenerator rosterGenerator = buildRosterGenerator();
        Roster roster = rosterGenerator.generateRoster(10, 7);

        roster = solver.solve(roster);
        assertNotNull(roster.getScore());
        // Due to overconstrained planning, the score is always feasible
        assertTrue(roster.getScore().isFeasible());
        assertFalse(roster.getShiftList().isEmpty());
        assertTrue(roster.getShiftList().stream().anyMatch(s -> s.getEmployee() != null));
    }
    
    // A solver "integration" test that verify it moves only draft shifts
    @Test(timeout = 600000)
    public void testMoveOnlyDraftShifts() {
        Solver<Roster> solver = getSolverFactory().buildSolver();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = getDefaultContract(idGenerator);
        
        Skill skill = new Skill(TENANT_ID, "Skill");
        skill.setId(idGenerator.getAndIncrement());

        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());
        
        Employee employeeB = new Employee(TENANT_ID, "Bill", contract, Collections.singleton(skill));
        employeeB.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.singleton(skill));
        spotA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(rosterState.getFirstPublishedDate().atTime(9, 0),
                                                         ZoneOffset.UTC);
        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(8))
                .withTimeBetweenShifts(Duration.ofDays(1));

        List<Shift> shiftList = shiftBuilder.generateShifts(14);
        shiftList.forEach(s -> s.setEmployee(employeeA));

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Arrays.asList(employeeA, employeeB));
        roster.setSkillList(Collections.singletonList(skill));
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(shiftList);
        
        roster = solver.solve(roster);
        assertTrue(roster.getShiftList().stream()
                   .filter(s -> !rosterState.isDraft(s))
                   .allMatch(s -> s.getEmployee().equals(employeeA)));
        assertTrue(roster.getShiftList().stream()
                   .filter(rosterState::isDraft)
                   .allMatch(s -> s.getEmployee().equals(employeeB)));
    }

    private void testContractConstraint(ContractField contractField) {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = contractField.getContract(idGenerator);
        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());

        employeeA.setId(idGenerator.getAndIncrement());
        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        LocalDate firstDayOfWeek = START_DATE.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime firstDateTime = OffsetDateTime.of(firstDayOfWeek, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(1));

        List<Shift> shiftList = contractField.generateShifts(shiftBuilder);
        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(shiftList);

        shiftList.get(0).setEmployee(employeeA);
        shiftList.get(1).setEmployee(employeeA);

        shiftList.get(3).setEmployee(employeeA);
        shiftList.get(4).setEmployee(employeeA);

        Constraints constraint = contractField.getConstraint();

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);

        shiftList.get(2).setEmployee(employeeA);

        // -1 for each shift in overloaded week
        constraint.verifyNumOfInstances(scoreVerifier, roster, 3);

        shiftList.get(5).setEmployee(employeeA);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 6);

        shiftList.get(1).setEmployee(null);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 3);
    }

    @Test(timeout = 600000)
    public void testContractConstraints() {
        for (ContractField field : ContractField.values()) {
            testContractConstraint(field);
        }
    }

    @Test(timeout = 600000)
    public void testRequiredSkillForShiftConstraint() {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Skill skillA = new Skill(TENANT_ID, "Skill A");
        Skill skillB = new Skill(TENANT_ID, "Skill B");
        skillA.setId(idGenerator.getAndIncrement());
        skillB.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot A", new HashSet<>(Arrays.asList(skillA, skillB)));
        spotA.setId(idGenerator.getAndIncrement());

        Contract contract = getDefaultContract(idGenerator);
        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        Shift shift = new Shift(TENANT_ID, spotA, firstDateTime, firstDateTime.plusHours(9));
        shift.setId(idGenerator.getAndIncrement());
        shift.setEmployee(employeeA);

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Arrays.asList(skillA, skillB));
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(Collections.singletonList(shift));

        final Constraints constraint = Constraints.REQUIRED_SKILL_FOR_A_SHIFT;
        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        employeeA.setSkillProficiencySet(new HashSet<>(Collections.singleton(skillA)));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        employeeA.setSkillProficiencySet(new HashSet<>(Collections.singleton(skillB)));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        employeeA.setSkillProficiencySet(new HashSet<>(Arrays.asList(skillA, skillB)));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);
    }

    private void testAvailabilityConstraint(EmployeeAvailabilityState availabilityState) {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = getDefaultContract(idGenerator);

        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        Shift shift = new Shift(TENANT_ID, spotA, firstDateTime, firstDateTime.plusHours(9));
        shift.setId(idGenerator.getAndIncrement());
        shift.setEmployee(employeeA);

        EmployeeAvailability availability = new EmployeeAvailability(TENANT_ID, employeeA, firstDateTime,
                                                                     firstDateTime.plusHours(9));
        availability.setId(idGenerator.getAndIncrement());
        availability.setState(availabilityState);

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.singletonList(availability));
        roster.setShiftList(Collections.singletonList(shift));

        Constraints constraint;

        switch (availabilityState) {
            case DESIRED:
                constraint = Constraints.DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
                break;

            case UNDESIRED:
                constraint = Constraints.UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
                break;

            case UNAVAILABLE:
                constraint = Constraints.UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE;
                break;

            default:
                throw new IllegalArgumentException("No case for (" + availabilityState + ")");
        }

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shift.setStartDateTime(firstDateTime.minusHours(3));
        shift.setEndDateTime(firstDateTime.plusHours(6));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shift.setStartDateTime(firstDateTime.plusHours(3));
        shift.setEndDateTime(firstDateTime.plusHours(12));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shift.setStartDateTime(firstDateTime.plusHours(12));
        shift.setEndDateTime(firstDateTime.plusHours(21));

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);
    }

    @Test(timeout = 600000)
    public void testEmployeeAvilabilityConstraints() {
        for (EmployeeAvailabilityState state : EmployeeAvailabilityState.values()) {
            testAvailabilityConstraint(state);
        }
    }

    @Test(timeout = 600000)
    public void testAtMostOneShiftAssignmentPerDayPerEmployee() {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = getDefaultContract(idGenerator);

        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(1))
                .withTimeBetweenShifts(Duration.ofHours(1));

        List<Shift> shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(shiftList);

        final Constraints constraint = Constraints.AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE;
        constraint.verifyNumOfInstances(scoreVerifier, roster, 2);

        shiftBuilder.withTimeBetweenShifts(Duration.ofDays(1));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);

        // Start time is midnight, so one hour before is a different day
        shiftBuilder.withTimeBetweenShifts(Duration.ofHours(-1));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);
    }

    @Test(timeout = 600000)
    public void testNoTwoShiftsWithin10HoursOfEachOther() {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = getDefaultContract(idGenerator);

        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(1))
                .withTimeBetweenShifts(Duration.ofHours(1));

        List<Shift> shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(shiftList);

        final Constraints constraint = Constraints.NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER;
        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shiftBuilder.withTimeBetweenShifts(Duration.ofHours(10));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);

        // Although start times are 10 hours apart, first end time is 9 hours apart
        // from next start time
        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shiftBuilder.withTimeBetweenShifts(Duration.ofHours(11));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);

        // Start time is midnight, so one hour before is a different day
        shiftBuilder.withTimeBetweenShifts(Duration.ofHours(-1));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);
    }

    @Test(timeout = 600000)
    public void testAssignEveryShift() {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract = getDefaultContract(idGenerator);

        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());

        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(1))
                .withTimeBetweenShifts(Duration.ofDays(1));

        List<Shift> shiftList = shiftBuilder.generateShifts(3);

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(shiftList);

        final Constraints constraint = Constraints.ASSIGN_EVERY_SHIFT;
        constraint.verifyNumOfInstances(scoreVerifier, roster, 3);

        shiftList.get(0).setEmployee(employeeA);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 2);

        shiftList.get(1).setEmployee(employeeA);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shiftList.get(2).setEmployee(employeeA);

        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);
    }

    @Test(timeout = 600000)
    public void testEmployeeIsNotRotationEmployeeConstraint() {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Spot spotA = new Spot(TENANT_ID, "Spot A", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        Contract contract = getDefaultContract(idGenerator);
        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());
        Employee rotationEmployee = new Employee(TENANT_ID, "Anna", contract, Collections.emptySet());
        rotationEmployee.setId(idGenerator.getAndIncrement());

        OffsetDateTime firstDateTime = OffsetDateTime.of(START_DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        Shift shift = new Shift(TENANT_ID, spotA, firstDateTime, firstDateTime.plusHours(9));
        shift.setId(idGenerator.getAndIncrement());
        shift.setEmployee(employeeA);
        shift.setRotationEmployee(rotationEmployee);

        roster.setTenantId(TENANT_ID);
        roster.setRosterState(rosterState);
        roster.setSpotList(Collections.singletonList(spotA));
        roster.setEmployeeList(Collections.singletonList(employeeA));
        roster.setSkillList(Collections.emptyList());
        roster.setRosterParametrization(rosterParametrization);
        roster.setEmployeeAvailabilityList(Collections.emptyList());
        roster.setShiftList(Collections.singletonList(shift));

        final Constraints constraint = Constraints.EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE;
        constraint.verifyNumOfInstances(scoreVerifier, roster, 1);

        shift.setEmployee(rotationEmployee);
        constraint.verifyNumOfInstances(scoreVerifier, roster, 0);
    }

    protected RosterGenerator buildRosterGenerator() {
        EntityManager entityManager = mock(EntityManager.class);
        AtomicInteger tenantIdGenerator = new AtomicInteger(0);
        doAnswer(invocation -> {
            Tenant tenant = (Tenant) invocation.getArgument(0);
            tenant.setId(tenantIdGenerator.getAndIncrement());
            return invocation;
        }).when(entityManager).persist(any(Tenant.class));
        AtomicLong idGenerator = new AtomicLong(0L);
        doAnswer(invocation -> {
            AbstractPersistable o = (AbstractPersistable) invocation.getArgument(0);
            o.setId(idGenerator.getAndIncrement());
            return invocation;
        }).when(entityManager).persist(any(AbstractPersistable.class));

        RosterGenerator rosterGenerator = new RosterGenerator(entityManager);
        rosterGenerator.setUpGeneratedData();
        return rosterGenerator;
    }

    private RosterState getRosterState(AtomicLong idGenerator) {
        final int PUBLISH_NOTICE = 7;
        final int PUBLISH_LENGTH = 7;
        final int DRAFT_LENGTH = 14;
        final int ROTATION_OFFSET = 0;
        final int ROTATION_LENGTH = 7;

        RosterState rosterState = new RosterState(TENANT_ID, PUBLISH_NOTICE, START_DATE.minusDays(PUBLISH_NOTICE),
                                                  PUBLISH_LENGTH, DRAFT_LENGTH, ROTATION_OFFSET, ROTATION_LENGTH,
                                                  START_DATE.minusDays(2 * PUBLISH_NOTICE), ZoneId.systemDefault());
        rosterState.setId(idGenerator.getAndIncrement());
        return rosterState;
    }

    private Contract getDefaultContract(AtomicLong idGenerator) {
        Contract out = new Contract(TENANT_ID, "Default Contract", null, null, null, null);
        out.setId(idGenerator.getAndIncrement());
        return out;
    }

    private RosterParametrization getRosterParametrization(AtomicLong idGenerator) {
        ROSTER_PARAMETRIZATION.setTenantId(TENANT_ID);
        ROSTER_PARAMETRIZATION.setId(idGenerator.getAndIncrement());
        ROSTER_PARAMETRIZATION.setWeekStartDay(DayOfWeek.MONDAY);
        return ROSTER_PARAMETRIZATION;
    }

    private enum Constraints {
        REQUIRED_SKILL_FOR_A_SHIFT("Required skill for a shift",
                                   HardMediumSoftScore.of(-100, 0, 0)),
        UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE("Unavailable time slot for an employee",
                                              HardMediumSoftScore.of(-50, 0, 0)),
        AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE("At most one shift assignment per day per employee",
                                                          HardMediumSoftScore.of(-10, 0, 0)),
        NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER("No 2 shifts within 10 hours from each other",
                                                    HardMediumSoftScore.of(-1, 0, 0)),
        DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM("Daily minutes must not exceed contract maximum",
                                                       HardMediumSoftScore.of(-1, 0, 0)),
        WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM("Weekly minutes must not exceed contract maximum",
                                                        HardMediumSoftScore.of(-1, 0, 0)),
        MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM("Monthly minutes must not exceed contract maximum",
                                                         HardMediumSoftScore.of(-1, 0, 0)),
        YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM("Yearly minutes must not exceed contract maximum",
                                                        HardMediumSoftScore.of(-1, 0, 0)),
        ASSIGN_EVERY_SHIFT("Assign every shift", HardMediumSoftScore.of(0, -1, 0)),
        UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE("Undesired time slot for an employee",
                                            HardMediumSoftScore.of(0, 0, -ROSTER_PARAMETRIZATION.
                                                    getUndesiredTimeSlotWeight())),
        DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE("Desired time slot for an employee",
                                          HardMediumSoftScore.of(0, 0, ROSTER_PARAMETRIZATION
                                                  .getDesiredTimeSlotWeight())),
        EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE("Employee is not rotation employee",
                                          HardMediumSoftScore.of(0, 0, -ROSTER_PARAMETRIZATION
                                                  .getRotationEmployeeMatchWeight()));

        String constraintName;
        HardMediumSoftScore constraintWeight;

        private Constraints(String constraintName, HardMediumSoftScore constraintWeight) {
            this.constraintName = constraintName;
            this.constraintWeight = constraintWeight;
        }

        public void verifyNumOfInstances(HardMediumSoftLongScoreVerifier<Roster> scoreVerifier, Roster roster,
                                         int numOfInstances) {
            scoreVerifier.assertHardWeight(constraintName, constraintWeight.getHardScore() * numOfInstances, roster);
            scoreVerifier.assertMediumWeight(constraintName, constraintWeight.getMediumScore() * numOfInstances,
                                             roster);
            scoreVerifier.assertSoftWeight(constraintName, constraintWeight.getSoftScore() * numOfInstances, roster);
        }
    }

    private enum ContractField {
        DAILY(Constraints.DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM, 2 * 60, null, null, null,
              Duration.ofHours(6), Duration.ofDays(1)),
        WEEKLY(Constraints.WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM, null, 2 * 60, null, null,
               Duration.ofDays(1), Duration.ofDays(7)),
        MONTHLY(Constraints.MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM, null, null, 2 * 60, null,
                Duration.ofDays(7), Duration.ofDays(31)),
        ANNUALLY(Constraints.YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM, null, null, null, 2 * 60,
                 Duration.ofDays(31), Duration.ofDays(366));

        Constraints constraint;
        Integer dailyHours, weeklyHours, monthlyHours, yearlyHours;
        Duration timeBetweenShifts, periodLength;

        private ContractField(Constraints constraint, Integer dailyHours, Integer weeklyHours, Integer monthlyHours,
                              Integer yearlyHours, Duration timeBetweenShifts, Duration periodLength) {
            this.constraint = constraint;
            this.dailyHours = dailyHours;
            this.weeklyHours = weeklyHours;
            this.monthlyHours = monthlyHours;
            this.yearlyHours = yearlyHours;
            this.timeBetweenShifts = timeBetweenShifts;
            this.periodLength = periodLength;
        }

        public Constraints getConstraint() {
            return constraint;
        }

        public Contract getContract(AtomicLong idGenerator) {
            Contract out = new Contract(TENANT_ID, "Contract", dailyHours, weeklyHours, monthlyHours,
                                        yearlyHours);
            out.setId(idGenerator.getAndIncrement());
            return out;
        }

        public List<Shift> generateShifts(ShiftBuilder shiftBuilder) {
            List<Shift> out = new ArrayList<>();
            shiftBuilder.withTimeBetweenShifts(timeBetweenShifts);
            out.addAll(shiftBuilder.generateShifts(3));
            shiftBuilder.startingAtDate(shiftBuilder.firstShiftStartTime.plus(periodLength));
            out.addAll(shiftBuilder.generateShifts(3));
            return out;
        }
    }

    private static class ShiftBuilder {

        OffsetDateTime firstShiftStartTime;
        Duration lengthOfShift;
        Duration durationBetweenShifts;
        Spot shiftSpot;
        AtomicLong idGenerator;

        public ShiftBuilder(AtomicLong idGenerator) {
            this.idGenerator = idGenerator;
        }

        public ShiftBuilder startingAtDate(OffsetDateTime startDate) {
            this.firstShiftStartTime = startDate;
            return this;
        }

        public ShiftBuilder withShiftLength(Duration duration) {
            this.lengthOfShift = duration;
            return this;
        }

        public ShiftBuilder withTimeBetweenShifts(Duration duration) {
            this.durationBetweenShifts = duration;
            return this;
        }

        public ShiftBuilder forSpot(Spot spot) {
            this.shiftSpot = spot;
            return this;
        }

        public List<Shift> generateShifts(int numberOfShifts) {
            if (firstShiftStartTime == null || lengthOfShift == null || durationBetweenShifts == null ||
                    shiftSpot == null) {
                throw new IllegalStateException("ShiftBuilder not initialized");
            }
            List<Shift> out = new ArrayList<>();
            OffsetDateTime shiftStart = firstShiftStartTime;

            for (int i = 0; i < numberOfShifts; i++, shiftStart = shiftStart.plus(durationBetweenShifts)) {
                out.add(new Shift(TENANT_ID, shiftSpot, shiftStart, shiftStart.plus(lengthOfShift)));
                out.get(i).setId(idGenerator.getAndIncrement());
            }

            return out;
        }
    }
}
