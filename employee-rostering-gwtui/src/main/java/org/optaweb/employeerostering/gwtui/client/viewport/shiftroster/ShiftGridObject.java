/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.viewport.shiftroster;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.util.CommonUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.SingleGridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.Draggability;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.Resizability;
import org.optaweb.employeerostering.shared.common.HasTimeslot;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.shared.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.shared.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.shared.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.shared.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.shared.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.shared.violation.UndesiredTimeslotForEmployeePenalty;

@Templated
public class ShiftGridObject extends AbstractHasTimeslotGridObject<ShiftRosterMetadata> implements SingleGridObject<LocalDateTime, ShiftRosterMetadata> {

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement label;

    @Inject
    @DataField("indictments")
    private IconContainer indictmentIcons;

    @Inject
    private Draggability<LocalDateTime, ShiftRosterMetadata> draggability;
    @Inject
    private Resizability<LocalDateTime, ShiftRosterMetadata> resizability;
    @Inject
    private ManagedInstance<ShiftGridObjectPopup> popoverInstances;
    @Inject
    private CommonUtils commonUtils;

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
            label.innerHTML = (shiftView.getEmployeeId() == null) ? "Unassigned" : getLane().getMetadata()
                                                                                            .getEmployeeIdToEmployeeMap().get(shiftView.getEmployeeId()).getName();
            setClassProperty("pinned", shiftView.isPinnedByUser());
            setClassProperty("unassigned", shiftView.getEmployeeId() == null);
            RosterState rosterState = getLane().getMetadata().getRosterState();
            setClassProperty("historic", rosterState.isHistoric(shiftView));
            setClassProperty("published", rosterState.isPublished(shiftView));
            setClassProperty("draft", rosterState.isDraft(shiftView));

            indictmentIcons.clear();
            if (shiftView.getIndictmentScore() != null) { // can be null iff shift view was added manually
                getElement().title = "Score is " + shiftView.getIndictmentScore();
                for (RequiredSkillViolation violation : shiftView.getRequiredSkillViolationList()) {
                    // Note: Unassigned shif
                    indictmentIcons.add(IconContainer.Icon.REQUIRED_SKILL_VIOLATION, "Employee " + violation.getShift().getEmployee().getName() +
                                                                                     " does not have the following skills required for " +
                                                                                     violation.getShift().getSpot().getName() + ": " +
                                                                                     commonUtils.delimitCollection(violation.getShift()
                                                                                                                            .getSpot()
                                                                                                                            .getRequiredSkillSet()
                                                                                                                            .stream()
                                                                                                                            .filter(s -> !violation.getShift()
                                                                                                                                                   .getEmployee()
                                                                                                                                                   .getSkillProficiencySet()
                                                                                                                                                   .contains(s))
                                                                                                                            .collect(Collectors.toList()),
                                                                                                                   s -> s.getName(),
                                                                                                                   ",") + " (Score: " + violation.getScore() + ").");
                }

                for (UnavailableEmployeeViolation violation : shiftView.getUnavailableEmployeeViolationList()) {
                    indictmentIcons.add(IconContainer.Icon.UNAVAILABLE_EMPLOYEE_VIOLATION, "Employee " + violation.getShift().getEmployee().getName() +
                                                                                           " is not avaliable from " +
                                                                                           violation.getEmployeeAvailability().getStartDateTime() + " to " +
                                                                                           violation.getEmployeeAvailability().getEndDateTime() + " (Score: " + violation.getScore() + ").");
                }

                for (ShiftEmployeeConflict conflict : shiftView.getShiftEmployeeConflictList()) {
                    if (!conflict.getLeftShift().getId().equals(shiftView.getId())) {
                        indictmentIcons.add(IconContainer.Icon.SHIFT_EMPLOYEE_CONFLICT, "Employee " + conflict.getLeftShift().getEmployee().getName() +
                                                                                        " has a later shift that conflicts with this one: " +
                                                                                        conflict.getRightShift() + " (Score: " + conflict.getScore() + ").");
                    } else {
                        indictmentIcons.add(IconContainer.Icon.SHIFT_EMPLOYEE_CONFLICT, "Employee " + conflict.getRightShift().getEmployee().getName() +
                                                                                        " has a eariler shift that conflicts with this one: " +
                                                                                        conflict.getLeftShift() + " (Score: " + conflict.getScore() + ").");
                    }

                }

                for (DesiredTimeslotForEmployeeReward reward : shiftView.getDesiredTimeslotForEmployeeRewardList()) {
                    indictmentIcons.add(IconContainer.Icon.DESIRED_TIMESLOT_FOR_EMPLOYEE_REWARD, "Employee " + reward.getShift().getEmployee().getName() +
                                                                                                 " want to work from " +
                                                                                                 reward.getEmployeeAvailability().getStartDateTime() + " to " +
                                                                                                 reward.getEmployeeAvailability().getEndDateTime() + " (Score: " + reward.getScore() + ").");
                }

                for (UndesiredTimeslotForEmployeePenalty penalty : shiftView.getUndesiredTimeslotForEmployeePenaltyList()) {
                    indictmentIcons.add(IconContainer.Icon.UNDESIRED_TIMESLOT_FOR_EMPLOYEE_PENALTY, "Employee " + penalty.getShift().getEmployee().getName() +
                                                                                                    " does not want to work from " +
                                                                                                    penalty.getEmployeeAvailability().getStartDateTime() + " to " +
                                                                                                    penalty.getEmployeeAvailability().getEndDateTime() + " (Score: " + penalty.getScore() + ").");
                }

                for (RotationViolationPenalty penalty : shiftView.getRotationViolationPenaltyList()) {
                    if (penalty.getShift().getEmployee() != null) {
                        indictmentIcons.add(IconContainer.Icon.ROTATION_VIOLATION_PENALTY, "Employee " + penalty.getShift().getEmployee().getName() +
                                                                                           " does not match Rotation Employee (" +
                                                                                           penalty.getShift().getRotationEmployee().getName() + ") (Score: " + penalty.getScore() + ").");
                    } else {
                        indictmentIcons.add(IconContainer.Icon.ROTATION_VIOLATION_PENALTY, "Shift is unassigned although it has a Rotation Employee (" +
                                                                                           penalty.getShift().getRotationEmployee().getName() + ") (Score: " + penalty.getScore() + ").");
                    }
                }
                
                for (UnassignedShiftPenalty penalty : shiftView.getUnassignedShiftPenaltyList()) {
                    indictmentIcons.add(IconContainer.Icon.UNASSIGNED_SHIFT_PENALTY, "Shift is unassigned (Score: " + penalty.getScore() + ").");
                }
            }
        }
    }

    @Override
    protected void onMouseClick(MouseEvent e) {
        if (e.shiftKey) {
            getLane().removeGridObject(this);
        } else {
            popoverInstances.get().init(this);
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

    @Override
    public void save() {
        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView,
                                            FailureShownRestCallback.onSuccess(sv -> {
                                                withShiftView(sv);
                                            }));
    }
}
