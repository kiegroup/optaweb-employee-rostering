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

import {
  createIdMapFromList, mapWithElement, mapWithoutElement,
  mapWithUpdatedElement,
} from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { Employee } from 'domain/Employee';
import { ActionType, EmployeeList, EmployeeAction } from './types';

export const initialState: EmployeeList = {
  isLoading: true,
  employeeMapById: new Map<number, DomainObjectView<Employee>>(),
};

const employeeReducer = (state = initialState, action: EmployeeAction): EmployeeList => {
  switch (action.type) {
    case ActionType.SET_EMPLOYEE_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_EMPLOYEE: {
      return { ...state, employeeMapById: mapWithElement(state.employeeMapById, action.employee) };
    }
    case ActionType.REMOVE_EMPLOYEE: {
      return { ...state, employeeMapById: mapWithoutElement(state.employeeMapById, action.employee) };
    }
    case ActionType.UPDATE_EMPLOYEE: {
      return { ...state, employeeMapById: mapWithUpdatedElement(state.employeeMapById, action.employee) };
    }
    case ActionType.REFRESH_EMPLOYEE_LIST: {
      return { ...state, employeeMapById: createIdMapFromList(action.employeeList) };
    }
    default:
      return state;
  }
};

export default employeeReducer;
