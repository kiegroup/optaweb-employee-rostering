package org.optaplanner.openshift.employeerostering.shared.rotation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Entity
@NamedQueries({
               @NamedQuery(name = "ShiftTemplate.findAll",
                           query = "select distinct sa from ShiftTemplate sa" +
                                   " left join fetch sa.spot s" +
                                   " left join fetch sa.rotationEmployee re" +
                                   " where sa.tenantId = :tenantId" +
                                   " order by sa.startDayOffset, sa.startTime, s.name, re.name"),
})
public class ShiftTemplate extends AbstractPersistable {

    @NotNull
    @ManyToOne
    private Spot spot;

    @NotNull
    private Integer startDayOffset;
    @NotNull
    private LocalTime startTime;

    @NotNull
    private Integer endDayOffset;
    @NotNull
    private LocalTime endTime;

    @ManyToOne
    private Employee rotationEmployee;

    @SuppressWarnings("unused")
    public ShiftTemplate() {}

    public ShiftTemplate(Integer tenantId, Spot spot, int startDayOffset, LocalTime startTime, int endDayOffset, LocalTime endTime) {
        this(tenantId, spot, startDayOffset, startTime, endDayOffset, endTime, null);
    }

    public ShiftTemplate(Integer tenantId, Spot spot,
                         int startDayOffset, LocalTime startTime, int endDayOffset, LocalTime endTime,
                         Employee rotationEmployee) {
        super(tenantId);
        this.rotationEmployee = rotationEmployee;
        this.spot = spot;
        this.startDayOffset = startDayOffset;
        this.endDayOffset = endDayOffset;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Shift createShiftOnDate(LocalDate startDate, int rotationLength, ZoneId zoneId, boolean defaultToRotationEmployee) {
        LocalDateTime startDateTime = startDate.atTime(getStartTime());

        LocalDate endDate;
        if (getStartDayOffset() <= getEndDayOffset()) {
            endDate = startDate.plusDays(getEndDayOffset() - getStartDayOffset());
        } else {
            // Happens for shifts that "wrap around" in the rotation (ex: start on last day of the rotation,
            // end on first day of the rotation)
            endDate = startDate.plusDays(rotationLength + getEndDayOffset() - getStartDayOffset());
        }
        LocalDateTime endDateTime = endDate.atTime(getEndTime());

        // TODO Can this and RosterGenerator.createEmployeeAvailabilityList() be simplified and be DST spring/fall compatible?
        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime, zoneId.getRules().getOffset(startDateTime));
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime, zoneId.getRules().getOffset(endDateTime));
        Shift shift = new Shift(getTenantId(), getSpot(), startOffsetDateTime, endOffsetDateTime, rotationEmployee);
        if (defaultToRotationEmployee) {
            shift.setEmployee(rotationEmployee);
        }
        return shift;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public Integer getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(Integer offsetStartDay) {
        this.startDayOffset = offsetStartDay;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getEndDayOffset() {
        return endDayOffset;
    }

    public void setEndDayOffset(Integer offsetEndDay) {
        this.endDayOffset = offsetEndDay;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Employee getRotationEmployee() {
        return rotationEmployee;
    }

    public void setRotationEmployee(Employee rotationEmployee) {
        this.rotationEmployee = rotationEmployee;
    }

}
