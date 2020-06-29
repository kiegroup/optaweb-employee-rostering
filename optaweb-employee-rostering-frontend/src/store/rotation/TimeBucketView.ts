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
import { TimeBucket } from 'domain/TimeBucket';
import { DomainObject } from 'domain/DomainObject';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import moment from 'moment';
import DomainObjectView from 'domain/DomainObjectView';

export function timeBucketToTimeBucketView(timeBucket: TimeBucket): TimeBucketView {
  return {
    ...objectWithout(timeBucket, 'spot', 'additionalSkillSet', 'seatList'),
    spotId: timeBucket.spot.id as number,
    additionalSkillSetIdList: timeBucket.additionalSkillSet.map(skill => skill.id as number),
    seatList: timeBucket.seatList
      .map(seat => ({
        dayInRotation: seat.dayInRotation,
        employeeId: seat.employee ? seat.employee.id as number : null,
      })),
    startTime: moment(timeBucket.startTime).format('HH:mm:ss'),
    endTime: moment(timeBucket.endTime).format('HH:mm:ss'),
  };
}

export function timeBucketViewToDomainObjectView(view: TimeBucketView): DomainObjectView<TimeBucket> {
  return {
    ...objectWithout(view, 'spotId', 'additionalSkillSetIdList'),
    spot: view.spotId,
    additionalSkillSet: view.additionalSkillSetIdList,
    seatList: view.seatList.map(sv => ({ dayInRotation: sv.dayInRotation, employee: sv.employeeId })),
    startTime: moment(view.startTime, 'HH:mm:ss').toDate(),
    endTime: moment(view.endTime, 'HH:mm:ss').toDate(),
  };
}

export interface SeatView {
  dayInRotation: number;
  employeeId: number|null;
}

export interface TimeBucketView extends DomainObject {
  spotId: number;
  additionalSkillSetIdList: number[];
  repeatOnDaySetList: string[];
  startTime: string;
  endTime: string;
  seatList: SeatView[];
}
