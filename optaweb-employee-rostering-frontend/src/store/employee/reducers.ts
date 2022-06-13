
import { createIdMapFromList, mapDomainObjectToView } from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { Employee } from 'domain/Employee';
import { Map } from 'immutable';
import { ActionType, EmployeeList, EmployeeAction } from './types';

export const initialState: EmployeeList = {
  isLoading: true,
  employeeMapById: Map<number, DomainObjectView<Employee>>(),
};

const employeeReducer = (state = initialState, action: EmployeeAction): EmployeeList => {
  switch (action.type) {
    case ActionType.SET_EMPLOYEE_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_EMPLOYEE:
    case ActionType.UPDATE_EMPLOYEE: {
      return { ...state,
        employeeMapById: state.employeeMapById.set(action.employee.id as number,
          mapDomainObjectToView(action.employee)) };
    }
    case ActionType.REMOVE_EMPLOYEE: {
      return { ...state, employeeMapById: state.employeeMapById.remove(action.employee.id as number) };
    }
    case ActionType.REFRESH_EMPLOYEE_LIST: {
      return { ...state, employeeMapById: createIdMapFromList(action.employeeList) };
    }
    default:
      return state;
  }
};

export default employeeReducer;
