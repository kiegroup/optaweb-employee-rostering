import React from 'react';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
import { employeeSelectors } from 'store/employee';
import { shallow } from 'enzyme';
import { Button, Modal } from '@patternfly/react-core';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import {
  EmployeeNickNameProps, EmployeeNickName, EmployeeStubProps, EmployeeStub,
  EditEmployeeStubListModalProps, EditEmployeeStubListModal,
  EmployeeStubListProps, Stub, EmployeeStubList,
} from './EmployeeStub';

const mockSelectorReturnValue = new Map();
jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn().mockImplementation(selector => mockSelectorReturnValue.get(selector)),
}));

function mockSelector<T>(selector: (state: AppState) => T, value: T): void {
  mockSelectorReturnValue.set(selector, value);
}

const employee: Employee = {
  tenantId: 0,
  id: 4,
  version: 0,
  name: 'Employee 1',
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: 'Basic Contract',
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000,
  },
  skillProficiencySet: [],
  shortId: 'e1',
  color: '#FFFFFF',
};

const newEmployee: Employee = {
  tenantId: 0,
  id: 5,
  version: 0,
  name: 'Employee 2',
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: 'Basic Contract',
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000,
  },
  skillProficiencySet: [],
  shortId: 'e2',
  color: '#000000',
};


const employeeList: Employee[] = [
  employee,
  newEmployee,
];

describe('EmployeeNickName Component', () => {
  const baseProps: EmployeeNickNameProps = {
    employee,
  };
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('It should render correctly with an employee', () => {
    const employeeNickName = shallow(<EmployeeNickName {...baseProps} />);
    expect(employeeNickName).toMatchSnapshot();
  });

  it('It should render correctly without an employee', () => {
    const employeeNickName = shallow(<EmployeeNickName employee={null} />);
    expect(employeeNickName).toMatchSnapshot();
  });

  it('It color should contrast the employee color, or be gray for unassigned', () => {
    let employeeNickName = shallow(<EmployeeNickName employee={employee} />);
    expect(employeeNickName.prop('style').color).toEqual('black');

    employeeNickName = shallow(<EmployeeNickName employee={newEmployee} />);
    expect(employeeNickName.prop('style').color).toEqual('white');

    employeeNickName = shallow(<EmployeeNickName employee={null} />);
    expect(employeeNickName.prop('style').color).toEqual('gray');
  });
});

describe('EmployeeStub Component', () => {
  const baseProps: EmployeeStubProps = {
    isSelected: false,
    employee,
    color: '#FF0000',
    onClick: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('It should render correctly with an employee', () => {
    const employeeStub = shallow(<EmployeeStub {...baseProps} />);
    expect(employeeStub).toMatchSnapshot();
  });

  it('It should render correctly without an employee', () => {
    const employeeStub = shallow(<EmployeeStub {...baseProps} employee="NO_SHIFT" />);
    expect(employeeStub).toMatchSnapshot();
  });

  it('It should call on click on click', () => {
    const employeeStub = shallow(<EmployeeStub {...baseProps} employee="NO_SHIFT" />);
    employeeStub.simulate('click');
    expect(baseProps.onClick).toBeCalled();
  });

  it('It should set color to contrast the background', () => {
    let employeeStub = shallow(<EmployeeStub {...baseProps} color="white" />);
    expect(employeeStub.prop('style').color).toEqual('black');

    employeeStub = shallow(<EmployeeStub {...baseProps} color="black" />);
    expect(employeeStub.prop('style').color).toEqual('white');
  });

  it('It have a small black outline if not selected', () => {
    const employeeStub = shallow(<EmployeeStub {...baseProps} isSelected={false} />);
    expect(employeeStub.prop('style').outline).toEqual('1px solid black');
  });

  it('It have a thick blue outline if selected', () => {
    const employeeStub = shallow(<EmployeeStub {...baseProps} isSelected />);
    expect(employeeStub.prop('style').outline).toEqual('5px solid var(--pf-global--primary-color--100)');
  });
});

describe('EditEmployeeStubListModal component', () => {
  const baseProps: EditEmployeeStubListModalProps = {
    isVisible: true,
    currentStubList: [employee],
    onClose: jest.fn(),
    onUpdateStubList: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockSelector(employeeSelectors.getEmployeeList, employeeList);
  });

  it('should render correctly', () => {
    const editEmployeeStubListModal = shallow(<EditEmployeeStubListModal {...baseProps} />);
    expect(editEmployeeStubListModal).toMatchSnapshot();
  });

  it('closing the modal should close the modal', () => {
    const editEmployeeStubListModal = shallow(<EditEmployeeStubListModal {...baseProps} />);
    editEmployeeStubListModal.find(Modal).simulate('close');
    expect(baseProps.onClose).toBeCalled();
    expect(baseProps.onUpdateStubList).not.toBeCalled();
  });

  it('clicking the close button should close the modal', () => {
    const editEmployeeStubListModal = shallow(<EditEmployeeStubListModal {...baseProps} />);
    editEmployeeStubListModal.find(Modal).prop('actions')[0].props.onClick();
    expect(baseProps.onClose).toBeCalled();
    expect(baseProps.onUpdateStubList).not.toBeCalled();
  });

  it('clicking the save button should close the modal and call onUpdateStubList', () => {
    const editEmployeeStubListModal = shallow(<EditEmployeeStubListModal {...baseProps} />);

    editEmployeeStubListModal.find(TypeaheadSelectInput).simulate('change', newEmployee);

    editEmployeeStubListModal.find(Modal).prop('actions')[1].props.onClick();
    expect(baseProps.onClose).toBeCalled();
    expect(baseProps.onUpdateStubList).toBeCalledWith([newEmployee]);
  });
});

describe('EmployeeStubList Component', () => {
  const stub1: Stub = employee;

  const stub2: Stub = newEmployee;

  const baseProps: EmployeeStubListProps = {
    selectedStub: stub1,
    stubList: [stub1, stub2],
    onStubSelect: jest.fn(),
    onUpdateStubList: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockSelector(employeeSelectors.getEmployeeList, employeeList);
  });

  it('should render correctly', () => {
    const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
    expect(employeeStubList).toMatchSnapshot();
  });

  it('clicking on the first employee stub should set the employee stub to null', () => {
    const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
    employeeStubList.find(EmployeeStub).first().simulate('click');
    expect(baseProps.onStubSelect).toBeCalledWith('NO_SHIFT');
  });

  it('clicking on the second employee stub should set the employee stub to Unassigned (stub with null employee)',
    () => {
      const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
      employeeStubList.find(EmployeeStub).at(1).simulate('click');
      expect(baseProps.onStubSelect).toBeCalledWith('SHIFT_WITH_NO_EMPLOYEE');
    });

  it('clicking on the employee stub should set the employee stub to it', () => {
    const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
    employeeStubList.find(EmployeeStub).filterWhere(wrapper => wrapper.prop('employee') === stub2)
      .simulate('click');
    expect(baseProps.onStubSelect).toBeCalledWith(stub2);
  });

  it('clicking on the button should open the edit stub list modal', () => {
    const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
    expect(employeeStubList.find(EditEmployeeStubListModal).prop('isVisible')).toEqual(false);
    employeeStubList.find(Button).simulate('click');
    expect(employeeStubList.find(EditEmployeeStubListModal).prop('isVisible')).toEqual(true);
    employeeStubList.find(EditEmployeeStubListModal).simulate('close');
    expect(employeeStubList.find(EditEmployeeStubListModal).prop('isVisible')).toEqual(false);
  });

  it('updating the stub list in the modal should update the stub list', () => {
    const employeeStubList = shallow(<EmployeeStubList {...baseProps} />);
    employeeStubList.find(EditEmployeeStubListModal).simulate('updateStubList', [stub2]);
    expect(baseProps.onUpdateStubList).toBeCalledWith([stub2]);
  });
});
