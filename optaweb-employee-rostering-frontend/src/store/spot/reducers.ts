
import { createIdMapFromList, mapDomainObjectToView } from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { Spot } from 'domain/Spot';
import { Map } from 'immutable';
import { ActionType, SpotList, SpotAction } from './types';

export const initialState: SpotList = {
  isLoading: true,
  spotMapById: Map<number, DomainObjectView<Spot>>(),
};

const spotReducer = (state = initialState, action: SpotAction): SpotList => {
  switch (action.type) {
    case ActionType.SET_SPOT_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_SPOT:
    case ActionType.UPDATE_SPOT: {
      return { ...state,
        spotMapById: state.spotMapById.set(action.spot.id as number,
          mapDomainObjectToView(action.spot)) };
    }
    case ActionType.REMOVE_SPOT: {
      return { ...state, spotMapById: state.spotMapById.remove(action.spot.id as number) };
    }
    case ActionType.REFRESH_SPOT_LIST: {
      return { ...state, spotMapById: createIdMapFromList(action.spotList) };
    }
    default:
      return state;
  }
};

export default spotReducer;
