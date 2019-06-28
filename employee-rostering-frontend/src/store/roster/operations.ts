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
import { SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction, TerminateRosterEarlyAction } from './types';
import RosterState from 'domain/RosterState';
import ShiftRosterView from 'domain/ShiftRosterView';
import { PaginationData } from 'types';
import moment from 'moment';

export type RosterSliceInfo = {
  pagination: PaginationData;
  fromDate: Date;
  toDate: Date;
};

export const getRosterState: ThunkCommandFactory<void, SetRosterStateIsLoadingAction | SetRosterStateAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setRosterStateIsLoading(true));
    return client.get<RosterState>(`/tenant/${tenantId}/roster/state`).then(newRosterState => {
      dispatch(actions.setRosterState(newRosterState));
      dispatch(actions.setRosterStateIsLoading(false));
    });
  };

export const getCurrentShiftRoster: ThunkCommandFactory<PaginationData, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = pagination =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<ShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then(newShiftRosterView => {
      dispatch(actions.setShiftRosterView({ shiftRoster: newShiftRosterView, paginationData: pagination }));
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getShiftRoster: ThunkCommandFactory<RosterSliceInfo, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<ShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView?` +
    `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then(newShiftRosterView => {
      dispatch(actions.setShiftRosterView({ shiftRoster: newShiftRosterView, paginationData: params.pagination }));
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };