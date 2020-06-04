/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 
import React from 'react';
import { Modal, Text, Title, Flex, FlexItem, InputGroup, TextInput, InputGroupText, Button, Stack, StackItem, FlexModifiers, SplitItem, Split, Bullseye, GridItem, Grid } from '@patternfly/react-core';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { skillSelectors } from 'store/skill';
import { useSelector } from 'react-redux';
import { Skill } from 'domain/Skill';
import { v4 as uuid } from 'uuid';
import { TrashIcon } from '@patternfly/react-icons';

export interface TimeBucket {
  startTime: string;
  endTime: string;
  repeatOn: string[];
  additionalSkillSet: Skill[];
};

export interface EditTimeBucketModalProps {
  isOpen: boolean;
  timeBuckets: TimeBucket[]; 
  onUpdateTimeBucketList: (timeBuckets: TimeBucket[]) => void;
  onClose: () => void;
};

export const TimeBucket: React.FC<{
  name: string,
  timeBucket: TimeBucket,
  onUpdateTimeBucket: (newValue: TimeBucket) => void,
}> = props => {
  const skillList = useSelector(skillSelectors.getSkillList);
  const weekDaysEnum = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  const weekDayTitle = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  return (
    <>
      <Title size='lg'>{props.name}</Title>
      <Flex breakpointMods={[{modifier: FlexModifiers.column}, {modifier: FlexModifiers["space-items-md"]}]}>
        <FlexItem>
          <InputGroup>
            <TextInput
              type='time'
              value={props.timeBucket.startTime}
              onChange={startTime => {
                  props.onUpdateTimeBucket({
                  ...props.timeBucket,
                  startTime,
                });
              }}
            />
            <InputGroupText>
            to
            </InputGroupText>
            <TextInput
              type='time'
              value={props.timeBucket.endTime}
              onChange={endTime => {
                props.onUpdateTimeBucket({
                  ...props.timeBucket,
                  endTime,
                });
              }}
            />
          </InputGroup>
        </FlexItem>
        <FlexItem>
          <Grid gutter="md">
            <GridItem span={4}>
              <Text>
                Additional Skills
              </Text>
            </GridItem>
            <GridItem span={8}>
              <MultiTypeaheadSelectInput
                options={skillList}
                optionToStringMap={skill => skill.name}
                emptyText='Select additional skills...'
                autoSize={false}
                value={props.timeBucket.additionalSkillSet}
                onChange={additionalSkillSet => {
                  props.onUpdateTimeBucket({
                    ...props.timeBucket,
                    additionalSkillSet,
                  })
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
                  const index = props.timeBucket.repeatOn.indexOf(enumValue);
                  
                  if (index === -1) {
                    props.onUpdateTimeBucket({
                    ...props.timeBucket,
                    repeatOn: [
                      ...props.timeBucket.repeatOn,
                      enumValue
                    ]
                  });
                } else {
                  props.onUpdateTimeBucket({
                    ...props.timeBucket,
                    repeatOn: props.timeBucket.repeatOn.filter(v => v !== enumValue),
                  });
                }
              }}
            >
              <span 
                className='pf-l-bullseye'
                style={{
                  width: '30px',
                  height: '30px',
                  display: 'inline-block',
                  borderRadius: '50%',
                  backgroundColor: (props.timeBucket.repeatOn.filter(weekday => weekday == enumValue).length !== 0)?
                    'var(--pf-global--primary-color--100)' : 'var(--pf-global--BackgroundColor--100)',
                  border: '1px solid var(--pf-global--palette--black-300)',
                }}
              >
                <Bullseye>
                  <Text
                    style={{
                      color: (props.timeBucket.repeatOn.filter(weekday => weekday == enumValue).length !== 0)?
                        'var(--pf-global--BackgroundColor--100)' : 'var(--pf-global--BackgroundColor--dark-100)',
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

export const EditTimeBucketModal: React.FC<EditTimeBucketModalProps> = props => {
  const [editedTimeBucketList, setEditedTimeBucketList] = React.useState(props.timeBuckets);
  React.useEffect(() => {
    setEditedTimeBucketList(props.timeBuckets);
  }, [props.timeBuckets, props.isOpen]);
  
  return (
    <Modal
      title='Create Working Time Bucket'
      isOpen={props.isOpen}
      isSmall
      actions={[
        (
          <Button
            key={0}
            variant='secondary'
            onClick={props.onClose}
          >
            Cancel
          </Button>
        ),
        (
          <Button
            key={1}
            variant='primary'
            onClick={() => {
              props.onUpdateTimeBucketList([]);
              props.onClose();
             }}
           >
             Save
           </Button>
         ),  
      ]}
    >
      <Stack>
        <StackItem>
          <Flex breakpointMods={[{modifier: FlexModifiers.column}]}>
            {editedTimeBucketList.map((timeBucket, index) => (
              <FlexItem key={uuid()}>
                <Split>
                  <SplitItem>
                    <TimeBucket
                      name={`Time Bucket ${index + 1}`}
                      timeBucket={timeBucket}
                      onUpdateTimeBucket={updatedTimeBucket => setEditedTimeBucketList([
                        ...editedTimeBucketList.filter((_,i) => i < index),
                        updatedTimeBucket,
                        ...editedTimeBucketList.filter((_,i) => i > index),
                      ])}
                    />
                  </SplitItem>
                  <SplitItem isFilled><span/></SplitItem>
                  <SplitItem>
                    <Button
                      variant='link'
                      onClick={() => {
                        setEditedTimeBucketList(editedTimeBucketList.filter(item => item !== timeBucket));
                      }}
                    >
                      <TrashIcon />
                    </Button>
                  </SplitItem>
                </Split>
              </FlexItem>
            ))}
          </Flex>
        </StackItem>
        <StackItem isFilled><div /></StackItem>
        <StackItem>
          <Button
            variant='link'
            onClick={() => setEditedTimeBucketList([
              ...editedTimeBucketList,
              {
                startTime: '00:00',
                endTime: '00:00',
                repeatOn: [],
                additionalSkillSet: [],
              },
            ])}
          >
           + New Time Bucket
          </Button>
        </StackItem>
      </Stack>
    </Modal>
  );
};