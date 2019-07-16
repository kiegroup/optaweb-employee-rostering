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
import * as rosterActions from '../roster/actions';
import * as skillOperations from '../skill/operations';
import * as spotOperations from '../spot/operations';
import * as contractOperations from '../contract/operations';
import * as employeeOperations from '../employee/operations';
import * as rosterOperations from '../roster/operations';
import reducer, { tenantOperations } from './index';
import { onGet } from 'store/rest/RestTestUtils';
import Tenant from 'domain/Tenant';
import moment from 'moment';

describe('Tenant operations', () => {
  const mockRefreshSkillList = jest.spyOn(skillOperations, "refreshSkillList");
  const mockRefreshSpotList = jest.spyOn(spotOperations, "refreshSpotList");
  const mockRefreshContractList = jest.spyOn(contractOperations, "refreshContractList");
  const mockRefreshEmployeeList = jest.spyOn(employeeOperations, "refreshEmployeeList");
  const mockGetShiftRosterFor = jest.spyOn(rosterOperations, "getShiftRosterFor");
  const mockRefreshRosterState = jest.spyOn(rosterOperations, "getRosterState");

  beforeAll(() => {
    mockRefreshSkillList.mockImplementation(() => () => {});
    mockRefreshSpotList.mockImplementation(() => () => {});
    mockRefreshContractList.mockImplementation(() => () => {});
    mockRefreshEmployeeList.mockImplementation(() => () => {});
    mockGetShiftRosterFor.mockImplementation(() => () => {});
    mockRefreshRosterState.mockImplementation(() => () => {});
  });

  afterAll(() => {
    mockRefreshSkillList.mockRestore();
    mockRefreshSpotList.mockRestore();
    mockRefreshContractList.mockRestore();
    mockRefreshEmployeeList.mockRestore();
    mockGetShiftRosterFor.mockRestore();
    mockRefreshRosterState.mockRestore();
  });

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
      onGet(`/tenant/`, mockTenantList);
    
      await store.dispatch(tenantOperations.refreshTenantList());
    
      expect(store.getActions()).toEqual([
        actions.refreshTenantList({currentTenantId: 1, tenantList: mockTenantList}),
        rosterActions.setShiftRosterIsLoading(true)
      ]);

      expect(client.get).toHaveBeenCalledTimes(1);
      expect(client.get).toHaveBeenCalledWith(`/tenant/`);

      expect(mockRefreshSkillList).toBeCalled();
      expect(mockRefreshSpotList).toBeCalled();
      expect(mockRefreshContractList).toBeCalled();
      expect(mockRefreshEmployeeList).toBeCalled();
      expect(mockRefreshRosterState).toBeCalled();
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

      mockTenantList[1].id = 0;

      await store.dispatch(tenantOperations.refreshTenantList());

      expect(store.getActions()).toEqual([
        actions.refreshTenantList({currentTenantId: 0, tenantList: mockTenantList}),
        rosterActions.setShiftRosterIsLoading(true),
      ]);

      expect(client.get).toHaveBeenCalledTimes(1);
      expect(client.get).toHaveBeenCalledWith(`/tenant/`);

      expect(mockRefreshSkillList).toBeCalled();
      expect(mockRefreshSpotList).toBeCalled();
      expect(mockRefreshContractList).toBeCalled();
      expect(mockRefreshEmployeeList).toBeCalled();
      expect(mockRefreshRosterState).toBeCalled();
    });

  it('should change the tenant and refresh all lists on change tenant', async () => {
    const { store, client } = mockStore(state);
    await store.dispatch(tenantOperations.changeTenant(0));
   
    expect(store.getActions()).toEqual([
      actions.changeTenant(0),
      rosterActions.setShiftRosterIsLoading(true)
    ]);

    expect(client.get).not.toBeCalled();

    expect(mockRefreshSkillList).toBeCalled();
    expect(mockRefreshSpotList).toBeCalled();
    expect(mockRefreshContractList).toBeCalled();
    expect(mockRefreshEmployeeList).toBeCalled();
    expect(mockRefreshRosterState).toBeCalled();
  });

  it('should get the Shift Roster for a spot in the spot list if roster state is not null', async () => {
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

    mockTenantList[1].id = 0;
    const date = moment("2018-01-01", "YYYY-MM-DD").toDate();
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
      },
      rosterState: {
        isLoading: false,
        rosterState: {
          publishNotice: 7,
          publishLength: 7,
          draftLength: 2,
          unplannedRotationOffset: 0,
          rotationLength: 1,
          lastHistoricDate: date,
          firstDraftDate: date,
          timeZone: "NA",
          tenant: mockTenantList[1]
        }
      },
      shiftRoster: {
        isLoading: true,
        shiftRosterView: null
      },
      solverState: {
        isSolving: false
      }
    };
    
    const { store, client } = mockStore(state);
    onGet(`/tenant/`, mockTenantList);

    await store.dispatch(tenantOperations.refreshTenantList());

    expect(store.getActions()).toEqual([
      actions.refreshTenantList({currentTenantId: 0, tenantList: mockTenantList}),
      rosterActions.setShiftRosterIsLoading(true),
    ]);

    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/`);

    expect(mockRefreshSkillList).toBeCalled();
    expect(mockRefreshSpotList).toBeCalled();
    expect(mockRefreshContractList).toBeCalled();
    expect(mockRefreshEmployeeList).toBeCalled();
    expect(mockRefreshRosterState).toBeCalled();
    expect(mockGetShiftRosterFor).toBeCalled();
  })
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
  },
  rosterState: {
    isLoading: true,
    rosterState: null
  },
  shiftRoster: {
    isLoading: true,
    shiftRosterView: null
  },
  solverState: {
    isSolving: false
  }
};
