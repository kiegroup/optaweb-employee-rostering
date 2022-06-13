
import { TimeBucket } from 'domain/TimeBucket';
import DomainObjectView from 'domain/DomainObjectView';
import { ActionFactory } from '../types';
import {
  ActionType, SetTimeBucketListLoadingAction, AddTimeBucketAction,
  UpdateTimeBucketAction, RemoveTimeBucketAction, RefreshTimeBucketListAction,
} from './types';

export const setIsTimeBucketListLoading: ActionFactory<boolean, SetTimeBucketListLoadingAction> = isLoading => ({
  type: ActionType.SET_TIME_BUCKET_LIST_LOADING,
  isLoading,
});

export const addTimeBucket:
ActionFactory<DomainObjectView<TimeBucket>, AddTimeBucketAction> = timeBucket => ({
  type: ActionType.ADD_TIME_BUCKET,
  timeBucket,
});

export const removeTimeBucket: ActionFactory<DomainObjectView<TimeBucket>,
RemoveTimeBucketAction> = timeBucket => ({
  type: ActionType.REMOVE_TIME_BUCKET,
  timeBucket,
});

export const updateTimeBucket: ActionFactory<DomainObjectView<TimeBucket>,
UpdateTimeBucketAction> = timeBucket => ({
  type: ActionType.UPDATE_TIME_BUCKET,
  timeBucket,
});

export const refreshTimeBucketList: ActionFactory<DomainObjectView<TimeBucket>[],
RefreshTimeBucketListAction> = timeBucketList => ({
  type: ActionType.REFRESH_TIME_BUCKET_LIST,
  timeBucketList,
});
