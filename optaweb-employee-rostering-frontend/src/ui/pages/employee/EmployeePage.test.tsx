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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Employee } from 'domain/Employee';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { FileUpload } from '@patternfly/react-core';
import { Map } from 'immutable';
import { mockStore } from 'store/mockStore';
import { Contract } from 'domain/Contract';
import DomainObjectView from 'domain/DomainObjectView';
import { mockRedux } from 'setupTests';
import { employeeOperations, employeeSelectors } from 'store/employee';
import { RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { alert } from 'store/alert';
import { doNothing } from 'types';
import { EditableEmployeeRow, EmployeeRow, EmployeesPage } from './EmployeesPage';

const noEmployeesNoContractsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: Map(),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: Map(),
  },
}).store;

const contract: Contract = {
  id: 0,
  tenantId: 0,
  name: 'Contract',
  maximumMinutesPerDay: null,
  maximumMinutesPerWeek: null,
  maximumMinutesPerMonth: null,
  maximumMinutesPerYear: null,
};

const noEmployeesOneContractsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, contract),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: Map(),
  },
}).store;

const twoEmployeesOneContractsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, contract),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: Map<number, DomainObjectView<Employee>>()
      .set(1, {
        tenantId: 0,
        id: 1,
        name: 'Employee 1',
        contract: contract.id as number,
        shortId: 'E1',
        color: '#FF0000',
        skillProficiencySet: [],
      }).set(2, {
        tenantId: 0,
        id: 2,
        name: 'Employee 2',
        contract: contract.id as number,
        shortId: 'E2',
        color: '#00FF00',
        skillProficiencySet: [],
      }),
  },
}).store;


describe('Employees page', () => {
  const addEmployee = (employee: Employee) => ['add', employee];
  const updateEmployee = (employee: Employee) => ['update', employee];
  const removeEmployee = (employee: Employee) => ['remove', employee];
  const uploadEmployeeList = (file: File) => ['upload', file];
  const showErrorMessage = (key: string, params: any) => ['showErrorMessage', key, params];

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(employeeOperations, 'addEmployee').mockImplementation(employee => addEmployee(employee) as any);
    jest.spyOn(employeeOperations, 'updateEmployee').mockImplementation(employee => updateEmployee(employee) as any);
    jest.spyOn(employeeOperations, 'removeEmployee').mockImplementation(employee => removeEmployee(employee) as any);
    jest.spyOn(employeeOperations, 'uploadEmployeeList').mockImplementation(file => uploadEmployeeList(file) as any);
    jest.spyOn(alert, 'showErrorMessage').mockImplementation((key, params) => showErrorMessage(key, params) as any);
    jest.spyOn(twoEmployeesOneContractsStore, 'dispatch').mockImplementation(doNothing);
    jest.spyOn(noEmployeesNoContractsStore, 'dispatch').mockImplementation(doNothing);
  });

  it('should render correctly with no employees', () => {
    mockRedux(noEmployeesOneContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    expect(toJson(employeesPage)).toMatchSnapshot();
  });

  it('should render correctly with a few employees', () => {
    mockRedux(twoEmployeesOneContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    expect(toJson(employeesPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const viewer = shallow(<EmployeeRow {...employee} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const editor = shallow(<EditableEmployeeRow employee={employee} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('no name should be invalid', () => {
    const employee = {
      ...employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2),
      name: '',
    };
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const editor = shallow(<EditableEmployeeRow employee={employee} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const employee = {
      ...employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2),
      name: 'Employee 1',
      id: 3,
    };
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const editor = shallow(<EditableEmployeeRow employee={employee} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new employee should call add employee', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const editor = shallow(<EditableEmployeeRow employee={employee} isNew onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(employeeOperations.addEmployee).toBeCalledWith(employee);
    expect(twoEmployeesOneContractsStore.dispatch).toBeCalledWith(addEmployee(employee));
  });

  it('saving updated employee should call update employee', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const editor = shallow(<EditableEmployeeRow employee={employee} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(employeeOperations.updateEmployee).toBeCalledWith(employee);
    expect(twoEmployeesOneContractsStore.dispatch).toBeCalledWith(updateEmployee(employee));
  });

  it('deleting should call delete employee', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const viewer = shallow(<EmployeeRow {...employee} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(employeeOperations.removeEmployee).toBeCalledWith(employee);
    expect(twoEmployeesOneContractsStore.dispatch).toBeCalledWith(removeEmployee(employee));
  });

  it('should upload the file on Excel input', () => {
    mockRedux(noEmployeesNoContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    const file = new File([], 'hello.xlsx');
    employeesPage.find(FileUpload).simulate('change', file);
    expect(noEmployeesNoContractsStore.dispatch).toBeCalledWith(uploadEmployeeList(file));
  });

  it('should show an error on non-excel input', () => {
    mockRedux(noEmployeesNoContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    employeesPage.find(FileUpload).simulate('change', '');
    expect(employeeOperations.uploadEmployeeList).not.toBeCalled();
    expect(noEmployeesNoContractsStore.dispatch)
      .toBeCalledWith(showErrorMessage('badFileType', { fileTypes: 'Excel (.xlsx)' }));
  });
});
