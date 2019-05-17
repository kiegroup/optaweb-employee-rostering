/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.server.solver;

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
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.test.impl.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreVerifier;
import org.optaweb.employeerostering.server.roster.RosterGenerator;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.shared.roster.Roster;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.tenant.RosterParametrization;
import org.optaweb.employeerostering.shared.tenant.Tenant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class SolverTest {
    
    private final int TENANT_ID = 0;

    private SolverFactory<Roster> getSolverFactory() {
        SolverFactory<Roster> solverFactory = SolverFactory.createFromXmlResource(WannabeSolverManager.SOLVER_CONFIG);
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig().withScoreCalculationCountLimit(10000L));
        return solverFactory;
    }

    private HardMediumSoftLongScoreVerifier<Roster> getScoreVerifier() {
        return new HardMediumSoftLongScoreVerifier<Roster>(getSolverFactory());
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
    
    private static class ShiftBuilder {
        OffsetDateTime firstShiftStartTime;
        Duration lengthOfShift = Duration.ofHours(9);
        Duration durationBetweenShifts = Duration.ofDays(1);
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
            List<Shift> out = new ArrayList<>();
            OffsetDateTime shiftStart = firstShiftStartTime;
            
            for(int i = 0; i < numberOfShifts; i++, shiftStart = shiftStart.plus(durationBetweenShifts)) {
                out.add(new Shift(0, shiftSpot, shiftStart, shiftStart.plus(lengthOfShift)));
            }
            out.forEach(s -> s.setId(idGenerator.getAndIncrement()));
            
            return out;
        }
    }
    
    private LocalDate getStartDate() {
        return LocalDate.of(2019,5,13);
    }
    
    private RosterState getRosterState(AtomicLong idGenerator) {
        RosterState rosterState = new RosterState(TENANT_ID, 7, getStartDate().minusWeeks(1), 7, 14, 0, 7, getStartDate().minusWeeks(2),
                                                  ZoneId.systemDefault());
        rosterState.setId(idGenerator.getAndIncrement());
        return rosterState;
    }
    
    private RosterParametrization getRosterParametrization(AtomicLong idGenerator) {
        RosterParametrization rosterParametrization = new RosterParametrization();
        rosterParametrization.setTenantId(TENANT_ID);
        rosterParametrization.setId(idGenerator.getAndIncrement());
        rosterParametrization.setWeekStartDay(DayOfWeek.MONDAY);
        return rosterParametrization;
    }
    
    private enum ContractField {
        DAILY, WEEKLY, MONTHLY, ANNUALLY;
    }
    
    private void testContractConstraint(ContractField contractField) {
        HardMediumSoftLongScoreVerifier<Roster> scoreVerifier = getScoreVerifier();

        AtomicLong idGenerator = new AtomicLong(1L);

        Roster roster = new Roster();
        Tenant tenant = new Tenant("Test Tenant");
        tenant.setId(TENANT_ID);

        RosterState rosterState = getRosterState(idGenerator);
        RosterParametrization rosterParametrization = getRosterParametrization(idGenerator);

        Contract contract;
        switch (contractField) {
            case DAILY:
                contract = new Contract(TENANT_ID, "Max 2 Hours Per Day", 2 * 60, null, null, null);
                break;
            case WEEKLY:
                contract = new Contract(TENANT_ID, "Max 2 Hours Per Week", null, 2 * 60, null, null);
                break;
            case MONTHLY:
                contract = new Contract(TENANT_ID, "Max 2 Hours Per Month", null, null, 2 * 60, null);
                break;
            case ANNUALLY:
                contract = new Contract(TENANT_ID, "Max 2 Hours Per Year", null, null, null, 2 * 60);
                break;
            default:
                throw new IllegalArgumentException("No case for (" + contractField + ")");
        }
        
        Employee employeeA = new Employee(TENANT_ID, "Bill", contract, Collections.emptySet());
        
        employeeA.setId(idGenerator.getAndIncrement());
        Spot spotA = new Spot(TENANT_ID, "Spot", Collections.emptySet());
        spotA.setId(idGenerator.getAndIncrement());

        LocalDate firstDayOfWeek = getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime firstDateTime = OffsetDateTime.of(firstDayOfWeek, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        List<Shift> shiftList = new ArrayList<>();
        
        ShiftBuilder shiftBuilder = new ShiftBuilder(idGenerator)
                .forSpot(spotA)
                .startingAtDate(firstDateTime)
                .withShiftLength(Duration.ofHours(1));
        
        switch (contractField) {
            case DAILY:
                shiftBuilder.withTimeBetweenShifts(Duration.ofHours(6));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                shiftBuilder.startingAtDate(firstDateTime.plusDays(1));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                break;
            case WEEKLY:
                shiftBuilder.withTimeBetweenShifts(Duration.ofDays(1));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                shiftBuilder.startingAtDate(firstDateTime.plusWeeks(1));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                break;
            case MONTHLY:
                shiftBuilder.withTimeBetweenShifts(Duration.ofDays(7));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                shiftBuilder.startingAtDate(firstDateTime.plusMonths(1));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                break;
            case ANNUALLY:
                shiftBuilder.withTimeBetweenShifts(Duration.ofDays(7 * 4));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                shiftBuilder.startingAtDate(firstDateTime.plusYears(1));
                shiftList.addAll(shiftBuilder.generateShifts(3));
                break;
            default:
                throw new IllegalArgumentException("No case for (" + contractField + ")");
        }

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
        
        String contraintName;
        
        switch (contractField) {
            case DAILY:
                contraintName = "Daily minutes must not exceed contract maximum";
                break;
            case WEEKLY:
                contraintName = "Weekly minutes must not exceed contract maximum";
                break;
            case MONTHLY:
                contraintName = "Monthly minutes must not exceed contract maximum";
                break;
            case ANNUALLY:
                contraintName = "Yearly minutes must not exceed contract maximum";
                break;
            default:
                throw new IllegalArgumentException("No case for (" + contractField + ")");
        }

        scoreVerifier.assertHardWeight(contraintName, 0, roster);
        scoreVerifier.assertMediumWeight(contraintName, 0, roster);
        scoreVerifier.assertSoftWeight(contraintName, 0, roster);

        shiftList.get(2).setEmployee(employeeA);

        // -1 for each shift in overloaded week
        scoreVerifier.assertHardWeight(contraintName, -3, roster);
        scoreVerifier.assertMediumWeight(contraintName, 0, roster);
        scoreVerifier.assertSoftWeight(contraintName, 0, roster);

        shiftList.get(5).setEmployee(employeeA);

        scoreVerifier.assertHardWeight(contraintName, -6, roster);
        scoreVerifier.assertMediumWeight(contraintName, 0, roster);
        scoreVerifier.assertSoftWeight(contraintName, 0, roster);

        shiftList.get(1).setEmployee(null);

        scoreVerifier.assertHardWeight(contraintName, -3, roster);
        scoreVerifier.assertMediumWeight(contraintName, 0, roster);
        scoreVerifier.assertSoftWeight(contraintName, 0, roster);
    } 
    
    @Test(timeout = 600000)
    public void testContractConstraints() {
        for (ContractField field : ContractField.values()) {
            testContractConstraint(field);
        }
    }
    
    private Contract getDefaultContract(AtomicLong idGenerator) {
        Contract out = new Contract(TENANT_ID, "Default", null, null, null, null);
        out.setId(idGenerator.getAndIncrement());
        return out;
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
        Employee employeeA = new Employee(0, "Bill", contract, Collections.emptySet());
        employeeA.setId(idGenerator.getAndIncrement());
        
        OffsetDateTime firstDateTime = OffsetDateTime.of(getStartDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC);
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
        
        scoreVerifier.assertHardWeight("Required skill for a shift", -100, roster);
        scoreVerifier.assertMediumWeight("Required skill for a shift", 0, roster);
        scoreVerifier.assertSoftWeight("Required skill for a shift", 0, roster);
        
        employeeA.setSkillProficiencySet(new HashSet<>(Collections.singleton(skillA)));
        
        scoreVerifier.assertHardWeight("Required skill for a shift", -100, roster);
        scoreVerifier.assertMediumWeight("Required skill for a shift", 0, roster);
        scoreVerifier.assertSoftWeight("Required skill for a shift", 0, roster);
        
        employeeA.setSkillProficiencySet(new HashSet<>(Collections.singleton(skillB)));
        
        scoreVerifier.assertHardWeight("Required skill for a shift", -100, roster);
        scoreVerifier.assertMediumWeight("Required skill for a shift", 0, roster);
        scoreVerifier.assertSoftWeight("Required skill for a shift", 0, roster);
        
        employeeA.setSkillProficiencySet(new HashSet<>(Arrays.asList(skillA, skillB)));
        
        scoreVerifier.assertHardWeight("Required skill for a shift", 0, roster);
        scoreVerifier.assertMediumWeight("Required skill for a shift", 0, roster);
        scoreVerifier.assertSoftWeight("Required skill for a shift", 0, roster);
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
        
        OffsetDateTime firstDateTime = OffsetDateTime.of(getStartDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC);
        Shift shift = new Shift(TENANT_ID, spotA, firstDateTime, firstDateTime.plusHours(9));
        shift.setId(idGenerator.getAndIncrement());
        shift.setEmployee(employeeA);
        
        EmployeeAvailability availability = new EmployeeAvailability(TENANT_ID, employeeA, firstDateTime, firstDateTime.plusHours(9));
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
        
        String constraintName;
        int hardScore, softScore;
        
        switch (availabilityState) {
            case DESIRED:
                constraintName = "Desired time slot for an employee";
                hardScore = 0;
                softScore = rosterParametrization.getDesiredTimeSlotWeight();
                break;
                
            case UNDESIRED:
                constraintName = "Undesired time slot for an employee";
                hardScore = 0;
                softScore = -rosterParametrization.getUndesiredTimeSlotWeight();
                break;
                
            case UNAVAILABLE:
                constraintName = "Unavailable time slot for an employee";
                hardScore = -50;
                softScore = 0;
                break;
                
            default:
                throw new IllegalArgumentException("No case for (" + availabilityState + ")");
        }
        
        scoreVerifier.assertHardWeight(constraintName, hardScore, roster);
        scoreVerifier.assertMediumWeight(constraintName, 0, roster);
        scoreVerifier.assertSoftWeight(constraintName, softScore, roster);
        
        shift.setStartDateTime(firstDateTime.minusHours(3));
        shift.setEndDateTime(firstDateTime.plusHours(6));
        
        scoreVerifier.assertHardWeight(constraintName, hardScore, roster);
        scoreVerifier.assertMediumWeight(constraintName, 0, roster);
        scoreVerifier.assertSoftWeight(constraintName, softScore, roster);
        
        shift.setStartDateTime(firstDateTime.plusHours(3));
        shift.setEndDateTime(firstDateTime.plusHours(12));
        
        scoreVerifier.assertHardWeight(constraintName, hardScore, roster);
        scoreVerifier.assertMediumWeight(constraintName, 0, roster);
        scoreVerifier.assertSoftWeight(constraintName, softScore, roster);
        
        shift.setStartDateTime(firstDateTime.plusHours(12));
        shift.setEndDateTime(firstDateTime.plusHours(21));
        
        scoreVerifier.assertHardWeight(constraintName, 0, roster);
        scoreVerifier.assertMediumWeight(constraintName, 0, roster);
        scoreVerifier.assertSoftWeight(constraintName, 0, roster);
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
        
        OffsetDateTime firstDateTime = OffsetDateTime.of(getStartDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC);
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
        
        scoreVerifier.assertHardWeight("At most one shift assignment per day per employee", -20, roster);
        scoreVerifier.assertMediumWeight("At most one shift assignment per day per employee", 0, roster);
        scoreVerifier.assertSoftWeight("At most one shift assignment per day per employee", 0, roster);
        
        shiftBuilder.withTimeBetweenShifts(Duration.ofDays(1));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);
        
        scoreVerifier.assertHardWeight("At most one shift assignment per day per employee", 0, roster);
        scoreVerifier.assertMediumWeight("At most one shift assignment per day per employee", 0, roster);
        scoreVerifier.assertSoftWeight("At most one shift assignment per day per employee", 0, roster);
        
        // Start time is midnight, so one hour before is a different day
        shiftBuilder.withTimeBetweenShifts(Duration.ofHours(-1));
        shiftList = shiftBuilder.generateShifts(2);
        shiftList.forEach(s -> s.setEmployee(employeeA));
        roster.setShiftList(shiftList);
        
        scoreVerifier.assertHardWeight("At most one shift assignment per day per employee", 0, roster);
        scoreVerifier.assertMediumWeight("At most one shift assignment per day per employee", 0, roster);
        scoreVerifier.assertSoftWeight("At most one shift assignment per day per employee", 0, roster);
        
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
}
