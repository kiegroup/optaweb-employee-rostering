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

import { ThunkCommandFactory, AppState } from '../types';
import * as actions from './actions';
import { SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction, TerminateSolvingRosterEarlyAction } from './types';
import RosterState from 'domain/RosterState';
import ShiftRosterView from 'domain/ShiftRosterView';
import { PaginationData, ObjectNumberMap, mapObjectNumberMap } from 'types';
import moment from 'moment';
import Spot from 'domain/Spot';
import { showInfoMessage } from 'ui/Alerts';
import { ThunkDispatch } from 'redux-thunk';
import { KindaShiftView, kindaShiftViewAdapter } from 'store/shift/operations';
import RestServiceClient from 'store/rest';

export type RosterSliceInfo = {
  fromDate: Date;
  toDate: Date;
};

interface KindaShiftRosterView extends Omit<ShiftRosterView, "spotIdToShiftViewListMap"> {
  spotIdToShiftViewListMap: ObjectNumberMap<KindaShiftView[]>;
}

let lastCalledShiftRosterArgs: any | null;
let lastCalledShiftRoster: ThunkCommandFactory<any, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> | null = null;

let stopSolvingRosterTimeout: NodeJS.Timeout|null = null;
let autoRefreshShiftRosterDuringSolvingIntervalTimeout: NodeJS.Timeout|null = null;

function stopSolvingRoster(dispatch: ThunkDispatch<AppState, RestServiceClient, TerminateSolvingRosterEarlyAction>) {
  if (stopSolvingRosterTimeout !== null) {
    clearTimeout(stopSolvingRosterTimeout);
    stopSolvingRosterTimeout = null;
  }
  if (autoRefreshShiftRosterDuringSolvingIntervalTimeout !== null) {
    clearInterval(autoRefreshShiftRosterDuringSolvingIntervalTimeout);
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = null;
  }
  dispatch(actions.terminateSolvingRosterEarly());
  Promise.all([
    dispatch(refreshShiftRoster())
  ]).then(() => {
    showInfoMessage("Finished Solving Roster", `Finished Solving Roster at ${moment(new Date()).format("LLL")}`);
  });
}

export const solveRoster: ThunkCommandFactory<void, SolveRosterAction> = () => 
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    return client.post(`/tenant/${tenantId}/roster/solve`, {}).then(() => {
      let solvingStartTime: number = new Date().getTime();
      const updateInterval = 1000;
      const solvingLength = 30 * 1000;
      dispatch(actions.solveRoster());
      showInfoMessage("Started Solving Roster", `Started Solving Roster at ${moment(solvingStartTime).format("LLL")}.`);
      autoRefreshShiftRosterDuringSolvingIntervalTimeout = setInterval(() => {
        dispatch(refreshShiftRoster());
      },updateInterval);
      stopSolvingRosterTimeout = setTimeout(() => stopSolvingRoster(dispatch), solvingLength);
    });
  }


export const terminateSolvingRosterEarly: ThunkCommandFactory<void, TerminateSolvingRosterEarlyAction> = () => 
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    return client.post(`/tenant/${tenantId}/roster/terminate`, {}).then(() => stopSolvingRoster(dispatch));
  }


export const refreshShiftRoster: ThunkCommandFactory<void, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = () =>
  (dispatch, state, client) => {
    if (lastCalledShiftRosterArgs !== null && lastCalledShiftRoster !== null) {
      dispatch(lastCalledShiftRoster(lastCalledShiftRosterArgs));
    }
  }

export const getRosterState: ThunkCommandFactory<void, SetRosterStateIsLoadingAction | SetRosterStateAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setRosterStateIsLoading(true));
    return client.get<RosterState>(`/tenant/${tenantId}/roster/state`).then(newRosterState => {
      dispatch(actions.setRosterState(newRosterState));
      dispatch(actions.setRosterStateIsLoading(false));
    });
  };

function convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView: KindaShiftRosterView): ShiftRosterView {
  return {
    ...newShiftRosterView,
    spotIdToShiftViewListMap: mapObjectNumberMap(newShiftRosterView.spotIdToShiftViewListMap, shiftViewList =>
      shiftViewList.map(kindaShiftViewAdapter))
  };
}

export const getCurrentShiftRoster: ThunkCommandFactory<PaginationData, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = pagination =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getCurrentShiftRoster;
      lastCalledShiftRosterArgs = pagination;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getShiftRoster: ThunkCommandFactory<RosterSliceInfo & { pagination: PaginationData; }, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView?` +
    `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getShiftRoster;
      lastCalledShiftRosterArgs = params;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getShiftRosterFor: ThunkCommandFactory<RosterSliceInfo & { spotList: Spot[] }, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.post<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/for?` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`, params.spotList).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getShiftRosterFor;
      lastCalledShiftRosterArgs = params;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };