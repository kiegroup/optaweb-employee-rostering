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
import { Middleware } from 'redux';
import * as Redux from 'react-redux';
import createMockStore, { MockStoreCreator } from 'redux-mock-store';
import thunk, { ThunkDispatch } from 'redux-thunk';
import { resetRestClientMock } from 'store/rest/RestTestUtils';
import { Map, List } from 'immutable';
import RestServiceClient from './rest/RestServiceClient';
import { TenantAction } from './tenant/types';
import { SkillAction } from './skill/types';
import { AppState } from './types';

jest.mock('./rest/RestServiceClient');

export const mockStore = (state: Partial<AppState>) => {
  const client: jest.Mocked<RestServiceClient> = new RestServiceClient('', {} as any) as any;
  resetRestClientMock(client);
  jest.clearAllMocks();

  const middlewares: Middleware[] = [thunk.withExtraArgument(client)];
  type DispatchExts = ThunkDispatch<AppState, RestServiceClient, SkillAction | TenantAction>;
  const mockStoreCreator: MockStoreCreator<AppState, DispatchExts> = createMockStore<AppState, DispatchExts>(
    middlewares,
  );
  const out = { store: mockStoreCreator({
    tenantData: {
      currentTenantId: 0,
      tenantList: List(),
      timezoneList: ['America/Toronto'],
    },
    employeeList: {
      isLoading: true,
      employeeMapById: Map(),
    },
    contractList: {
      isLoading: true,
      contractMapById: Map(),
    },
    spotList: {
      isLoading: true,
      spotMapById: Map(),
    },
    skillList: {
      isLoading: true,
      skillMapById: Map(),
    },
    timeBucketList: {
      isLoading: true,
      timeBucketMapById: Map(),
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
      solverStatus: 'NOT_SOLVING',
    },
    alerts: {
      alertList: List(),
      idGeneratorIndex: 0,
    },
    isConnected: true,
    ...state,
  }),
  client };

  jest
    .spyOn(Redux, 'useSelector')
    .mockImplementation((selector: Function) => selector(out.store.getState()));

  return out;
};
