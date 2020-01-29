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
import { Employee } from 'domain/Employee';
import DomainObjectView from 'domain/DomainObjectView';

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
