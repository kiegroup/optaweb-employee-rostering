import { skillSelectors } from 'store/skill';
import { Spot } from 'domain/Spot';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';
import { AppState } from '../types';

export const getSpotById = (state: AppState, id: number): Spot => {
  if (state.spotList.isLoading || state.skillList.isLoading) {
    throw Error('Spot list is loading');
  }
  const spotView = state.spotList.spotMapById.get(id) as DomainObjectView<Spot>;
  return {
    ...spotView,
    requiredSkillSet: spotView.requiredSkillSet.map(skillId => skillSelectors.getSkillById(state, skillId)),
  };
};

let oldSpotMapById: Map<number, DomainObjectView<Spot>> | null = null;
let spotListForOldSpotMapById: Spot[] | null = null;

export const getSpotList = (state: AppState): Spot[] => {
  if (state.spotList.isLoading || state.skillList.isLoading) {
    return [];
  }
  if (oldSpotMapById === state.spotList.spotMapById && spotListForOldSpotMapById !== null) {
    return spotListForOldSpotMapById;
  }

  const out = state.spotList.spotMapById.keySeq().map(key => getSpotById(state, key))
    .sortBy(spot => spot.name).toList();

  oldSpotMapById = state.spotList.spotMapById;
  spotListForOldSpotMapById = out.toArray();
  return spotListForOldSpotMapById;
};
