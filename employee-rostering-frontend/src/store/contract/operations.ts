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

import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import Contract from 'domain/Contract';
import { AddContractAction, RemoveContractAction, UpdateContractAction, RefreshContractListAction } from './types';

export const addContract: ThunkCommandFactory<Contract, AddContractAction> = contract =>
  (dispatch, state, client) => {
    let tenantId = contract.tenantId;
    return client.post<Contract>(`/tenant/${tenantId}/contract/add`, contract).then(newContract => {
      dispatch(actions.addContract(newContract))
    });
  };

export const removeContract: ThunkCommandFactory<Contract, RemoveContractAction> = contract =>
  (dispatch, state, client) => {
    let tenantId = contract.tenantId;
    let contractId = contract.id;
    return client.delete<boolean>(`/tenant/${tenantId}/contract/${contractId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeContract(contract));
      }
    });
  };

export const updateContract: ThunkCommandFactory<Contract, UpdateContractAction> = contract =>
  (dispatch, state, client) => {
    let tenantId = contract.tenantId;
    return client.post<Contract>(`/tenant/${tenantId}/contract/update`, contract).then(updatedContract => {
      dispatch(actions.updateContract(updatedContract));
    });
  };

export const refreshContractList: ThunkCommandFactory<void, RefreshContractListAction> = () =>
  (dispatch, state, client) => {
    let tenantId = state().tenantData.currentTenantId;
    return client.get<Contract[]>(`/tenant/${tenantId}/contract/`).then(contractList => {
      dispatch(actions.refreshContractList(contractList));
    });
  };
