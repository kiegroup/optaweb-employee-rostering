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
