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
import { Stub } from './EmployeeStub';
import { TimeBucket } from './EditTimeBucketModal';
import { Title, Text, Grid, GridItem, Flex, FlexItem, FlexModifiers } from '@patternfly/react-core';
import moment from 'moment';
import { rosterSelectors } from 'store/roster';
import { useSelector } from 'react-redux';

export interface SeatJigsawProps {
  selectedStub: Stub | null;
  timeBucket: TimeBucket;
  onUpdateTimeBucket: (updatedTimeBucket: TimeBucket) => void;
}

export const SeatJigsaw: React.FC<SeatJigsawProps> = props => {
  const rosterState = useSelector(rosterSelectors.getRosterState);
  const rotationLength = rosterState? rosterState.rotationLength : 0;
  
  return (
    <>
      <Title size='lg'>
        {moment(props.timeBucket.startTime, 'HH:mm').format('LT')}
        -
        {moment(props.timeBucket.endTime, 'HH:mm').format('LT')}
        {props.timeBucket.additionalSkillSet.length > 0 &&
          <Text>
          (
            {props.timeBucket.additionalSkillSet.map(skill => skill.name).join(', ')}
          )
          </Text>
        }
      </Title>
      <Flex breakpointMods={[{ modifier: FlexModifiers["space-items-lg"]}]}>
        {new Array(Math.ceil(rotationLength / 7)).fill(null).map((_, index) => (
          <FlexItem>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(7, 30px)',
                columnGap: '0.5rem',
                cursor: 'pointer',
              }}
            >
              {props.timeBucket.seatList
                 .filter(seat => seat.dayInRotation >= index * 7 && seat.dayInRotation < (index + 1) * 7)
                 .sort((a,b) => b.dayInRotation - a.dayInRotation)
                 .map(seat => (
                   <div
                     title={seat.stub? seat.stub.employee? seat.stub.employee.name : 'Unassigned' : 'No Shift'}
                     style={{
                       border: '1px solid black',
                       height: '50px',
                       backgroundColor: seat.stub? seat.stub.color : 'gray'
                     }}
                     onMouseDown={() => {
                       props.onUpdateTimeBucket({
                         ...props.timeBucket,
                         seatList: [
                           ...props.timeBucket.seatList.filter(s => s.dayInRotation < seat.dayInRotation),
                           {
                             ...seat,
                             stub: props.selectedStub, 
                           },
                           ...props.timeBucket.seatList.filter(s => s.dayInRotation > seat.dayInRotation),
                         ],
                       });
                     }}
                     onMouseMove={e => {
                       if ((e.buttons & 1) === 1) { // 1 = Left mouse button
                         props.onUpdateTimeBucket({
                           ...props.timeBucket,
                           seatList: [
                             ...props.timeBucket.seatList.filter(s => s.dayInRotation < seat.dayInRotation),
                             {
                               ...seat,
                               stub: props.selectedStub, 
                             },
                             ...props.timeBucket.seatList.filter(s => s.dayInRotation > seat.dayInRotation),
                           ],
                         });
                       }
                     }}
                   />
                 ))}
             <span style={{ gridRow: 2, gridColumn: 1, justifySelf: 'center', userSelect: 'none' }}>{(index * 7) + 1}</span>
             <span style={{ gridRow: 2, gridColumn: 7, justifySelf: 'center', userSelect: 'none' }}>{(index + 1) * 7}</span>
            </div>
          </FlexItem>
        ))}
      </Flex>
    </>
  );
};