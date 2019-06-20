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

import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { employeeOperations } from './index';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';
import {onGet, onPost, onDelete, resetRestClientMock} from 'store/rest/RestTestUtils';
import Employee from 'domain/Employee';

describe('Employee operations', () => {
  it('should dispatch actions and call client', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    
    const mockEmployee: Employee = {
      tenantId: tenantId,
      id: 0,
      version: 0,
      name: "Employee 1",
      skillProficiencySet: [],
      contract: {
        tenantId: tenantId,
        id: 1,
        name: "Contract",
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null
      }
    };

    const mockEmployeeList: Employee[] = [mockEmployee];

    onGet(`/tenant/${tenantId}/employee/`, mockEmployeeList);
    await store.dispatch(employeeOperations.refreshEmployeeList());
    expect(store.getActions()).toEqual([actions.refreshEmployeeList(mockEmployeeList)]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/`);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToDelete = mockEmployee;
    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, true);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([actions.removeEmployee(employeeToDelete)]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);

    store.clearActions();
    resetRestClientMock(client);

    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, false);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToAdd: Employee = {...mockEmployee, id: undefined, version: undefined};
    const employeeWithUpdatedId: Employee = {...employeeToAdd, id: 4, version: 0};
    onPost(`/tenant/${tenantId}/employee/add`, employeeToAdd, employeeWithUpdatedId);
    await store.dispatch(employeeOperations.addEmployee(employeeToAdd));
    expect(store.getActions()).toEqual([actions.addEmployee(employeeWithUpdatedId)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/add`, employeeToAdd);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToUpdate: Employee = mockEmployee;
    const employeeWithUpdatedVersion: Employee = {...mockEmployee, version: 1};
    onPost(`/tenant/${tenantId}/employee/update`, employeeToUpdate, employeeWithUpdatedVersion);
    await store.dispatch(employeeOperations.updateEmployee(employeeToUpdate));
    expect(store.getActions()).toEqual([actions.updateEmployee(employeeWithUpdatedVersion)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/update`, employeeToUpdate);
  });
});

describe('Employee reducers', () => {
  const addedEmployee: Employee = {
    tenantId: 0,
    id: 4,
    version: 0,
    name: "Employee 1",
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 2,
      name: "Contract",
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    }
  };
  const updatedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: "Updated Employee 1",
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: "Contract",
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    }
  };
  const deletedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: "Employee 1",
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: "Contract",
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null
    }
  };
  it('add employee', () => {
    expect(
      reducer(state.employeeList, actions.addEmployee(addedEmployee))
    ).toEqual({employeeList: withElement(state.employeeList.employeeList, addedEmployee)})
  });
  it('remove employee', () => {
    expect(
      reducer(state.employeeList, actions.removeEmployee(deletedEmployee)),
    ).toEqual({employeeList: withoutElement(state.employeeList.employeeList, deletedEmployee)})
  });
  it('update employee', () => {
    expect(
      reducer(state.employeeList, actions.updateEmployee(updatedEmployee)),
    ).toEqual({employeeList: withUpdatedElement(state.employeeList.employeeList, updatedEmployee)})
  });
  it('refresh employee list', () => {
    expect(
      reducer(state.employeeList, actions.refreshEmployeeList([addedEmployee])),
    ).toEqual({employeeList: [addedEmployee]});
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: []
  },
  employeeList: {
    employeeList: [
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: "Employee 1",
        skillProficiencySet: [],
        contract: {
          tenantId: 0,
          id: 1,
          name: "Contract",
          maximumMinutesPerDay: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: null,
          maximumMinutesPerYear: null
        }
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: "Employee 2",
        skillProficiencySet: [],
        contract: {
          tenantId: 0,
          id: 1,
          name: "Contract",
          maximumMinutesPerDay: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: null,
          maximumMinutesPerYear: null
        }
      }
    ]
  },
  spotList: {
    spotList: []
  },
  contractList: {
    contractList: []
  },
  skillList: {
    skillList: []
  }
};
