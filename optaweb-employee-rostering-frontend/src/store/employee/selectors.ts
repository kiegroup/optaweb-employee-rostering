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
import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import { Employee } from 'domain/Employee';
import DomainObjectView from 'domain/DomainObjectView';
import { AppState } from '../types';

export const getEmployeeById = (state: AppState, id: number): Employee => {
  if (state.employeeList.isLoading || state.skillList.isLoading || state.contractList.isLoading) {
    throw Error('Employee list is loading');
  }
  const employeeView = state.employeeList.employeeMapById.get(id) as DomainObjectView<Employee>;
  return {
    ...employeeView,
    skillProficiencySet: employeeView.skillProficiencySet.map(skillId => skillSelectors.getSkillById(state, skillId)),
    contract: contractSelectors.getContractById(state, employeeView.contract),
  };
};

let oldEmployeeMapById: Map<number, DomainObjectView<Employee>> | null = null;
let employeeListForOldEmployeeMapById: Employee[] | null = null;

export const getEmployeeList = (state: AppState): Employee[] => {
  if (state.employeeList.isLoading || state.skillList.isLoading || state.contractList.isLoading) {
    return [];
  }
  if (oldEmployeeMapById === state.employeeList.employeeMapById && employeeListForOldEmployeeMapById !== null) {
    return employeeListForOldEmployeeMapById;
  }

  const out: Employee[] = [];
  state.employeeList.employeeMapById.forEach((value, key) => out.push(getEmployeeById(state, key)));

  oldEmployeeMapById = state.employeeList.employeeMapById;
  employeeListForOldEmployeeMapById = out;
  return out;
};
