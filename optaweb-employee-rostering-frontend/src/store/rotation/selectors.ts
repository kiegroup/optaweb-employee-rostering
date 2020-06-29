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
import DomainObjectView from 'domain/DomainObjectView';
import { spotSelectors } from 'store/spot';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { employeeSelectors } from 'store/employee';
import { skillSelectors } from 'store/skill';
import { AppState } from '../types';

export function isLoading(state: AppState) {
  return state.timeBucketList.isLoading || state.employeeList.isLoading
    || state.contractList.isLoading || state.spotList.isLoading || state.skillList.isLoading;
}

export const getTimeBucketById = (state: AppState, id: number): TimeBucket => {
  if (isLoading(state)) {
    throw Error('Time Bucket list is loading');
  }
  const timeBucketView = state.timeBucketList.timeBucketMapById.get(id) as DomainObjectView<TimeBucket>;
  return {
    ...objectWithout(timeBucketView, 'spot', 'additionalSkillSet', 'seatList'),
    spot: spotSelectors.getSpotById(state, timeBucketView.spot),
    additionalSkillSet: timeBucketView.additionalSkillSet.map(skillId => skillSelectors.getSkillById(state, skillId)),
    seatList: timeBucketView.seatList.map(seat => (
      { ...seat, employee: (seat.employee != null) ? employeeSelectors.getEmployeeById(state, seat.employee) : null })),
  };
};

export const getTimeBucketList = (state: AppState): TimeBucket[] => {
  if (isLoading(state)) {
    return [];
  }
  const out: TimeBucket[] = [];
  state.timeBucketList.timeBucketMapById.forEach((value, key) => out.push(getTimeBucketById(state, key)));
  return out;
};
