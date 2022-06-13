
import { Spot } from 'domain/Spot';
import { ActionFactory } from '../types';
import {
  ActionType, SetSpotListLoadingAction, AddSpotAction, UpdateSpotAction, RemoveSpotAction,
  RefreshSpotListAction,
} from './types';

export const setIsSpotListLoading: ActionFactory<boolean, SetSpotListLoadingAction> = isLoading => ({
  type: ActionType.SET_SPOT_LIST_LOADING,
  isLoading,
});

export const addSpot: ActionFactory<Spot, AddSpotAction> = newSpot => ({
  type: ActionType.ADD_SPOT,
  spot: newSpot,
});

export const removeSpot: ActionFactory<Spot, RemoveSpotAction> = deletedSpot => ({
  type: ActionType.REMOVE_SPOT,
  spot: deletedSpot,
});

export const updateSpot: ActionFactory<Spot, UpdateSpotAction> = updatedSpot => ({
  type: ActionType.UPDATE_SPOT,
  spot: updatedSpot,
});

export const refreshSpotList: ActionFactory<Spot[], RefreshSpotListAction> = newSpotList => ({
  type: ActionType.REFRESH_SPOT_LIST,
  spotList: newSpotList,
});
