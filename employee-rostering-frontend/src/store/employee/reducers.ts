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

import { ActionType, EmployeeList, EmployeeAction } from './types';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';

export const initialState: EmployeeList = {
  employeeList: []
};

const employeeReducer = (state = initialState, action: EmployeeAction): EmployeeList => {
  switch (action.type) {
    case ActionType.ADD_EMPLOYEE: {
      return { ...initialState, employeeList: withElement(state.employeeList, action.employee) };
    }
    case ActionType.REMOVE_EMPLOYEE: {
      return { ...initialState, employeeList: withoutElement(state.employeeList, action.employee) };
    }
    case ActionType.UPDATE_EMPLOYEE: {
      return { ...initialState, employeeList: withUpdatedElement(state.employeeList, action.employee) };
    }
    case ActionType.REFRESH_EMPLOYEE_LIST: {
      return { ...initialState, employeeList: action.employeeList };
    }
    default:
      return state;
  }
};

export default employeeReducer;
