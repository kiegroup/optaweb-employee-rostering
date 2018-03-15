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
                                   " order by sa.offsetStartDay, sa.startTime, s.name, re.name"),
})
public class ShiftTemplate extends AbstractPersistable {

    @NotNull
    @ManyToOne
    private Spot spot;

    @NotNull
    private Integer offsetStartDay;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private Integer offsetEndDay;

    @NotNull
    private LocalTime endTime;

    @ManyToOne
    private Employee rotationEmployee;

    @SuppressWarnings("unused")
    public ShiftTemplate() {}

    public ShiftTemplate(Integer tenantId, Spot spot, int offsetStartDay, LocalTime startTime, int offsetEndDay, LocalTime endTime) {
        this(tenantId, spot, offsetStartDay, startTime, offsetEndDay, endTime, null);
    }

    public ShiftTemplate(Integer tenantId, Spot spot, int offsetStartDay, LocalTime startTime, int offsetEndDay, LocalTime endTime, Employee rotationEmployee) {
        super(tenantId);
        this.rotationEmployee = rotationEmployee;
        this.spot = spot;
        this.offsetStartDay = offsetStartDay;
        this.offsetEndDay = offsetEndDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public Integer getOffsetStartDay() {
        return offsetStartDay;
    }

    public void setOffsetStartDay(Integer offsetStartDay) {
        this.offsetStartDay = offsetStartDay;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getOffsetEndDay() {
        return offsetEndDay;
    }

    public void setOffsetEndDay(Integer offsetEndDay) {
        this.offsetEndDay = offsetEndDay;
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

    public Shift asShiftOnDate(LocalDate date, ZoneId timeZone) {
        LocalDateTime startDateTime = date.atTime(getStartTime());
        LocalDateTime endDateTime = date.plusDays(getOffsetEndDay() - getOffsetStartDay()).atTime(getEndTime());

        // TODO: How to handle start/end time in transitions? Current is the Offset BEFORE the transition
        OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime, timeZone.getRules().getOffset(startDateTime));
        OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime, timeZone.getRules().getOffset(endDateTime));
        return new Shift(getTenantId(), getSpot(), startOffsetDateTime, endOffsetDateTime, getRotationEmployee());
    }
}
