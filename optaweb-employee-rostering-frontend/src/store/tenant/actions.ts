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

import { Tenant } from 'domain/Tenant';
import { ActionFactory } from '../types';
import {
  ActionType, ChangeTenantAction, RefreshTenantListAction, AddTenantAction,
  RefreshSupportedTimezoneListAction,
  RemoveTenantAction,
} from './types';

export const changeTenant: ActionFactory<number, ChangeTenantAction> = newTenantId => ({
  type: ActionType.CHANGE_TENANT,
  tenantId: newTenantId,
});

export const addTenant: ActionFactory<Tenant, AddTenantAction> = newTenant => ({
  type: ActionType.ADD_TENANT,
  tenant: newTenant,
});

export const removeTenant: ActionFactory<Tenant, RemoveTenantAction> = removedTenant => ({
  type: ActionType.REMOVE_TENANT,
  tenant: removedTenant,
});

export const refreshSupportedTimezones:
ActionFactory<string[], RefreshSupportedTimezoneListAction> = supportedTimezones => ({
  type: ActionType.REFRESH_SUPPORTED_TIMEZONES,
  timezoneList: supportedTimezones,
});

export const refreshTenantList: ActionFactory<{currentTenantId: number; tenantList: Tenant[]},
RefreshTenantListAction> = params => ({
  type: ActionType.REFRESH_TENANT_LIST,
  tenantId: params.currentTenantId,
  tenantList: params.tenantList,
});
