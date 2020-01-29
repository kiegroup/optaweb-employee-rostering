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

import { Action } from 'redux';
import { Contract } from 'domain/Contract';
import DomainObjectView from 'domain/DomainObjectView';

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
