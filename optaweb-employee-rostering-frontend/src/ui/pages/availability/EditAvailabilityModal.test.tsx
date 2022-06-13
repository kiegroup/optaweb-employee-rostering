import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Employee } from 'domain/Employee';
import moment from 'moment';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { useTranslation } from 'react-i18next';
import { EditAvailabilityModal } from './EditAvailabilityModal';

describe('Edit Availability Modal', () => {
  it('should render correctly when closed', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen={false}
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editAvailabilityModal)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editAvailabilityModal)).toMatchSnapshot();
  });

  it('should render correctly with a availability when opened', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editAvailabilityModal)).toMatchSnapshot();
  });

  it('should update state to match availability when props change', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editAvailabilityModal.setProps({
      availability: baseEmployeeAvailability,
    });
    expect(editAvailabilityModal.state('editedValue')).toEqual(baseEmployeeAvailability);

    editAvailabilityModal.setState({
      editedValue: {
        ...baseEmployeeAvailability,
        state: 'UNDESIRED',
      },
    });
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      ...baseEmployeeAvailability,
      state: 'UNDESIRED',
    });

    editAvailabilityModal.setProps({
      availability: undefined,
    });
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      tenantId: baseProps.tenantId,
    });
  });

  it('should call onSave iff all required properties are defined', () => {
    const onSave = jest.fn();
    const editAvailabilityModal = new EditAvailabilityModal({
      ...baseProps,
      availability: baseEmployeeAvailability,
      isOpen: true,
      onSave,
      onClose: jest.fn(),
      onDelete: jest.fn(),
    });

    editAvailabilityModal.state = { resetCount: 0, editedValue: {} };

    editAvailabilityModal.onSave();
    expect(onSave).not.toBeCalled();

    editAvailabilityModal.state = {
      resetCount: 1,
      editedValue: {
        employee,
        state: 'DESIRED',
      },
    };

    editAvailabilityModal.onSave();
    expect(onSave).not.toBeCalled();

    editAvailabilityModal.state = {
      resetCount: 2,
      editedValue: baseEmployeeAvailability,
    };

    editAvailabilityModal.onSave();
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith({ ...baseEmployeeAvailability, tenantId: 1 });
  });

  it('should call onSave when the save button is clicked with a completed availability', () => {
    const onSave = jest.fn();
    const editAvailabilityModal = mount(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={onSave}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editAvailabilityModal.find('button[aria-label="Save"]').simulate('click');
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith({ ...baseEmployeeAvailability, tenantId: 1 });
  });

  it('should call onClose when the bottom close button is clicked', () => {
    const onClose = jest.fn();
    const editAvailabilityModal = mount(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editAvailabilityModal.find('button[aria-label="Close Modal"]').simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onClose when the top right X close button is clicked', () => {
    const onClose = jest.fn();
    const editAvailabilityModal = mount(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editAvailabilityModal.find('button[aria-label="Close"]').simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onDelete when the delete button is clicked', () => {
    const onDelete = jest.fn();
    const editAvailabilityModal = mount(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={onDelete}
    />);

    editAvailabilityModal.find('button[aria-label="Delete"]').simulate('click');
    expect(onDelete).toBeCalled();
  });

  it('should prevent changing URL when completed', () => {
    const event = {
      preventDefault: jest.fn(),
    };
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      availability={baseEmployeeAvailability}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editAvailabilityModal.find('Form').simulate('submit', event);
    expect(event.preventDefault).toBeCalled();
  });

  it('should update the start date when the start date is changed', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editAvailabilityModal.find('[aria-label="Availability Start"]')
      .simulate('change', new Date('2019-01-01T09:00'));
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      startDateTime: new Date('2019-01-01T09:00'),
    });
    editAvailabilityModal.find('[aria-label="Availability Start"]').simulate('change', null);
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      startDateTime: undefined,
    });
  });

  it('should update the end date when the end date is changed', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editAvailabilityModal.find('[aria-label="Availability End"]')
      .simulate('change', new Date('2019-01-01T09:00'));
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      endDateTime: new Date('2019-01-01T09:00'),
    });
    editAvailabilityModal.find('[aria-label="Availability End"]').simulate('change', null);
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      endDateTime: undefined,
    });
  });

  it('should update the employee when the employee is changed', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editAvailabilityModal.find('TypeaheadSelectInput[aria-label="Employee"]').simulate('change', employee);
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      employee,
    });

    editAvailabilityModal.find('TypeaheadSelectInput[aria-label="Employee"]').simulate('change', undefined);
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      employee: undefined,
    });
  });

  it('should update the type when the type is changed', () => {
    const editAvailabilityModal = shallow(<EditAvailabilityModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editAvailabilityModal.find('TypeaheadSelectInput[aria-label="Type"]').simulate('change', 'UNDESIRED');
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      state: 'UNDESIRED',
    });

    editAvailabilityModal.find('TypeaheadSelectInput[aria-label="Type"]').simulate('change', undefined);
    expect(editAvailabilityModal.state('editedValue')).toEqual({
      state: undefined,
    });
  });
});

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

const baseEmployeeAvailability: EmployeeAvailability = {
  tenantId: 0,
  id: 1,
  version: 0,
  startDateTime: moment('2018-07-01T09:00').toDate(),
  endDateTime: moment('2018-07-01T17:00').toDate(),
  employee,
  state: 'DESIRED',
};

const baseProps = {
  ...useTranslation(),
  tReady: true,
  tenantId: 1,
  isOpen: true,
  employeeList: [employee],
};
