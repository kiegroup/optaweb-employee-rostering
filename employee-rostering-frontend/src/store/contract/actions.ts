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
