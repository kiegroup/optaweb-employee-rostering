
import { Action } from 'redux';
import { Tenant } from 'domain/Tenant';
import { List } from 'immutable';

export enum ActionType {
  CHANGE_TENANT = 'CHANGE_TENANT',
  ADD_TENANT = 'ADD_TENANT',
  REMOVE_TENANT = 'REMOVE_TENANT',
  REFRESH_TENANT_LIST = 'REFRESH_TENANT_LIST',
  REFRESH_SUPPORTED_TIMEZONES = 'REFRESH_SUPPORTED_TIMEZONES',
}

export enum ConnectionActionType {
  SET_CONNECTED_ACTION = 'SET_CONNECTED_ACTION'
}

export interface ChangeTenantAction extends Action<ActionType.CHANGE_TENANT> {
  readonly tenantId: number;
}

export interface AddTenantAction extends Action<ActionType.ADD_TENANT> {
  readonly tenant: Tenant;
}

export interface RemoveTenantAction extends Action<ActionType.REMOVE_TENANT> {
  readonly tenant: Tenant;
}

export interface RefreshTenantListAction extends Action<ActionType.REFRESH_TENANT_LIST> {
  readonly tenantId: number;
  readonly tenantList: Tenant[];
}

export interface RefreshSupportedTimezoneListAction extends Action<ActionType.REFRESH_SUPPORTED_TIMEZONES> {
  readonly timezoneList: string[];
}

export interface SetConnectedAction extends Action<ConnectionActionType.SET_CONNECTED_ACTION> {
  readonly isConnected: boolean;
}

export type ConnectAction = SetConnectedAction;

// TODO: Add roster parameterization (somewhere)

export type TenantAction = ChangeTenantAction | RefreshTenantListAction | AddTenantAction |
RemoveTenantAction | RefreshSupportedTimezoneListAction;

export interface TenantData {
  readonly currentTenantId: number;
  readonly tenantList: List<Tenant>;
  readonly timezoneList: string[];
}
