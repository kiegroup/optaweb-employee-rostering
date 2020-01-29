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
import { Contract } from 'domain/Contract';
import { AddAlertAction } from 'store/alert/types';
import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import {
  SetContractListLoadingAction, AddContractAction, RemoveContractAction,
  UpdateContractAction, RefreshContractListAction,
} from './types';


export const addContract:
ThunkCommandFactory<Contract, AddAlertAction | AddContractAction> = contract => (dispatch, state, client) => {
  const { tenantId } = contract;
  return client.post<Contract>(`/tenant/${tenantId}/contract/add`, contract).then((newContract) => {
    dispatch(alert.showSuccessMessage('addContract', { name: newContract.name }));
    dispatch(actions.addContract(newContract));
  });
};

export const removeContract:
ThunkCommandFactory<Contract, AddAlertAction | RemoveContractAction> = contract => (dispatch, state, client) => {
  const { tenantId } = contract;
  const contractId = contract.id;
  return client.delete<boolean>(`/tenant/${tenantId}/contract/${contractId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeContract', { name: contract.name }));
      dispatch(actions.removeContract(contract));
    } else {
      dispatch(alert.showErrorMessage('removeContractError', { name: contract.name }));
    }
  });
};

export const updateContract:
ThunkCommandFactory<Contract, AddAlertAction | UpdateContractAction> = contract => (dispatch, state, client) => {
  const { tenantId } = contract;
  return client.post<Contract>(`/tenant/${tenantId}/contract/update`, contract).then((updatedContract) => {
    dispatch(alert.showSuccessMessage('updateContract', { id: contract.id }));
    dispatch(actions.updateContract(updatedContract));
  });
};

export const refreshContractList:
ThunkCommandFactory<void, RefreshContractListAction |
SetContractListLoadingAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsContractListLoading(true));
  return client.get<Contract[]>(`/tenant/${tenantId}/contract/`).then((contractList) => {
    dispatch(actions.refreshContractList(contractList));
    dispatch(actions.setIsContractListLoading(false));
  });
};
