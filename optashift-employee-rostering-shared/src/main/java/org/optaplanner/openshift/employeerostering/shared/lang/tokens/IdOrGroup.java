package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import javax.persistence.Entity;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

/**
 * Specifics if {@link IdOrGroup#itemId} refers to an individual entity or a group.<br>
 * Properties:<br>
 * {@link IdOrGroup#isGroup} <br>
 * {@link IdOrGroup#itemId} <br>
 */
@Entity
public class IdOrGroup extends AbstractPersistable {

    /**
     * If true, {@link IdOrGroup#itemId} refers to a group. Otherwise,
     * {@link IdOrGroup#itemId} refers to a single entity.
     */
    boolean isGroup;

    /**
     * Long used to uniquely identify entities or groups. To check if this
     * object refer to an entity or a group, use {@link IdOrGroup#getIsGroup()}.
     */
    Long itemId;

    public IdOrGroup() {

    }

    public IdOrGroup(Integer tenantId, boolean isGroup, Long id) {
        super(tenantId);
        this.isGroup = isGroup;
        this.itemId = id;
    }

    /**
     * Getter for {@link IdOrGroup#isGroup}
     * @return Value of {@link IdOrGroup#isGroup}
     */
    public boolean getIsGroup() {
        return isGroup;
    }

    /**
     * Getter for {@link IdOrGroup#itemId}
     * @return Value of {@link IdOrGroup#itemId}
     */
    public Long getItemId() {
        return itemId;
    }

    /**
     * Setter for {@link IdOrGroup#isGroup}
     * 
     * @param isGroup Value to set {@link IdOrGroup#isGroup} to
     */
    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    /**
     * Setter for {@link IdOrGroup#itemId}
     * 
     * @param id Value to set {@link IdOrGroup#itemId} to
     */
    public void setItemId(Long id) {
        this.itemId = id;
    }
}
