
import { Action } from 'redux';
import { Employee } from 'domain/Employee';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';

export enum ActionType {
  ADD_EMPLOYEE = 'ADD_EMPLOYEE',
  REMOVE_EMPLOYEE = 'REMOVE_EMPLOYEE',
  UPDATE_EMPLOYEE = 'UPDATE_EMPLOYEE',
  REFRESH_EMPLOYEE_LIST = 'REFRESH_EMPLOYEE_LIST',
  SET_EMPLOYEE_LIST_LOADING = 'SET_EMPLOYEE_LIST_LOADING'
}

export interface SetEmployeeListLoadingAction extends Action<ActionType.SET_EMPLOYEE_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddEmployeeAction extends Action<ActionType.ADD_EMPLOYEE> {
  readonly employee: Employee;
}

export interface RemoveEmployeeAction extends Action<ActionType.REMOVE_EMPLOYEE> {
  readonly employee: Employee;
}

export interface UpdateEmployeeAction extends Action<ActionType.UPDATE_EMPLOYEE> {
  readonly employee: Employee;
}

export interface RefreshEmployeeListAction extends Action<ActionType.REFRESH_EMPLOYEE_LIST> {
  readonly employeeList: Employee[];
}

export type EmployeeAction = SetEmployeeListLoadingAction | AddEmployeeAction | RemoveEmployeeAction |
UpdateEmployeeAction | RefreshEmployeeListAction;

export interface EmployeeList {
  readonly isLoading: boolean;
  readonly employeeMapById: Map<number, DomainObjectView<Employee>>;
}
