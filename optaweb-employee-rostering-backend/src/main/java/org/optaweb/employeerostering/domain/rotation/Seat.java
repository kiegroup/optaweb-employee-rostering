package org.optaweb.employeerostering.domain.rotation;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import org.optaweb.employeerostering.domain.employee.Employee;

/**
 * A Seat is a shift to create in a time bucket for a particular day
 * in the rotation, with an optional employee as the default employee
 * for said shift.
 */
@Embeddable
public class Seat {

    private Integer dayInRotation;

    @ManyToOne
    private Employee employee;

    public Seat() {

    }

    public Seat(Integer dayInRotation, Employee employee) {
        this.setDayInRotation(dayInRotation);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getDayInRotation() {
        return dayInRotation;
    }

    public void setDayInRotation(Integer dayInRotation) {
        this.dayInRotation = dayInRotation;
    }
}
