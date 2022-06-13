
import { Tenant } from 'domain/Tenant';
import { ActionFactory } from '../types';
import {
  ActionType, ConnectionActionType, ChangeTenantAction, RefreshTenantListAction,
  AddTenantAction, RefreshSupportedTimezoneListAction, SetConnectedAction,
  RemoveTenantAction,
} from './types';

export const setConnectionStatus: ActionFactory<boolean, SetConnectedAction> = isConnected => ({
  type: ConnectionActionType.SET_CONNECTED_ACTION,
  isConnected,
});

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
