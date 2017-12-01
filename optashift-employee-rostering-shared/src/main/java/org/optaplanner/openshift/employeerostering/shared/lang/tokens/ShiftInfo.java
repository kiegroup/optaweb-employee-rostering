package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.jackson.LocalDateTimeDeserializer;
import org.optaplanner.openshift.employeerostering.shared.jackson.LocalDateTimeSerializer;

/**
 * Describes a shift to generate.<br>
 * Properties:<br>
 * {@link ShiftInfo#startTime} <br>
 * {@link ShiftInfo#endTime} <br>
 * {@link ShiftInfo#spots} <br>
 * {@link ShiftInfo#employees} <br>
 * {@link ShiftInfo#exceptions} (Nullable) <br>
 */
@Entity
public class ShiftInfo extends AbstractPersistable {

    /**
     * How long after the base date to create the shift. The time is calculated by the following formula:
     * <pre>
     * <code>
     * startTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
     * </code>
     * </pre>
     * This value must be before endTime
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime startTime;

    /**
     * How long after the base date to end the shift. The time is calculated by the following formula:
     * <pre>
     * <code>
     * endTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
     * </code>
     * </pre>
     * This value must be after startTime.
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime endTime;

    /**
     * List of spots/spot groups to create
     */
    @OneToMany(cascade = CascadeType.ALL)
    List<IdOrGroup> spots;

    /**
     * List of employees/employee groups and their availability. If an employee appear multiple
     * times or in multiple groups in this list, their last entry in the list determines their
     * availability 
     */
    @OneToMany(cascade = CascadeType.ALL)
    List<EmployeeTimeSlotInfo> employees;

    /**
     * List of conditions that causes this Shift not to be generated, potentially causing another
     * one to be generated instead. These are evalulated before {@link ShiftTemplate#universalExceptions}.
     */
    @OneToMany(cascade = CascadeType.ALL)
    List<ShiftConditional> exceptions;

    public ShiftInfo() {
    }

    public ShiftInfo(Integer tenantId, ShiftInfo src) {
        this(tenantId, src.startTime, src.endTime, src.spots, src.employees, src.exceptions);
    }

    public ShiftInfo(Integer tenantId, LocalDateTime startTime, LocalDateTime endTime, List<IdOrGroup> spots, List<
            EmployeeTimeSlotInfo> employees) {
        super(tenantId);
        this.startTime = startTime;
        this.endTime = endTime;
        this.spots = spots;
        this.employees = employees;
    }

    public ShiftInfo(Integer tenantId, LocalDateTime startTime, LocalDateTime endTime, List<IdOrGroup> spots, List<
            EmployeeTimeSlotInfo> employees, List<ShiftConditional> exceptions) {
        super(tenantId);
        this.startTime = startTime;
        this.endTime = endTime;
        this.spots = spots;
        this.employees = employees;
        this.exceptions = exceptions;
    }

    /**
     * Getter for {@link ShiftInfo#startTime}
     * @return Value of {@link ShiftInfo#startTime}
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Getter for {@link ShiftInfo#endTime}
     * @return Value of {@link ShiftInfo#endTime}
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Getter for {@link ShiftInfo#spots}
     * @return Value of {@link ShiftInfo#spots}
     */
    public List<IdOrGroup> getSpots() {
        return spots;
    }

    /**
     * Setter for {@link ShiftInfo#spots}
     * 
     * @param spots Value to set {@link ShiftInfo#spots} to
     */
    public void setSpots(List<IdOrGroup> spots) {
        this.spots = spots;
    }

    /**
     * Getter for {@link ShiftInfo#employees}
     * @return Value of {@link ShiftInfo#employees}
     */
    public List<EmployeeTimeSlotInfo> getEmployees() {
        return employees;
    }

    /**
     * Setter for {@link ShiftInfo#employees}
     * 
     * @param employees Value to set {@link ShiftInfo#employees} to
     */
    public void setEmployees(List<EmployeeTimeSlotInfo> employees) {
        this.employees = employees;
    }

    /**
     * Getter for {@link ShiftInfo#exceptions}
     * @return Value of {@link ShiftInfo#exceptions}
     */
    public List<ShiftConditional> getExceptions() {
        return exceptions;
    }

    /**
     * Setter for {@link ShiftInfo#exceptions}
     * 
     * @param exceptions Value to set {@link ShiftInfo#exceptions} to
     */
    public void setExceptions(List<ShiftConditional> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Setter for {@link ShiftInfo#startTime}
     * 
     * @param startTime Value to set {@link ShiftInfo#startTime} to
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Setter for {@link ShiftInfo#endTime}
     * 
     * @param endTime Value to set {@link ShiftInfo#endTime} to
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}
