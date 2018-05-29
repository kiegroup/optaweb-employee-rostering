package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class IconContainer {

    @Inject
    @DataField("hard-constraint-broken")
    private IndictmentBadge hardConstraintBroken;
    
    @Inject
    @DataField("medium-constraint-broken")
    private IndictmentBadge mediumConstraintBroken;

    @Inject
    @DataField("soft-constraint-penalty")
    private IndictmentBadge softConstraintPenalty;

    @Inject
    @DataField("soft-constraint-reward")
    private IndictmentBadge softConstraintReward;

    @PostConstruct
    public void init() {
        hardConstraintBroken.getIcon().classList.add("fa", "fa-ban");
        mediumConstraintBroken.getIcon().classList.add("fa", "fa-times-circle");
        softConstraintPenalty.getIcon().classList.add("pficon", "pficon-warning-triangle-o");
        softConstraintReward.getIcon().classList.add("pficon", "pficon-ok");
    }

    public void add(Icon icon, String tooltip) {
        switch (icon.getConstraintMatchCategory()) {
            case HARD_CONSTRAINT_BROKEN:
                hardConstraintBroken.addConstraintMatch(tooltip);
                break;
            case MEDIUM_CONSTRAINT_BROKEN:
                mediumConstraintBroken.addConstraintMatch(tooltip);
                break;
            case SOFT_CONSTRAINT_PENALTY:
                softConstraintPenalty.addConstraintMatch(tooltip);
                break;
            case SOFT_CONSTRAINT_REWARD:
                softConstraintReward.addConstraintMatch(tooltip);
                break;
            default:
                throw new IllegalStateException("Missing case for " + icon.getConstraintMatchCategory().name() + ".");
        }
    }

    public void clear() {
        hardConstraintBroken.clear();
        mediumConstraintBroken.clear();
        softConstraintPenalty.clear();
        softConstraintReward.clear();
    }

    public static enum Icon {
        UNAVAILABLE_EMPLOYEE_VIOLATION(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        SHIFT_EMPLOYEE_CONFLICT(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        DESIRED_TIMESLOT_FOR_EMPLOYEE_REWARD(ConstraintMatchCategory.SOFT_CONSTRAINT_REWARD),
        UNDESIRED_TIMESLOT_FOR_EMPLOYEE_PENALTY(ConstraintMatchCategory.SOFT_CONSTRAINT_PENALTY),
        ROTATION_VIOLATION_PENALTY(ConstraintMatchCategory.SOFT_CONSTRAINT_PENALTY),
        UNASSIGNED_SHIFT_PENALTY(ConstraintMatchCategory.MEDIUM_CONSTRAINT_BROKEN),
        REQUIRED_SKILL_VIOLATION(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN);

        private ConstraintMatchCategory constraintMatchCategory;

        private Icon(ConstraintMatchCategory category) {
            this.constraintMatchCategory = category;
        }

        private ConstraintMatchCategory getConstraintMatchCategory() {
            return constraintMatchCategory;
        }
    }

    private static enum ConstraintMatchCategory {
        HARD_CONSTRAINT_BROKEN,
        MEDIUM_CONSTRAINT_BROKEN,
        SOFT_CONSTRAINT_PENALTY,
        SOFT_CONSTRAINT_REWARD;
    }
}
