
import { Action } from 'redux';
import DomainObjectView from 'domain/DomainObjectView';
import { TimeBucket } from 'domain/TimeBucket';
import { Map } from 'immutable';

export enum ActionType {
  ADD_TIME_BUCKET = 'ADD__TIME_BUCKET',
  REMOVE_TIME_BUCKET = 'REMOVE_TIME_BUCKET',
  UPDATE_TIME_BUCKET = 'UPDATE_TIME_BUCKET',
  REFRESH_TIME_BUCKET_LIST = 'REFRESH_TIME_BUCKET_LIST',
  SET_TIME_BUCKET_LIST_LOADING = 'SET_TIME_BUCKET_LIST_LOADING'
}

export interface SetTimeBucketListLoadingAction extends Action<ActionType.SET_TIME_BUCKET_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddTimeBucketAction extends Action<ActionType.ADD_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface RemoveTimeBucketAction extends Action<ActionType.REMOVE_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface UpdateTimeBucketAction extends Action<ActionType.UPDATE_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface RefreshTimeBucketListAction extends Action<ActionType.REFRESH_TIME_BUCKET_LIST> {
  readonly timeBucketList: DomainObjectView<TimeBucket>[];
}

export type TimeBucketAction = SetTimeBucketListLoadingAction | AddTimeBucketAction |
RemoveTimeBucketAction | UpdateTimeBucketAction | RefreshTimeBucketListAction;

export interface TimeBucketList {
  readonly isLoading: boolean;
  readonly timeBucketMapById: Map<number, DomainObjectView<TimeBucket>>;
}
