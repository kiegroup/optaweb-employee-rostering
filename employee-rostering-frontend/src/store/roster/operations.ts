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
import ShiftView from 'domain/ShiftView';
import { SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction, TerminateRosterEarlyAction } from './types';
import RosterState from 'domain/RosterState';
import ShiftRosterView from 'domain/ShiftRosterView';

export const getRosterState: ThunkCommandFactory<Shift, SetRosterStateIsLoadingAction | SetRosterStateAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setRosterStateIsLoading(true));
    return client.get<RosterState>(`/tenant/${tenantId}/roster/state`).then(newRosterState => {
      dispatch(actions.setRosterState(newRosterState));
      dispatch(actions.setRosterStateIsLoading(false));
    });
  };

export const getCurrentShiftRoster: ThunkCommandFactory<Shift, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<ShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/current`).then(newShiftRosterView => {
      dispatch(actions.setShiftRosterView(newShiftRosterView));
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const updateShift: ThunkCommandFactory<Shift, UpdateShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.post<ShiftView>(`/tenant/${tenantId}/shift/update`, shift).then(updatedShift => {
      dispatch(actions.updateShift(updatedShift));
    });
  };

export const refreshShiftList: ThunkCommandFactory<void, SetShiftListLoadingAction | RefreshShiftListAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setIsShiftListLoading(true));
    return client.get<ShiftView[]>(`/tenant/${tenantId}/shift/`).then(shiftList => {
      dispatch(actions.refreshShiftList(shiftList));
      dispatch(actions.setIsShiftListLoading(false));
    });
  };
