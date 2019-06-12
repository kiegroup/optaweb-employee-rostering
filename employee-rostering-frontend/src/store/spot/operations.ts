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

import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import Spot from 'domain/Spot';
import { AddSpotAction, RemoveSpotAction, UpdateSpotAction, RefreshSpotListAction } from './types';

export const addSpot: ThunkCommandFactory<Spot, AddSpotAction> = spot =>
  (dispatch, state, client) => {
    let tenantId = spot.tenantId;
    return client.post<Spot>(`/tenant/${tenantId}/spot/add`, spot).then(newSpot => {
      dispatch(actions.addSpot(newSpot))
    });
  };

export const removeSpot: ThunkCommandFactory<Spot, RemoveSpotAction> = spot =>
  (dispatch, state, client) => {
    let tenantId = spot.tenantId;
    let spotId = spot.id;
    return client.delete<boolean>(`/tenant/${tenantId}/spot/${spotId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeSpot(spot));
      }
    });
  };

export const updateSpot: ThunkCommandFactory<Spot, UpdateSpotAction> = spot =>
  (dispatch, state, client) => {
    let tenantId = spot.tenantId;
    return client.post<Spot>(`/tenant/${tenantId}/spot/update`, spot).then(updatedSpot => {
      dispatch(actions.updateSpot(updatedSpot));
    });
  };

export const refreshSpotList: ThunkCommandFactory<void, RefreshSpotListAction> = () =>
  (dispatch, state, client) => {
    let tenantId = state().tenantData.currentTenantId;
    return client.get<Spot[]>(`/tenant/${tenantId}/spot/`).then(spotList => {
      dispatch(actions.refreshSpotList(spotList));
    });
  };
