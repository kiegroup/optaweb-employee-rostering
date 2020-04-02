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
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { ShiftTemplate } from 'domain/ShiftTemplate';
import moment from 'moment';
import { useTranslation, WithTranslation } from 'react-i18next';
import {
  EditShiftTemplateModal, Props, shiftTemplateDataToShiftTemplate,
  shiftTemplateToShiftTemplateData,
} from './EditShiftTemplateModal';

describe('Edit Shift Template Modal', () => {
  it('should render correctly when closed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen={false}
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftTemplateModal)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftTemplateModal)).toMatchSnapshot();
  });

  it('should render correctly with a shift template when opened', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    expect(toJson(editShiftTemplateModal)).toMatchSnapshot();
  });

  it('should update state to match shift template when props change', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editShiftTemplateModal.setProps({
      shiftTemplate: baseShiftTemplate,
    });
    expect(editShiftTemplateModal.state('editedValue')).toEqual(
      shiftTemplateToShiftTemplateData(baseShiftTemplate, rotationLength),
    );

    editShiftTemplateModal.setState({
      editedValue: shiftTemplateToShiftTemplateData({
        ...baseShiftTemplate,
        shiftTemplateDuration: moment.duration(1, 'h'),
      }, rotationLength),
    });

    expect(editShiftTemplateModal.state('editedValue')).toEqual(
      shiftTemplateToShiftTemplateData({
        ...baseShiftTemplate,
        shiftTemplateDuration: moment.duration(1, 'h'),
      }, rotationLength),
    );

    editShiftTemplateModal.setProps({
      shiftTemplate: undefined,
    });
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      tenantId: baseProps.tenantId,
    });
  });

  it('should call onSave iff all required properties are defined', () => {
    const onSave = jest.fn();
    const editShiftTemplateModal = new EditShiftTemplateModal({
      ...baseProps,
      onSave,
      onClose: jest.fn(),
      onDelete: jest.fn(),
      isOpen: false,
    });

    editShiftTemplateModal.state = {
      resetCount: 0,
      editedValue: {

      },
    };
    editShiftTemplateModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftTemplateModal.state = {
      resetCount: 1,
      editedValue: {
        rotationEmployee: null,
        spot,
      },
    };
    editShiftTemplateModal.onSave();
    expect(onSave).not.toBeCalled();

    editShiftTemplateModal.state = {
      resetCount: 2,
      editedValue: shiftTemplateToShiftTemplateData(baseShiftTemplate, rotationLength),
    };
    editShiftTemplateModal.onSave();
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith(baseShiftTemplate);
  });

  it('should call onSave when the save button is clicked with a completed shift', () => {
    const onSave = jest.fn();
    const editShiftTemplateModal = mount(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={onSave}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);

    editShiftTemplateModal.find('button[aria-label="Save"]').simulate('click');
    expect(onSave).toBeCalled();
    expect(onSave).toBeCalledWith(baseShiftTemplate);
  });

  it('should call onClose when the bottom close button is clicked', () => {
    const onClose = jest.fn();
    const editShiftTemplateModal = mount(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editShiftTemplateModal.find('[aria-label="Close Modal"]').last().simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onClose when the top right X close button is clicked', () => {
    const onClose = jest.fn();
    const editShiftTemplateModal = mount(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={onClose}
      onDelete={jest.fn()}
    />);

    editShiftTemplateModal.find('[aria-label="Close"]').last().simulate('click');
    expect(onClose).toBeCalled();
  });

  it('should call onDelete when the delete button is clicked', () => {
    const onDelete = jest.fn();
    const editShiftTemplateModal = mount(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={onDelete}
    />);

    editShiftTemplateModal.find('[aria-label="Delete"]').last().simulate('click');
    expect(onDelete).toBeCalled();
  });

  it('should prevent changing URL when completed', () => {
    const event = {
      preventDefault: jest.fn(),
    };
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      shiftTemplate={baseShiftTemplate}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('Form').simulate('submit', event);
    expect(event.preventDefault).toBeCalled();
  });

  it('should update the start day offset when the start day offset is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="Start Day Offset"]').simulate('change', 2);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      startDayOffset: 1,
    });
    editShiftTemplateModal.find('[aria-label="Start Day Offset"]').simulate('change', undefined);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      startDayOffset: undefined,
    });
  });

  it('should update the start time when the start time is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="Start Time"]').simulate('change', '13:30');
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      startTime: { hours: 13, minutes: 30 },
    });
  });

  it('should update the end day offset when the end day offset is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="End Day Offset"]').simulate('change', 4);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      endDayOffset: 3,
    });
    editShiftTemplateModal.find('[aria-label="End Day Offset"]').simulate('change', undefined);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      endDayOffset: undefined,
    });
  });

  it('should update the end time when the end time is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="End Time"]').simulate('change', '13:30');
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      endTime: { hours: 13, minutes: 30 },
    });
  });

  it('should update the spot when the spot is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="Spot"]').simulate('change', spot);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      spot,
    });
  });

  it('should update the employee when the employee is changed', () => {
    const editShiftTemplateModal = shallow(<EditShiftTemplateModal
      {...baseProps}
      isOpen
      onSave={jest.fn()}
      onClose={jest.fn()}
      onDelete={jest.fn()}
    />);
    editShiftTemplateModal.find('[aria-label="Employee"]').simulate('change', employee);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      rotationEmployee: employee,
    });

    editShiftTemplateModal.find('[aria-label="Employee"]').simulate('change', undefined);
    expect(editShiftTemplateModal.state('editedValue')).toEqual({
      rotationEmployee: null,
    });
  });

  it('shiftTemplateDataToShiftTemplate should work as expected', () => {
    expect(shiftTemplateDataToShiftTemplate({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: employee,
      startDayOffset: 2,
      startTime: { hours: 9, minutes: 0 },
      endDayOffset: 3,
      endTime: { hours: 9, minutes: 0 },
    }, rotationLength)).toEqual({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: employee,
      durationBetweenRotationStartAndTemplateStart: moment.duration(2, 'd').add(9, 'h'),
      shiftTemplateDuration: moment.duration(1, 'd'),
    });

    expect(shiftTemplateDataToShiftTemplate({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: null,
      startDayOffset: rotationLength - 1,
      startTime: { hours: 9, minutes: 0 },
      endDayOffset: 0,
      endTime: { hours: 9, minutes: 0 },
    }, rotationLength)).toEqual({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration(rotationLength - 1, 'd').add(9, 'h'),
      shiftTemplateDuration: moment.duration(1, 'd'),
    });
  });

  it('shiftTemplateToShiftTemplateData should work as expected', () => {
    expect(shiftTemplateToShiftTemplateData(baseShiftTemplate, rotationLength)).toEqual({
      tenantId: 0,
      id: 1,
      version: 0,
      spot,
      requiredSkillSet: [],
      rotationEmployee: employee,
      startDayOffset: 1,
      startTime: { hours: 9, minutes: 0 },
      endDayOffset: 1,
      endTime: { hours: 17, minutes: 0 },
    });

    expect(shiftTemplateToShiftTemplateData({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration(rotationLength - 1, 'days').add(9, 'hours'),
      shiftTemplateDuration: moment.duration(24, 'hours'),
    }, rotationLength)).toEqual({
      tenantId: 0,
      id: 1,
      version: 2,
      spot,
      requiredSkillSet: [],
      rotationEmployee: null,
      startDayOffset: rotationLength - 1,
      startTime: { hours: 9, minutes: 0 },
      endDayOffset: 0,
      endTime: { hours: 9, minutes: 0 },
    });
  });
});

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
  covidWard: false,
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
  covidRiskType: 'INOCULATED',
};

const baseShiftTemplate: ShiftTemplate = {
  tenantId: 0,
  id: 1,
  version: 0,
  durationBetweenRotationStartAndTemplateStart: moment.duration(1, 'd').add(9, 'h'),
  shiftTemplateDuration: moment.duration(8, 'h'),
  spot,
  requiredSkillSet: [],
  rotationEmployee: employee,
};

const rotationLength = 24;

const baseProps: Props & WithTranslation = {
  ...useTranslation(),
  tReady: true,
  tenantId: 0,
  isOpen: true,
  spotList: [spot],
  employeeList: [employee],
  rotationLength,
  onSave: jest.fn(),
  onDelete: jest.fn(),
  onClose: jest.fn(),
};
