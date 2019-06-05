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
import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import { ChangeTenantAction, RefreshTenantListAction } from './types';
import {refreshSkillList} from 'store/skill/operations';

export const changeTenant: ThunkCommandFactory<number, ChangeTenantAction> = tenantId =>
  (dispatch, state, client) => {
    dispatch(actions.changeTenant(tenantId));
    refreshSkillList();
  };

export const refreshTenantList: ThunkCommandFactory<void, RefreshTenantListAction> = () =>
  (dispatch, state, client) => {
    client.get<Tenant[]>(`/tenant/`).then(tenantList => {
      let currentTenantId = state().tenantData.currentTenantId;
      if (tenantList.filter(tenant => tenant.id === currentTenantId).length !== 0) {
        dispatch(actions.refreshTenantList({tenantList: tenantList, currentTenantId: currentTenantId}));
      }
      else if (tenantList.length > 0) {
        dispatch(actions.refreshTenantList({tenantList: tenantList, currentTenantId: tenantList[0].id as number}));
      }
      else {
      // TODO: this case occurs iff there are no tenants; need a special screen for that
        dispatch(actions.refreshTenantList({tenantList: tenantList, currentTenantId: 0}))
      }
    });
  };

// TODO: Add addTenant and removeTenant when work on Admin page started
