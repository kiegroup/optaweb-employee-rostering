import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Contract } from 'domain/Contract';
import { Employee } from 'domain/Employee';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import moment from 'moment';
import AvailabilityEvent, { AvailabilityEventProps } from './AvailabilityEvent';

describe('Availability Event', () => {
  it('should render correctly', () => {
    const event = shallow(<AvailabilityEvent {...baseProps} />);
    expect(toJson(event)).toMatchSnapshot();
  });

  it('should call removeEmployeeAvailability if they click on the delete button', () => {
    const event = shallow(<AvailabilityEvent {...baseProps} />);
    event.find('[aria-label="Delete"]').simulate('click');
    expect(baseProps.removeEmployeeAvailability).toBeCalled();
    expect(baseProps.removeEmployeeAvailability).toBeCalledWith(availability);
  });

  it('should call updateEmployeeAvailability with a "DESIRED" state'
    + 'if they click on the "DESIRED" button', () => {
    const event = shallow(<AvailabilityEvent
      {...baseProps}
      availability={{
        ...baseProps.availability,
        state: 'UNAVAILABLE',
      }}
    />);
    event.find('[aria-label="Desired"]').simulate('click');
    expect(baseProps.updateEmployeeAvailability).toBeCalled();
    expect(baseProps.updateEmployeeAvailability).toBeCalledWith(availability);
  });

  it('should call updateEmployeeAvailability with a "UNDESIRED" state'
  + 'if they click on the "UNDESIRED" button', () => {
    const event = shallow(<AvailabilityEvent {...baseProps} />);
    event.find('[aria-label="Undesired"]').simulate('click');
    expect(baseProps.updateEmployeeAvailability).toBeCalled();
    expect(baseProps.updateEmployeeAvailability).toBeCalledWith({
      ...baseProps.availability,
      state: 'UNDESIRED',
    });
  });

  it('should call updateEmployeeAvailability with a "UNAVAILABLE" state'
  + 'if they click on the "UNAVAILABLE" button', () => {
    const event = shallow(<AvailabilityEvent {...baseProps} />);
    event.find('[aria-label="Unavailable"]').simulate('click');
    expect(baseProps.updateEmployeeAvailability).toBeCalled();
    expect(baseProps.updateEmployeeAvailability).toBeCalledWith({
      ...baseProps.availability,
      state: 'UNAVAILABLE',
    });
  });
});

const contract: Contract = {
  id: 0,
  version: 0,
  tenantId: 0,
  name: 'Contract',
  maximumMinutesPerDay: 100,
  maximumMinutesPerWeek: 1000,
  maximumMinutesPerMonth: 10000,
  maximumMinutesPerYear: 100000,
};

const employee: Employee = {
  id: 1,
  version: 0,
  tenantId: 0,
  name: 'Amy',
  contract,
  skillProficiencySet: [],
  shortId: 'A',
  color: '#FFFFFF',
};

const startDateTime = moment('2018-01-01T09:00').toDate();
const endDateTime = moment('2018-01-01T17:00').toDate();

const availability: EmployeeAvailability = {
  id: 2,
  version: 0,
  tenantId: 0,
  employee,
  startDateTime,
  endDateTime,
  state: 'DESIRED',
};

const baseProps: AvailabilityEventProps = {
  availability,
  onEdit: jest.fn(),
  onDelete: jest.fn(),
  updateEmployeeAvailability: jest.fn(),
  removeEmployeeAvailability: jest.fn(),
};
