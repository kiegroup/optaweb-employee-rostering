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
  covidRiskType: 'INOCULATED',
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
