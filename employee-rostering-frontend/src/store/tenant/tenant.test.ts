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
import * as spotActions from '../spot/actions';
import * as contractActions from '../contract/actions';
import * as employeeActions from '../employee/actions';
import reducer, { tenantOperations } from './index';
import { onGet } from 'store/rest/RestTestUtils';
import Tenant from 'domain/Tenant';

describe('Tenant operations', () => {
  it('should dispatch actions and change tenant when current tenant not in refreshed the tenant list ',
    async () => {
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

      let expectedRefreshActions: any[] = [
        actions.refreshTenantList({currentTenantId: 1, tenantList: mockTenantList}),
        skillActions.refreshSkillList([]),
        spotActions.refreshSpotList([]),
        contractActions.refreshContractList([]),
        employeeActions.refreshEmployeeList([]),
        skillActions.setIsSkillListLoading(true),
        spotActions.setIsSpotListLoading(true),
        contractActions.setIsContractListLoading(true),
        employeeActions.setIsEmployeeListLoading(true),
        skillActions.setIsSkillListLoading(false),
        spotActions.setIsSpotListLoading(false),
        contractActions.setIsContractListLoading(false),
        employeeActions.setIsEmployeeListLoading(false)
      ];

      onGet(`/tenant/`, mockTenantList);
      onGet(`/tenant/0/skill/`, []);
      onGet(`/tenant/0/spot/`, []);
      onGet(`/tenant/0/contract/`, []);
      onGet(`/tenant/0/employee/`, []);
    
      await store.dispatch(tenantOperations.refreshTenantList());
    
      const actualRefreshActions = store.getActions();
      expect(actualRefreshActions).toEqual(expect.arrayContaining(expectedRefreshActions));
      expect(actualRefreshActions.length).toEqual(expectedRefreshActions.length);

      expect(client.get).toHaveBeenCalledTimes(5);
      expect(client.get).toHaveBeenCalledWith(`/tenant/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/skill/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/spot/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/contract/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/employee/`);
    });

  it('should dispatch actions and not change tenant when current tenant is in refreshed tenant list',
    async () => {
      const { store, client } = mockStore(state);
      const mockTenantList: Tenant[] = [{
        id: 1,
        version: 0,
        name: "Tenant 1"
      },
      {
        id: 0,
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
      onGet(`/tenant/0/spot/`, []);
      onGet(`/tenant/0/contract/`, []);
      onGet(`/tenant/0/employee/`, []);

      mockTenantList[1].id = 0;
      const expectedRefreshActions = [
        actions.refreshTenantList({currentTenantId: 0, tenantList: mockTenantList}),
        skillActions.refreshSkillList([]),
        spotActions.refreshSpotList([]),
        contractActions.refreshContractList([]),
        employeeActions.refreshEmployeeList([]),
        skillActions.setIsSkillListLoading(true),
        spotActions.setIsSpotListLoading(true),
        contractActions.setIsContractListLoading(true),
        employeeActions.setIsEmployeeListLoading(true),
        skillActions.setIsSkillListLoading(false),
        spotActions.setIsSpotListLoading(false),
        contractActions.setIsContractListLoading(false),
        employeeActions.setIsEmployeeListLoading(false)
      ];

      await store.dispatch(tenantOperations.refreshTenantList());
    
      const actualRefreshActions = store.getActions();
      expect(actualRefreshActions).toEqual(expect.arrayContaining(expectedRefreshActions));
      expect(actualRefreshActions.length).toEqual(expectedRefreshActions.length);

      expect(client.get).toHaveBeenCalledTimes(5);
      expect(client.get).toHaveBeenCalledWith(`/tenant/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/skill/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/spot/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/contract/`);
      expect(client.get).toHaveBeenCalledWith(`/tenant/0/employee/`);
    });

  it('should change the tenant and refresh all lists on change tenant', async () => {
    const { store, client } = mockStore(state);
    onGet(`/tenant/0/skill/`, []);
    onGet(`/tenant/0/spot/`, []);
    onGet(`/tenant/0/contract/`, []);
    onGet(`/tenant/0/employee/`, []);
    
    const expectedRefreshActions = [
      actions.changeTenant(0),
      skillActions.refreshSkillList([]),
      spotActions.refreshSpotList([]),
      contractActions.refreshContractList([]),
      employeeActions.refreshEmployeeList([]),
      skillActions.setIsSkillListLoading(true),
      spotActions.setIsSpotListLoading(true),
      contractActions.setIsContractListLoading(true),
      employeeActions.setIsEmployeeListLoading(true),
      skillActions.setIsSkillListLoading(false),
      spotActions.setIsSpotListLoading(false),
      contractActions.setIsContractListLoading(false),
      employeeActions.setIsEmployeeListLoading(false)
    ];

    await store.dispatch(tenantOperations.changeTenant(0));

    const actualRefreshActions = store.getActions();
    expect(actualRefreshActions).toEqual(expect.arrayContaining(expectedRefreshActions));
    expect(actualRefreshActions.length).toEqual(expectedRefreshActions.length);

    expect(client.get).toHaveBeenCalledTimes(4);
    expect(client.get).toHaveBeenCalledWith(`/tenant/0/skill/`);
    expect(client.get).toHaveBeenCalledWith(`/tenant/0/spot/`);
    expect(client.get).toHaveBeenCalledWith(`/tenant/0/contract/`);
    expect(client.get).toHaveBeenCalledWith(`/tenant/0/employee/`);
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
  employeeList: {
    isLoading: false,
    employeeMapById: new Map()
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map()
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map()
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map()
  }
};
