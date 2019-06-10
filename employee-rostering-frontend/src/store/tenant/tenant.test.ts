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

import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import * as skillActions from '../skill/actions';
import reducer, { tenantOperations } from './index';
import {onGet} from 'store/rest/RestServiceClient';
import Tenant from 'domain/Tenant';

describe('Tenant operations', () => {
  it('should dispatch actions and call client', async () => {
    const { store, client } = mockStore(state);
    const mockTenantList: Tenant[] = [{
      id: 1,
      version: 0,
      name: "Tenant 1"
    },
    {
      id: 2,
      version: 0,
      name: "Tenant 2"
    },
    {
      id: 3,
      version: 0,
      name: "Tenant 3"
    }];

    onGet(`/tenant/`, mockTenantList);
    onGet(`/tenant/0/skill/`, []);
    await store.dispatch(tenantOperations.refreshTenantList());
    expect(store.getActions()).toEqual([actions.refreshTenantList({currentTenantId: 1, tenantList: mockTenantList}),
      skillActions.refreshSkillList([])]);
    expect(client.get).toHaveBeenCalledTimes(2);
    expect(client.get).nthCalledWith(1, `/tenant/`);
    expect(client.get).nthCalledWith(2, `/tenant/0/skill/`);

    store.clearActions();
    client.get.mockClear();

    mockTenantList[1].id = 0;
    await store.dispatch(tenantOperations.refreshTenantList());
    expect(store.getActions()).toEqual([actions.refreshTenantList({currentTenantId: 0, tenantList: mockTenantList}),
      skillActions.refreshSkillList([])]);
    expect(client.get).toHaveBeenCalledTimes(2);
    expect(client.get).nthCalledWith(1, `/tenant/`);
    expect(client.get).nthCalledWith(2, `/tenant/0/skill/`);

    store.clearActions();
    client.get.mockClear();

    await store.dispatch(tenantOperations.changeTenant(0));
    expect(store.getActions()).toEqual([actions.changeTenant(0), skillActions.refreshSkillList([])]);
  });
});

describe('Tenant reducers', () => {
  const newTenantList: Tenant[] = [{
    id: 1,
    version: 0,
    name: "Tenant 1"
  },
  {
    id: 2,
    version: 0,
    name: "Tenant 2"
  },
  {
    id: 3,
    version: 0,
    name: "Tenant 3"
  }];

  it('refresh tenant list', () => {
    expect(
      reducer(state.tenantData, actions.refreshTenantList({currentTenantId: 1, tenantList: newTenantList})),
    ).toEqual({currentTenantId: 1, tenantList: newTenantList})
  });
  it('change tenant', () => {
    expect(
      reducer(state.tenantData, actions.changeTenant(1)),
    ).toEqual({...state.tenantData, currentTenantId: 1});
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: [
      {
        id: 0,
        version: 0,
        name: "Tenant 0"
      },
      {
        id: 1,
        version: 0,
        name: "Tenant 1"
      }
    ]
  },
  skillList: {
    skillList: []
  }
};