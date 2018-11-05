/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class IconContainer
        implements
        IsElement {

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

    @Inject
    @Named("span")
    private HTMLElement spanFactory;

    private List<HTMLElement> addedIcons;

    @PostConstruct
    public void init() {
        hardConstraintBroken.getIcon().classList.add("fa", "fa-ban");
        mediumConstraintBroken.getIcon().classList.add("fa", "fa-times-circle");
        softConstraintPenalty.getIcon().classList.add("pficon", "pficon-warning-triangle-o");
        softConstraintReward.getIcon().classList.add("pficon", "pficon-ok");
        addedIcons = new ArrayList<>();
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
            case NONE:
                HTMLElement newInfoIcon = (HTMLElement) (spanFactory.cloneNode(true));
                switch (icon) {
                    case PINNED:
                        newInfoIcon.classList.add("pinned");
                        break;
                    default:
                        throw new IllegalStateException("Missing case for " + icon.getConstraintMatchCategory().name() + ".");
                }
                newInfoIcon.title = tooltip;
                getElement().insertBefore(newInfoIcon, hardConstraintBroken.getElement());
                addedIcons.add(newInfoIcon);
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
        addedIcons.forEach(e -> e.remove());
    }

    public static enum Icon {
        UNAVAILABLE_EMPLOYEE_VIOLATION(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        SHIFT_EMPLOYEE_CONFLICT(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        DESIRED_TIMESLOT_FOR_EMPLOYEE_REWARD(ConstraintMatchCategory.SOFT_CONSTRAINT_REWARD),
        UNDESIRED_TIMESLOT_FOR_EMPLOYEE_PENALTY(ConstraintMatchCategory.SOFT_CONSTRAINT_PENALTY),
        ROTATION_VIOLATION_PENALTY(ConstraintMatchCategory.SOFT_CONSTRAINT_PENALTY),
        UNASSIGNED_SHIFT_PENALTY(ConstraintMatchCategory.MEDIUM_CONSTRAINT_BROKEN),
        REQUIRED_SKILL_VIOLATION(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        CONTRACT_MINUTES_VIOLATION(ConstraintMatchCategory.HARD_CONSTRAINT_BROKEN),
        PINNED(ConstraintMatchCategory.NONE);

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
        SOFT_CONSTRAINT_REWARD,
        NONE;
    }
}
