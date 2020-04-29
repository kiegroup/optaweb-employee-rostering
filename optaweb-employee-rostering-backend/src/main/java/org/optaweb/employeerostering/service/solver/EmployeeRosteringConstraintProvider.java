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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.Functions;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.impl.score.stream.uni.DefaultUniConstraintCollector;
import org.optaweb.employeerostering.domain.common.DateTimeUtils;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

import static java.time.Duration.between;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumDuration;
import static org.optaplanner.core.api.score.stream.Joiners.equal;
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

public final class EmployeeRosteringConstraintProvider implements ConstraintProvider {

    private static BiConstraintStream<Shift, Shift> getConsecutiveCovidAndNonCovidShiftsConstraintStream(
            ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
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
                                Shift::getEndDateTime));
    }

    private static BiConstraintStream<EmployeeAvailability, Shift> getAvailabilityConstraintStream(
            ConstraintFactory constraintFactory, EmployeeAvailabilityState employeeAvailabilityState) {
        return constraintFactory.from(EmployeeAvailability.class)
                .filter(employeeAvailability -> employeeAvailability.getState() == employeeAvailabilityState)
                .join(Shift.class, equal(EmployeeAvailability::getEmployee, Shift::getEmployee))
                .filter((employeeAvailability, shift) -> DateTimeUtils.doTimeslotsIntersect(
                        employeeAvailability.getStartDateTime(), employeeAvailability.getEndDateTime(),
                        shift.getStartDateTime(), shift.getEndDateTime()));
    }

    private static long getHoursOverMaximum(long maximum, long current) {
        long minutesOverMaximum = Math.max(current - maximum, 0);
        long hours = minutesOverMaximum / 60;
        return (minutesOverMaximum % 60 == 0) ? hours : hours + 1;
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
                breakBetweenShiftsIsAtLeastTenHours(constraintFactory),
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
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> shift.getSpot().isCovidWard())
                .filter(shift -> shift.getEmployee().getCovidRiskType() == riskType)
                .penalizeConfigurable(constraintName);
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
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> !shift.getSpot().isCovidWard())
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .penalizeConfigurable(CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD);
    }

    Constraint uniformDistributionOfInoculatedHours(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .groupBy(new DefaultUniConstraintCollector<Shift, Map<OffsetDateTime, Integer>, Collection<Integer>>(
                        () -> new HashMap<>(0),
                        (container, shift) -> {
                            shift.increaseHourlyCounts(container);
                            return () -> shift.decreaseHourlyCounts(container);
                        },
                        Map::values
                ))
                .penalizeConfigurableLong(CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS,
                        Shift::calculateLoad);
    }

    Constraint maximizeInoculatedHours(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> shift.getEmployee().getCovidRiskType() == INOCULATED)
                .rewardConfigurableLong(CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS, Shift::getLengthInMinutes);
    }

    Constraint migrationBetweenCovidAndNonCovidWards(ConstraintFactory constraintFactory) {
        return getConsecutiveCovidAndNonCovidShiftsConstraintStream(constraintFactory)
                .penalizeConfigurable(CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS);
    }

    Constraint requiredSkillForShift(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> !shift.getEmployee().hasSkills(shift.getSpot().getRequiredSkillSet()))
                .penalizeConfigurable(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT);
    }

    Constraint unavailableEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getAvailabilityConstraintStream(constraintFactory, UNAVAILABLE)
                .penalizeConfigurable(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Shift.class,
                equal(Shift::getEmployee),
                lessThanOrEqual(Shift::getStartDateTime))
                .filter((s1, s2) -> !Objects.equals(s1.getEndDateTime(), s2.getStartDateTime()))
                .filter(Shift::intersects)
                .penalizeConfigurableLong(CONSTRAINT_NO_OVERLAPPING_SHIFTS,
                        (s1, s2) -> s2.getLengthInMinutes());
    }

    Constraint noMoreThanTwoConsecutiveShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Shift.class,
                equal(Shift::getEmployee),
                equal(Shift::getEndDateTime, Shift::getStartDateTime))
                .join(Shift.class,
                        equal((s1, s2) -> s2.getEmployee(), Shift::getEmployee),
                        equal((s1, s2) -> s2.getEndDateTime(), Shift::getStartDateTime))
                .penalizeConfigurableLong(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS,
                        (s1, s2, s3) -> s3.getLengthInMinutes());
    }

    Constraint breakBetweenShiftsIsAtLeastTenHours(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Shift.class,
                equal(Shift::getEmployee),
                lessThan(Shift::getEndDateTime, Shift::getStartDateTime))
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
                .penalizeConfigurableLong(CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, day, totalWorkingTime) ->
                                getHoursOverMaximum(employee.getContract().getMaximumMinutesPerDay(),
                                        totalWorkingTime.toMinutes()));
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
                .penalizeConfigurableLong(CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) ->
                                getHoursOverMaximum(employee.getContract().getMaximumMinutesPerWeek(),
                                        totalWorkingTime.toMinutes()));
    }

    Constraint monthlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerMonth() != null)
                .join(Shift.class, equal(Functions.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> YearMonth.from(shift.getStartDateTime()),
                        sumDuration((employee, shift) ->
                                between(shift.getStartDateTime(), shift.getEndDateTime())))
                .penalizeConfigurableLong(CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, month, totalWorkingTime) ->
                                getHoursOverMaximum(employee.getContract().getMaximumMinutesPerMonth(),
                                        totalWorkingTime.toMinutes()));
    }

    Constraint yearlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerYear() != null)
                .join(Shift.class, equal(Functions.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> shift.getStartDateTime().getYear(),
                        sumDuration((employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .penalizeConfigurableLong(CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, year, totalWorkingTime) ->
                                getHoursOverMaximum(employee.getContract().getMaximumMinutesPerYear(),
                                        totalWorkingTime.toMinutes()));
    }

    Constraint assignEveryShift(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() == null)
                .penalizeConfigurable(CONSTRAINT_ASSIGN_EVERY_SHIFT);
    }

    Constraint employeeIsNotOriginalEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> !Objects.equals(shift.getEmployee(), shift.getOriginalEmployee()))
                .penalizeConfigurable(CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE);
    }

    Constraint undesiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getAvailabilityConstraintStream(constraintFactory, UNDESIRED)
                .penalizeConfigurable(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint desiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getAvailabilityConstraintStream(constraintFactory, DESIRED)
                .rewardConfigurable(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint employeeNotRotationEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> !Objects.equals(shift.getRotationEmployee(), shift.getEmployee()))
                .penalizeConfigurable(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE);
    }
}
