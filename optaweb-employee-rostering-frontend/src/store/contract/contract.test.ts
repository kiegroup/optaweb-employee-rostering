
import { alert } from 'store/alert';
import { createIdMapFromList } from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete } from 'store/rest/RestTestUtils';
import { Contract } from 'domain/Contract';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { contractSelectors, contractOperations } from './index';

const state: Partial<AppState> = {
  contractList: {
    isLoading: false,
    contractMapById: createIdMapFromList([
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
    ]),
  },
};

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
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('set loading', () => {
    expect(
      reducer(state.contractList, actions.setIsContractListLoading(true)),
    ).toEqual({ ...state.contractList, isLoading: true });
  });
  it('add contract', () => {
    expect(
      reducer(state.contractList, actions.addContract(addedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: storeState.contractList.contractMapById.set(addedContract.id as number, addedContract) });
  });
  it('remove contract', () => {
    expect(
      reducer(state.contractList, actions.removeContract(deletedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: storeState.contractList.contractMapById.delete(deletedContract.id as number) });
  });
  it('update contract', () => {
    expect(
      reducer(state.contractList, actions.updateContract(updatedContract)),
    ).toEqual({ ...state.contractList,
      contractMapById: storeState.contractList.contractMapById.set(updatedContract.id as number, updatedContract) });
  });
  it('refresh contract list', () => {
    expect(
      reducer(state.contractList, actions.refreshContractList([addedContract])),
    ).toEqual({ ...state.contractList,
      contractMapById: createIdMapFromList([addedContract]) });
  });
});

describe('Contract selectors', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('should throw an error if contract list is loading', () => {
    expect(() => contractSelectors.getContractById({
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    }, 0)).toThrow();
  });

  it('should get a contract by id', () => {
    const contract = contractSelectors.getContractById(storeState, 0);
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
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    });
    expect(contractList).toEqual([]);
  });

  it('should return a list of all contracts', () => {
    const contractList = contractSelectors.getContractList(storeState);
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
