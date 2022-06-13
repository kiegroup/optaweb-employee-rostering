
import { Action } from 'redux';
import { Spot } from 'domain/Spot';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';

export enum ActionType {
  ADD_SPOT = 'ADD_SPOT',
  REMOVE_SPOT = 'REMOVE_SPOT',
  UPDATE_SPOT = 'UPDATE_SPOT',
  REFRESH_SPOT_LIST = 'REFRESH_SPOT_LIST',
  SET_SPOT_LIST_LOADING = 'SET_SPOT_LIST_LOADING'
}

export interface SetSpotListLoadingAction extends Action<ActionType.SET_SPOT_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddSpotAction extends Action<ActionType.ADD_SPOT> {
  readonly spot: Spot;
}

export interface RemoveSpotAction extends Action<ActionType.REMOVE_SPOT> {
  readonly spot: Spot;
}

export interface UpdateSpotAction extends Action<ActionType.UPDATE_SPOT> {
  readonly spot: Spot;
}

export interface RefreshSpotListAction extends Action<ActionType.REFRESH_SPOT_LIST> {
  readonly spotList: Spot[];
}

export type SpotAction = SetSpotListLoadingAction | AddSpotAction | RemoveSpotAction |
UpdateSpotAction | RefreshSpotListAction;

export interface SpotList {
  readonly isLoading: boolean;
  readonly spotMapById: Map<number, DomainObjectView<Spot>>;
}
