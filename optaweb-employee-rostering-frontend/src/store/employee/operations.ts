
import { alert } from 'store/alert';
import { Employee } from 'domain/Employee';
import { AddAlertAction } from 'store/alert/types';
import * as skillActions from 'store/skill/actions';
import * as contractActions from 'store/contract/actions';
import { Skill } from 'domain/Skill';
import { SetSkillListLoadingAction, RefreshSkillListAction } from 'store/skill/types';
import { Contract } from 'domain/Contract';
import { RefreshContractListAction, SetContractListLoadingAction } from 'store/contract/types';
import {
  SetEmployeeListLoadingAction, AddEmployeeAction, RemoveEmployeeAction, UpdateEmployeeAction,
  RefreshEmployeeListAction,
} from './types';
import * as actions from './actions';
import { ThunkCommandFactory } from '../types';

export const addEmployee:
ThunkCommandFactory<Employee, AddAlertAction | AddEmployeeAction> = employee => (dispatch, state, client) => {
  const { tenantId } = employee;
  return client.post<Employee>(`/tenant/${tenantId}/employee/add`, employee).then((newEmployee) => {
    dispatch(alert.showSuccessMessage('addEmployee', { name: newEmployee.name }));
    dispatch(actions.addEmployee(newEmployee));
  });
};

export const removeEmployee:
ThunkCommandFactory<Employee, AddAlertAction | RemoveEmployeeAction> = employee => (dispatch, state, client) => {
  const { tenantId } = employee;
  const employeeId = employee.id;
  return client.delete<boolean>(`/tenant/${tenantId}/employee/${employeeId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeEmployee', { name: employee.name }));
      dispatch(actions.removeEmployee(employee));
    } else {
      dispatch(alert.showErrorMessage('removeEmployeeError', { name: employee.name }));
    }
  });
};

export const updateEmployee:
ThunkCommandFactory<Employee, AddAlertAction | UpdateEmployeeAction> = employee => (dispatch, state, client) => {
  const { tenantId } = employee;
  return client.post<Employee>(`/tenant/${tenantId}/employee/update`, employee).then((updatedEmployee) => {
    dispatch(alert.showSuccessMessage('updateEmployee', { id: updatedEmployee.id }));
    dispatch(actions.updateEmployee(updatedEmployee));
  });
};

export const refreshEmployeeList:
ThunkCommandFactory<void, SetEmployeeListLoadingAction |
RefreshEmployeeListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsEmployeeListLoading(true));
  return client.get<Employee[]>(`/tenant/${tenantId}/employee/`).then((employeeList) => {
    dispatch(actions.refreshEmployeeList(employeeList));
    dispatch(actions.setIsEmployeeListLoading(false));
  });
};

export const uploadEmployeeList:
ThunkCommandFactory<File, AddAlertAction | SetEmployeeListLoadingAction |
RefreshEmployeeListAction | SetSkillListLoadingAction | RefreshSkillListAction |
RefreshContractListAction | SetContractListLoadingAction > = file => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsEmployeeListLoading(true));
  dispatch(skillActions.setIsSkillListLoading(true));
  dispatch(contractActions.setIsContractListLoading(true));
  return client.uploadFile<Employee[]>(`/tenant/${tenantId}/employee/import`, file)
    .then(employeeList => client.get<Skill[]>(`/tenant/${tenantId}/skill/`)
      .then(skillList => client.get<Contract[]>(`/tenant/${tenantId}/contract/`).then((contractList) => {
        dispatch(alert.showSuccessMessage('importSuccessful'));
        dispatch(skillActions.refreshSkillList(skillList));
        dispatch(skillActions.setIsSkillListLoading(false));
        dispatch(contractActions.refreshContractList(contractList));
        dispatch(contractActions.setIsContractListLoading(false));
        dispatch(actions.refreshEmployeeList(employeeList));
        dispatch(actions.setIsEmployeeListLoading(false));
      })));
};
