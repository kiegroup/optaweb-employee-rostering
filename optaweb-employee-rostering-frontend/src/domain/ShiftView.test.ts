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
        shortId: 'e1',
        color: '#000000',
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
        shortId: 'e2',
        color: '#000000',
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
