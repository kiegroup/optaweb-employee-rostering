import React from 'react';
import {
  Modal, Text, Title, Flex, FlexItem, InputGroup, TextInput,
  InputGroupText, Button, Bullseye, GridItem, Grid,
} from '@patternfly/react-core';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { skillSelectors } from 'store/skill';
import { useSelector } from 'react-redux';
import { TimeBucket } from 'domain/TimeBucket';
import moment from 'moment';

export interface EditTimeBucketModalProps {
  isOpen: boolean;
  timeBucket: TimeBucket;
  onUpdateTimeBucket: (timeBucket: TimeBucket) => void;
  onClose: () => void;
}

export interface TimeBucketEditorProps {
  name: string;
  timeBucket: TimeBucket;
  onUpdateTimeBucket: (newValue: TimeBucket) => void;
}

export const TimeBucketEditor: React.FC<TimeBucketEditorProps> = (props) => {
  const skillList = useSelector(skillSelectors.getSkillList);
  const weekDaysEnum = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  const weekDayTitle = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  const TIME_FORMAT = 'HH:mm';
  return (
    <>
      <Title headingLevel="h1" size="lg">{props.name}</Title>
      <Flex direction={{ default: 'column' }} spaceItems={{ default: 'spaceItemsMd' }}>
        <FlexItem>
          <InputGroup>
            <TextInput
              aria-label="Start Time"
              type="time"
              value={moment(props.timeBucket.startTime, TIME_FORMAT).format(TIME_FORMAT)}
              onChange={(startTime) => {
                props.onUpdateTimeBucket({
                  ...props.timeBucket,
                  startTime: moment(startTime, TIME_FORMAT).toDate(),
                });
              }}
            />
            <InputGroupText>
            to
            </InputGroupText>
            <TextInput
              aria-label="End Time"
              type="time"
              value={moment(props.timeBucket.endTime, TIME_FORMAT).format(TIME_FORMAT)}
              onChange={(endTime) => {
                props.onUpdateTimeBucket({
                  ...props.timeBucket,
                  endTime: moment(endTime, TIME_FORMAT).toDate(),
                });
              }}
            />
          </InputGroup>
        </FlexItem>
        <FlexItem>
          <Grid hasGutter>
            <GridItem span={4}>
              <Text>
                Additional Skills
              </Text>
            </GridItem>
            <GridItem span={8}>
              <MultiTypeaheadSelectInput
                options={skillList}
                optionToStringMap={skill => skill.name}
                emptyText="Select additional skills..."
                autoSize={false}
                value={props.timeBucket.additionalSkillSet}
                onChange={(additionalSkillSet) => {
                  props.onUpdateTimeBucket({
                    ...props.timeBucket,
                    additionalSkillSet,
                  });
                }}
              />
            </GridItem>
          </Grid>
        </FlexItem>
        <FlexItem>
          <Flex>
            <FlexItem>Repeat on</FlexItem>
            {weekDaysEnum.map((enumValue, index) => (
              <FlexItem
                key={enumValue}
                onClick={() => {
                  const weekdayIndex = props.timeBucket.repeatOnDaySetList.indexOf(enumValue);

                  if (weekdayIndex === -1) {
                    props.onUpdateTimeBucket({
                      ...props.timeBucket,
                      repeatOnDaySetList: [
                        ...props.timeBucket.repeatOnDaySetList,
                        enumValue,
                      ],
                    });
                  } else {
                    props.onUpdateTimeBucket({
                      ...props.timeBucket,
                      repeatOnDaySetList: props.timeBucket.repeatOnDaySetList.filter(v => v !== enumValue),
                      seatList: props.timeBucket.seatList.filter(seat => seat.dayInRotation % 7 !== index),
                    });
                  }
                }}
              >
                <span
                  className="pf-l-bullseye"
                  style={{
                    width: '30px',
                    height: '30px',
                    display: 'inline-block',
                    borderRadius: '50%',
                    backgroundColor: (props.timeBucket.repeatOnDaySetList
                      .filter(weekday => weekday === enumValue).length !== 0)
                      ? 'var(--pf-global--primary-color--100)' : 'var(--pf-global--BackgroundColor--100)',
                    border: '1px solid var(--pf-global--palette--black-300)',
                  }}
                >
                  <Bullseye>
                    <Text
                      style={{
                        color: (props.timeBucket.repeatOnDaySetList
                          .filter(weekday => weekday === enumValue).length !== 0)
                          ? 'var(--pf-global--BackgroundColor--100)' : 'var(--pf-global--BackgroundColor--dark-100)',
                      }}
                    >
                      {weekDayTitle[index]}
                    </Text>
                  </Bullseye>
                </span>
              </FlexItem>
            ))}
          </Flex>
        </FlexItem>
      </Flex>
    </>
  );
};

export const EditTimeBucketModal: React.FC<EditTimeBucketModalProps> = (props) => {
  const [editedTimeBucket, setEditedTimeBucket] = React.useState(props.timeBucket);
  React.useEffect(() => {
    setEditedTimeBucket(props.timeBucket);
  }, [props.timeBucket, props.isOpen]);

  return (
    <Modal
      title="Create Working Time Bucket"
      isOpen={props.isOpen}
      onClose={props.onClose}
      variant="small"
      actions={[
        (
          <Button
            key={0}
            variant="secondary"
            onClick={props.onClose}
          >
            Cancel
          </Button>
        ),
        (
          <Button
            key={1}
            variant="primary"
            onClick={() => {
              props.onUpdateTimeBucket(editedTimeBucket);
              props.onClose();
            }}
          >
             Save
          </Button>
        ),
      ]}
    >
      <TimeBucketEditor
        name="Time Bucket"
        timeBucket={editedTimeBucket}
        onUpdateTimeBucket={setEditedTimeBucket}
      />
    </Modal>
  );
};
