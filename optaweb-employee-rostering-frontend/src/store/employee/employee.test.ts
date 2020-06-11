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

import { alert } from 'store/alert';
import {
  createIdMapFromList, mapWithElement, mapWithoutElement,
  mapWithUpdatedElement,
} from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete, resetRestClientMock, onUploadFile } from 'store/rest/RestTestUtils';
import { Employee } from 'domain/Employee';
import * as skillActions from 'store/skill/actions';
import * as contractActions from 'store/contract/actions';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { employeeSelectors, employeeOperations } from './index';

describe('Employee operations', () => {
  // TODO: Separate this test into separate tests
  it('should dispatch actions and call client', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const mockEmployee: Employee = {
      tenantId,
      id: 0,
      version: 0,
      name: 'Employee 1',
      skillProficiencySet: [],
      contract: {
        tenantId,
        id: 1,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    };

    const mockEmployeeList: Employee[] = [mockEmployee];

    onGet(`/tenant/${tenantId}/employee/`, mockEmployeeList);
    await store.dispatch(employeeOperations.refreshEmployeeList());
    expect(store.getActions()).toEqual([
      actions.setIsEmployeeListLoading(true),
      actions.refreshEmployeeList(mockEmployeeList),
      actions.setIsEmployeeListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/`);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToDelete = mockEmployee;
    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, true);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeEmployee', { name: employeeToDelete.name }),
      actions.removeEmployee(employeeToDelete),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);

    store.clearActions();
    resetRestClientMock(client);

    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, false);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeEmployeeError', { name: employeeToDelete.name }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToAdd: Employee = { ...mockEmployee, id: undefined, version: undefined };
    const employeeWithUpdatedId: Employee = { ...employeeToAdd, id: 4, version: 0 };
    onPost(`/tenant/${tenantId}/employee/add`, employeeToAdd, employeeWithUpdatedId);
    await store.dispatch(employeeOperations.addEmployee(employeeToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addEmployee', { name: employeeToAdd.name }),
      actions.addEmployee(employeeWithUpdatedId),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/add`, employeeToAdd);

    store.clearActions();
    resetRestClientMock(client);

    const employeeToUpdate: Employee = mockEmployee;
    const employeeWithUpdatedVersion: Employee = { ...mockEmployee, version: 1 };
    onPost(`/tenant/${tenantId}/employee/update`, employeeToUpdate, employeeWithUpdatedVersion);
    await store.dispatch(employeeOperations.updateEmployee(employeeToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('updateEmployee', { id: employeeToUpdate.id }),
      actions.updateEmployee(employeeWithUpdatedVersion),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/update`, employeeToUpdate);

    store.clearActions();
    resetRestClientMock(client);

    const fileMock = 'myFile' as unknown as File;
    onUploadFile(`/tenant/${tenantId}/employee/import`, fileMock, mockEmployeeList);
    onGet(`/tenant/${tenantId}/skill/`, []);
    onGet(`/tenant/${tenantId}/contract/`, []);

    await store.dispatch(employeeOperations.uploadEmployeeList(fileMock));
    expect(store.getActions()).toEqual([
      actions.setIsEmployeeListLoading(true),
      skillActions.setIsSkillListLoading(true),
      contractActions.setIsContractListLoading(true),
      alert.showSuccessMessage('importSuccessful'),
      skillActions.refreshSkillList([]),
      skillActions.setIsSkillListLoading(false),
      contractActions.refreshContractList([]),
      contractActions.setIsContractListLoading(false),
      actions.refreshEmployeeList(mockEmployeeList),
      actions.setIsEmployeeListLoading(false),

    ]);
    expect(client.uploadFile).toHaveBeenCalledTimes(1);
    expect(client.uploadFile).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/import`, fileMock);
    expect(client.get).toBeCalledTimes(2);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/skill/`);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/contract/`);
  });
});

describe('Employee reducers', () => {
  const addedEmployee: Employee = {
    tenantId: 0,
    id: 4,
    version: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 2,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    covidRiskType: 'INOCULATED',
  };
  const updatedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: 'Updated Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    covidRiskType: 'INOCULATED',
  };
  const deletedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    covidRiskType: 'INOCULATED',
  };
  it('set is loading', () => {
    expect(
      reducer(state.employeeList, actions.setIsEmployeeListLoading(true)),
    ).toEqual({ ...state.employeeList,
      isLoading: true });
  });
  it('add employee', () => {
    expect(
      reducer(state.employeeList, actions.addEmployee(addedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: mapWithElement(state.employeeList.employeeMapById, addedEmployee) });
  });
  it('remove employee', () => {
    expect(
      reducer(state.employeeList, actions.removeEmployee(deletedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: mapWithoutElement(state.employeeList.employeeMapById, deletedEmployee) });
  });
  it('update employee', () => {
    expect(
      reducer(state.employeeList, actions.updateEmployee(updatedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: mapWithUpdatedElement(state.employeeList.employeeMapById, updatedEmployee) });
  });
  it('refresh employee list', () => {
    expect(
      reducer(state.employeeList, actions.refreshEmployeeList([addedEmployee])),
    ).toEqual({ ...state.employeeList,
      employeeMapById: createIdMapFromList([addedEmployee]) });
  });
});

describe('Employee selectors', () => {
  it('should throw an error if employee list, contract list or spot list is loading', () => {
    expect(() => employeeSelectors.getEmployeeById({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    }, 1234)).toThrow();
    expect(() => employeeSelectors.getEmployeeById({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    }, 1234)).toThrow();
    expect(() => employeeSelectors.getEmployeeById({
      ...state,
      spotList: { ...state.spotList, isLoading: true },
    }, 1234)).toThrow();
  });

  it('should get a employee by id', () => {
    const employee = employeeSelectors.getEmployeeById(state, 1);
    expect(employee).toEqual({
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Employee 1',
      skillProficiencySet: [
        {
          tenantId: 0,
          id: 3,
          version: 0,
          name: 'Skill 3',
        },
      ],
      contract: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 50,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: 10,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    });
  });

  it('should return an empty list if employee list, skill list or contract list is loading', () => {
    let employeeList = employeeSelectors.getEmployeeList({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
    employeeList = employeeSelectors.getEmployeeList({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
    employeeList = employeeSelectors.getEmployeeList({
      ...state,
      employeeList: { ...state.employeeList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
  });

  it('should return a list of all employee', () => {
    const employeeList = employeeSelectors.getEmployeeList(state);
    expect(employeeList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Employee 1',
        skillProficiencySet: [
          {
            tenantId: 0,
            id: 3,
            version: 0,
            name: 'Skill 3',
          },
        ],
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract 1',
          maximumMinutesPerDay: 50,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 10,
          maximumMinutesPerYear: null,
        },
        covidRiskType: 'INOCULATED',
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Employee 2',
        skillProficiencySet: [],
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract 1',
          maximumMinutesPerDay: 50,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 10,
          maximumMinutesPerYear: null,
        },
        covidRiskType: 'INOCULATED',
      },
    ]));
    expect(employeeList.length).toEqual(2);
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: [],
    timezoneList: ['America/Toronto'],
  },
  employeeList: {
    isLoading: false,
    employeeMapById: new Map([
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Employee 1',
        skillProficiencySet: [3],
        contract: 1,
        covidRiskType: 'INOCULATED',
      }],
      [2, {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Employee 2',
        skillProficiencySet: [],
        contract: 1,
        covidRiskType: 'INOCULATED',
      }],
    ]),
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map([
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 50,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: 10,
        maximumMinutesPerYear: null,
      }],
    ]),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map([
      [3, {
        tenantId: 0,
        id: 3,
        version: 0,
        name: 'Skill 3',
      }],
    ]),
  },
  shiftTemplateList: {
    isLoading: false,
    shiftTemplateMapById: new Map(),
  },
  rosterState: {
    isLoading: true,
    rosterState: null,
  },
  shiftRoster: {
    isLoading: true,
    shiftRosterView: null,
  },
  availabilityRoster: {
    isLoading: true,
    availabilityRosterView: null,
  },
  solverState: {
    solverStatus: 'TERMINATED',
  },
  alerts: {
    alertList: [],
    idGeneratorIndex: 0,
  },
};
