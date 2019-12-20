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

import { withElement, withoutElement } from 'util/ImmutableCollectionOperations';
import { ActionType, TenantData, TenantAction } from './types';

const initialState: TenantData = {
  currentTenantId: 0,
  tenantList: [],
  timezoneList: [],
};

const tenantReducer = (state = initialState, action: TenantAction): TenantData => {
  switch (action.type) {
    case ActionType.CHANGE_TENANT: {
      return { ...state, currentTenantId: action.tenantId };
    }
    case ActionType.REFRESH_TENANT_LIST: {
      return { ...state, currentTenantId: action.tenantId, tenantList: action.tenantList };
    }
    case ActionType.ADD_TENANT: {
      return { ...state, tenantList: withElement(state.tenantList, action.tenant) };
    }
    case ActionType.REMOVE_TENANT: {
      return { ...state, tenantList: withoutElement(state.tenantList, action.tenant) };
    }
    case ActionType.REFRESH_SUPPORTED_TIMEZONES: {
      return { ...state, timezoneList: action.timezoneList };
    }
    default:
      return state;
  }
};

export default tenantReducer;
