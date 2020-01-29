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
