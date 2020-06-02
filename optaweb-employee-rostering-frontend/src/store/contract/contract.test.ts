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

import { alert } from 'store/alert';
import {
  createIdMapFromList, mapWithElement, mapWithoutElement,
  mapWithUpdatedElement,
} from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete } from 'store/rest/RestTestUtils';
import { Contract } from 'domain/Contract';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { contractSelectors, contractOperations } from './index';

describe('Contract operations', () => {
  it('should dispatch actions and call client on refresh contract list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockContractList: Contract[] = [{
      tenantId,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    }];

    onGet(`/tenant/${tenantId}/contract/`, mockContractList);
    await store.dispatch(contractOperations.refreshContractList());
    expect(store.getActions()).toEqual([actions.setIsContractListLoading(true),
      actions.refreshContractList(mockContractList), actions.setIsContractListLoading(false)]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/`);
  });

  it('should dispatch actions and call client on a successful delete contract', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const contractToDelete = {
      tenantId,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    };

    onDelete(`/tenant/${tenantId}/contract/${contractToDelete.id}`, true);
    await store.dispatch(contractOperations.removeContract(contractToDelete));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeContract', {
        name: contractToDelete.name,
      }),
      actions.removeContract(contractToDelete)]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/${contractToDelete.id}`);
  });

  it('should call client but not dispatch actions on a failed delete contract', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const contractToDelete = {
      tenantId,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    };

    onDelete(`/tenant/${tenantId}/contract/${contractToDelete.id}`, false);
    await store.dispatch(contractOperations.removeContract(contractToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeContractError', { name: contractToDelete.name }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/${contractToDelete.id}`);
  });

  it('should dispatch actions and call client on add contract', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const contractToAdd: Contract = { tenantId,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null };
    const contractWithUpdatedId: Contract = { ...contractToAdd, id: 4, version: 0 };
    onPost(`/tenant/${tenantId}/contract/add`, contractToAdd, contractWithUpdatedId);
    await store.dispatch(contractOperations.addContract(contractToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addContract', { name: contractToAdd.name }),
      actions.addContract(contractWithUpdatedId)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/add`, contractToAdd);
  });

  it('should dispatch actions and call client on update contract', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const contractToUpdate: Contract = { tenantId,
      name: 'Contract 1',
      id: 4,
      version: 0,
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null };

    const contractWithUpdatedVersion: Contract = { ...contractToUpdate, id: 4, version: 1 };
    onPost(`/tenant/${tenantId}/contract/update`, contractToUpdate, contractWithUpdatedVersion);
    await store.dispatch(contractOperations.updateContract(contractToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('updateContract', { id: contractToUpdate.id }),
      actions.updateContract(contractWithUpdatedVersion)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/contract/update`, contractToUpdate);
  });
});

describe('Contract reducers', () => {
  const addedContract: Contract = {
    tenantId: 0,
    id: 4,
    version: 0,
    name: 'Contract 4',
    maximumMinutesPerDay: null,
    maximumMinutesPerWeek: 1,
    maximumMinutesPerMonth: 2,
    maximumMinutesPerYear: 3,
  };
  const updatedContract: Contract = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: 'Updated Contract 2',
    maximumMinutesPerDay: 1,
    maximumMinutesPerWeek: 2,
    maximumMinutesPerMonth: 3,
    maximumMinutesPerYear: 4,
  };
  const deletedContract: Contract = {
    tenantId: 0,
    id: 2,
    version: 0,
    name: 'Contract 3',
    maximumMinutesPerDay: 100,
    maximumMinutesPerWeek: null,
    maximumMinutesPerMonth: null,
    maximumMinutesPerYear: 100,
  };
  it('set loading', () => {
    expect(
      reducer(state.contractList, actions.setIsContractListLoading(true)),
    ).toEqual({ ...state.contractList, isLoading: true });
  });
  it('add contract', () => {
    expect(
      reducer(state.contractList, actions.addContract(addedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: mapWithElement(state.contractList.contractMapById, addedContract) });
  });
  it('remove contract', () => {
    expect(
      reducer(state.contractList, actions.removeContract(deletedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: mapWithoutElement(state.contractList.contractMapById, deletedContract) });
  });
  it('update contract', () => {
    expect(
      reducer(state.contractList, actions.updateContract(updatedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: mapWithUpdatedElement(state.contractList.contractMapById, updatedContract) });
  });
  it('refresh contract list', () => {
    expect(
      reducer(state.contractList, actions.refreshContractList([addedContract])),
    ).toEqual({ ...state.contractList,
      contractMapById: createIdMapFromList([addedContract]) });
  });
});

describe('Contract selectors', () => {
  it('should throw an error if contract list is loading', () => {
    expect(() => contractSelectors.getContractById({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    }, 0)).toThrow();
  });

  it('should get a contract by id', () => {
    const contract = contractSelectors.getContractById(state, 0);
    expect(contract).toEqual({
      tenantId: 0,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    });
  });

  it('should return an empty list if contract list is loading', () => {
    const contractList = contractSelectors.getContractList({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    });
    expect(contractList).toEqual([]);
  });

  it('should return a list of all contracts', () => {
    const contractList = contractSelectors.getContractList(state);
    expect(contractList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 0,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 2',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: 100,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Contract 3',
        maximumMinutesPerDay: 100,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: 100,
      },
    ]));
    expect(contractList.length).toEqual(3);
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
    contractMapById: new Map([
      [0, {
        tenantId: 0,
        id: 0,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      }],
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 2',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: 100,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      }],
      [2, {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Contract 3',
        maximumMinutesPerDay: 100,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: 100,
      }],
    ]),
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
