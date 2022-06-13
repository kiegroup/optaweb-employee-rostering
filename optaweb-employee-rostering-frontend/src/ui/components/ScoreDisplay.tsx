import React from 'react';
import { useTranslation } from 'react-i18next';
import { HardMediumSoftScore, convertHardMediumSoftScoreToString } from 'domain/HardMediumSoftScore';
import { Chip, Button, ButtonVariant, Popover, List } from '@patternfly/react-core';
import { IndictmentSummary } from 'domain/indictment/IndictmentSummary';
import {
  HelpIcon, ExclamationTriangleIcon, UserFriendsIcon, FileContractIcon, UserPlusIcon, PhoneVolumeIcon,
  CalendarTimesIcon, AlignCenterIcon, SchoolIcon, UserTimesIcon, UserMinusIcon,
} from '@patternfly/react-icons';

export interface ScoreDisplayProps {
  score: HardMediumSoftScore;
  indictmentSummary: IndictmentSummary;
  isSolving: boolean;
}

export const IndictmentIcon: React.FC<{ indictment: string }> = (props) => {
  switch (props.indictment) {
    case 'Assign every shift':
      return (<ExclamationTriangleIcon />);

    case 'No more than 2 consecutive shifts':
      return (<AlignCenterIcon />);
    case 'No overlapping shifts':
      return (<UserFriendsIcon />);
    case 'Required skill for a shift':
      return (<SchoolIcon />);
    case 'Break between non-consecutive shifts is at least 10 hours':
      return (<UserFriendsIcon />);

    case 'Daily minutes must not exceed contract maximum':
      return (<FileContractIcon />);
    case 'Weekly minutes must not exceed contract maximum':
      return (<FileContractIcon />);
    case 'Monthly minutes must not exceed contract maximum':
      return (<FileContractIcon />);
    case 'Yearly minutes must not exceed contract maximum':
      return (<FileContractIcon />);

    case 'Unavailable time slot for an employee':
      return (<UserTimesIcon />);
    case 'Undesired time slot for an employee':
      return (<UserMinusIcon />);
    case 'Desired time slot for an employee':
      return (<UserPlusIcon />);

    case 'Employee is not original employee':
      return (<PhoneVolumeIcon />);
    case 'Employee is not rotation employee':
      return (<CalendarTimesIcon />);

    default:
      return (<span />);
  }
};

const CONSTRAINTS = ['Assign every shift',
  'No more than 2 consecutive shifts',
  'No overlapping shifts',
  'Required skill for a shift',
  'Break between non-consecutive shifts is at least 10 hours',
  'Daily minutes must not exceed contract maximum',
  'Weekly minutes must not exceed contract maximum',
  'Monthly minutes must not exceed contract maximum',
  'Yearly minutes must not exceed contract maximum',
  'Unavailable time slot for an employee',
  'Undesired time slot for an employee',
  'Desired time slot for an employee',
  'Employee is not original employee',
  'Employee is not rotation employee',
];

export const ConstraintMatches: React.FC<{ indictmentSummary: IndictmentSummary }> = props => (
  <List>
    {Object.keys(props.indictmentSummary.constraintToCountMap)
      .sort((a, b) => CONSTRAINTS.indexOf(b) - CONSTRAINTS.indexOf(a)).map(constraint => (
        <li key={constraint}>
          <IndictmentIcon indictment={constraint} />
          <span style={{ paddingLeft: '8px' }}>
            {`${constraint}: ${props.indictmentSummary.constraintToCountMap[constraint]}
                (Impact: 
                ${convertHardMediumSoftScoreToString(props.indictmentSummary.constraintToScoreImpactMap[constraint])
        })`
            }
          </span>
        </li>
      ))}
  </List>
);

export const ScoreDisplay: React.FC<ScoreDisplayProps> = (props) => {
  const { t } = useTranslation('ScoreDisplay');
  const { hardScore, mediumScore, softScore } = props.score;
  const { isSolving } = props;
  const [isOpen, setIsOpen] = React.useState(false);

  return (
    <span>
      <Chip isReadOnly>
        {t('hardScore', { hardScore })}
      </Chip>
      <Chip isReadOnly>
        {t('mediumScore', { mediumScore })}
      </Chip>
      <Chip isReadOnly>
        {t('softScore', { softScore })}
      </Chip>
      <Popover
        headerContent="Constraint Matches"
        bodyContent={<ConstraintMatches {...props} />}
        boundary="viewport"
        maxWidth="800px"
        shouldClose={() => setIsOpen(false)}
        isVisible={isOpen}
      >
        <Button variant={ButtonVariant.plain} onClick={() => setIsOpen(!isOpen)}>
          <HelpIcon />
        </Button>
      </Popover>
      {(hardScore < 0 && !isSolving) && 'The schedule is currently not feasible.'}
      {(hardScore < 0 && isSolving) && 'Stand by while resolving hard constraint violations...'}
      {(hardScore === 0 && mediumScore < 0 && !isSolving) && 'Some shifts are unassigned.'}
      {(hardScore === 0 && mediumScore < 0 && isSolving) && 'Stand by while assigning shifts...'}
    </span>
  );
};
