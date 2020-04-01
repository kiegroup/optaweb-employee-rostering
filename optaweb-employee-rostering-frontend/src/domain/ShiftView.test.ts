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
import { Shift } from './Shift';
import { shiftToShiftView, shiftViewToDomainObjectView, ShiftView } from './ShiftView';
import DomainObjectView from './DomainObjectView';

describe('ShiftView operations', () => {
  it('shiftToShiftView should convert a Shift to a ShiftView', () => {
    const startDateTime: Date = new Date('2019-07-16T16:00');
    const endDateTime: Date = new Date('2019-07-17T01:00');

    const shift: Shift = {
      tenantId: 0,
      id: 10,
      version: 2,
      startDateTime,
      endDateTime,
      spot: {
        tenantId: 0,
        id: 5,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: {
        tenantId: 0,
        id: 3,
        version: 0,
        name: 'Employee 1',
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract',
          maximumMinutesPerDay: null,
          maximumMinutesPerMonth: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerYear: null,

        },
        skillProficiencySet: [],
        covidRiskType: 'INOCULATED',
      },
      rotationEmployee: {
        tenantId: 0,
        id: 11,
        version: 0,
        name: 'Employee 2',
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract',
          maximumMinutesPerDay: null,
          maximumMinutesPerMonth: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerYear: null,

        },
        skillProficiencySet: [],
        covidRiskType: 'INOCULATED',
      },
      pinnedByUser: true,
    };

    const expectedShiftView: ShiftView = {
      tenantId: 0,
      id: 10,
      version: 2,
      startDateTime,
      endDateTime,
      spotId: 5,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: 3,
      rotationEmployeeId: 11,
      pinnedByUser: true,
    };

    let actualShiftView = shiftToShiftView(shift);
    expect(actualShiftView).toEqual(expectedShiftView);

    shift.employee = null;
    expectedShiftView.employeeId = null;
    actualShiftView = shiftToShiftView(shift);
    expect(actualShiftView).toEqual(expectedShiftView);

    shift.rotationEmployee = null;
    expectedShiftView.rotationEmployeeId = null;
    actualShiftView = shiftToShiftView(shift);
    expect(actualShiftView).toEqual(expectedShiftView);
  });

  it('shiftViewToDomainObjectView should convert a ShiftView to a DomainObjectView of Shift', () => {
    const startDateTime: Date = new Date('2019-07-16T16:00');
    const endDateTime: Date = new Date('2019-07-17T01:00');

    const shiftView = {
      tenantId: 0,
      id: 10,
      version: 2,
      startDateTime,
      endDateTime,
      spotId: 5,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: 3,
      rotationEmployeeId: 11,
      pinnedByUser: true,
    };

    const expectedDomainObjectViewOfShift: DomainObjectView<Shift> = {
      tenantId: 0,
      id: 10,
      version: 2,
      startDateTime,
      endDateTime,
      spot: 5,
      requiredSkillSet: [],
      originalEmployee: null,
      employee: 3,
      rotationEmployee: 11,
      pinnedByUser: true,
    };

    const actualDomainObjectViewOfShift = shiftViewToDomainObjectView(shiftView);
    expect(actualDomainObjectViewOfShift).toEqual(expectedDomainObjectViewOfShift);
  });
});
