import React, { useState } from 'react';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { RosterState } from 'domain/RosterState';
import { tenantSelectors } from 'store/tenant';
import { AppState } from 'store/types';
import { rosterSelectors } from 'store/roster';
import { timeBucketSelectors } from 'store/rotation';
import { spotSelectors } from 'store/spot';
import { shallow } from 'enzyme';
import { TimeBucket } from 'domain/TimeBucket';
import moment from 'moment';
import { Trans } from 'react-i18next';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { Button } from '@patternfly/react-core';
import { SeatJigsaw } from './SeatJigsaw';
import { EmployeeStubList, Stub } from './EmployeeStub';
import { EditTimeBucketModal } from './EditTimeBucketModal';
import { RotationPage } from './RotationPage';

const mockSelectorReturnValue = new Map();
const mockUrlState = new Map();
const mockHistory = jest.fn();
const mockDispatch = jest.fn();
const mockUseState = useState;

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn().mockImplementation(selector => mockSelectorReturnValue.get(selector)),
  useDispatch: jest.fn().mockImplementation(() => mockDispatch),
}));

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useHistory: jest.fn().mockImplementation(() => ({ push: mockHistory })),
}));

jest.mock('util/FunctionalComponentUtils', () => ({
  ...jest.requireActual('util/FunctionalComponentUtils'),
  useUrlState: jest.fn().mockImplementation((name, initialValue) => {
    const [state, setState] = mockUseState(mockUrlState.get(name) || initialValue);

    if (!mockUrlState.has(name)) {
      mockUrl(name, initialValue);
    }

    return [
      state,
      (value: string) => { mockUrl(name, value); setState(value); },
    ];
  }),
}));

const mockAddTimeBucket = (tb: any) => ['addTimeBucket', tb];
const mockRemoveTimeBucket = (tb: any) => ['removeTimeBucket', tb];
const mockUpdateTimeBucket = (tb: any) => ['updateTimeBucket', tb];

jest.mock('store/rotation', () => ({
  ...jest.requireActual('store/rotation'),
  timeBucketOperations: {
    // For some reason, the mock constants above are not executed
    // before this
    addTimeBucket: jest.fn(tb => mockAddTimeBucket(tb)),
    removeTimeBucket: jest.fn(tb => mockRemoveTimeBucket(tb)),
    updateTimeBucket: jest.fn(tb => mockUpdateTimeBucket(tb)),
  },
}));

function mockSelector<T>(selector: (state: AppState) => T, value: T): void {
  mockSelectorReturnValue.set(selector, value);
}

function mockUrl(name: string, value: string) {
  mockUrlState.set(name, value);
}

describe('Rotation Page', () => {
  beforeAll(() => jest.spyOn(React, 'useEffect').mockImplementation(React.useLayoutEffect));
  afterAll(() => (React.useEffect as any).mockRestore());

  beforeEach(() => {
    jest.clearAllMocks();
    mockSelectorReturnValue.clear();
    mockUrlState.clear();

    mockSelector(tenantSelectors.getTenantId, 0);
    mockSelector(rosterSelectors.getRosterState, rosterState);
    mockSelector(timeBucketSelectors.isLoading, false);
    mockSelector(spotSelectors.getSpotList, [spot, newSpot]);
    mockSelector(timeBucketSelectors.getTimeBucketList, [timeBucket]);
  });

  it('It should render empty state if loading', () => {
    mockSelector(timeBucketSelectors.isLoading, true);
    const rotationPage = shallow(<RotationPage />);
    (rotationPage.find(Trans).prop('components') as any[])[2].props.onClick();

    expect(mockHistory).toBeCalledWith('/0/spots');
    expect(rotationPage).toMatchSnapshot();
  });

  it('It should render correctly', () => {
    const rotationPage = shallow(<RotationPage />);
    expect(mockUrlState.get('spot')).toEqual(spot.name);
    expect(rotationPage.find(TypeaheadSelectInput).prop('value')).toEqual(spot);
    expect(rotationPage.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(EmployeeStubList).prop('stubList')).toEqual([employee]);
    expect(rotationPage.find(SeatJigsaw).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(SeatJigsaw).prop('timeBucket')).toEqual(timeBucket);
    expect(rotationPage).toMatchSnapshot();
  });

  it('should use spot in url', () => {
    mockUrl('spot', 'New Spot');
    const rotationPage = shallow(<RotationPage />);
    expect(rotationPage.find(TypeaheadSelectInput).prop('value')).toEqual(newSpot);
    expect(rotationPage.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(EmployeeStubList).prop('stubList')).toEqual([]);
    expect(rotationPage.find(SeatJigsaw).exists()).toEqual(false);
  });

  it('should set spot in url', () => {
    const rotationPage = shallow(<RotationPage />);
    expect(rotationPage.find(TypeaheadSelectInput).prop('value')).toEqual(spot);
    expect(rotationPage.find(EmployeeStubList).prop('stubList')).toEqual([employee]);
    expect(rotationPage.find(SeatJigsaw).prop('timeBucket')).toEqual(timeBucket);

    rotationPage.find(TypeaheadSelectInput).prop('onChange')(newSpot);

    expect(mockUrlState.get('spot')).toEqual(newSpot.name);
    expect(rotationPage.find(TypeaheadSelectInput).prop('value')).toEqual(newSpot);
    expect(rotationPage.find(SeatJigsaw).exists()).toEqual(false);
  });

  it('should update stub when stub changed', () => {
    const rotationPage = shallow(<RotationPage />);
    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(SeatJigsaw).prop('selectedStub')).toEqual('NO_SHIFT');

    const myStub: Stub = employee;
    rotationPage.find(EmployeeStubList).prop('onStubSelect')(myStub);

    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual(myStub);
    expect(rotationPage.find(SeatJigsaw).prop('selectedStub')).toEqual(myStub);

    rotationPage.find(EmployeeStubList).prop('onStubSelect')('NO_SHIFT');

    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(SeatJigsaw).prop('selectedStub')).toEqual('NO_SHIFT');
  });

  it('should update stub list when stub list changed', () => {
    const rotationPage = shallow(<RotationPage />);
    expect(rotationPage.find(EmployeeStubList).prop('selectedStub')).toEqual('NO_SHIFT');
    expect(rotationPage.find(SeatJigsaw).prop('selectedStub')).toEqual('NO_SHIFT');

    const otherEmployee: Employee = {
      ...employee,
      id: 9012,
      name: 'Other',
      shortId: 'O',
      color: '#000000',
    };
    const myStubList: Stub[] = [employee, otherEmployee];
    rotationPage.find(EmployeeStubList).simulate('updateStubList', myStubList);

    expect(rotationPage.find(EmployeeStubList).prop('stubList')).toEqual(myStubList);
  });

  it('should dispatch update time bucket on update time bucket', () => {
    const rotationPage = shallow(<RotationPage />);
    const updatedTimeBucket = { ...timeBucket, id: 999 };
    rotationPage.find(SeatJigsaw).simulate('updateTimeBucket', updatedTimeBucket);

    expect(mockDispatch).toBeCalledWith(mockUpdateTimeBucket(updatedTimeBucket));
  });

  it('should dispatch delete time bucket on delete time bucket', () => {
    const rotationPage = shallow(<RotationPage />);
    rotationPage.find(SeatJigsaw).simulate('deleteTimeBucket');

    expect(mockDispatch).toBeCalledWith(mockRemoveTimeBucket(timeBucket));
  });

  it('should open the add new time bucket modal on click', () => {
    const rotationPage = shallow(<RotationPage />);

    expect(rotationPage.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
    rotationPage.find(Button).filter('[aria-label="Add New Time Bucket"]').simulate('click');
    expect(rotationPage.find(EditTimeBucketModal).prop('isOpen')).toEqual(true);

    rotationPage.find(EditTimeBucketModal).simulate('close');
    expect(rotationPage.find(EditTimeBucketModal).prop('isOpen')).toEqual(false);
  });

  it('should add the time bucket on time bucket modal update', () => {
    const rotationPage = shallow(<RotationPage />);
    rotationPage.find(EditTimeBucketModal).simulate('updateTimeBucket', timeBucket);

    expect(mockDispatch).toBeCalledWith(mockAddTimeBucket(timeBucket));
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: 'Spot',
  requiredSkillSet: [],
};

const newSpot: Spot = {
  ...spot,
  id: 111,
  name: 'New Spot',
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
  additionalSkillSet: [],
  repeatOnDaySetList: [],
  startTime: moment('09:00', 'HH:mm').toDate(),
  endTime: moment('17:00', 'HH:mm').toDate(),
  seatList: [{ dayInRotation: 0, employee }],
};
