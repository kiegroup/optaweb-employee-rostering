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
import createMockStore, { MockStoreCreator } from 'redux-mock-store';
import thunk, { ThunkDispatch } from 'redux-thunk';
import { resetRestClientMock } from 'store/rest/RestTestUtils';
import RestServiceClient from './rest/RestServiceClient';
import { TenantAction } from './tenant/types';
import { SkillAction } from './skill/types';
import { AppState } from './types';

jest.mock('./rest/RestServiceClient');

export const mockStore = (state: AppState) => {
  const client: jest.Mocked<RestServiceClient> = new RestServiceClient('', {} as any) as any;
  resetRestClientMock(client);
  jest.clearAllMocks();

  const middlewares: Middleware[] = [thunk.withExtraArgument(client)];
  type DispatchExts = ThunkDispatch<AppState, RestServiceClient, SkillAction | TenantAction>;
  const mockStoreCreator: MockStoreCreator<AppState, DispatchExts> = createMockStore<AppState, DispatchExts>(
    middlewares,
  );
  return { store: mockStoreCreator(state), client };
};
