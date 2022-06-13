import React from 'react';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
import { skillSelectors } from 'store/skill';
import { shallow } from 'enzyme';
import { TimeBucket } from 'domain/TimeBucket';
import moment from 'moment';
import { Modal, TextInput, FlexItem, Text } from '@patternfly/react-core';
import { Skill } from 'domain/Skill';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import {
  EditTimeBucketModal, TimeBucketEditor,
  EditTimeBucketModalProps, TimeBucketEditorProps,
} from './EditTimeBucketModal';

const mockSelectorReturnValue = new Map();
jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn().mockImplementation(selector => mockSelectorReturnValue.get(selector)),
}));

function mockSelector<T>(selector: (state: AppState) => T, value: T): void {
  mockSelectorReturnValue.set(selector, value);
}

describe('EditTimeBucketModal Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('It should render correctly when open', () => {
    const editTimeBucketModal = shallow(<EditTimeBucketModal {...editTimeBucketModalProps} />);

    expect(editTimeBucketModal.find(Modal).prop('isOpen')).toEqual(true);
    expect(editTimeBucketModal).toMatchSnapshot();
  });

  it('It should render correctly when closed', () => {
    const editTimeBucketModal = shallow(<EditTimeBucketModal {...editTimeBucketModalProps} isOpen={false} />);

    expect(editTimeBucketModal.find(Modal).prop('isOpen')).toEqual(false);
  });

  it('It should call onClose when the modal is closed', () => {
    const editTimeBucketModal = shallow(<EditTimeBucketModal {...editTimeBucketModalProps} />);

    editTimeBucketModal.find(Modal).simulate('close');

    expect(editTimeBucketModalProps.onClose).toBeCalled();
    expect(editTimeBucketModalProps.onUpdateTimeBucket).not.toBeCalled();
  });

  it('It should call onClose when the cancel button is clicked', () => {
    const editTimeBucketModal = shallow(<EditTimeBucketModal {...editTimeBucketModalProps} />);

    // shallow is not working on the prop actions
    editTimeBucketModal.find(Modal).prop('actions')[0].props.onClick();

    expect(editTimeBucketModalProps.onClose).toBeCalled();
    expect(editTimeBucketModalProps.onUpdateTimeBucket).not.toBeCalled();
  });

  it('It should call onUpdate with updatedTimeBucket when the save button is clicked', () => {
    const editTimeBucketModal = shallow(<EditTimeBucketModal {...editTimeBucketModalProps} />);
    const updatedTimeBucket = { ...timeBucket, version: 111, seatList: [] };

    editTimeBucketModal.find(TimeBucketEditor).simulate('updateTimeBucket', updatedTimeBucket);

    // shallow is not working on the prop actions
    editTimeBucketModal.find(Modal).prop('actions')[1].props.onClick();

    expect(editTimeBucketModalProps.onClose).toBeCalled();
    expect(editTimeBucketModalProps.onUpdateTimeBucket).toBeCalledWith(updatedTimeBucket);
  });
});

describe('TimeBucketEditor Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSelector(skillSelectors.getSkillList, skillList);
  });

  it('should render correctly', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);
    expect(timeBucketEditor).toMatchSnapshot();
  });

  it('editing start time should update start time', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);
    const START_TIME = '13:00';

    timeBucketEditor.find(TextInput).filter('[aria-label="Start Time"]').simulate('change', START_TIME);
    expect(timeBucketEditorProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      startTime: moment(START_TIME, 'HH:mm').toDate(),
    });
  });

  it('editing end time should update end time', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);
    const END_TIME = '21:00';

    timeBucketEditor.find(TextInput).filter('[aria-label="End Time"]').simulate('change', END_TIME);
    expect(timeBucketEditorProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      endTime: moment(END_TIME, 'HH:mm').toDate(),
    });
  });

  it('editing skill list should update skill list', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);

    timeBucketEditor.find(MultiTypeaheadSelectInput).simulate('change', skillList);
    expect(timeBucketEditorProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      additionalSkillSet: skillList,
    });
  });

  it('adding day should add it to repeatOnDaySet', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);

    // last() to skip parent, which is also a FlexItem
    timeBucketEditor.find(FlexItem).filterWhere(item => item.containsMatchingElement(<Text>W</Text>))
      .last().simulate('click');
    expect(timeBucketEditorProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      repeatOnDaySetList: [...timeBucket.repeatOnDaySetList, 'WEDNESDAY'],
    });
  });

  it('removing day should remove it from repeatOnDaySet', () => {
    const timeBucketEditor = shallow(<TimeBucketEditor {...timeBucketEditorProps} />);

    // last() to skip parent, which is also a FlexItem
    timeBucketEditor.find(FlexItem).filterWhere(item => item.containsMatchingElement(<Text>M</Text>))
      .last().simulate('click');
    expect(timeBucketEditorProps.onUpdateTimeBucket).toBeCalledWith({
      ...timeBucket,
      repeatOnDaySetList: ['TUESDAY'],
    });
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

const timeBucket: TimeBucket = {
  tenantId: 0,
  id: 22,
  spot,
  additionalSkillSet: [{ tenantId: 0, id: 999, version: 0, name: 'Skill' }],
  repeatOnDaySetList: ['MONDAY', 'TUESDAY'],
  startTime: moment('09:00', 'HH:mm').toDate(),
  endTime: moment('17:00', 'HH:mm').toDate(),
  seatList: [{ dayInRotation: 0, employee }],
};

const skillList: Skill[] = [
  {
    tenantId: 0,
    id: 1000,
    version: 0,
    name: 'Skill 1',
  },
  {
    tenantId: 0,
    id: 2000,
    version: 0,
    name: 'Skill 2',
  },
];

const editTimeBucketModalProps: EditTimeBucketModalProps = {
  isOpen: true,
  timeBucket,
  onUpdateTimeBucket: jest.fn(),
  onClose: jest.fn(),
};

const timeBucketEditorProps: TimeBucketEditorProps = {
  name: 'Title',
  timeBucket,
  onUpdateTimeBucket: jest.fn(),
};
