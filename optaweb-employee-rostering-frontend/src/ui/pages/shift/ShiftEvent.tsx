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
import React from 'react';
import { EventProps } from 'react-big-calendar';
import { Shift } from 'domain/Shift';
import { Text, Button, ButtonVariant, List } from '@patternfly/react-core';
import moment from 'moment';
import { Employee } from 'domain/Employee';
import { convertHardMediumSoftScoreToString } from 'domain/HardMediumSoftScore';
import { BlueprintIcon, EditIcon, TrashIcon, ThumbTackIcon } from '@patternfly/react-icons';
import Color from 'color';
import { useTranslation } from 'react-i18next';

import 'react-big-calendar/lib/css/react-big-calendar.css';
import 'ui/components/calendar/ReactBigCalendarOverrides.css';

export const Indictments: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  const indictmentList = (
    <List>
      <RequiredSkillViolations {...shift} />
      <ContractMinutesViolations {...shift} />
      <UnavailableEmployeeViolations {...shift} />
      <ShiftEmployeeConflictViolations {...shift} />
      <UnassignedShiftPenalties {...shift} />
      <RotationViolationPenalties {...shift} />
      <UndesiredTimeslotForEmployeePenalties {...shift} />
      <DesiredTimeslotForEmployeeRewards {...shift} />
    </List>
  );

  if (React.Children.count(indictmentList) === 0) {
    return null;
  }
  return (
    <>
      <Text>{t('indictments')}</Text>
      {indictmentList}
    </>
  );
};

export const RequiredSkillViolations: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.requiredSkillViolationList || []).map((v, index) => (
        <li key={String(index)}>
          {t('missingRequiredSkills',
            {
              employee: (v.shift.employee as Employee).name,
              spot: v.shift.spot.name,
            })
          }
          <List>
            {v.shift.spot.requiredSkillSet.filter(skill => (v.shift.employee as Employee).skillProficiencySet
              .find(s => s.id === skill.id) === undefined)
              .map(skill => <li key={skill.id}>{skill.name}</li>)
            }
          </List>
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const ContractMinutesViolations: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.contractMinutesViolationPenaltyList || []).map((v, index) => (
        <li key={String(index)}>
          {t('exceededContractMaximumMinutes', {
            employee: (v.employee as Employee).name,
            // eslint-disable-next-line no-nested-ternary
            durationly: t((v.type === 'DAY' ? 'daily'
              // eslint-disable-next-line no-nested-ternary
              : v.type === 'WEEK' ? 'weekly'
                : v.type === 'MONTH' ? 'monthly'
                  : 'yearly')),
            duration: t(v.type.toLowerCase()),
            minutesWorked: v.minutesWorked,
            // eslint-disable-next-line no-nested-ternary
            maxMinutesAllowed: (v.type === 'DAY' ? v.employee.contract.maximumMinutesPerDay
              // eslint-disable-next-line no-nested-ternary
              : v.type === 'WEEK' ? v.employee.contract.maximumMinutesPerWeek
                : v.type === 'MONTH' ? v.employee.contract.maximumMinutesPerMonth
                  : v.employee.contract.maximumMinutesPerYear),
          })}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const UnavailableEmployeeViolations: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.unavailableEmployeeViolationList || []).map((v, index) => (
        <li key={String(index)}>
          {t('unavailableEmployee', {
            employee: v.employeeAvailability.employee.name,
            from: moment(v.employeeAvailability.startDateTime).format('LLL'),
            to: moment(v.employeeAvailability.startDateTime).format('LLL'),
          })}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

// NOTE: ShiftEmployeeConflict refer to indictments from two constraints:
// - "At most one shift assignment per day per employee"
// - "No 2 shifts within 10 hours from each other"
export const ShiftEmployeeConflictViolations: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.shiftEmployeeConflictList || []).map((v, index) => (
        <li key={String(index)}>
          {t('conflictingShifts', {
            employee: (v.leftShift.employee as Employee).name,
            spot: (v.leftShift.id === shift.id) ? v.rightShift.spot.name : v.leftShift.spot.name,
            from: (v.leftShift.id === shift.id) ? moment(v.rightShift.startDateTime).format('LT')
              : moment(v.leftShift.startDateTime).format('LT'),
            to: (v.leftShift.id === shift.id) ? moment(v.rightShift.endDateTime).format('LT')
              : moment(v.leftShift.endDateTime).format('LT'),
          })}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const RotationViolationPenalties: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.rotationViolationPenaltyList || []).map(v => (
        <li key={v.shift.id}>
          {t('employeeDoesNotMatchRotationEmployee', {
            employee: (v.shift.employee as Employee).name,
            rotationEmployee: (v.shift.rotationEmployee as Employee).name,
          })}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const UnassignedShiftPenalties: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.unassignedShiftPenaltyList || []).map(v => (
        <li key={v.shift.id}>
          {t('unassignedShift')}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const UndesiredTimeslotForEmployeePenalties: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.undesiredTimeslotForEmployeePenaltyList || []).map((v, index) => (
        <li key={String(index)}>
          {t('undesiredShiftForEmployee', {
            employee: v.employeeAvailability.employee.name,
            from: moment(v.employeeAvailability.startDateTime).format('LLL'),
            to: moment(v.employeeAvailability.startDateTime).format('LLL'),
          })}
          <br />
          {t('penalty', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const DesiredTimeslotForEmployeeRewards: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <>
      {(shift.desiredTimeslotForEmployeeRewardList || []).map((v, index) => (
        <li key={String(index)}>
          {t('desiredShiftForEmployee', {
            employee: v.employeeAvailability.employee.name,
            from: moment(v.employeeAvailability.startDateTime).format('LLL'),
            to: moment(v.employeeAvailability.startDateTime).format('LLL'),
          })}
          <br />
          {t('reward', { score: convertHardMediumSoftScoreToString(v.score) })}
        </li>
      ))}
    </>
  );
};

export const NEGATIVE_HARD_SCORE_COLOR = Color('rgb(139, 0, 0)', 'rgb');
export const NEGATIVE_MEDIUM_SCORE_COLOR = Color('rgb(245, 193, 46)', 'rgb');
export const NEGATIVE_SOFT_SCORE_COLOR = Color('rgb(209, 209, 209)', 'rgb');
export const ZERO_SCORE_COLOR = Color('rgb(207, 231, 205)', 'rgb');
export const POSITIVE_SOFT_SCORE_COLOR = Color('rgb(63, 156, 53)', 'rgb');

export function getShiftColor(shift: Shift): string {
  if (shift.indictmentScore !== undefined && shift.indictmentScore.hardScore < 0) {
    const fromColor = NEGATIVE_HARD_SCORE_COLOR;
    const toColor = NEGATIVE_MEDIUM_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.hardScore) / 100).hex();
  }
  if (shift.indictmentScore !== undefined && shift.indictmentScore.mediumScore < 0) {
    return NEGATIVE_MEDIUM_SCORE_COLOR.hex();
  }
  if (shift.indictmentScore !== undefined && shift.indictmentScore.softScore < 0) {
    const fromColor = NEGATIVE_MEDIUM_SCORE_COLOR;
    const toColor = NEGATIVE_SOFT_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.softScore) / 100).hex();
  }
  if (shift.indictmentScore !== undefined && shift.indictmentScore.softScore > 0) {
    const fromColor = ZERO_SCORE_COLOR;
    const toColor = POSITIVE_SOFT_SCORE_COLOR;
    return fromColor.mix(toColor, (20 + shift.indictmentScore.softScore) / 100).hex();
  }

  // Zero score
  return ZERO_SCORE_COLOR.hex();
}

const ShiftPopupHeader: React.FC<{
  shift: Shift;
  onEdit: (shift: Shift) => void;
  onCopy?: (shift: Shift) => void;
  onDelete: (shift: Shift) => void;
}> = (props) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <span>
      <Text>
        {
          `${props.shift.spot.name}, ${
            moment(props.shift.startDateTime).format('LT')}-${
            moment(props.shift.endDateTime).format('LT')}: ${
            props.shift.employee ? props.shift.employee.name : t('unassigned')}`
        }
      </Text>
      <Button
        onClick={() => props.onEdit(props.shift)}
        variant={ButtonVariant.link}
      >
        <EditIcon />
      </Button>
      {
        props.onCopy
          ? (
            <Button
              onClick={() => props.onCopy && props.onCopy(props.shift)}
              variant={ButtonVariant.link}
            >
              <BlueprintIcon />
            </Button>
          )
          : null
      }
      <Button
        onClick={() => props.onDelete(props.shift)}
        variant={ButtonVariant.link}
      >
        <TrashIcon />
      </Button>
    </span>
  );
};

const ShiftPopupBody: React.FC<Shift> = (shift) => {
  const { t } = useTranslation('ShiftEvent');
  return (
    <span
      style={{
        display: 'block',
        maxHeight: '20vh',
        overflowY: 'auto',
        pointerEvents: 'all',
      }}
    >

      {t('totalIndictment', { score: shift.indictmentScore ? convertHardMediumSoftScoreToString(shift.indictmentScore)
        : 'N/A' })}
      <br />
      <Indictments {...shift} />
    </span>
  );
};

const ShiftEvent: React.FC<EventProps<Shift>> = props => (
  <span
    style={{
      display: 'flex',
      height: '100%',
      width: '100%',
    }}
  >
    {props.event.pinnedByUser && <ThumbTackIcon />}
    {props.title}
  </span>
);

export { ShiftPopupBody, ShiftPopupHeader };

export default ShiftEvent;
