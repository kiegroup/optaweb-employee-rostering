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
