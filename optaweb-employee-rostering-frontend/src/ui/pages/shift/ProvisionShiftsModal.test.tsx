import { shallow } from 'enzyme';
import DatePicker from 'react-datepicker';
import * as React from 'react';
import { Spot } from 'domain/Spot';
import moment from 'moment';
import { useTranslation } from 'react-i18next';
import { AppState } from 'store/types';
import { spotSelectors } from 'store/spot';
import { timeBucketSelectors } from 'store/rotation';
import { TimeBucket } from 'domain/TimeBucket';
import { rosterSelectors } from 'store/roster';
import { RosterState } from 'domain/RosterState';
import { Modal, TextInput, Checkbox, AccordionToggle, AccordionContent } from '@patternfly/react-core';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';

import {
  ProvisionShiftsModal, ProvisionShiftsModalProps, SpotTimeBucketSelect,
  SpotTimeBucketSelectProps,
} from './ProvisionShiftsModal';

const mockSelectorReturnValue = new Map();
const mockDispatch = jest.fn();
const mockUseEffect = jest.fn();

jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useEffect: jest.fn((f, d) => mockUseEffect(f, d)),
}));

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn().mockImplementation(selector => mockSelectorReturnValue.get(selector)),
  useDispatch: jest.fn(() => mockDispatch),
}));

const mockProvision = (params: any) => ['provision', params];

jest.mock('store/roster', () => ({
  ...jest.requireActual('store/roster'),
  rosterOperations: {
    // For some reason, the mock constants above are not executed
    // before this
    provision: jest.fn(params => mockProvision(params)),
  },
}));

function mockSelector<T>(selector: (state: AppState) => T, value: T): void {
  mockSelectorReturnValue.set(selector, value);
}

describe('Provision Shifts Modal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSelectorReturnValue.clear();
    mockUseEffect.mockImplementationOnce(f => f());
    mockSelector(timeBucketSelectors.getTimeBucketList, [timeBucket]);
    mockSelector(spotSelectors.getSpotList, [spot]);
    mockSelector(rosterSelectors.getRosterState, rosterState);
  });

  it('should render correctly', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);
    expect(provisionShiftsModal).toMatchSnapshot();
  });

  it('should be closed when closed', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
      isOpen={false}
    />);
    expect(provisionShiftsModal.find(Modal).prop('isOpen')).toEqual(false);
  });

  it('should be open when open', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);
    expect(provisionShiftsModal.find(Modal).prop('isOpen')).toEqual(true);
  });

  it('should have an Accordian item for each selected spot', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);
    let spotTimeBucketSelect = provisionShiftsModal.find(SpotTimeBucketSelect);
    expect(spotTimeBucketSelect.prop('spot')).toEqual(spot);
    expect(spotTimeBucketSelect.prop('timeBucketList')).toEqual([timeBucket]);
    expect(spotTimeBucketSelect.prop('selectedTimeBucketList')).toEqual([timeBucket]);

    spotTimeBucketSelect.simulate('updateSelectedTimeBucketList', []);

    spotTimeBucketSelect = provisionShiftsModal.find(SpotTimeBucketSelect);
    expect(spotTimeBucketSelect.prop('timeBucketList')).toEqual([timeBucket]);
    expect(spotTimeBucketSelect.prop('selectedTimeBucketList')).toEqual([]);
  });

  it('editing start time should update start time', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal {...baseProps} />);

    let startDatePicker = provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=fromDate)"]');
    expect(startDatePicker.prop('selected')).toEqual(baseProps.defaultFromDate);

    const newDate = moment(baseProps.defaultFromDate).add(2, 'weeks').toDate();
    startDatePicker.simulate('change', newDate);

    startDatePicker = provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=fromDate)"]');
    expect(startDatePicker.prop('selected')).toEqual(newDate);
  });

  it('editing end time should update end time', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal {...baseProps} />);

    let endDatePicker = provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=toDate)"]');
    expect(endDatePicker.prop('selected')).toEqual(baseProps.defaultToDate);

    const newDate = moment(baseProps.defaultToDate).add(2, 'weeks').toDate();
    endDatePicker.simulate('change', newDate);

    endDatePicker = provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=toDate)"]');
    expect(endDatePicker.prop('selected')).toEqual(newDate);
  });

  it('editing startingFromRotationOffset should update startingFromRotationOffset', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal {...baseProps} />);

    let startingFromRotationOffset = provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]');
    expect(startingFromRotationOffset.prop('value')).toEqual(rosterState.unplannedRotationOffset);

    const newOffset = rosterState.unplannedRotationOffset + 2;
    startingFromRotationOffset.simulate('change', newOffset);

    startingFromRotationOffset = provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]');
    expect(startingFromRotationOffset.prop('value')).toEqual(newOffset);
  });

  it('editing startingFromRotationOffset should update startingFromRotationOffset', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal {...baseProps} />);

    let startingFromRotationOffset = provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]');
    expect(startingFromRotationOffset.prop('value')).toEqual(rosterState.unplannedRotationOffset);

    const newOffset = rosterState.unplannedRotationOffset + 2;
    startingFromRotationOffset.simulate('change', newOffset);

    startingFromRotationOffset = provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]');
    expect(startingFromRotationOffset.prop('value')).toEqual(newOffset);
  });

  it('editing spot select should edit selected spots', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal {...baseProps} />);
    let spotSelect = provisionShiftsModal.find(MultiTypeaheadSelectInput)
      .filter('[aria-label="Trans(i18nKey=forSpots)"]');

    expect(spotSelect.prop('value')).toEqual([spot]);
    expect(spotSelect.prop('options')).toEqual([spot]);

    let spotTimeBucketSelect = provisionShiftsModal.find(SpotTimeBucketSelect);
    expect(spotTimeBucketSelect.prop('spot')).toEqual(spot);

    spotSelect.simulate('change', []);

    spotSelect = provisionShiftsModal.find(MultiTypeaheadSelectInput).filter('[aria-label="Trans(i18nKey=forSpots)"]');
    expect(spotSelect.prop('value')).toEqual([]);
    expect(spotSelect.prop('options')).toEqual([spot]);

    spotTimeBucketSelect = provisionShiftsModal.find(SpotTimeBucketSelect);
    expect(spotTimeBucketSelect.exists()).toEqual(false);
  });

  it('should dispatch action on save', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);

    provisionShiftsModal.find(Modal).prop('actions')[1].props.onClick();
    expect(mockDispatch).toBeCalledWith(mockProvision({
      startRotationOffset: rosterState.unplannedRotationOffset,
      fromDate: baseProps.defaultFromDate,
      toDate: baseProps.defaultToDate,
      timeBucketList: [timeBucket],
    }));

    expect(baseProps.onClose).toBeCalled();
  });

  it('should close on close', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);

    provisionShiftsModal.find(Modal).prop('actions')[0].props.onClick();
    expect(mockDispatch).not.toBeCalled();
    expect(baseProps.onClose).toBeCalled();
  });

  it('should reset fields on reopen', () => {
    const provisionShiftsModal = shallow(<ProvisionShiftsModal
      {...baseProps}
    />);

    provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=fromDate)"]').simulate('change', null);
    provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=toDate)"]').simulate('change', null);
    provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]').simulate('change', 0);
    provisionShiftsModal.find(MultiTypeaheadSelectInput).filter('[aria-label="Trans(i18nKey=forSpots)"]')
      .simulate('change', []);

    // Need two more calls of useEffect to be mock
    mockUseEffect.mockImplementationOnce(f => f());
    mockUseEffect.mockImplementationOnce(f => f());
    provisionShiftsModal.setProps({ ...baseProps, isOpen: false });
    provisionShiftsModal.setProps({ ...baseProps, isOpen: true });

    provisionShiftsModal.update();

    expect(provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=fromDate)"]').prop('selected'))
      .toEqual(baseProps.defaultFromDate);
    expect(provisionShiftsModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=toDate)"]').prop('selected'))
      .toEqual(baseProps.defaultToDate);

    expect(provisionShiftsModal.find(TextInput)
      .filter('[aria-label="Trans(i18nKey=startingFromRotationOffset)"]').prop('value'))
      .toEqual(rosterState.unplannedRotationOffset);

    expect(provisionShiftsModal.find(MultiTypeaheadSelectInput).filter('[aria-label="Trans(i18nKey=forSpots)"]')
      .prop('value'))
      .toEqual([spot]);
  });
});

describe('SpotTimeBucketSelect', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly', () => {
    const spotTimeBucketSelect = shallow(<SpotTimeBucketSelect {...spotTimeBucketSelectProps} />);
    expect(spotTimeBucketSelect).toMatchSnapshot();
  });

  it(`should have global checkbox isChecked set to null if niether all timebuckets or no timebuckets
     are all selected/not selected`, () => {
    let spotTimeBucketSelect = shallow(<SpotTimeBucketSelect {...spotTimeBucketSelectProps} />);
    let checkbox = spotTimeBucketSelect.find(Checkbox).filter('[id="Spot-toggle-all"]');
    expect(checkbox.prop('isChecked')).toEqual(null);

    checkbox.simulate('change', true);

    expect(spotTimeBucketSelectProps.onUpdateSelectedTimeBucketList).toBeCalledWith([timeBucket, otherTimeBucket]);

    jest.clearAllMocks();
    spotTimeBucketSelect = shallow(<SpotTimeBucketSelect
      {...spotTimeBucketSelectProps}
      selectedTimeBucketList={[timeBucket, otherTimeBucket]}
    />);
    checkbox = spotTimeBucketSelect.find(Checkbox).filter('[id="Spot-toggle-all"]');
    expect(checkbox.prop('isChecked')).toEqual(true);

    checkbox.simulate('change', false);

    expect(spotTimeBucketSelectProps.onUpdateSelectedTimeBucketList).toBeCalledWith([]);

    jest.clearAllMocks();
    spotTimeBucketSelect = shallow(<SpotTimeBucketSelect
      {...spotTimeBucketSelectProps}
      selectedTimeBucketList={[]}
    />);
    checkbox = spotTimeBucketSelect.find(Checkbox).filter('[id="Spot-toggle-all"]');
    expect(checkbox.prop('isChecked')).toEqual(false);

    checkbox.simulate('change', true);

    expect(spotTimeBucketSelectProps.onUpdateSelectedTimeBucketList).toBeCalledWith([timeBucket, otherTimeBucket]);
  });

  it('clicking the AccordianToggle should toggle if it expanded', () => {
    const spotTimeBucketSelect = shallow(<SpotTimeBucketSelect {...spotTimeBucketSelectProps} />);
    const targetIsCurrentTargetEvent = { currentTarget: 0, target: 0 };
    const targetIsNotCurrentTargetEvent = { currentTarget: 0, target: 1 };

    let accordianToggle = spotTimeBucketSelect.find(AccordionToggle);
    let accordianContent = spotTimeBucketSelect.find(AccordionContent);
    expect(accordianToggle.prop('isExpanded')).toEqual(false);
    expect(accordianContent.prop('isHidden')).toEqual(true);
    accordianToggle.simulate('click', targetIsCurrentTargetEvent);

    accordianToggle = spotTimeBucketSelect.find(AccordionToggle);
    accordianContent = spotTimeBucketSelect.find(AccordionContent);
    expect(accordianToggle.prop('isExpanded')).toEqual(true);
    expect(accordianContent.prop('isHidden')).toEqual(false);
    accordianToggle.simulate('click', targetIsNotCurrentTargetEvent);

    accordianToggle = spotTimeBucketSelect.find(AccordionToggle);
    accordianContent = spotTimeBucketSelect.find(AccordionContent);
    expect(accordianToggle.prop('isExpanded')).toEqual(true);
    expect(accordianContent.prop('isHidden')).toEqual(false);
    accordianToggle.simulate('click', targetIsCurrentTargetEvent);

    accordianToggle = spotTimeBucketSelect.find(AccordionToggle);
    accordianContent = spotTimeBucketSelect.find(AccordionContent);
    expect(accordianToggle.prop('isExpanded')).toEqual(false);
    expect(accordianContent.prop('isHidden')).toEqual(true);
    accordianToggle.simulate('click', targetIsNotCurrentTargetEvent);

    accordianToggle = spotTimeBucketSelect.find(AccordionToggle);
    accordianContent = spotTimeBucketSelect.find(AccordionContent);
    expect(accordianToggle.prop('isExpanded')).toEqual(false);
    expect(accordianContent.prop('isHidden')).toEqual(true);
  });

  it('clicking on a selected timebucket checkbox should unselect it', () => {
    const spotTimeBucketSelect = shallow(<SpotTimeBucketSelect {...spotTimeBucketSelectProps} />);
    const checkbox = spotTimeBucketSelect.find(Checkbox).filter(`[id="timebucket-${timeBucket.id}-toggle"]`);
    expect(checkbox.prop('isChecked')).toEqual(true);
    checkbox.simulate('change', false);
    expect(spotTimeBucketSelectProps.onUpdateSelectedTimeBucketList).toBeCalledWith([]);
  });

  it('clicking on a unselected timebucket checkbox should select it', () => {
    const spotTimeBucketSelect = shallow(<SpotTimeBucketSelect {...spotTimeBucketSelectProps} />);
    const checkbox = spotTimeBucketSelect.find(Checkbox).filter(`[id="timebucket-${otherTimeBucket.id}-toggle"]`);
    expect(checkbox.prop('isChecked')).toEqual(false);
    checkbox.simulate('change', true);
    expect(spotTimeBucketSelectProps.onUpdateSelectedTimeBucketList).toBeCalledWith([timeBucket, otherTimeBucket]);
  });
});

const spot: Spot = {
  tenantId: 1,
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

const timeBucket: TimeBucket = {
  tenantId: 0,
  id: 22,
  spot,
  additionalSkillSet: [],
  repeatOnDaySetList: [],
  startTime: moment('09:00', 'HH:mm').toDate(),
  endTime: moment('17:00', 'HH:mm').toDate(),
  seatList: [],
};

const otherTimeBucket: TimeBucket = {
  ...timeBucket,
  id: 33,
  startTime: moment('17:00', 'HH:mm').toDate(),
  endTime: moment('05:00', 'HH:mm').toDate(),
};

const rosterState: RosterState = {
  publishNotice: 7,
  firstDraftDate: moment('2020-01-01').toDate(),
  lastHistoricDate: moment('2019-01-01').toDate(),
  publishLength: 7,
  draftLength: 7,
  rotationLength: 7,
  unplannedRotationOffset: 2,
  timeZone: 'UTC',
  tenant: {
    id: 0,
    name: 'Tenant',
  },
};

const baseProps: ProvisionShiftsModalProps = {
  ...useTranslation('ProvisionShiftsModal'),
  isOpen: true,
  onClose: jest.fn(),
  defaultFromDate: moment('2018-07-01').toDate(),
  defaultToDate: moment('2018-07-07').toDate(),
};

const spotTimeBucketSelectProps: SpotTimeBucketSelectProps = {
  spot,
  timeBucketList: [timeBucket, otherTimeBucket],
  selectedTimeBucketList: [timeBucket],
  onUpdateSelectedTimeBucketList: jest.fn(),
};
