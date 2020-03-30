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
import org.optaplanner.core.impl.score.stream.uni.DefaultUniConstraintCollector;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

import static java.time.Duration.between;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumDuration;
import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.EXTREME;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.HIGH;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.INOCULATED;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.LOW;
import static org.optaweb.employeerostering.domain.employee.CovidRiskType.MODERATE;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.DESIRED;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNAVAILABLE;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNDESIRED;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_ASSIGN_EVERY_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_EXTREME_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_HIGH_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_LOW_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_MODERATE_RISK_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_NON_COVID_SHIFT_STARTED_SOON_AFTER_FINISHING_A_COVID_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;

public final class EmployeeRosteringConstraintProvider implements ConstraintProvider {

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
                nonCovidShiftStartedLessThan8HoursAfterFinishingCovidShift(constraintFactory),
                requiredSkillForShift(constraintFactory),
                unavailableEmployeeTimeSlot(constraintFactory),
                atMostOneShiftPerDayPerEmployee(constraintFactory),
                noTwoShiftsWithin10HoursFromEachOther(constraintFactory),
                dailyMinutesMustNotExceedContractMaximum(constraintFactory),
                weeklyMinutesMustNotExceedContractMaximum(constraintFactory),
                monthlyMinutesMustNotExceedContractMaximum(constraintFactory),
                yearlyMinutesMustNotExceedContractMaximum(constraintFactory),
                assignEveryShift(constraintFactory),
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
                .rewardConfigurableLong(CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS, Shift::getLengthInHours);
    }

    Constraint migrationBetweenCovidAndNonCovidWards(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> shift.getSpot().isCovidWard())
                .join(Shift.class, equal(Shift::getEmployee))
                .filter((covidShift, otherShift) -> !otherShift.getSpot().isCovidWard())
                .filter((covidShift, nonCovidShift) -> nonCovidShift.follows(covidShift))
                .ifNotExists(Shift.class,
                        equal((covidShift, nonCovidShift) -> covidShift.getEmployee(), Shift::getEmployee),
                        filtering((covidShift, nonCovidShift, otherShift) -> otherShift.precedes(nonCovidShift)),
                        filtering((covidShift, nonCovidShift, otherShift) -> otherShift.follows(covidShift)))
                .penalizeConfigurable(CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS);
    }

    Constraint nonCovidShiftStartedLessThan8HoursAfterFinishingCovidShift(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> !shift.getSpot().isCovidWard())
                .join(Shift.class, equal(Shift::getEmployee))
                .filter((nonCovidShift, otherShift) -> otherShift.getSpot().isCovidWard())
                .filter((nonCovidShift, covidShift) -> covidShift.precedes(nonCovidShift))
                .filter((nonCovidShift, covidShift) ->
                        covidShift.getEndDateTime().until(nonCovidShift.getStartDateTime(), HOURS) < 8)
                .ifNotExists(Shift.class,
                        equal((nonCovidShift, covidShift) -> covidShift.getEmployee(), Shift::getEmployee),
                        filtering((nonCovidShift, covidShift, otherShift) -> otherShift.precedes(nonCovidShift)),
                        filtering((nonCovidShift, covidShift, otherShift) -> otherShift.follows(covidShift)))
                .penalizeConfigurable(CONSTRAINT_COVID_NON_COVID_SHIFT_STARTED_SOON_AFTER_FINISHING_A_COVID_SHIFT);
    }

    Constraint requiredSkillForShift(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> shift.getEmployee() != null)
                .filter(shift -> !shift.getEmployee().hasSkills(shift.getSpot().getRequiredSkillSet()))
                .penalizeConfigurable(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT);
    }

    Constraint unavailableEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return constraintFactory.from(EmployeeAvailability.class)
                .filter(employeeAvailability -> employeeAvailability.getState() == UNAVAILABLE)
                .join(Shift.class, equal(EmployeeAvailability::getEmployee, Shift::getEmployee))
                .filter((employeeAvailability, shift) -> doTimeslotsIntersect(
                        employeeAvailability.getStartDateTime(), employeeAvailability.getEndDateTime(),
                        shift.getStartDateTime(), shift.getEndDateTime()))
                .penalizeConfigurable(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint atMostOneShiftPerDayPerEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Shift.class,
                equal(Shift::getEmployee),
                equal(shift -> shift.getStartDateTime().toLocalDate()))
                .filter((shift1, shift2) -> shift1.getEmployee() != null)
                .penalizeConfigurable(CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE);
    }

    Constraint noTwoShiftsWithin10HoursFromEachOther(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .join(Shift.class, equal(Shift::getEmployee))
                .filter(Shift::precedes)
                .filter((shift1, shift2) -> shift1.getEndDateTime().until(shift2.getStartDateTime(), HOURS) < 10)
                .penalizeConfigurable(CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER);
    }

    private long getOverMaximum(long maximum, long current) {
        return Math.max(current - maximum, 0);
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
                                getOverMaximum(employee.getContract().getMaximumMinutesPerDay(),
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
                                getOverMaximum(employee.getContract().getMaximumMinutesPerWeek(),
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
                                getOverMaximum(employee.getContract().getMaximumMinutesPerMonth(),
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
                                getOverMaximum(employee.getContract().getMaximumMinutesPerYear(),
                                        totalWorkingTime.toMinutes()));
    }

    Constraint assignEveryShift(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(Shift.class)
                .filter(shift -> shift.getEmployee() == null)
                .penalizeConfigurable(CONSTRAINT_ASSIGN_EVERY_SHIFT);
    }

    Constraint undesiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return constraintFactory.from(EmployeeAvailability.class)
                .filter(availability -> availability.getState() == UNDESIRED)
                .join(Shift.class, equal(EmployeeAvailability::getEmployee, Shift::getEmployee))
                .filter((availability, shift) ->
                        doTimeslotsIntersect(availability.getStartDateTime(), availability.getEndDateTime(),
                                shift.getStartDateTime(), shift.getEndDateTime()))
                .penalizeConfigurable(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint desiredEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return constraintFactory.from(EmployeeAvailability.class)
                .filter(availability -> availability.getState() == DESIRED)
                .join(Shift.class, equal(EmployeeAvailability::getEmployee, Shift::getEmployee))
                .filter((availability, shift) ->
                        doTimeslotsIntersect(availability.getStartDateTime(), availability.getEndDateTime(),
                                shift.getStartDateTime(), shift.getEndDateTime()))
                .rewardConfigurable(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE);
    }

    Constraint employeeNotRotationEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shift.class)
                .filter(shift -> !Objects.equals(shift.getRotationEmployee(), shift.getEmployee()))
                .penalizeConfigurable(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE);
    }

    private static boolean doTimeslotsIntersect(OffsetDateTime start1, OffsetDateTime end1, OffsetDateTime start2,
            OffsetDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private static LocalDate extractFirstDayOfWeek(DayOfWeek weekStarting, OffsetDateTime date) {
        return date.with(TemporalAdjusters.previousOrSame(weekStarting)).toLocalDate();
    }

}
