import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Employee } from 'domain/Employee';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { Button, FileUpload, TextInput } from '@patternfly/react-core';
import { Map } from 'immutable';
import { mockStore } from 'store/mockStore';
import { Contract } from 'domain/Contract';
import DomainObjectView from 'domain/DomainObjectView';
import { mockRedux, mockTranslate } from 'setupTests';
import { employeeOperations, employeeSelectors } from 'store/employee';
import { DataTable, RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { alert } from 'store/alert';
import { doNothing } from 'types';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { Skill } from 'domain/Skill';
import { skillSelectors } from 'store/skill';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import * as ColorPicker from 'ui/components/ColorPicker';
import { ArrowIcon } from '@patternfly/react-icons';
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

const contract1: Contract = {
  id: 0,
  tenantId: 0,
  name: 'Contract',
  maximumMinutesPerDay: null,
  maximumMinutesPerWeek: null,
  maximumMinutesPerMonth: null,
  maximumMinutesPerYear: null,
};

const contract2: Contract = {
  id: 1,
  tenantId: 0,
  name: 'Contract 2',
  maximumMinutesPerDay: 5,
  maximumMinutesPerWeek: null,
  maximumMinutesPerMonth: null,
  maximumMinutesPerYear: null,
};

const skill1: Skill = {
  id: 0,
  tenantId: 0,
  name: 'Skill 1',
};

const noEmployeesOneContractsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, contract1)
      .set(1, contract2),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: Map(),
  },
}).store;

const twoEmployeesOneContractsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map<number, Skill>()
      .set(0, skill1),
  },
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, contract1),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: Map<number, DomainObjectView<Employee>>()
      .set(1, {
        tenantId: 0,
        id: 1,
        name: 'Employee 1',
        contract: contract1.id as number,
        shortId: 'E1',
        color: '#FF0000',
        skillProficiencySet: [],
      }).set(2, {
        tenantId: 0,
        id: 2,
        name: 'Employee 2',
        contract: contract1.id as number,
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

  it('clicking on the edit button should show editor', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    getRouterProps('/0/employees', {});
    const viewer = shallow(<EmployeeRow {...employee} />);
    viewer.find(RowViewButtons).simulate('edit');

    expect(viewer).toMatchSnapshot();
    viewer.find(EditableEmployeeRow).simulate('close');
    expect(viewer).toMatchSnapshot();
  });

  it('clicking on the arrow should take you to the Availability Page', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    const routerProps = getRouterProps('/0/employees', {});
    const viewer = shallow(<EmployeeRow {...employee} />);
    viewer.find(Button).filterWhere(wrapper => wrapper.contains(<ArrowIcon />)).simulate('click');
    expect(routerProps.history.push)
      .toBeCalledWith(`/${employee.tenantId}/availability?employee=${encodeURIComponent(employee.name)}`);
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

    const name = 'New Employee Name';
    const contract = contract2;
    const skillProficiencySet = skillSelectors.getSkillList(twoEmployeesOneContractsStore.getState());
    const shortId = 'NEN';
    const color = '#FF00FF';

    editor.find(`[columnName="${mockTranslate('name')}"]`).find(TextInput).simulate('change', name);
    editor.find(`[columnName="${mockTranslate('contract')}"]`)
      .find(TypeaheadSelectInput).simulate('change', contract);
    editor.find(`[columnName="${mockTranslate('skillProficiencies')}"]`)
      .find(MultiTypeaheadSelectInput).simulate('change', skillProficiencySet);
    editor.find(`[columnName="${mockTranslate('shortId')}"]`)
      .find(TextInput).simulate('change', shortId);
    editor.find(`[columnName="${mockTranslate('color')}"]`)
      .find(ColorPicker.ColorPicker).simulate('changeColor', color);

    const newEmployee = {
      ...employee,
      name,
      contract,
      skillProficiencySet,
      shortId,
      color,
    };

    editor.find(RowEditButtons).prop('onSave')();
    expect(employeeOperations.addEmployee).toBeCalledWith(newEmployee);
    expect(twoEmployeesOneContractsStore.dispatch).toBeCalledWith(addEmployee(newEmployee));
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

  it('clicking on the edit button in the viewer should show the editor', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    const viewer = shallow(<EmployeeRow {...employee} />);

    // Clicking the edit button should show the editor
    viewer.find(RowViewButtons).prop('onEdit')();
    expect(viewer).toMatchSnapshot();

    // Clicking the close button should show the viwer
    viewer.find(EditableEmployeeRow).prop('onClose')();
    expect(viewer).toMatchSnapshot();
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

  it('DataTable rowWrapper should be EmployeeRow', () => {
    const employee = employeeSelectors.getEmployeeById(twoEmployeesOneContractsStore.getState(), 2);
    mockRedux(twoEmployeesOneContractsStore);
    const employeePage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    const rowWrapper = shallow(employeePage.find(DataTable).prop('rowWrapper')(employee));
    expect(rowWrapper).toMatchSnapshot();
  });

  it('DataTable newRowWrapper should be EditableEmployeeRow', () => {
    mockRedux(twoEmployeesOneContractsStore);
    const employeePage = shallow(<EmployeesPage {...getRouterProps('/0/employee', {})} />);
    const removeRow = jest.fn();

    jest.spyOn(ColorPicker, 'getRandomColor').mockImplementation(() => '#040404'); // chosen by fair dice roll
    const newRowWrapper = shallow((employeePage.find(DataTable).prop('newRowWrapper') as any)(removeRow));
    expect(newRowWrapper).toMatchSnapshot();
    newRowWrapper.find(RowEditButtons).prop('onClose')();
    expect(removeRow).toBeCalled();
  });
});
