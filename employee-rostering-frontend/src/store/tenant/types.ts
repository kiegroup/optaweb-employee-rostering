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

import { Action } from 'redux';
import Tenant from 'domain/Tenant';

export enum ActionType {
  CHANGE_TENANT = 'CHANGE_TENANT',
  ADD_TENANT = 'ADD_TENANT',
  REMOVE_TENANT = 'REMOVE_TENANT',
  REFRESH_TENANT_LIST = 'REFRESH_TENANT_LIST'
}

export interface ChangeTenantAction extends Action<ActionType.CHANGE_TENANT> {
  readonly tenantId: number;
}

export interface AddTenantAction extends Action<ActionType.ADD_TENANT> {
  readonly tenant: Tenant;
  // TODO: Add roster parameterization when work on Admin page started
}

export interface RemoveTenantAction extends Action<ActionType.REMOVE_TENANT> {
  readonly tenant: Tenant;
}

export interface RefreshTenantListAction extends Action<ActionType.REFRESH_TENANT_LIST> {
  readonly tenantId: number;
  readonly tenantList: Tenant[];
}

export type TenantAction = ChangeTenantAction | RefreshTenantListAction;

export interface TenantData {
  readonly currentTenantId: number;
  readonly tenantList: Tenant[];
}
