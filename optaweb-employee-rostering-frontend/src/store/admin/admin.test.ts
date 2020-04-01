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

import * as tenantOperations from 'store/tenant/operations';
import { onPost } from 'store/rest/RestTestUtils';
import { alert } from 'store/alert';
import { doNothing } from 'types';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as adminOperations from './operations';

describe('Contract operations', () => {
  const mockRefreshTenantList = jest.spyOn(tenantOperations, 'refreshTenantList');

  beforeAll(() => {
    mockRefreshTenantList.mockImplementation(() => doNothing);
  });

  afterAll(() => {
    mockRefreshTenantList.mockRestore();
  });

  it('should dispatch actions and call client on reset application', async () => {
    const { store, client } = mockStore(state);

    onPost('/admin/reset', {}, {});
    await store.dispatch(adminOperations.resetApplication());
    expect(store.getActions()).toEqual([
      alert.showInfoMessage('resetApplicationSuccessful'),
    ]);

    expect(mockRefreshTenantList).toBeCalledTimes(1);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith('/admin/reset', {});
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: [],
    timezoneList: ['America/Toronto'],
  },
  employeeList: {
    isLoading: false,
    employeeMapById: new Map(),
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map(),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map(),
  },
  shiftTemplateList: {
    isLoading: false,
    shiftTemplateMapById: new Map(),
  },
  rosterState: {
    isLoading: true,
    rosterState: null,
  },
  shiftRoster: {
    isLoading: true,
    shiftRosterView: null,
  },
  availabilityRoster: {
    isLoading: true,
    availabilityRosterView: null,
  },
  solverState: {
    solverStatus: 'TERMINATED',
  },
  alerts: {
    alertList: [],
    idGeneratorIndex: 0,
  },
};
