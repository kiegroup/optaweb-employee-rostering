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

import { alert } from 'store/alert';
import { Spot } from 'domain/Spot';
import { AddAlertAction } from 'store/alert/types';
import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import {
  SetSpotListLoadingAction, AddSpotAction, RemoveSpotAction, UpdateSpotAction,
  RefreshSpotListAction,
} from './types';

export const addSpot: ThunkCommandFactory<Spot, AddAlertAction | AddSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  return client.post<Spot>(`/tenant/${tenantId}/spot/add`, spot).then((newSpot) => {
    dispatch(alert.showSuccessMessage('addSpot', { name: newSpot.name }));
    dispatch(actions.addSpot(newSpot));
  });
};

export const removeSpot:
ThunkCommandFactory<Spot, AddAlertAction | RemoveSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  const spotId = spot.id;
  return client.delete<boolean>(`/tenant/${tenantId}/spot/${spotId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeSpot', { name: spot.name }));
      dispatch(actions.removeSpot(spot));
    } else {
      dispatch(alert.showErrorMessage('removeSpotError', { name: spot.name }));
    }
  });
};

export const updateSpot:
ThunkCommandFactory<Spot, AddAlertAction | UpdateSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  return client.post<Spot>(`/tenant/${tenantId}/spot/update`, spot).then((updatedSpot) => {
    dispatch(alert.showSuccessMessage('updateSpot', { id: updatedSpot.id }));
    dispatch(actions.updateSpot(updatedSpot));
  });
};

export const refreshSpotList:
ThunkCommandFactory<void, SetSpotListLoadingAction | RefreshSpotListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsSpotListLoading(true));
  return client.get<Spot[]>(`/tenant/${tenantId}/spot/`).then((spotList) => {
    dispatch(actions.refreshSpotList(spotList));
    dispatch(actions.setIsSpotListLoading(false));
  });
};
