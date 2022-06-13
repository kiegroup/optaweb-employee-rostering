import React from 'react';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { RosterState } from 'domain/RosterState';
import { AppState } from 'store/types';
import { rosterSelectors } from 'store/roster';
import { shallow } from 'enzyme';
import { TimeBucket } from 'domain/TimeBucket';
import moment from 'moment';
import { Button, Flex } from '@patternfly/react-core';
import { EditIcon, TrashIcon } from '@patternfly/react-icons';
import { SeatJigsaw, SeatJigsawProps } from './SeatJigsaw';
import { EditTimeBucketModal } from './EditTimeBucketModal';

const mockSelectorReturnValue = new Map();
jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn().mockImplementation(selector => mockSelectorReturnValue.get(selector)),
}));

function mockSelector<T>(selector: (state: AppState) => T, value: T): void {
  mockSelectorReturnValue.set(selector, value);
}

describe('SeatJigsaw Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSelector(rosterSelectors.getRosterState, rosterState);
  });

  it('It should render correctly', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);
    expect(seatJigsaw).toMatchSnapshot();
  });

  it('should open the EditTimeBucketModal on edit', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    expect(seatJigsaw.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
    seatJigsaw.find(Button).filterWhere(wr => wr.contains(<EditIcon />)).simulate('click');
    expect(seatJigsaw.find(EditTimeBucketModal).prop('isOpen')).toEqual(true);

    seatJigsaw.find(EditTimeBucketModal).simulate('close');
    expect(seatJigsaw.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
  });

  it('should call updateTimeBucket on EditTimeBucketModal update', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    const updatedTimeBucket = { ...timeBucket, version: 20, seatList: [] };
    seatJigsaw.find(EditTimeBucketModal).simulate('updateTimeBucket', updatedTimeBucket);

    expect(baseProps.onUpdateTimeBucket).toBeCalledWith(updatedTimeBucket);
  });


  it('should call onDeleteTimeBucket on delete', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    seatJigsaw.find(Button).filterWhere(wr => wr.contains(<TrashIcon />)).simulate('click');
    expect(baseProps.onDeleteTimeBucket).toBeCalled();
  });

  it('should set employee in seat on click', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    seatJigsaw.find('[type="button"]').last().simulate('click');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee }],
    });
  });

  it('should set employee in seat on mouseDown', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    seatJigsaw.find('[type="button"]').last().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee }],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').last().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee }],
    });
  });

  it('should set employee in seat on mouseMove with left button down', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee }],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee }],
    });
  });

  it('should erase employee in seat on click if stub is no_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="NO_SHIFT" />);
    seatJigsaw.find('[type="button"]').first().simulate('click');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [],
    });
  });

  it('should erase employee in seat on mouseDown if stub is no_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="NO_SHIFT" />);

    seatJigsaw.find('[type="button"]').first().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').first().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [],
    });
  });

  it('should erase employee in seat on mouseMove with left button down if stub is no_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="NO_SHIFT" />);

    seatJigsaw.find('[type="button"]').first().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').first().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [],
    });
  });

  it('should erase employee in seat on click if stub is unassigned_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="SHIFT_WITH_NO_EMPLOYEE" />);

    seatJigsaw.find('[type="button"]').last().simulate('click');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee: null }],
    });
  });

  it('should erase employee in seat on mouseDown if stub is unassigned_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="SHIFT_WITH_NO_EMPLOYEE" />);

    seatJigsaw.find('[type="button"]').last().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee: null }],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').last().simulate('mouseDown');
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee: null }],
    });
  });

  it('should erase employee in seat on mouseMove with left button down if stub is unassigned_shift', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} selectedStub="SHIFT_WITH_NO_EMPLOYEE" />);

    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee: null }],
    });
    jest.clearAllMocks();
    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 1 });
    seatJigsaw.find(Flex).simulate('mouseUp');
    expect(baseProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      seatList: [...timeBucket.seatList, { dayInRotation: 6, employee: null }],
    });
  });


  it('should not set employee in seat on mouseMove without left button down', () => {
    const seatJigsaw = shallow(<SeatJigsaw {...baseProps} />);

    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 2 });
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).not.toBeCalled();

    seatJigsaw.find('[type="button"]').last().simulate('mouseMove', { buttons: 0 });
    seatJigsaw.find(Flex).simulate('mouseLeave');
    expect(baseProps.onUpdateTimeBucket).not.toBeCalled();
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: 'Spot',
  requiredSkillSet: [],
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
  skillProficiencySet: [],
  shortId: 'e1',
  color: '#FFFFFF',
};

const rosterState: RosterState = {
  tenant: {
    id: 0,
    version: 0,
    name: 'Tenant',
  },
  publishNotice: 14,
  publishLength: 7,
  firstDraftDate: new Date('2018-07-01'),
  draftLength: 7,
  unplannedRotationOffset: 0,
  rotationLength: 7,
  lastHistoricDate: new Date('2018-07-01'),
  timeZone: 'EST',
};

const timeBucket: TimeBucket = {
  tenantId: 0,
  id: 22,
  spot,
  additionalSkillSet: [{ tenantId: 0, id: 999, version: 0, name: 'Skill' }],
  repeatOnDaySetList: [],
  startTime: moment('09:00', 'HH:mm').toDate(),
  endTime: moment('17:00', 'HH:mm').toDate(),
  seatList: [{ dayInRotation: 0, employee }],
};

const baseProps: SeatJigsawProps = {
  selectedStub: employee,
  timeBucket,
  onUpdateTimeBucket: jest.fn(),
  onDeleteTimeBucket: jest.fn(),
};
