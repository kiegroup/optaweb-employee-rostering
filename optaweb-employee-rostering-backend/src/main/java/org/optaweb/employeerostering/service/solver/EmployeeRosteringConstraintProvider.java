package org.optaweb.employeerostering.service.solver;

import static java.time.Duration.between;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumDuration;
import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.greaterThan;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;
import static org.optaplanner.core.api.score.stream.Joiners.overlapping;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.DESIRED;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNAVAILABLE;
import static org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState.UNDESIRED;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_ASSIGN_EVERY_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.function.Function;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

/**
 * Designed to match the DRL exactly.
 * Any score discrepancy between the two would be considered a bug.
 */
public final class EmployeeRosteringConstraintProvider implements ConstraintProvider {

    private static BiConstraintStream<EmployeeAvailability, Shift> getConstraintStreamWithAvailabilityIntersections(
            ConstraintFactory constraintFactory, EmployeeAvailabilityState employeeAvailabilityState) {
        return constraintFactory.forEach(EmployeeAvailability.class)
                .filter(employeeAvailability -> employeeAvailability.getState() == employeeAvailabilityState)
                .join(Shift.class,
                        equal(EmployeeAvailability::getEmployee, Shift::getEmployee),
                        lessThan(EmployeeAvailability::getStartDateTime, Shift::getEndDateTime),
                        greaterThan(EmployeeAvailability::getEndDateTime, Shift::getStartDateTime));
    }

    public static LocalDate extractFirstDayOfWeek(DayOfWeek weekStarting, OffsetDateTime date) {
        return date.with(TemporalAdjusters.previousOrSame(weekStarting)).toLocalDate();
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
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

    Constraint requiredSkillForShift(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> !shift.hasRequiredSkills())
                .penalizeConfigurableLong(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT, Shift::getLengthInMinutes);
    }

    Constraint unavailableEmployeeTimeSlot(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory, UNAVAILABLE)
                .penalizeConfigurableLong(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE,
                        ((employeeAvailability, shift) -> shift.getLengthInMinutes()));
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class,
                equal(Shift::getEmployee),
                overlapping(Shift::getStartDateTime, Shift::getEndDateTime))
                .penalizeConfigurableLong(CONSTRAINT_NO_OVERLAPPING_SHIFTS,
                        (shift, otherShift) -> otherShift.getLengthInMinutes());
    }

    Constraint noMoreThanTwoConsecutiveShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Shift.class,
                        equal(Shift::getEmployee),
                        equal(Shift::getEndDateTime, Shift::getStartDateTime))
                .join(Shift.class,
                        equal((s1, s2) -> s2.getEmployee(), Shift::getEmployee),
                        equal((s1, s2) -> s2.getEndDateTime(), Shift::getStartDateTime))
                .penalizeConfigurableLong(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS,
                        (s1, s2, s3) -> s3.getLengthInMinutes());
    }

    Constraint breakBetweenNonConsecutiveShiftsIsAtLeastTenHours(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
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
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerDay() != null)
                .join(Shift.class, equal(Function.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> shift.getStartDateTime().toLocalDate(),
                        sumDuration((employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek,
                        totalWorkingTime) -> totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerDay())
                .penalizeConfigurableLong(CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) -> totalWorkingTime.toMinutes()
                                - employee.getContract().getMaximumMinutesPerDay());
    }

    Constraint weeklyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RosterConstraintConfiguration.class)
                .join(Employee.class)
                .filter((configuration, employee) -> employee.getContract().getMaximumMinutesPerWeek() != null)
                .join(Shift.class, equal((configuration, employee) -> employee, Shift::getEmployee))
                .groupBy((configuration, employee, shift) -> employee,
                        (configuration, employee, shift) -> extractFirstDayOfWeek(configuration.getWeekStartDay(),
                                shift.getStartDateTime()),
                        sumDuration(
                                (configuration, employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek,
                        totalWorkingTime) -> totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerWeek())
                .penalizeConfigurableLong(CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) -> totalWorkingTime.toMinutes()
                                - employee.getContract().getMaximumMinutesPerWeek());
    }

    Constraint monthlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerMonth() != null)
                .join(Shift.class, equal(Function.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> YearMonth.from(shift.getStartDateTime()),
                        sumDuration((employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek,
                        totalWorkingTime) -> totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerMonth())
                .penalizeConfigurableLong(CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) -> totalWorkingTime.toMinutes()
                                - employee.getContract().getMaximumMinutesPerMonth());
    }

    Constraint yearlyMinutesMustNotExceedContractMaximum(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> employee.getContract().getMaximumMinutesPerYear() != null)
                .join(Shift.class, equal(Function.identity(), Shift::getEmployee))
                .groupBy((employee, shift) -> employee,
                        (employee, shift) -> shift.getStartDateTime().getYear(),
                        sumDuration((employee, shift) -> between(shift.getStartDateTime(), shift.getEndDateTime())))
                .filter((employee, firstDayOfWeek,
                        totalWorkingTime) -> totalWorkingTime.toMinutes() > employee.getContract().getMaximumMinutesPerYear())
                .penalizeConfigurableLong(CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                        (employee, firstDayOfWeek, totalWorkingTime) -> totalWorkingTime.toMinutes()
                                - employee.getContract().getMaximumMinutesPerYear());
    }

    Constraint assignEveryShift(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(Shift.class)
                .filter(shift -> shift.getEmployee() == null)
                .penalizeConfigurable(CONSTRAINT_ASSIGN_EVERY_SHIFT);
    }

    Constraint employeeIsNotOriginalEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
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
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> shift.getRotationEmployee() != null && shift.getRotationEmployee() != shift.getEmployee())
                .penalizeConfigurableLong(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE, Shift::getLengthInMinutes);
    }
}
