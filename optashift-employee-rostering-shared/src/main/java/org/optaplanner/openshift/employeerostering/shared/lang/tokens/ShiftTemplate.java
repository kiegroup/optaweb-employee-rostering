package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

/**
 * Describes the shifts to generate and how to generate them.<br>
 * Properties:<br>
 * {@link ShiftTemplate#baseDateType} <br>
 * {@link ShiftTemplate#repeatType} <br>
 * {@link ShiftTemplate#universalExceptionList} <br>
 * {@link ShiftTemplate#shiftList} <br>
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ShiftTemplate.get", query = "select distinct t from ShiftTemplate t" +
                " where t.tenantId = :tenantId")
})
public class ShiftTemplate extends AbstractPersistable {

    /**
     * Defines the base date used by {@link ShiftInfo#startTime} and {@link ShiftInfo#endTime}.
     * Valid values for non-custom definitions are the members of {@link BaseDateDefinitions}.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    EnumOrCustom baseDateType;

    /**
     * Define how to repeat the shifts defined in this template.
     * Valid values for non-custom definitions are members of {@link RepeatMode}
     */
    @ManyToOne(cascade = CascadeType.ALL)
    EnumOrCustom repeatType;

    /**
     * List of exceptions that apply to all shifts. It is strongly recommend to leave
     * {@link ShiftConditional#shift} null for each member of the list, as that shift will be created for all
     * shifts that match the exception. {@link ShiftTemplate#universalExceptionList} are evaluated after
     * {@link ShiftInfo#exceptionList}. 
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "orderIndex")
    List<ShiftConditional> universalExceptionList;

    /**
     * List of shifts to generate
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "orderIndex")
    List<ShiftInfo> shiftList;

    public ShiftTemplate() {

    }

    public ShiftTemplate(Integer tenantId, EnumOrCustom baseDateType, EnumOrCustom repeatType, List<
            ShiftConditional> universalExceptions,
            List<ShiftInfo> shifts) {
        super(tenantId);
        this.baseDateType = baseDateType;
        this.repeatType = repeatType;
        this.universalExceptionList = universalExceptions;
        this.shiftList = shifts;
    }

    /**
     * Getter for {@link ShiftTemplate#baseDateType}
     * @return Value of {@link ShiftTemplate#baseDateType}
     */
    public EnumOrCustom getBaseDateType() {
        return baseDateType;
    }

    /**
     * Setter for {@link ShiftTemplate#baseDateType}
     * 
     * @param baseDateType Value to set {@link ShiftTemplate#baseDateType} to
     */
    public void setBaseDateType(EnumOrCustom baseDateType) {
        this.baseDateType = baseDateType;
    }

    /**
     * Getter for {@link ShiftTemplate#repeatType}
     * @return Value of {@link ShiftTemplate#repeatType}
     */
    public EnumOrCustom getRepeatType() {
        return repeatType;
    }

    /**
     * Setter for {@link ShiftTemplate#repeatType}
     * 
     * @param repeatType Value to set {@link ShiftTemplate#repeatType} to
     */
    public void setRepeatType(EnumOrCustom repeatType) {
        this.repeatType = repeatType;
    }

    /**
     * Getter for {@link ShiftTemplate#universalExceptionList}
     * @return Value of {@link ShiftTemplate#universalExceptionList}
     */
    public List<ShiftConditional> getUniversalExceptionList() {
        return universalExceptionList;
    }

    /**
     * Setter for {@link ShiftTemplate#universalExceptionList}
     * 
     * @param universalExceptionList Value to set {@link ShiftTemplate#universalExceptionList} to
     */
    public void setUniversalExceptionList(List<ShiftConditional> universalExceptionList) {
        this.universalExceptionList = universalExceptionList;
    }

    /**
     * Getter for {@link ShiftTemplate#shiftList}
     * @return Value of {@link ShiftTemplate#shiftList}
     */
    public List<ShiftInfo> getShiftList() {
        return shiftList;
    }

    /**
     * Setter for {@link ShiftTemplate#shiftList}
     * 
     * @param shiftList Value to set {@link ShiftTemplate#shiftList} to
     */
    public void setShiftList(List<ShiftInfo> shiftList) {
        this.shiftList = shiftList;
    }

}
