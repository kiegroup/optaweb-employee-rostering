package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

/**
 * Specifics if {@link IdOrGroup#id} refers to an individual entity or a group.<br>
 * Properties:<br>
 * {@link IdOrGroup#isGroup} <br>
 * {@link IdOrGroup#id} <br>
 */
public class IdOrGroup {

    /**
     * If true, {@link IdOrGroup#id} refers to a group. Otherwise,
     * {@link IdOrGroup#id} refers to a single entity.
     */
    boolean isGroup;

    /**
     * Long used to uniquely identify entities or groups. To check if this
     * object refer to an entity or a group, use {@link IdOrGroup#getIsGroup()}.
     */
    Long id;

    public IdOrGroup() {

    }

    public IdOrGroup(boolean isGroup, Long id) {
        this.isGroup = isGroup;
        this.id = id;
    }

    /**
     * Getter for {@link IdOrGroup#isGroup}
     * @return Value of {@link IdOrGroup#isGroup}
     */
    public boolean getIsGroup() {
        return isGroup;
    }

    /**
     * Getter for {@link IdOrGroup#id}
     * @return Value of {@link IdOrGroup#id}
     */
    public Long getId() {
        return id;
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
     * Setter for {@link IdOrGroup#id}
     * 
     * @param id Value to set {@link IdOrGroup#id} to
     */
    public void setId(Long id) {
        this.id = id;
    }
}
