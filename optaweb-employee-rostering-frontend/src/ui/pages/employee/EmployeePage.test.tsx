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
import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { Sorter } from 'types';
import { Employee } from 'domain/Employee';
import { act } from 'react-dom/test-utils';
import { useTranslation, Trans } from 'react-i18next';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { FileUpload } from '@patternfly/react-core';
import { List } from 'immutable';
import { Contract } from 'domain/Contract';
import { Skill } from 'domain/Skill';
import { EmployeesPage, Props } from './EmployeesPage';

describe('Employees page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly with no employees', () => {
    const employeesPage = shallow(<EmployeesPage {...noEmployees} />);
    expect(toJson(employeesPage)).toMatchSnapshot();
  });

  it('should render correctly with a few employees', () => {
    const employeesPage = shallow(<EmployeesPage {...twoEmployees} />);
    expect(toJson(employeesPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const spot = twoEmployees.tableData.get(1) as Employee;
    const viewer = shallow(employeesPage.renderViewer(spot));
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const spot = twoEmployees.tableData.get(1) as Employee;
    const editor = shallow(employeesPage.renderEditor(spot));
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('should update properties on change', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const setProperty = jest.fn();
    const editor = employeesPage.editDataRow(employeesPage.getInitialStateForNewRow(), setProperty);
    const nameCol = shallow(editor[0] as React.ReactElement);
    nameCol.simulate('change', 'Test');
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('name', 'Test');
    expect(setProperty).toBeCalledWith('shortId', 'T');

    setProperty.mockClear();
    const contractCol = mount(editor[1] as React.ReactElement);
    act(() => {
      contractCol.find(TypeaheadSelectInput).props().onChange(twoEmployees.contractList.get(0) as Contract);
    });
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('contract', twoEmployees.contractList.get(0) as Contract);

    setProperty.mockClear();
    const skillProficiencySetCol = mount(editor[2] as React.ReactElement);
    act(() => {
      skillProficiencySetCol.find(MultiTypeaheadSelectInput).props().onChange([twoEmployees.skillList.get(0) as Skill]);
    });
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('skillProficiencySet', [twoEmployees.skillList.get(0) as Skill]);

    setProperty.mockClear();
    const shortIdCol = shallow(editor[3] as React.ReactElement);
    shortIdCol.simulate('change', 'ID');
    expect(setProperty).toBeCalledWith('shortId', 'ID');

    setProperty.mockClear();
    // Mount and set props on shortIdCol so the ref is updated
    mount(editor[3] as React.ReactElement).setProps({ value: 'ID' });
    nameCol.simulate('change', 'Mimi');
    expect(setProperty).toBeCalledWith('name', 'Mimi');
    expect(setProperty).not.toBeCalledWith('shortId', expect.any(String));

    setProperty.mockClear();
    const colorCol = shallow(editor[4] as React.ReactElement);
    colorCol.simulate('changeColor', '#ffffff');
    expect(setProperty).toBeCalledWith('color', '#ffffff');
  });

  it('should call addEmployee on addData', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const employee: Employee = {
      name: 'Employee',
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
      tenantId: 0,
      id: 1,
      version: 0,
      shortId: 'N',
      color: '#FFFFFF',
    };
    employeesPage.addData(employee);
    expect(twoEmployees.addEmployee).toBeCalled();
    expect(twoEmployees.addEmployee).toBeCalledWith(employee);
  });

  it('should call updateEmployee on updateData', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const employee: Employee = {
      name: 'Employee',
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
      tenantId: 0,
      id: 1,
      version: 0,
      shortId: 'N',
      color: '#FFFFFF',
    };
    employeesPage.updateData(employee);
    expect(twoEmployees.updateEmployee).toBeCalled();
    expect(twoEmployees.updateEmployee).toBeCalledWith(employee);
  });

  it('should call removeEmployee on removeData', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const employee: Employee = {
      name: 'Employee',
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
      tenantId: 0,
      id: 1,
      version: 0,
      shortId: 'N',
      color: '#FFFFFF',
    };
    employeesPage.removeData(employee);
    expect(twoEmployees.removeEmployee).toBeCalled();
    expect(twoEmployees.removeEmployee).toBeCalledWith(employee);
  });

  it('should return a filter that match by name, skills and contract', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const filter = employeesPage.getFilter();

    expect(twoEmployees.tableData.filter(filter('1'))).toEqual(List([
      twoEmployees.tableData.get(0) as Employee,
      twoEmployees.tableData.get(1) as Employee,
    ]));
    expect(twoEmployees.tableData.filter(filter('Skill 1'))).toEqual(List([
      twoEmployees.tableData.get(1) as Employee,
    ]));
    expect(twoEmployees.tableData.filter(filter('2'))).toEqual(List([twoEmployees.tableData.get(1) as Employee]));
    expect(twoEmployees.tableData.filter(filter('Contract 2'))).toEqual(List([
      twoEmployees.tableData.get(1) as Employee,
    ]));
    expect(twoEmployees.tableData.filter(filter('Employee 2'))).toEqual(List([
      twoEmployees.tableData.get(1) as Employee,
    ]));
  });

  it('should return a sorter that sort by name and contract', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const nameSorter = employeesPage.getSorters()[0] as Sorter<Employee>;
    let list = [twoEmployees.tableData.get(1) as Employee, twoEmployees.tableData.get(0) as Employee];
    expect(list.sort(nameSorter)).toEqual(twoEmployees.tableData.toArray());
    list = [twoEmployees.tableData.get(1) as Employee, twoEmployees.tableData.get(0) as Employee];
    const contractSorter = employeesPage.getSorters()[1] as Sorter<Employee>;
    expect(list.sort(contractSorter)).toEqual(twoEmployees.tableData.toArray());
    expect(employeesPage.getSorters()[2]).toBeNull();
  });

  it('should go to the Contract page if the user click on the link', () => {
    const employeesPage = shallow(<EmployeesPage
      {...noEmployees}
    />);
    mount((employeesPage.find(Trans).prop('components') as any)[2]).simulate('click');
    expect(noEmployees.history.push).toBeCalled();
    expect(noEmployees.history.push).toBeCalledWith('/0/contracts');
  });

  it('should treat incomplete data as incomplete', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const noName = {
      tenantId: 0,
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
    };
    const result1 = employeesPage.isDataComplete(noName);
    expect(result1).toEqual(false);

    const noSkills = {
      tenantId: 0,
      name: 'Name',
      contract: twoEmployees.contractList.get(0) as Contract,
    };
    const result2 = employeesPage.isDataComplete(noSkills);
    expect(result2).toEqual(false);

    const noContract = {
      tenantId: 0,
      name: 'Name',
      skillProficiencySet: [],
    };
    const result3 = employeesPage.isDataComplete(noContract);
    expect(result3).toEqual(false);

    const completed: Employee = {
      tenantId: 0,
      name: 'Name',
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
      shortId: 'N',
      color: '#FFFFFF',
    };
    const result4 = employeesPage.isDataComplete(completed);
    expect(result4).toEqual(true);
  });

  it('should treat empty name as invalid', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const noName: Employee = {
      tenantId: 0,
      name: '',
      skillProficiencySet: [],
      contract: twoEmployees.contractList.get(0) as Contract,
      shortId: 'N',
      color: '#FFFFFF',
    };
    const result1 = employeesPage.isValid(noName);
    expect(result1).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const employeesPage = new EmployeesPage(twoEmployees);
    const components = twoEmployees.tableData.get(0) as Employee;
    const result = employeesPage.isValid(components);
    expect(result).toEqual(true);
  });

  it('should upload the file on Excel input', () => {
    const employeesPage = shallow(<EmployeesPage {...noEmployees} />);
    const file = new File([], 'hello.xlsx');
    employeesPage.find(FileUpload).simulate('change', file);
    expect(noEmployees.uploadEmployeeList).toBeCalledWith(file);
  });

  it('should show an error on non-excel input', () => {
    const employeesPage = shallow(<EmployeesPage {...noEmployees} />);
    employeesPage.find(FileUpload).simulate('change', '');
    expect(noEmployees.uploadEmployeeList).not.toBeCalled();
    expect(noEmployees.showErrorMessage).toBeCalledWith('badFileType', { fileTypes: 'Excel (.xlsx)' });
  });
});

const noEmployees: Props = {
  ...useTranslation('EmployeePage'),
  tReady: true,
  tenantId: 0,
  title: 'Employees',
  columnTitles: ['Name', 'Contract', 'Skill Set'],
  tableData: List(),
  skillList: List(),
  contractList: List(),
  addEmployee: jest.fn(),
  updateEmployee: jest.fn(),
  removeEmployee: jest.fn(),
  uploadEmployeeList: jest.fn(),
  showErrorMessage: jest.fn(),
  ...getRouterProps('/contacts', {}),
};

const twoEmployees: Props = {
  ...useTranslation('EmployeePage'),
  tReady: true,
  tenantId: 0,
  title: 'Employees',
  columnTitles: ['Name', 'Contract', 'Skill Set'],
  tableData: List([{
    id: 0,
    version: 0,
    tenantId: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: 'Employee 2',
    skillProficiencySet: [{ tenantId: 0, name: 'Skill 1' }, { tenantId: 0, name: 'Skill 2' }],
    contract: {
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Contract 2',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  }]),
  skillList: List([{ tenantId: 0, name: 'Skill 1' }, { tenantId: 0, name: 'Skill 2' }]),
  contractList: List([
    {
      tenantId: 0,
      id: 0,
      version: 0,
      name: 'Contract 1',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    {
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Contract 2',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
  ]),
  addEmployee: jest.fn(),
  updateEmployee: jest.fn(),
  removeEmployee: jest.fn(),
  uploadEmployeeList: jest.fn(),
  showErrorMessage: jest.fn(),
  ...getRouterProps('/employees', {}),
};
