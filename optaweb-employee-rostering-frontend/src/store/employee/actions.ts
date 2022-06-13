
import { Employee } from 'domain/Employee';
import { ActionFactory } from '../types';
import {
  ActionType, SetEmployeeListLoadingAction, AddEmployeeAction, UpdateEmployeeAction,
  RemoveEmployeeAction, RefreshEmployeeListAction,
} from './types';

export const setIsEmployeeListLoading: ActionFactory<boolean, SetEmployeeListLoadingAction> = isLoading => ({
  type: ActionType.SET_EMPLOYEE_LIST_LOADING,
  isLoading,
});

export const addEmployee: ActionFactory<Employee, AddEmployeeAction> = newEmployee => ({
  type: ActionType.ADD_EMPLOYEE,
  employee: newEmployee,
});

export const removeEmployee: ActionFactory<Employee, RemoveEmployeeAction> = deletedEmployee => ({
  type: ActionType.REMOVE_EMPLOYEE,
  employee: deletedEmployee,
});

export const updateEmployee: ActionFactory<Employee, UpdateEmployeeAction> = updatedEmployee => ({
  type: ActionType.UPDATE_EMPLOYEE,
  employee: updatedEmployee,
});

export const refreshEmployeeList: ActionFactory<Employee[], RefreshEmployeeListAction> = newEmployeeList => ({
  type: ActionType.REFRESH_EMPLOYEE_LIST,
  employeeList: newEmployeeList,
});
