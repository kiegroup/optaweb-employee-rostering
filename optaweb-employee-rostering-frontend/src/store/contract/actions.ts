
import { Contract } from 'domain/Contract';
import { ActionFactory } from '../types';
import {
  ActionType, SetContractListLoadingAction, AddContractAction, UpdateContractAction,
  RemoveContractAction, RefreshContractListAction,
} from './types';

export const setIsContractListLoading: ActionFactory<boolean, SetContractListLoadingAction> = isLoading => ({
  type: ActionType.SET_CONTRACT_LIST_LOADING,
  isLoading,
});

export const addContract: ActionFactory<Contract, AddContractAction> = newContract => ({
  type: ActionType.ADD_CONTRACT,
  contract: newContract,
});

export const removeContract: ActionFactory<Contract, RemoveContractAction> = deletedContract => ({
  type: ActionType.REMOVE_CONTRACT,
  contract: deletedContract,
});

export const updateContract: ActionFactory<Contract, UpdateContractAction> = updatedContract => ({
  type: ActionType.UPDATE_CONTRACT,
  contract: updatedContract,
});

export const refreshContractList: ActionFactory<Contract[], RefreshContractListAction> = newContractList => ({
  type: ActionType.REFRESH_CONTRACT_LIST,
  contractList: newContractList,
});
