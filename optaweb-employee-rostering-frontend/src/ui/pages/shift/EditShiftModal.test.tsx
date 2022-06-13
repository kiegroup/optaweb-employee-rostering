import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { Shift } from 'domain/Shift';
import moment from 'moment';
import { useTranslation } from 'react-i18next';
import { EditShiftModal } from './EditShiftModal';

describe('Edit Shift Modal', () => {
  it('should render correctly when closed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen={false}
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftModal)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftModal)).toMatchSnapshot();
  });

  it('should render correctly with a shift when opened', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftModal)).toMatchSnapshot();
  });

  it('should update state to match shift when props change', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editShiftModal.setProps({
      shift: baseShift,
    });
    expect(editShiftModal.state('editedValue')).toEqual(baseShift);

    editShiftModal.setState({
      editedValue: {
        ...baseShift,
        pinnedByUser: true,
      },
    });
    expect(editShiftModal.state('editedValue')).toEqual({
      ...baseShift,
      pinnedByUser: true,
    });

    editShiftModal.setProps({
      shift: undefined,
    });
    expect(editShiftModal.state('editedValue')).toEqual({
      tenantId: baseProps.tenantId,
      employee: null,
      rotationEmployee: null,
      originalEmployee: null,
      requiredSkillSet: [],
      pinnedByUser: false,
    });
  });

  it('should call onSave iff all required properties are defined', () => {
    const onSave = jest.fn();
    const editShiftModal = new EditShiftModal({
      ...baseProps,
      onSave,
      onClose: jest.fn(),
      onDelete: jest.fn(),
      isOpen: false,
    });

    editShiftModal.state = {
      resetCount: 0,
      editedValue: {

      },
    };
    editShiftModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftModal.state = {
      resetCount: 1,
      editedValue: {
        employee: null,
        spot,
      },
    };
    editShiftModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftModal.state = {
      resetCount: 2,
      editedValue: baseShift,
    };
    editShiftModal.onSave();
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith({ ...baseShift, tenantId: 1 });
  });

  it('should call onSave when the save button is clicked with a completed shift', () => {
    const onSave = jest.fn();
    const editShiftModal = mount(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={onSave}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editShiftModal.find('button[aria-label="Save"]').simulate('click');
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith({ ...baseShift, tenantId: 1 });
  });

  it('should call onClose when the bottom close button is clicked', () => {
    const onClose = jest.fn();
    const editShiftModal = mount(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editShiftModal.find('button[aria-label="Close Modal"]').simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onClose when the top right X close button is clicked', () => {
    const onClose = jest.fn();
    const editShiftModal = mount(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editShiftModal.find('button[aria-label="Close"]').simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onDelete when the delete button is clicked', () => {
    const onDelete = jest.fn();
    const editShiftModal = mount(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={onDelete}
    />);

    editShiftModal.find('button[aria-label="Delete"]').simulate('click');
    expect(onDelete).toBeCalled();
  });

  it('should prevent changing URL when completed', () => {
    const event = {
      preventDefault: jest.fn(),
    };
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('Form').simulate('submit', event);
    expect(event.preventDefault).toBeCalled();
  });

  it('should update the start date when the start date is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('[aria-label="Shift Start"]').simulate('change', new Date('2019-01-01T09:00'));
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      startDateTime: new Date('2019-01-01T09:00'),
    });
    editShiftModal.find('[aria-label="Shift Start"]').simulate('change', null);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      startDateTime: undefined,
    });
  });

  it('should update the end date when the end date is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('[aria-label="Shift End"]').simulate('change', new Date('2019-01-01T09:00'));
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      endDateTime: new Date('2019-01-01T09:00'),
    });
    editShiftModal.find('[aria-label="Shift End"]').simulate('change', null);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      endDateTime: undefined,
    });
  });

  it('should update the spot when the spot is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('TypeaheadSelectInput[aria-label="Spot"]').simulate('change', spot);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      spot,
    });
  });

  it('should update the employee when the employee is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('TypeaheadSelectInput[aria-label="Employee"]').simulate('change', employee);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      employee,
    });

    editShiftModal.find('TypeaheadSelectInput[aria-label="Employee"]').simulate('change', undefined);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      employee: null,
    });
  });

  it('should update the rotation employee when the rotation employee is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('TypeaheadSelectInput[aria-label="Rotation Employee"]').simulate('change', employee);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      rotationEmployee: employee,
    });

    editShiftModal.find('TypeaheadSelectInput[aria-label="Rotation Employee"]').simulate('change', undefined);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      rotationEmployee: null,
    });
  });

  it('should update isPinned when isPinned is changed', () => {
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find('[aria-label="Is Pinned"]').simulate('change', true);
    expect(editShiftModal.state('editedValue')).toEqual({
      ...defaultValues,
      pinnedByUser: true,
    });
  });
});

const defaultValues = {
  tenantId: 1,
  employee: null,
  originalEmployee: null,
  requiredSkillSet: [],
  rotationEmployee: null,
  pinnedByUser: false,
};

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: 'Spot',
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: 'Skill',
    },
  ],
};

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
  skillProficiencySet: [{
    tenantId: 0,
    id: 6,
    version: 0,
    name: 'Not Required Skill',
  }],
  shortId: 'e1',
  color: '#FFFFFF',
};

const baseShift: Shift = {
  tenantId: 0,
  id: 1,
  version: 0,
  startDateTime: moment('2018-07-01T09:00').toDate(),
  endDateTime: moment('2018-07-01T17:00').toDate(),
  spot,
  requiredSkillSet: [],
  originalEmployee: null,
  employee,
  rotationEmployee: {
    ...employee,
    id: 7,
    name: 'Rotation Employee',
  },
  pinnedByUser: false,
  indictmentScore: { hardScore: 0, mediumScore: 0, softScore: 0 },
  requiredSkillViolationList: [],
  unavailableEmployeeViolationList: [],
  shiftEmployeeConflictList: [],
  desiredTimeslotForEmployeeRewardList: [],
  undesiredTimeslotForEmployeePenaltyList: [],
  rotationViolationPenaltyList: [],
  unassignedShiftPenaltyList: [],
  contractMinutesViolationPenaltyList: [],
};

const baseProps = {
  ...useTranslation('EditShiftModal'),
  shift: undefined,
  tReady: true,
  tenantId: 1,
  spotList: [spot],
  skillList: [],
  employeeList: [employee],
};
