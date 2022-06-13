package org.optaweb.employeerostering.domain.rotation.view;

import org.optaweb.employeerostering.domain.rotation.Seat;

public class SeatView {
    private Integer dayInRotation;
    private Long employeeId;

    public SeatView() {
    }

    public SeatView(Seat seat) {
        this.dayInRotation = seat.getDayInRotation();
        this.employeeId = (seat.getEmployee() != null) ? seat.getEmployee().getId() : null;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getDayInRotation() {
        return dayInRotation;
    }

    public void setDayInRotation(Integer dayInRotation) {
        this.dayInRotation = dayInRotation;
    }
}
