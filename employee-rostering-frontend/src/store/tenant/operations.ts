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

import Tenant from 'domain/Tenant';
import { ThunkCommandFactory, AppState } from '../types';
import * as actions from './actions';
import { ChangeTenantAction, RefreshTenantListAction, RefreshSupportedTimezoneListAction,
  AddTenantAction, RemoveTenantAction} from './types';
import { skillOperations } from 'store/skill';
import { ThunkDispatch } from 'redux-thunk';
import { Action } from 'redux';
import { rosterOperations } from 'store/roster';
import { spotOperations } from 'store/spot';
import { contractOperations } from 'store/contract';
import { employeeOperations } from 'store/employee';
import * as rosterActions from 'store/roster/actions';
import moment from 'moment';
import { shiftTemplateOperations } from 'store/rotation';
import RosterState from 'domain/RosterState';

function refreshData(dispatch: ThunkDispatch<any, any, Action<any>>, state: () => AppState): Promise<any> {
  dispatch(rosterActions.setShiftRosterIsLoading(true));
  dispatch(rosterActions.setAvailabilityRosterIsLoading(true));
  return Promise.all([
    dispatch(skillOperations.refreshSkillList()),
    dispatch(rosterOperations.getRosterState()),
    dispatch(spotOperations.refreshSpotList()),
    dispatch(contractOperations.refreshContractList()),
    dispatch(employeeOperations.refreshEmployeeList()),
    dispatch(shiftTemplateOperations.refreshShiftTemplateList())
  ]
  ).then(() => {
    dispatch(rosterOperations.getInitialShiftRoster());
    dispatch(rosterOperations.getInitialAvailabilityRoster());
  });
}

export const changeTenant: ThunkCommandFactory<number, ChangeTenantAction> = tenantId => (dispatch, state, client) => {
  dispatch(actions.changeTenant(tenantId));
  return refreshData(dispatch, state);
};

export const refreshTenantList:
ThunkCommandFactory<void, RefreshTenantListAction> = () => (dispatch, state, client) => (
  client.get<Tenant[]>('/tenant/').then((tenantList) => {
    const { currentTenantId } = state().tenantData;
    if (tenantList.filter(tenant => tenant.id === currentTenantId).length !== 0) {
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId }));
    } else if (tenantList.length > 0) {
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId: tenantList[0].id as number }));
    } else {
      // TODO: this case occurs iff there are no tenants; need a special screen for that
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId: 0 }));
    }
    refreshData(dispatch, state);
  }));

// TODO: Add addTenant and removeTenant when work on Admin page started
export const addTenant: ThunkCommandFactory<RosterState, AddTenantAction> = rs =>
  (dispatch, state, client) => {
    return client.post<Tenant>('/tenant/add', rs).then(tenant => {
      dispatch(actions.addTenant(tenant));
    });
  };

export const removeTenant: ThunkCommandFactory<Tenant, RemoveTenantAction> = tenant =>
  (dispatch, state, client) => {
    return client.post<boolean>(`/tenant/remove/${tenant.id}`, {}).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeTenant(tenant));
      }
      else {
        // TODO: Display error
      }
    });
  };

export const refreshSupportedTimezones: ThunkCommandFactory<void, RefreshSupportedTimezoneListAction> = () =>
  (dispatch, state, client) => {
    return client.get<string[]>("/tenant/supported/timezones").then(supportedTimezones => {
      dispatch(actions.refreshSupportedTimezones(supportedTimezones));
    });
  };
