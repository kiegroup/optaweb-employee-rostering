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
import Spot from 'domain/Spot';
import Employee from 'domain/Employee';
import Shift from 'domain/Shift';
import { EditShiftModal } from './EditShiftModal';
import moment from 'moment';

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
      shift: baseShift
    });
    expect(editShiftModal.state("editedValue")).toEqual(baseShift);

    editShiftModal.setState({
      editedValue: {
        ...baseShift,
        pinnedByUser: true
      }
    });
    expect(editShiftModal.state("editedValue")).toEqual({
      ...baseShift,
      pinnedByUser: true
    });

    editShiftModal.setProps({
      shift: undefined
    });
    expect(editShiftModal.state("editedValue")).toEqual({
      tenantId: baseProps.tenantId,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: false
    });
  });

  it('should call onSave iff all required properties are defined', () => {
    const onSave = jest.fn();
    const editShiftModal = new EditShiftModal({
      ...baseProps,
      onSave: onSave,
      onClose: jest.fn(),
      onDelete: jest.fn(),
      isOpen: false
    });

    editShiftModal.state = {
      editedValue: {

      }
    };
    editShiftModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftModal.state = {
      editedValue: {
        employee: null,
        spot: spot
      }
    };
    editShiftModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftModal.state = {
      editedValue: baseShift
    };
    editShiftModal.onSave();
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith(baseShift);
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

    editShiftModal.find('button[aria-label="Save"]').simulate("click");
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith(baseShift);
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
    
    editShiftModal.find('button[aria-label="Close Modal"]').simulate("click");
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
    
    editShiftModal.find('button[aria-label="Close"]').simulate("click");
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
    
    editShiftModal.find('button[aria-label="Delete"]').simulate("click");
    expect(onDelete).toBeCalled();
  });

  it('should prevent changing URL when completed', () => {
    const event = {
      preventDefault: jest.fn()
    }
    const editShiftModal = shallow(<EditShiftModal
      {...baseProps}
      shift={baseShift}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftModal.find("Form").simulate("submit", event);
    expect(event.preventDefault).toBeCalled();
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: "Spot",
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: "Skill"
    }
  ]
}

const employee: Employee = {
  tenantId: 0,
  id: 4,
  version: 0,
  name: "Employee 1",
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: "Basic Contract",
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000
  },
  skillProficiencySet: [{
    tenantId: 0,
    id: 6,
    version: 0,
    name: "Not Required Skill"
  }]
}

const baseShift: Shift = {
  tenantId: 0,
  id: 1,
  version: 0, 
  startDateTime: moment("2018-07-01T09:00").toDate(),
  endDateTime: moment("2018-07-01T17:00").toDate(),
  spot: spot,
  employee: employee,
  rotationEmployee: {
    ...employee,
    id: 7,
    name: "Rotation Employee"
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
  contractMinutesViolationPenaltyList: []
};

const baseProps = {
  tenantId: 0,
  spotList: [spot],
  employeeList: [employee]
}