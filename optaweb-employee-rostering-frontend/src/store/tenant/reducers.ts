
import { List } from 'immutable';
import { ActionType, TenantData, TenantAction, ConnectAction, ConnectionActionType } from './types';

const initialState: TenantData = {
  currentTenantId: 0,
  tenantList: List(),
  timezoneList: [],
};

const initialConnectionState = true;

const tenantReducer = (state = initialState, action: TenantAction): TenantData => {
  switch (action.type) {
    case ActionType.CHANGE_TENANT: {
      return { ...state, currentTenantId: action.tenantId };
    }
    case ActionType.REFRESH_TENANT_LIST: {
      return { ...state, currentTenantId: action.tenantId, tenantList: List(action.tenantList) };
    }
    case ActionType.ADD_TENANT: {
      return { ...state, tenantList: state.tenantList.push(action.tenant) };
    }
    case ActionType.REMOVE_TENANT: {
      return { ...state, tenantList: state.tenantList.filterNot(tenant => tenant.id === action.tenant.id) };
    }
    case ActionType.REFRESH_SUPPORTED_TIMEZONES: {
      return { ...state, timezoneList: action.timezoneList };
    }
    default:
      return state;
  }
};

export const connectionReducer = (state = initialConnectionState, action: ConnectAction): boolean => {
  switch (action.type) {
    case ConnectionActionType.SET_CONNECTED_ACTION:
      return action.isConnected;
    default:
      return state;
  }
};

export default tenantReducer;
