
import { TimeBucket } from 'domain/TimeBucket';
import { alert } from 'store/alert';
import { AddAlertAction } from 'store/alert/types';
import {
  timeBucketToTimeBucketView,
  timeBucketViewToDomainObjectView,
  TimeBucketView,
} from 'store/rotation/TimeBucketView';
import { mapDomainObjectToView } from 'util/ImmutableCollectionOperations';
import {
  SetTimeBucketListLoadingAction, AddTimeBucketAction, RemoveTimeBucketAction,
  UpdateTimeBucketAction, RefreshTimeBucketListAction,
} from './types';
import * as actions from './actions';
import { ThunkCommandFactory } from '../types';

export const addTimeBucket: ThunkCommandFactory<TimeBucket, AddAlertAction |
AddTimeBucketAction> = timeBucket => (dispatch, state, client) => {
  const { tenantId } = timeBucket;
  const view = timeBucketToTimeBucketView(timeBucket);
  return client.post<TimeBucketView>(`/tenant/${tenantId}/rotation/add`, view).then((newTimeBucket) => {
    dispatch(actions.addTimeBucket(timeBucketViewToDomainObjectView(newTimeBucket)));
  });
};

export const removeTimeBucket: ThunkCommandFactory<TimeBucket, AddAlertAction |
RemoveTimeBucketAction> = timeBucket => (dispatch, state, client) => {
  const { tenantId } = timeBucket;
  const timeBucketId = timeBucket.id;
  return client.delete<boolean>(`/tenant/${tenantId}/rotation/${timeBucketId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(actions.removeTimeBucket(mapDomainObjectToView(timeBucket)));
    } else {
      dispatch(alert.showErrorMessage('removeShiftTemplateError', { id: timeBucketId }));
    }
  });
};

export const updateTimeBucket: ThunkCommandFactory<TimeBucket, AddAlertAction |
UpdateTimeBucketAction> = timeBucket => (dispatch, state, client) => {
  const { tenantId } = timeBucket;
  const view = timeBucketToTimeBucketView(timeBucket);
  return client.put<TimeBucketView>(`/tenant/${tenantId}/rotation/update`, view).then((updatedTimeBucket) => {
    dispatch(actions.updateTimeBucket(timeBucketViewToDomainObjectView(updatedTimeBucket)));
  });
};

export const refreshTimeBucketList: ThunkCommandFactory<void, SetTimeBucketListLoadingAction |
RefreshTimeBucketListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsTimeBucketListLoading(true));
  return client.get<TimeBucketView[]>(`/tenant/${tenantId}/rotation/`).then((timeBucketList) => {
    dispatch(actions.refreshTimeBucketList(timeBucketList.map(timeBucketViewToDomainObjectView)));
    dispatch(actions.setIsTimeBucketListLoading(false));
  });
};
