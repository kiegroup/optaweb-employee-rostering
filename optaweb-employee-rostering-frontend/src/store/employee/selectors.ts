import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import { Employee } from 'domain/Employee';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';
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

  const out = state.employeeList.employeeMapById.keySeq().map(id => getEmployeeById(state, id))
    .sortBy(employee => employee.name).toList();

  oldEmployeeMapById = state.employeeList.employeeMapById;
  employeeListForOldEmployeeMapById = out.toArray();
  return employeeListForOldEmployeeMapById;
};
