
import { Action } from 'redux';
import { Contract } from 'domain/Contract';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';

export enum ActionType {
  ADD_CONTRACT = 'ADD_CONTRACT',
  REMOVE_CONTRACT = 'REMOVE_CONTRACT',
  UPDATE_CONTRACT = 'UPDATE_CONTRACT',
  REFRESH_CONTRACT_LIST = 'REFRESH_CONTRACT_LIST',
  SET_CONTRACT_LIST_LOADING = 'SET_CONTRACT_LIST_LOADING'
}

export interface SetContractListLoadingAction extends Action<ActionType.SET_CONTRACT_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddContractAction extends Action<ActionType.ADD_CONTRACT> {
  readonly contract: Contract;
}

export interface RemoveContractAction extends Action<ActionType.REMOVE_CONTRACT> {
  readonly contract: Contract;
}

export interface UpdateContractAction extends Action<ActionType.UPDATE_CONTRACT> {
  readonly contract: Contract;
}

export interface RefreshContractListAction extends Action<ActionType.REFRESH_CONTRACT_LIST> {
  readonly contractList: Contract[];
}

export type ContractAction = SetContractListLoadingAction | AddContractAction | RemoveContractAction |
UpdateContractAction | RefreshContractListAction;

export interface ContractList {
  readonly isLoading: boolean;
  readonly contractMapById: Map<number, DomainObjectView<Contract>>;
}
