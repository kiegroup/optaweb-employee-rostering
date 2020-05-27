/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.optaweb.employeerostering.service.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.Functions;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.uni.DefaultUniConstraintCollector;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

import static java.time.Duration.between;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumDuration;
import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.greaterThan;
import static org.optaplanner.core.api.score.stream.Joiners.greaterThanOrEqual;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;
import static org.optaplanner.core.api.score.stream.Joiners.lessThanOrEqual;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.EXTREME;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.HIGH;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.INOCULATED;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.LOW;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.MODERATE;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.DESIRED;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNAVAILABLE;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNDESIRED;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_ASSIGN_EVERY_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_EXTREME_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_HIGH_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_LOW_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MODERATE_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_OVERLAPPING_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;

/**
 * Designed to match the DRL exactly.
 * Any score discrepancy between the two would be considered a bug.
 */
public final class EmployeeRosteringConstraintProvider implements ConstraintProvider {

    private static BiConstraintStream<EmployeeAvailability, Shift> getConstraintStreamWithAvailabilityIntersections(
            ConstraintFactory constraintFactory, EmployeeAvailabilityState employeeAvailabilityState) {
        return constraintFactory.from(EmployeeAvailability.class)
                .filter(employeeAvailability -> employeeAvailability.getState() == employeeAvailabilityState)
                .join(Shift.class,
                        equal(EmployeeAvailability::getEmployee, Shift::getEmployee),
                        lessThan(EmployeeAvailability::getStartDateTime, Shift::getEndDateTime),
                        greaterThan(EmployeeAvailability::getEndDateTime, Shift::getStartDateTime));
    }

    private static UniConstraintStream<Shift> getAssignedShiftConstraintStream(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(Shift.class) // To match DRL
                .filter(shift -> shift.getEmployee() != null);
    }

    private static LocalDate extractFirstDayOfWeek(DayOfWeek weekStarting, OffsetDateTime date) {
        return date.with(TemporalAdjusters.previousOrSame(weekStarting)).toLocalDate();
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                lowRiskEmployeeAssignedToCovidWard(constraintFactory),
                moderateRiskEmployeeAssignedToCovidWard(constraintFactory),
                highRiskEmployeeAssignedToCovidWard(constraintFactory),
                extremeRiskEmployeeAssignedToCovidWard(constraintFactory),
                inoculatedEmployeeOutsideCovidWard(constraintFactory),
                uniformDistributionOfInoculatedHours(constraintFactory),
                maximizeInoculatedHours(constraintFactory),
                migrationBetweenCovidAndNonCovidWards(constraintFactory),
                requiredSkillForShift(constraintFactory),
                unavailableEmployeeTimeSlot(constraintFactory),
                noOverlappingShifts(constraintFactory),
                noMoreThanTwoConsecutiveShifts(constraintFactory),
                breakBetweenNonConsecutiveShiftsIsAtLeastTenHours(constraintFactory),
                dailyMinutesMustNotExceedContractMaximum(constraintFactory),
                weeklyMinutesMustNotExceedContractMaximum(constraintFactory),
                monthlyMinutesMustNotExceedContractMaximum(constraintFactory),
                yearlyMinutesMustNotExceedContractMaximum(constraintFactory),
                assignEveryShift(constraintFactory),
                employeeIsNotOriginalEmployee(constraintFactory),
                undesiredEmployeeTimeSlot(constraintFactory),
                desiredEmployeeTimeSlot(constraintFactory),
                employeeNotRotationEmployee(constraintFactory)
        };
    }

    private Constraint riskEmployeeAssignedToCovidWard(ConstraintFactory constraintFactory, CovidRiskType riskType,
            String constraintName) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> shift.getSpot().isCovidWard())
                .filter(shift -> shift.getEmployee().getCovidRiskType() == riskType)
                .penalizeConfigurableLong(constraintName, Shift::getLengthInMinutes);
    }

    Constraint lowRiskEmployeeAssignedToCovidWard(ConstraintFactory constraintFactory) {
        return riskEmployeeAssignedToCovidWard(constraintFactory, LOW,
                CONSTRAINT_COVID_LOW_RISK_EMPLOYEE);
    }

    Constraint moderateRiskEmployeeAssignedToCovidWard(ConstraintFactory constraintFactory) {
        return riskEmployeeAssignedToCovidWard(constraintFactory, MODERATE,
                CONSTRAINT_COVID_MODERATE_RISK_EMPLOYEE);
    }

    Constraint highRiskEmployeeAssignedToCovidWard(ConstraintFactory constraintFactory) {
        return riskEmployeeAssignedToCovidWard(constraintFactory, HIGH,
                CONSTRAINT_COVID_HIGH_RISK_EMPLOYEE);
    }

    Constraint extremeRiskEmployeeAssignedToCovidWard(ConstraintFactory constraintFactory) {
        return riskEmployeeAssignedToCovidWard(constraintFactory, EXTREME,
                CONSTRAINT_COVID_EXTREME_RISK_EMPLOYEE);
    }

    Constraint inoculatedEmployeeOutsideCovidWard(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> !shift.getSpot().isCovidWard())
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .penalizeConfigurableLong(CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD,
                        Shift::getLengthInMinutes);
    }

    Constraint uniformDistributionOfInoculatedHours(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .filter(shift -> shift.getSpot().isCovidWard())
                .groupBy(new DefaultUniConstraintCollector<>(
                        LoadBalancingHourCounter::new,
                        (container, shift) -> {
                            container.increaseHourlyCount(shift);
                            return () -> container.decreaseHourlyCount(shift);
                        },
                        LoadBalancingHourCounter::getLoadBalance
                ))
                .penalizeConfigurableLong(CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS,
                        loadBalance -> loadBalance);
    }

    Constraint maximizeInoculatedHours(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .rewardConfigurableLong(CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS, Shift::getLengthInMinutes);
    }

    Constraint migrationBetweenCovidAndNonCovidWards(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> shift.getSpot().isCovidWard())
                .join(Shift.class,
                        equal(Shift::getEmployee),
                        lessThanOrEqual(Shift::getEndDateTime, Shift::getStartDateTime))
                .filter((covidShift, futureShift) -> !futureShift.getSpot().isCovidWard())
                .ifNotExists(Shift.class,
                        equal((covidShift, nonCovidShift) -> covidShift.getEmployee(), Shift::getEmployee),
                        lessThanOrEqual((covidShift, nonCovidShift) -> covidShift.getEndDateTime(),
                                Shift::getStartDateTime),
                        greaterThanOrEqual((covidShift, nonCovidShift) -> nonCovidShift.getStartDateTime(),
                                Shift::getEndDateTime))
                .penalizeConfigurableLong(CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS,
                        (covidShift, nonCovidShift) -> nonCovidShift.getLengthInMinutes());
    }

    Constraint requiredSkillForShift(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> !shift.hasRequiredSkills())
                .penalizeConfigurableLong(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT, Shift::getLengthInMinutes);
    }

    Constraint unavailableEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory, UNAVAILABLE)
                .penalizeConfigurableLong(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE,
                        ((employeeAvailability, shift) -> shift.getLengthInMinutes()));
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .join(Shift.class,
                        equal(Shift::getEmployee),
                        lessThan(Shift::getStartDateTime, Shift::getEndDateTime),
                        greaterThan(Shift::getEndDateTime, Shift::getStartDateTime))
                .filter((shift, otherShift) -> !Objects.equals(shift, otherShift))
                .penalizeConfigurableLong(CONSTRAINT_NO_OVERLAPPING_SHIFTS,
                        (shift, otherShift) -> otherShift.getLengthInMinutes());
    }

    Constraint noMoreThanTwoConsecutiveShifts(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .join(Shift.class,
                        equal(Shift::getEmployee),
                        equal(Shift::getEndDateTime, Shift::getStartDateTime))
                .filter((s1, s2) -> !Objects.equals(s1, s2))
                .join(Shift.class,
                        equal((s1, s2) -> s2.getEmployee(), Shift::getEmployee),
                        equal((s1, s2) -> s2.getEndDateTime(), Shift::getStartDateTime))
                .penalizeConfigurableLong(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS,
                        (s1, s2, s3) -> s3.getLengthInMinutes());
    }

    Constraint breakBetweenNonConsecutiveShiftsIsAtLeastTenHours(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .join(Shift.class,
                        equal(Shift::getEmployee),
                        lessThan(Shift::getEndDateTime, Shift::getStartDateTime))
                .filter((s1, s2) -> !Objects.equals(s1, s2))
                .filter((s1, s2) -> s1.getEndDateTime().until(s2.getStartDateTime(), ChronoUnit.HOURS) < 10)
                .penalizeConfigurableLong(CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS, (s1, s2) -> {
                    long breakLength = s1.getEndDateTime().until(s2.getStartDateTime(), ChronoUnit.MINUTES);
                    return (10 * 60) - breakLength;
                });
    }

    Constraint dailyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerDay() != null)
                .join(Shift.class, equal(Function.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> shift.getStartDateTime().toLocalDate(),
                        sumDuration((employee, shift) ->
                                between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek, totalWorkingTime) ->
                        totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerDay())
                .penalizeConfigurableLong(CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) ->
                                totalWorkingTime.toMinutes() - employee.getContract().getMaximumMinutesPerDay());
    }

    Constraint weeklyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(RosterConstraintConfiguration.class)
                .join(Employee.class)
                .filter((configuration, employee) -> employee.getContract().getMaximumMinutesPerWeek() != null)
                .join(Shift.class, equal((configuration, employee) -> employee, Shift::getEmployee))
                .groupBy((configuration, employee, shift) -> employee,
                        (configuration, employee, shift) ->
                                extractFirstDayOfWeek(configuration.getWeekStartDay(), shift.getStartDateTime()),
                        sumDuration((configuration, employee, shift) ->
                                between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek, totalWorkingTime) ->
                        totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerWeek())
                .penalizeConfigurableLong(CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) ->
                                totalWorkingTime.toMinutes() - employee.getContract().getMaximumMinutesPerWeek());
    }

    Constraint monthlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerMonth() != null)
                .join(Shift.class, equal(Functions.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> YearMonth.from(shift.getStartDateTime()),
                        sumDuration((employee, shift) ->
                                between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek, totalWorkingTime) ->
                        totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerMonth())
                .penalizeConfigurableLong(CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) ->
                                totalWorkingTime.toMinutes() - employee.getContract().getMaximumMinutesPerMonth());
    }

    Constraint yearlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerYear() != null)
                .join(Shift.class, equal(Functions.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> shift.getStartDateTime().getYear(),
                        sumDuration((employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek, totalWorkingTime) ->
                        totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerYear())
                .penalizeConfigurableLong(CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) ->
                                totalWorkingTime.toMinutes() - employee.getContract().getMaximumMinutesPerYear());
    }

    Constraint assignEveryShift(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(Shift.class) // To match DRL.
                .filter(shift -> shift.getEmployee() == null)
                .penalizeConfigurable(CONSTRAINT_ASSIGN_EVERY_SHIFT);
    }

    Constraint employeeIsNotOriginalEmployee(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> shift.getOriginalEmployee() != null)
                .filter(shift -> !Objects.equals(shift.getEmployee(), shift.getOriginalEmployee()))
                .penalizeConfigurableLong(CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE, Shift::getLengthInMinutes);
    }

    Constraint undesiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory, UNDESIRED)
                .penalizeConfigurableLong(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE,
                        (employeeAvailability, shift) -> employeeAvailability.getDuration().toMinutes());
    }

    Constraint desiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory, DESIRED)
                .rewardConfigurableLong(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE,
                        (employeeAvailability, shift) -> employeeAvailability.getDuration().toMinutes());
    }

    Constraint employeeNotRotationEmployee(ConstraintFactory constraintFactory) {
        return getAssignedShiftConstraintStream(constraintFactory)
                .filter(shift -> !Objects.equals(shift.getRotationEmployee(), shift.getEmployee()))
                .penalizeConfigurableLong(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE, Shift::getLengthInMinutes);
    }
}
