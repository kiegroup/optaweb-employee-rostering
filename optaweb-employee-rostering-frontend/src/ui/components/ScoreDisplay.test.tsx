import { shallow } from 'enzyme';
import * as React from 'react';
import { HardMediumSoftScore } from 'domain/HardMediumSoftScore';
import { IndictmentSummary } from 'domain/indictment/IndictmentSummary';
import { Button, Popover } from '@patternfly/react-core';
import {
  ExclamationTriangleIcon, AlignCenterIcon, UserFriendsIcon, SchoolIcon,
  FileContractIcon, UserTimesIcon, UserMinusIcon, UserPlusIcon, PhoneVolumeIcon,
  CalendarTimesIcon,
} from '@patternfly/react-icons';
import { ScoreDisplay, IndictmentIcon, ConstraintMatches } from './ScoreDisplay';

describe('ScoreDisplay component', () => {
  it('should renderCorrectly when closed', () => {
    const score: HardMediumSoftScore = {
      hardScore: -10,
      mediumScore: -5,
      softScore: 20,
    };

    const scoreDisplay = shallow(<ScoreDisplay score={score} indictmentSummary={indictmentSummary} isSolving />);
    expect(scoreDisplay).toMatchSnapshot();
  });

  it('should renderCorrectly when open', () => {
    const score: HardMediumSoftScore = {
      hardScore: -10,
      mediumScore: -5,
      softScore: 20,
    };

    const scoreDisplay = shallow(<ScoreDisplay score={score} indictmentSummary={indictmentSummary} isSolving />);
    scoreDisplay.find(Button).simulate('click');
    expect(scoreDisplay).toMatchSnapshot();
  });

  it('should close when requested', () => {
    const score: HardMediumSoftScore = {
      hardScore: -10,
      mediumScore: -5,
      softScore: 20,
    };

    const scoreDisplay = shallow(<ScoreDisplay score={score} indictmentSummary={indictmentSummary} isSolving />);
    scoreDisplay.find(Button).simulate('click');
    expect(scoreDisplay.find(Popover).prop('isVisible')).toEqual(true);
    (scoreDisplay.find(Popover).prop('shouldClose') as Function)();
    expect(scoreDisplay.find(Popover).prop('isVisible')).toEqual(false);
  });
});

describe('IndictmentIcon component', () => {
  it('should render the correct icon for each constraint', () => {
    const constraintToIcon: any = {
      'Assign every shift': <ExclamationTriangleIcon />,
      'No more than 2 consecutive shifts': <AlignCenterIcon />,
      'No overlapping shifts': <UserFriendsIcon />,
      'Required skill for a shift': <SchoolIcon />,
      'Break between non-consecutive shifts is at least 10 hours': <UserFriendsIcon />,
      'Daily minutes must not exceed contract maximum': <FileContractIcon />,
      'Weekly minutes must not exceed contract maximum': <FileContractIcon />,
      'Monthly minutes must not exceed contract maximum': <FileContractIcon />,
      'Yearly minutes must not exceed contract maximum': <FileContractIcon />,
      'Unavailable time slot for an employee': <UserTimesIcon />,
      'Undesired time slot for an employee': <UserMinusIcon />,
      'Desired time slot for an employee': <UserPlusIcon />,
      'Employee is not original employee': <PhoneVolumeIcon />,
      'Employee is not rotation employee': <CalendarTimesIcon />,
    };

    Object.keys(constraintToIcon).forEach((constraint) => {
      const indictmentIcon = shallow(<IndictmentIcon indictment={constraint} />);
      expect(indictmentIcon.containsMatchingElement(constraintToIcon[constraint])).toEqual(true);
    });
  });
});

describe('ConstraintMatches component', () => {
  it('should display each constraint with impact', () => {
    const constraintMatches = shallow(<ConstraintMatches indictmentSummary={indictmentSummary} />);
    expect(constraintMatches).toMatchSnapshot();
  });
});

const indictmentSummary: IndictmentSummary = {
  constraintToCountMap: {
    'Assign every shift': 0,
    'No more than 2 consecutive shifts': 1,
    'No overlapping shifts': 2,
    'Required skill for a shift': 3,
    'Break between non-consecutive shifts is at least 10 hours': 4,
    'Daily minutes must not exceed contract maximum': 5,
    'Weekly minutes must not exceed contract maximum': 6,
    'Monthly minutes must not exceed contract maximum': 7,
    'Yearly minutes must not exceed contract maximum': 8,
    'Unavailable time slot for an employee': 9,
    'Undesired time slot for an employee': 10,
    'Desired time slot for an employee': 11,
    'Employee is not original employee': 12,
    'Employee is not rotation employee': 13,
  },
  constraintToScoreImpactMap: {
    'Assign every shift': { hardScore: 0, mediumScore: 0, softScore: 0 },
    'No more than 2 consecutive shifts': { hardScore: 1, mediumScore: 0, softScore: 0 },
    'No overlapping shifts': { hardScore: 0, mediumScore: 0, softScore: 2 },
    'Required skill for a shift': { hardScore: 3, mediumScore: 3, softScore: 0 },
    'Break between non-consecutive shifts is at least 10 hours': { hardScore: 0, mediumScore: 4, softScore: 4 },
    'Daily minutes must not exceed contract maximum': { hardScore: 5, mediumScore: 0, softScore: 5 },
    'Weekly minutes must not exceed contract maximum': { hardScore: 6, mediumScore: 6, softScore: 6 },
    'Monthly minutes must not exceed contract maximum': { hardScore: 7, mediumScore: 0, softScore: 0 },
    'Yearly minutes must not exceed contract maximum': { hardScore: 8, mediumScore: 0, softScore: 3 },
    'Unavailable time slot for an employee': { hardScore: 9, mediumScore: 0, softScore: 0 },
    'Undesired time slot for an employee': { hardScore: 10, mediumScore: 0, softScore: 0 },
    'Desired time slot for an employee': { hardScore: 11, mediumScore: 0, softScore: 0 },
    'Employee is not original employee': { hardScore: 12, mediumScore: 0, softScore: 0 },
    'Employee is not rotation employee': { hardScore: 13, mediumScore: 0, softScore: 0 },
  },
};
