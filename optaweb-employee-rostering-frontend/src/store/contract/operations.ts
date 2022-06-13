
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
