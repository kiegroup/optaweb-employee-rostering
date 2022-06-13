import React from 'react';
import {
  Title, Text, Flex, FlexItem,
  SplitItem, Split, Button,
} from '@patternfly/react-core';
import moment from 'moment';
import { rosterSelectors } from 'store/roster';
import { useSelector } from 'react-redux';
import { EditIcon, TrashIcon } from '@patternfly/react-icons';
import { TimeBucket } from 'domain/TimeBucket';
import { useTranslation } from 'react-i18next';
import { EditTimeBucketModal } from './EditTimeBucketModal';
import { Stub, EmployeeNickName } from './EmployeeStub';

export interface SeatJigsawProps {
  selectedStub: Stub | null;
  timeBucket: TimeBucket;
  onUpdateTimeBucket: (updatedTimeBucket: TimeBucket) => void;
  onDeleteTimeBucket: () => void;
}

export const SeatJigsaw: React.FC<SeatJigsawProps> = (props) => {
  const [isEditingTimeBucket, setIsEditingTimeBucket] = React.useState(false);
  const [editedTimeBucket, setEditedTimeBucket] = React.useState(props.timeBucket);

  // Update from props when timebucket id or version changes
  React.useEffect(() => {
    setEditedTimeBucket(props.timeBucket);
  }, [setEditedTimeBucket, props.timeBucket]);

  const rosterState = useSelector(rosterSelectors.getRosterState);
  const rotationLength = rosterState ? rosterState.rotationLength : 0;
  const { t } = useTranslation('SeatJigsaw');
  const updateSeatInTimeBucket = (day: number) => ({
    ...editedTimeBucket,
    seatList: [
      ...editedTimeBucket.seatList
        .filter(other => other.dayInRotation !== day),
      ...(props.selectedStub !== 'NO_SHIFT'
        ? [
          {
            dayInRotation: day,
            employee: props.selectedStub !== 'SHIFT_WITH_NO_EMPLOYEE'
              ? props.selectedStub : null,
          },
        ] : []),
    ],
  });

  return (
    <>
      <Title
        headingLevel="h1"
        size="lg"
        style={{
          userSelect: 'none',
        }}
      >
        {moment(editedTimeBucket.startTime, 'HH:mm').format('LT')}
        -
        {moment(editedTimeBucket.endTime, 'HH:mm').format('LT')}
        {editedTimeBucket.additionalSkillSet.length > 0
          && (
            <Text>
          (
              {editedTimeBucket.additionalSkillSet.map(skill => skill.name).join(', ')}
          )
            </Text>
          )
        }
      </Title>
      <Split>
        <SplitItem>
          <Flex
            spaceItems={{ default: 'spaceItemsLg' }}
            onMouseLeave={() => {
              if (props.timeBucket !== editedTimeBucket) {
                props.onUpdateTimeBucket(editedTimeBucket);
              }
            }}
            onMouseUp={() => {
              if (props.timeBucket !== editedTimeBucket) {
                props.onUpdateTimeBucket(editedTimeBucket);
              }
            }}
          >
            {new Array(Math.ceil(rotationLength / 7)).fill(null).map((_, index) => index).map(weekNumber => (
              <FlexItem key={`${weekNumber}`}>
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 30px)',
                    columnGap: '0.5rem',
                    cursor: 'pointer',
                  }}
                >
                  {Array(7).fill(null)
                    .map((_, i) => editedTimeBucket.seatList
                      .find(seat => seat.dayInRotation === i + weekNumber * 7) || null)
                    .map((seat, weekDay) => (
                      <button
                        key={`${weekDay + weekNumber * 7}`}
                        // eslint-disable-next-line no-nested-ternary
                        title={seat ? seat.employee ? seat.employee.name : 'Unassigned' : 'No Shift'}
                        style={{
                          border: '1px solid black',
                          height: '50px',
                          // eslint-disable-next-line no-nested-ternary
                          backgroundColor: seat ? seat.employee ? seat.employee.color : 'white' : 'gray',
                          writingMode: 'vertical-rl',
                          textOrientation: 'upright',
                          userSelect: 'none',
                        }}
                        type="button"
                        onClick={() => {
                          // Update the timebucket directly on click, but only if
                          // the edited timebucket has not changed, to detect
                          // changes from keyboard (for accessibility)
                          if (editedTimeBucket === props.timeBucket) {
                            const updatedTimeBucket = updateSeatInTimeBucket(weekDay + weekNumber * 7);
                            props.onUpdateTimeBucket(updatedTimeBucket);
                          }
                        }}
                        onMouseDown={() => {
                          const updatedTimeBucket = updateSeatInTimeBucket(weekDay + weekNumber * 7);
                          setEditedTimeBucket(updatedTimeBucket);
                        }}
                        onMouseMove={(e) => {
                          // eslint-disable-next-line no-bitwise
                          if ((e.buttons & 1) === 1) { // 1 = Left mouse button
                            const updatedTimeBucket = updateSeatInTimeBucket(weekDay + weekNumber * 7);
                            setEditedTimeBucket(updatedTimeBucket);
                          }
                        }}
                      >
                        <EmployeeNickName employee={seat ? seat.employee : null} />
                      </button>
                    ))}
                  <span
                    style={{
                      gridRow: 2,
                      gridColumn: 1,
                      justifySelf: 'center',
                      userSelect: 'none',
                    }}
                  >
                    {(weekNumber * 7) + 1}
                  </span>
                  <span
                    style={{ gridRow: 2,
                      gridColumn: 7,
                      justifySelf: 'center',
                      userSelect: 'none' }}
                  >
                    {(weekNumber + 1) * 7}
                  </span>
                </div>
              </FlexItem>
            ))}
          </Flex>
        </SplitItem>
        <SplitItem>
          <Button
            onClick={() => setIsEditingTimeBucket(true)}
            variant="link"
          >
            <EditIcon />
            {' '}
            {t('editTimeBucket')}
          </Button>
          <Button
            onClick={props.onDeleteTimeBucket}
            variant="link"
          >
            <TrashIcon />
            {' '}
            {t('deleteTimeBucket')}
          </Button>
        </SplitItem>
      </Split>
      <EditTimeBucketModal
        isOpen={isEditingTimeBucket}
        timeBucket={editedTimeBucket}
        onUpdateTimeBucket={props.onUpdateTimeBucket}
        onClose={() => setIsEditingTimeBucket(false)}
      />
    </>
  );
};
