/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import {
  Title, Text, Flex, FlexItem,
  FlexModifiers, SplitItem, Split, Button,
} from '@patternfly/react-core';
import moment from 'moment';
import { rosterSelectors } from 'store/roster';
import { useSelector } from 'react-redux';
import { EditIcon, TrashIcon } from '@patternfly/react-icons';
import { TimeBucket } from 'domain/TimeBucket';
import { EditTimeBucketModal } from './EditTimeBucketModal';
import { Stub, EmployeeNickName } from './EmployeeStub';

export interface SeatJigsawProps {
  selectedStub: Stub | null;
  timeBucket: TimeBucket;
  onUpdateTimeBucket: (updatedTimeBucket: TimeBucket) => void;
  onDeleteTimeBucket: () => void;
}

export const SeatJigsaw: React.FC<SeatJigsawProps> = (props) => {
  const [isEditingTimeBucket, setIsEditingTimeBucket] = React.useState(false);
  const rosterState = useSelector(rosterSelectors.getRosterState);
  const rotationLength = rosterState ? rosterState.rotationLength : 0;

  return (
    <>
      <Title
        size="lg"
        style={{
          userSelect: 'none',
        }}
      >
        {moment(props.timeBucket.startTime, 'HH:mm').format('LT')}
        -
        {moment(props.timeBucket.endTime, 'HH:mm').format('LT')}
        {props.timeBucket.additionalSkillSet.length > 0
          && (
            <Text>
          (
              {props.timeBucket.additionalSkillSet.map(skill => skill.name).join(', ')}
          )
            </Text>
          )
        }
      </Title>
      <Split>
        <SplitItem>
          <Flex breakpointMods={[{ modifier: FlexModifiers['space-items-lg'] }]}>
            {new Array(Math.ceil(rotationLength / 7)).fill(null).map((_, index) => index).map(weekNumber => (
              <FlexItem key={`${weekNumber}`}>
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 30px)',
                    columnGap: '0.5rem',
                    cursor: 'pointer',
                  }}
                >
                  {Array(7).fill(null)
                    .map((_, i) => props.timeBucket.seatList
                      .find(seat => seat.dayInRotation === i + weekNumber * 7) || null)
                    .map((seat, weekDay) => (
                      <button
                        key={`${weekDay + weekNumber * 7}`}
                        // eslint-disable-next-line no-nested-ternary
                        title={seat ? seat.employee ? seat.employee.name : 'Unassigned' : 'No Shift'}
                        style={{
                          border: '1px solid black',
                          height: '50px',
                          // eslint-disable-next-line no-nested-ternary
                          backgroundColor: seat ? seat.employee ? seat.employee.color : 'white' : 'gray',
                          writingMode: 'vertical-rl',
                          textOrientation: 'upright',
                          userSelect: 'none',
                        }}
                        type="button"
                        onClick={() => {
                          props.onUpdateTimeBucket({
                            ...props.timeBucket,
                            seatList: [
                              ...props.timeBucket.seatList
                                .filter(other => other.dayInRotation !== weekDay + weekNumber * 7),
                              ...(props.selectedStub !== 'NO_SHIFT'
                                ? [
                                  {
                                    dayInRotation: weekDay + weekNumber * 7,
                                    employee: props.selectedStub !== 'SHIFT_WITH_NO_EMPLOYEE'
                                      ? props.selectedStub : null,
                                  },
                                ] : []),
                            ],
                          });
                        }}
                        onMouseDown={() => {
                          props.onUpdateTimeBucket({
                            ...props.timeBucket,
                            seatList: [
                              ...props.timeBucket.seatList
                                .filter(other => other.dayInRotation !== weekDay + weekNumber * 7),
                              ...(props.selectedStub !== 'NO_SHIFT'
                                ? [
                                  {
                                    dayInRotation: weekDay + weekNumber * 7,
                                    employee: props.selectedStub !== 'SHIFT_WITH_NO_EMPLOYEE'
                                      ? props.selectedStub : null,
                                  },
                                ] : []),
                            ],
                          });
                        }}
                        onMouseMove={(e) => {
                          // eslint-disable-next-line no-bitwise
                          if ((e.buttons & 1) === 1) { // 1 = Left mouse button
                            props.onUpdateTimeBucket({
                              ...props.timeBucket,
                              seatList: [
                                ...props.timeBucket.seatList
                                  .filter(other => other.dayInRotation !== weekDay + weekNumber * 7),
                                ...(props.selectedStub !== 'NO_SHIFT'
                                  ? [
                                    {
                                      dayInRotation: weekDay + weekNumber * 7,
                                      employee: props.selectedStub !== 'SHIFT_WITH_NO_EMPLOYEE'
                                        ? props.selectedStub : null,
                                    },
                                  ] : []),
                              ],
                            });
                          }
                        }}
                      >
                        <EmployeeNickName employee={seat ? seat.employee : null} />
                      </button>
                    ))}
                  <span
                    style={{
                      gridRow: 2,
                      gridColumn: 1,
                      justifySelf: 'center',
                      userSelect: 'none',
                    }}
                  >
                    {(weekNumber * 7) + 1}
                  </span>
                  <span
                    style={{ gridRow: 2,
                      gridColumn: 7,
                      justifySelf: 'center',
                      userSelect: 'none' }}
                  >
                    {(weekNumber + 1) * 7}
                  </span>
                </div>
              </FlexItem>
            ))}
          </Flex>
        </SplitItem>
        <SplitItem>
          <Button
            onClick={() => setIsEditingTimeBucket(true)}
            variant="link"
          >
            <EditIcon />
            {' '}
Edit Time Bucket
          </Button>
          <Button
            onClick={props.onDeleteTimeBucket}
            variant="link"
          >
            <TrashIcon />
            {' '}
Delete Time Bucket
          </Button>
        </SplitItem>
      </Split>
      <EditTimeBucketModal
        isOpen={isEditingTimeBucket}
        timeBucket={props.timeBucket}
        onUpdateTimeBucket={props.onUpdateTimeBucket}
        onClose={() => setIsEditingTimeBucket(false)}
      />
    </>
  );
};
