/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import React from "react";
import { EventProps } from "react-big-calendar";
import Shift from "domain/Shift";
import { Text, Popover, Button, ButtonVariant, List } from "@patternfly/react-core";
import moment from "moment";
import Employee from "domain/Employee";
import { convertHardMediumSoftScoreToString } from 'domain/HardMediumSoftScore';
import { EditIcon, TrashIcon } from "@patternfly/react-icons";
import Color from 'color';

import "./BigCalendarSchedule.css";

const EMPTY_ELEMENT = (<></>); 

export function getIndictments(shift: Shift): JSX.Element {
  const indictmentList = (
    <List>
      {getRequiredSkillViolations(shift)}
      {getContractMinutesViolations(shift)}
      {getUnavaliableEmployeeViolations(shift)}
      {getShiftEmployeeConflictViolations(shift)}
      {getUnassignedShiftPenalties(shift)}
      {getRotationViolationPenalties(shift)}
      {getUndesiredTimeslotForEmployeePenalties(shift)}
      {getDesiredTimeslotForEmployeeRewards(shift)}
    </List>
  );

  if (React.Children.count(indictmentList) === 0){
    return EMPTY_ELEMENT;
  }
  return (
    <>
      <Text>Indictments:</Text>
      {indictmentList}
    </>
  );
}

export function getRequiredSkillViolations(shift: Shift): JSX.Element {
  if (shift.requiredSkillViolationList) {
    return (
      <>
        {shift.requiredSkillViolationList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {(v.shift.employee as Employee).name}
            &quot; does not have the following skills which are required
            for the Spot &quot;
            {v.shift.spot.name}
            &quot;:
            <List>
              {v.shift.spot.requiredSkillSet.filter(skill =>
                (v.shift.employee as Employee).skillProficiencySet
                  .find(s => s.id === skill.id) === undefined)
                .map(skill => <li key={skill.id}>{skill.name}</li>)
              }
            </List>
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

export function getContractMinutesViolations(shift: Shift): JSX.Element {
  if (shift.contractMinutesViolationPenaltyList) {
    return (
      <>
        {shift.contractMinutesViolationPenaltyList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {(v.employee as Employee).name}
            &quot; have exceeded their maximum
            {" " + (v.type === "DAY"? "daily" :
              v.type === "WEEK"? "weekly" :
                v.type === "MONTH"? "monthly" :
                  "yearly") + " "}
            minutes; They have worked
            {" " + v.minutesWorked + " "}
            this 
            {" " + (v.type === "DAY"? "day" :
              v.type === "WEEK"? "week" :
                v.type === "MONTH"? "month" :
                  "year")
            }
            ; they are allowed to work at most
            {" " + (v.type === "DAY"? v.employee.contract.maximumMinutesPerDay :
              v.type === "WEEK"? v.employee.contract.maximumMinutesPerWeek :
                v.type === "MONTH"? v.employee.contract.maximumMinutesPerMonth :
                  v.employee.contract.maximumMinutesPerYear)
            }
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

export function getUnavaliableEmployeeViolations(shift: Shift): JSX.Element {
  if (shift.unavailableEmployeeViolationList) {
    return (
      <>
        {shift.unavailableEmployeeViolationList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {v.employeeAvailability.employee.name}
            &quot; is unavailable from
            {" " + moment(v.employeeAvailability.startDateTime).format("LLL") + " "}
            to
            {" " + moment(v.employeeAvailability.endDateTime).format("LLL")}
            .
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

// NOTE: ShiftEmployeeConflict refer to indictments from two constraints:
// - "At most one shift assignment per day per employee"
// - "No 2 shifts within 10 hours from each other"
export function getShiftEmployeeConflictViolations(shift: Shift): JSX.Element {
  if (shift.shiftEmployeeConflictList) {
    return (
      <>
        {shift.shiftEmployeeConflictList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {(v.leftShift.employee as Employee).name}
            &quot; is assigned to a conflicting shift:
            {" " + ((v.leftShift.id === shift.id)?
              v.rightShift.spot.name + ", " + moment(v.rightShift.startDateTime).format("LT") + "-" + moment(v.rightShift.endDateTime).format("LT") :
              v.leftShift.spot.name + ", " + moment(v.leftShift.startDateTime).format("LT") + "-" + moment(v.leftShift.endDateTime).format("LT") 
            )}
            .
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
} 

export function getRotationViolationPenalties(shift: Shift): JSX.Element {
  if (shift.rotationViolationPenaltyList) {
    return (
      <>
        {shift.rotationViolationPenaltyList.map((v, index) => (
          <li key={v.shift.id}>
            The Shift&apos;s Employee &quot;
            {(v.shift.employee as Employee).name}
            &quot; does not match the Shift&apos;s Rotation Employee &quot;
            {(v.shift.rotationEmployee as Employee).name}
            &quot;.
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

export function getUnassignedShiftPenalties(shift: Shift): JSX.Element {
  if (shift.unassignedShiftPenaltyList) {
    return (
      <>
        {shift.unassignedShiftPenaltyList.map((v, index) => (
          <li key={v.shift.id}>
            The Shift is unassigned. 
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT
}

export function getUndesiredTimeslotForEmployeePenalties(shift: Shift) {
  if (shift.undesiredTimeslotForEmployeePenaltyList) {
    return (
      <>
        {shift.undesiredTimeslotForEmployeePenaltyList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {v.employeeAvailability.employee.name}
            &quot; does not want to work from
            {" " + moment(v.employeeAvailability.startDateTime).format("LLL") + " "}
            to
            {" " + moment(v.employeeAvailability.endDateTime).format("LLL")}
            .
            <br />
            {"Penalty: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

export function getDesiredTimeslotForEmployeeRewards(shift: Shift) {
  if (shift.desiredTimeslotForEmployeeRewardList) {
    return (
      <>
        {shift.desiredTimeslotForEmployeeRewardList.map((v, index) => (
          <li key={String(index)}>
            The Employee &quot;
            {v.employeeAvailability.employee.name}
            &quot; desires to work from
            {" " + moment(v.employeeAvailability.startDateTime).format("LLL") + " "}
            to
            {" " + moment(v.employeeAvailability.endDateTime).format("LLL")}
            .
            <br />
            {"Reward: " + convertHardMediumSoftScoreToString(v.score)}
          </li>
        ))}
      </>
    );
  }
  return EMPTY_ELEMENT;
}

export const NEGATIVE_HARD_SCORE_COLOR = Color("rgb(139, 0, 0)", "rgb");
export const NEGATIVE_MEDIUM_SCORE_COLOR = Color("rgb(245, 193, 46)", "rgb");
export const NEGATIVE_SOFT_SCORE_COLOR =  Color("rgb(209, 209, 209)", "rgb");
export const ZERO_SCORE_COLOR = Color("rgb(207, 231, 205)", "rgb");
export const POSITIVE_SOFT_SCORE_COLOR = Color("rgb(63, 156, 53)", "rgb");

export function getShiftColor(shift: Shift): string {
  if (shift.indictmentScore !== undefined && shift.indictmentScore.hardScore < 0) {
    const fromColor = NEGATIVE_HARD_SCORE_COLOR;
    const toColor = NEGATIVE_MEDIUM_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.hardScore) / 100).hex();
  }
  else if (shift.indictmentScore !== undefined && shift.indictmentScore.mediumScore < 0) {
    return NEGATIVE_MEDIUM_SCORE_COLOR.hex();
  }
  else if (shift.indictmentScore !== undefined && shift.indictmentScore.softScore < 0) {
    const fromColor = NEGATIVE_MEDIUM_SCORE_COLOR;
    const toColor = NEGATIVE_SOFT_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.softScore) / 100).hex();
  }
  else if (shift.indictmentScore !== undefined && shift.indictmentScore.softScore > 0) {
    const fromColor = ZERO_SCORE_COLOR;
    const toColor = POSITIVE_SOFT_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.softScore) / 100).hex();
  }
  else {
    // Zero score
    return ZERO_SCORE_COLOR.hex();
  }
}

const ShiftEvent: React.FC<EventProps<Shift> & {
  onEdit: (shift: Shift) => void;
  onDelete: (shift: Shift) => void;
}> = (props) => (
  <Popover
    className="my-popup"
    key={props.event.id}
    position="right"
    headerContent={(
      <span>
        <Text>{props.event.spot.name + ", " + moment(props.event.startDateTime).format("LT") + "-" + moment(props.event.endDateTime).format("LT") + ": " + (props.event.employee? props.event.employee.name : "Unassigned")}</Text>
        <Button
          onClick={() => props.onEdit(props.event)}
          variant={ButtonVariant.link}
        >
          <EditIcon />
        </Button>
        <Button
          onClick={() => props.onDelete(props.event)}
          variant={ButtonVariant.link}
        >
          <TrashIcon />
        </Button>
      </span>
    )}
    bodyContent={
      (
        <span
          style={{
            display: "block",
            maxHeight: "20vh",
            overflowY: "auto",
            pointerEvents: "all"
          }}
        >
          Shift Indictment Score:
          {" " + (props.event.indictmentScore? convertHardMediumSoftScoreToString(props.event.indictmentScore)
            : "N/A")}
          <br />
          {getIndictments(props.event)}
        </span>
      )
    }
  >
    <span
      data-tip
      data-for={String(props.event.id)}
      style={{
        display: "flex",
        height: "100%",
        width: "100%"
      }}
    >
      {props.title}
    </span>
  </Popover>
);

export default ShiftEvent;