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
import Shift from 'domain/Shift';
import { SetShiftListLoadingAction, AddShiftAction, RemoveShiftAction, UpdateShiftAction, RefreshShiftListAction } from './types';

export const addShift: ThunkCommandFactory<Shift, AddShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.post<Shift>(`/tenant/${tenantId}/shift/add`, shift).then(newShift => {
      dispatch(actions.addShift(newShift))
    });
  };

export const removeShift: ThunkCommandFactory<Shift, RemoveShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    const shiftId = shift.id;
    return client.delete<boolean>(`/tenant/${tenantId}/shift/${shiftId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeShift(shift));
      }
    });
  };

export const updateShift: ThunkCommandFactory<Shift, UpdateShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.post<Shift>(`/tenant/${tenantId}/shift/update`, shift).then(updatedShift => {
      dispatch(actions.updateShift(updatedShift));
    });
  };

export const refreshShiftList: ThunkCommandFactory<void, SetShiftListLoadingAction | RefreshShiftListAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setIsShiftListLoading(true));
    return client.get<Shift[]>(`/tenant/${tenantId}/shift/`).then(shiftList => {
      dispatch(actions.refreshShiftList(shiftList));
      dispatch(actions.setIsShiftListLoading(false));
    });
  };
