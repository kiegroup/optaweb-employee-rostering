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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.SingleGridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.powers.DraggabilityDecorator;
import org.optaweb.employeerostering.gwtui.client.viewport.powers.ResizabilityDecorator;
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaweb.employeerostering.shared.common.HasTimeslot;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.violation.ContractMinutesViolation;
import org.optaweb.employeerostering.shared.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.shared.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.shared.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.shared.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.shared.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.shared.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.shared.violation.UndesiredTimeslotForEmployeePenalty;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;

@Templated
public class ShiftGridObject extends AbstractHasTimeslotGridObject<ShiftRosterMetadata>
        implements
        SingleGridObject<LocalDateTime, ShiftRosterMetadata> {

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement employeeNameLabel;

    @Inject
    @DataField("indictments")
    private IconContainer indictmentIcons;

    @Inject
    private DraggabilityDecorator<LocalDateTime, ShiftRosterMetadata> draggability;
    @Inject
    private ResizabilityDecorator<LocalDateTime, ShiftRosterMetadata> resizability;
    @Inject
    private ManagedInstance<ShiftEditForm> shiftEditFormFactory;
    @Inject
    private EventManager eventManager;

    private ShiftView shiftView;

    public ShiftView getShiftView() {
        return shiftView;
    }

    @Override
    public void setStartPositionInScaleUnits(LocalDateTime newStartPosition) {
        shiftView.setStartDateTime(newStartPosition);
    }

    @Override
    public void setEndPositionInScaleUnits(LocalDateTime newEndPosition) {
        shiftView.setEndDateTime(newEndPosition);
    }

    public ShiftGridObject withShiftView(ShiftView shiftView) {
        this.shiftView = shiftView;
        refresh();
        return this;
    }

    @Override
    public Long getId() {
        return shiftView.getId();
    }

    private void refresh() {
        if (getLane() != null) {
            getLane().positionGridObject(this);
            employeeNameLabel.innerHTML = (shiftView.getEmployeeId() == null) ? "Unassigned" : getLane().getMetadata()
                    .getEmployeeIdToEmployeeMap().get(shiftView.getEmployeeId()).getName();
            setClassProperty("unassigned", shiftView.getEmployeeId() == null);
            RosterState rosterState = getLane().getMetadata().getRosterState();
            setClassProperty("historic", rosterState.isHistoric(shiftView));
            setClassProperty("published", rosterState.isPublished(shiftView));
            setClassProperty("draft", rosterState.isDraft(shiftView));
            updateConstraintMatches();
        }
    }

    private void updateConstraintMatches() {
        indictmentIcons.clear();
        if (shiftView.isPinnedByUser()) {
            indictmentIcons.add(IconContainer.Icon.PINNED, "The employee " + getEmployee().getName() + " is pinned to this shift");
        }

        setClassProperty("score", shiftView.getIndictmentScore() != null);
        if (shiftView.getIndictmentScore() != null) { // can be null iff shift view was added manually
            updateColor(shiftView.getIndictmentScore());
            getElement().title = "Score is " + shiftView.getIndictmentScore().toShortString();
            for (RequiredSkillViolation violation : shiftView.getRequiredSkillViolationList()) {
                // Note: Unassigned shif
                indictmentIcons.add(IconContainer.Icon.REQUIRED_SKILL_VIOLATION, "Employee " + violation.getShift().getEmployee().getName() +
                        " does not have the following skills required for " +
                        violation.getShift().getSpot().getName() + ": " +
                        violation.getShift()
                                .getSpot()
                                .getRequiredSkillSet()
                                .stream()
                                .filter(s -> !violation.getShift()
                                        .getEmployee()
                                        .getSkillProficiencySet()
                                        .contains(s))
                                .map(Skill::getName).collect(Collectors.joining(", ")) + " (Score: " + violation.getScore().toShortString() + ").");
            }

            for (UnavailableEmployeeViolation violation : shiftView.getUnavailableEmployeeViolationList()) {
                indictmentIcons.add(IconContainer.Icon.UNAVAILABLE_EMPLOYEE_VIOLATION, "Employee " + violation.getShift().getEmployee().getName() +
                        " is not avaliable from " +
                        getNiceString(violation.getEmployeeAvailability().getStartDateTime()) + " to " +
                        getNiceString(violation.getEmployeeAvailability().getEndDateTime()) + " (Score: " + violation
                        .getScore()
                        .toShortString() +
                        ").");
            }

            for (ShiftEmployeeConflict conflict : shiftView.getShiftEmployeeConflictList()) {
                if (!conflict.getLeftShift().getId().equals(shiftView.getId())) {
                    indictmentIcons.add(IconContainer.Icon.SHIFT_EMPLOYEE_CONFLICT, "Employee " + conflict.getLeftShift().getEmployee().getName() +
                            " has a later shift that conflicts with this one: " +
                            getNiceString(conflict.getRightShift()) + " (Score: " + conflict.getScore().toShortString() + ").");
                } else {
                    indictmentIcons.add(IconContainer.Icon.SHIFT_EMPLOYEE_CONFLICT, "Employee " + conflict.getRightShift().getEmployee().getName() +
                            " has a eariler shift that conflicts with this one: " +
                            getNiceString(conflict.getLeftShift()) + " (Score: " + conflict.getScore().toShortString() + ").");
                }
            }

            for (DesiredTimeslotForEmployeeReward reward : shiftView.getDesiredTimeslotForEmployeeRewardList()) {
                indictmentIcons.add(IconContainer.Icon.DESIRED_TIMESLOT_FOR_EMPLOYEE_REWARD, "Employee " + reward.getShift().getEmployee().getName() +
                        " want to work from " +
                        getNiceString(reward.getEmployeeAvailability().getStartDateTime()) + " to " +
                        getNiceString(reward.getEmployeeAvailability().getEndDateTime()) + " (Score: " + reward.getScore().toShortString() + ").");
            }

            for (UndesiredTimeslotForEmployeePenalty penalty : shiftView.getUndesiredTimeslotForEmployeePenaltyList()) {
                indictmentIcons.add(IconContainer.Icon.UNDESIRED_TIMESLOT_FOR_EMPLOYEE_PENALTY, "Employee " + penalty.getShift().getEmployee().getName() +
                        " does not want to work from " +
                        getNiceString(penalty.getEmployeeAvailability().getStartDateTime()) + " to " +
                        getNiceString(penalty.getEmployeeAvailability().getEndDateTime()) + " (Score: " + penalty.getScore().toShortString() +
                        ").");
            }

            for (RotationViolationPenalty penalty : shiftView.getRotationViolationPenaltyList()) {
                if (penalty.getShift().getEmployee() != null) {
                    indictmentIcons.add(IconContainer.Icon.ROTATION_VIOLATION_PENALTY, "Employee " + penalty.getShift().getEmployee().getName() +
                            " does not match Rotation Employee (" +
                            penalty.getShift().getRotationEmployee().getName() + ") (Score: " + penalty.getScore().toShortString() + ").");
                } else {
                    indictmentIcons.add(IconContainer.Icon.ROTATION_VIOLATION_PENALTY, "Shift is unassigned although it has a Rotation Employee (" +
                            penalty.getShift().getRotationEmployee().getName() + ") (Score: " + penalty.getScore().toShortString() + ").");
                }
            }

            for (UnassignedShiftPenalty penalty : shiftView.getUnassignedShiftPenaltyList()) {
                indictmentIcons.add(IconContainer.Icon.UNASSIGNED_SHIFT_PENALTY, "Shift is unassigned (Score: " + penalty.getScore().toShortString() + ").");
            }

            for (ContractMinutesViolation penalty : shiftView.getContractMinutesViolationPenaltyList()) {
                indictmentIcons.add(IconContainer.Icon.CONTRACT_MINUTES_VIOLATION, "Employee have worked more than their " + penalty.getType().toString() +
                        " minutes; the maximum they can work per " + penalty.getType().name().toLowerCase() + " is " +
                        penalty.getMaximumMinutesWorked() + ", but they worked " + penalty.getMinutesWorked() + " this " + penalty.getType().name()
                        .toLowerCase() +
                        " (Score: " + penalty.getScore().toShortString() + ").");
            }
        }
    }

    private String getNiceString(Shift shift) {
        return shift.getSpot().getName() + ":" + getNiceString(shift.getStartDateTime()) + " to " + getNiceString(shift.getEndDateTime());
    }

    private String getNiceString(OffsetDateTime dateTime) {
        DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
        return timeFormat.format(GwtJavaTimeWorkaroundUtil.toDate(GwtJavaTimeWorkaroundUtil.toLocalDateTime(dateTime)));
    }

    private void updateColor(HardMediumSoftLongScore score) {
        if (score.getHardScore() < 0) {
            setAnimationDelay(mapToRange(-score.getHardScore(), 5, 0, 10));
        } else if (score.getHardScore() > 0) {
            setAnimationDelay(mapToRange(score.getHardScore(), 85, 100, 10));
        } else if (score.getMediumScore() < 0) {
            setAnimationDelay(mapToRange(-score.getMediumScore(), 30, 15, 1));
        } else if (score.getMediumScore() > 0) {
            setAnimationDelay(mapToRange(score.getMediumScore(), 70, 85, 10));
        } else if (score.getSoftScore() < 0) {
            setAnimationDelay(mapToRange(-score.getSoftScore(), 50, 30, 100));
        } else if (score.getSoftScore() > 0) {
            setAnimationDelay(mapToRange(score.getSoftScore(), 50, 70, 30));
        } else {
            setAnimationDelay(50);
        }
    }

    // NOTE: if amount == 0, then it returns start
    //       if amount >= maxAmount, then it returns end
    //       else it returns start + amount * ((end - start)/maxAmount)
    private double mapToRange(double amount, double start, double end, double maxAmount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount (" + amount + ") must be non-negative.");
        }
        if (maxAmount <= 0) {
            throw new IllegalArgumentException("maxAmount (" + maxAmount + ") must be positive.");
        }
        return start + (end - start) * Math.min(maxAmount, amount) / maxAmount;
    }

    private void setAnimationDelay(double animationDelay) {
        // Due to CSS not allowing you to set animation-frame or something similar, we set
        // it to an NEGATIVE animation-delay with the animation being paused, so it will
        // begin that much ahead in the animation (so we are effectively setting the frame).
        // animation-duration should be 100s
        getElement().style.set("animation-delay", "-" + animationDelay + "s");
    }

    @Override
    protected void onMouseClick(MouseEvent e) {
        if (e.shiftKey) {
            getLane().removeGridObject(this);
        } else {
            shiftEditFormFactory.get().init(this);
        }
    }

    @Override
    protected HasTimeslot getTimeslot() {
        return shiftView;
    }

    @Override
    protected void init(Lane<LocalDateTime, ShiftRosterMetadata> lane) {
        draggability.applyFor(this, lane.getScale());
        resizability.applyFor(this, lane.getScale());
        refresh();
    }

    public Spot getSpot() {
        return getLane().getMetadata().getSpotIdToSpotMap().get(shiftView.getSpotId());
    }

    public Employee getEmployee() {
        return getLane().getMetadata().getEmployeeIdToEmployeeMap().get(shiftView.getEmployeeId());
    }

    public void setSelected(boolean isSelected) {
        if (isSelected) {
            getElement().classList.add("selected");
        } else {
            getElement().classList.remove("selected");
        }
    }

    @Override
    public void save() {
        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView,
                                            FailureShownRestCallback.onSuccess(sv -> {
                                                withShiftView(sv);
                                                eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
                                            }));
    }
}
