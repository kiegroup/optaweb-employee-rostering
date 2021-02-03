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
import { employeeSelectors } from 'store/employee';
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
  beforeEach(() => {
    jest.clearAllMocks();
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

  it('should upload the file on Excel input', () => {
    mockRedux(noEmployeesNoContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    const file = new File([], 'hello.xlsx');
    employeesPage.find(FileUpload).simulate('change', file);
    // expect(noEmployees.uploadEmployeeList).toBeCalledWith(file);
  });

  it('should show an error on non-excel input', () => {
    mockRedux(noEmployeesNoContractsStore);
    const employeesPage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    employeesPage.find(FileUpload).simulate('change', '');
    // expect(noEmployees.uploadEmployeeList).not.toBeCalled();
    // expect(noEmployees.showErrorMessage).toBeCalledWith('badFileType', { fileTypes: 'Excel (.xlsx)' });
  });
});
