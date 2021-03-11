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
