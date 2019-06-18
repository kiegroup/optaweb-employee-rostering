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
import reducer, { contractOperations } from './index';
import { withElement, withoutElement, withUpdatedElement } from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete, resetRestClientMock } from 'store/rest/RestTestUtils';
import Contract from 'domain/Contract';

describe('Contract operations', () => {
  it('should dispatch actions and call client', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockContractList: Contract[] = [{
      tenantId: tenantId,
      id: 0,
      version: 0,
      name: "Contract 1",
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    }];

    onGet(`/tenant/${tenantId}/contract/`, mockContractList);
    await store.dispatch(contractOperations.refreshContractList());
    expect(store.getActions()).toEqual([actions.refreshContractList(mockContractList)]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/`);

    store.clearActions();
    resetRestClientMock(client);

    const contractToDelete = mockContractList[0];
    onDelete(`/tenant/${tenantId}/contract/${contractToDelete.id}`, true);
    await store.dispatch(contractOperations.removeContract(contractToDelete));
    expect(store.getActions()).toEqual([actions.removeContract(contractToDelete)]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/${contractToDelete.id}`);

    store.clearActions();
    resetRestClientMock(client);

    const contractToAdd: Contract = {tenantId: tenantId,
      name: "Contract 1",
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    };
    const contractWithUpdatedId: Contract = {...contractToAdd, id: 4, version: 0};
    onPost(`/tenant/${tenantId}/contract/add`, contractToAdd, contractWithUpdatedId);
    await store.dispatch(contractOperations.addContract(contractToAdd));
    expect(store.getActions()).toEqual([actions.addContract(contractWithUpdatedId)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/add`, contractToAdd);

    store.clearActions();
    resetRestClientMock(client);

    const contractToUpdate: Contract = {tenantId: tenantId,
      name: "Contract 1",
      id: 4,
      version: 0,
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    };
    const contractWithUpdatedVersion: Contract = {...contractToUpdate, id: 4, version: 1};
    onPost(`/tenant/${tenantId}/contract/update`, contractToUpdate, contractWithUpdatedVersion);
    await store.dispatch(contractOperations.updateContract(contractToUpdate));
    expect(store.getActions()).toEqual([actions.updateContract(contractWithUpdatedVersion)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/update`, contractToUpdate);
  });
});

describe('Contract reducers', () => {
  const addedContract: Contract = {
    tenantId: 0,
    id: 4,
    version: 0,
    name: "Contract 4",
    maximumMinutesPerDay: null,
    maximumMinutesPerWeek: 1,
    maximumMinutesPerMonth: 2,
    maximumMinutesPerYear: 3
  };
  const updatedContract: Contract = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: "Updated Contract 2",
    maximumMinutesPerDay: 1,
    maximumMinutesPerWeek: 2,
    maximumMinutesPerMonth: 3,
    maximumMinutesPerYear: 4
  };
  const deletedContract: Contract = {
    tenantId: 0,
    id: 2,
    version: 0,
    name: "Contract 3",
    maximumMinutesPerDay: 100,
    maximumMinutesPerWeek: null,
    maximumMinutesPerMonth: null,
    maximumMinutesPerYear: 100
  };
  it('add contract', () => {
    expect(
      reducer(state.contractList, actions.addContract(addedContract))
    ).toEqual({contractList: withElement(state.contractList.contractList, addedContract)})
  });
  it('remove contract', () => {
    expect(
      reducer(state.contractList, actions.removeContract(deletedContract)),
    ).toEqual({contractList: withoutElement(state.contractList.contractList, deletedContract)})
  });
  it('update contract', () => {
    expect(
      reducer(state.contractList, actions.updateContract(updatedContract)),
    ).toEqual({contractList: withUpdatedElement(state.contractList.contractList, updatedContract)})
  });
  it('refresh contract list', () => {
    expect(
      reducer(state.contractList, actions.refreshContractList([addedContract])),
    ).toEqual({contractList: [addedContract]});
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: []
  },
  spotList: {
    spotList: []
  },
  contractList: {
    contractList: [
      {
        tenantId: 0,
        id: 0,
        version: 0,
        name: "Contract 1",
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null
      },
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: "Contract 2",
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: 100,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: "Contract 3",
        maximumMinutesPerDay: 100,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: 100
      }
    ]
  },
  skillList: {
    skillList: [
      {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: "Skill 2"
      },
      {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: "Skill 3"
      }
    ]
  }
};
