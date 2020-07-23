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
import { employeeSelectors } from 'store/employee';
import { useSelector } from 'react-redux';
import {
  Text, Grid, GridItem, Modal, InputGroup, Button, Popover,
  SplitItem, Split, InputGroupText, FlexItem, Flex, FlexModifiers, Bullseye,
} from '@patternfly/react-core';
import {
  UsersIcon, UserIcon, AngleDownIcon, TrashIcon,
  EditIcon,
} from '@patternfly/react-icons';
import { Employee } from 'domain/Employee';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import Color from 'color';
import { v4 as uuid } from 'uuid';
import { useListValidators } from 'util/ValidationUtils';

export interface EmployeeNickNameProps {
  employee: Employee | null;
}
export const EmployeeNickName: React.FC<EmployeeNickNameProps> = (props) => {
  const nickname = props.employee ? props.employee.shortId : '';
  return (
    <Bullseye
      style={{
        // eslint-disable-next-line no-nested-ternary
        color: props.employee ? (getColor(props.employee.color).isLight() ? 'black' : 'white') : 'gray',
      }}
    >
      <Text>{nickname}</Text>
    </Bullseye>
  );
};

export function getColor(color: string): Color {
  if (color.startsWith('var')) {
    // CSS variable
    return Color(getComputedStyle(document.documentElement)
      .getPropertyValue(color.substring(4, color.length - 1)).trim());
  }

  return Color(color);
}


export interface EmployeeStubProps {
  isSelected: boolean;
  employee: Employee | null;
  color: string;
  onClick: () => void;
}
export const EmployeeStub: React.FC<EmployeeStubProps> = props => (
  <button
    style={{
      width: '20px',
      height: '30px',
      marginTop: '10px',
      overflow: 'hidden',
      outline: `${props.isSelected ? 5 : 1}px solid ${props.isSelected
        ? 'var(--pf-global--primary-color--100)' : 'black'}`,
      cursor: 'pointer',
      writingMode: 'vertical-rl',
      textOrientation: 'upright',
      backgroundColor: props.color,
      color: getColor(props.color).isLight() ? 'black' : 'white',
    }}
    type="button"
    onClick={props.onClick}
    title={props.employee !== null ? props.employee.name : 'Unassigned'}
  >
    <EmployeeNickName employee={props.employee} />
  </button>
);

const defaultColorList = ['red', 'orange', 'gold', 'green', 'cyan', 'blue', 'purple'].flatMap(colorFamily => (
  ['600', '500', '400', '300', '200', '100'].map(value => `var(--pf-global--palette--${colorFamily}-${value})`)
));

export interface ColorPickerProps {
  currentColor: string;
  onChangeColor: (newColor: string) => void;
}
export const ColorPicker: React.FC<ColorPickerProps> = props => (
  <Popover
    aria-label="color-select"
    bodyContent={(
      <Grid>
        {defaultColorList.map(color => (
          <GridItem
            key={color}
            span={2}
            rowSpan={2}
            onClick={() => {
              props.onChangeColor(color);
            }}
          >
            <span
              style={{
                width: '30px',
                height: '30px',
                display: 'inline-block',
                borderRadius: '50%',
                backgroundColor: color,
                border: '1px solid var(--pf-global--palette--black-300)',
              }}
            />
          </GridItem>
        ))}
      </Grid>
    )}
    position="bottom"
  >
    <InputGroup
      style={{
        width: '100px',
      }}
    >
      <Button
        variant="control"
        style={{
          backgroundColor: props.currentColor,
        }}
      />
      <Button
        variant="control"
      >
        <AngleDownIcon />
      </Button>
    </InputGroup>
  </Popover>
);

export type Stub = Employee | 'SHIFT_WITH_NO_EMPLOYEE' | 'NO_SHIFT';
export interface EditEmployeeStubListModalProps {
  isVisible: boolean;
  currentStubList: Stub[];
  onClose: () => void;
  onUpdateStubList: (stubList: Stub[]) => void;
}
export const EditEmployeeStubListModal: React.FC<EditEmployeeStubListModalProps> = (props) => {
  const [editedStubList, setEditedStubList] = React.useState<{ key: string; stub: Stub }[]>(
    props.currentStubList.map(stub => ({ key: uuid(), stub })),
  );
  const employeeList = useSelector(employeeSelectors.getEmployeeList);
  const { isValid, showValidationErrors } = useListValidators(editedStubList, {
    noUnassignedStub: {
      predicate: stub => stub.stub !== 'SHIFT_WITH_NO_EMPLOYEE',
      errorMsg: () => 'All stubs must have an employee',
    },
    noDuplicateStub: {
      predicate: stub => typeof stub.stub === 'string'
        || editedStubList.filter(other => typeof other.stub !== 'string'
          && other.stub.id === (stub.stub as Employee).id && stub !== other).length === 0,
      errorMsg: stub => `Multiple stubs have employee ${(stub.stub as Employee).name}`,
    },
  });

  React.useEffect(() => {
    setEditedStubList(props.currentStubList.map(stub => ({ key: uuid(), stub })));
  }, [props.currentStubList, props.isVisible]);

  return (
    <Modal
      title="Edit Employee Stub List"
      isSmall
      onClose={props.onClose}
      isOpen={props.isVisible}
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
            isDisabled={!isValid}
            variant="primary"
            onClick={() => {
              props.onUpdateStubList(editedStubList.map(stub => stub.stub));
              props.onClose();
            }}
          >
             Save
          </Button>
        ),
      ]}
    >
      <Flex breakpointMods={[{ modifier: FlexModifiers.column }]}>
        {editedStubList.map((stub, index) => (
          <FlexItem key={stub.key}>
            <Split>
              <SplitItem>
                <InputGroup>
                  <InputGroupText>
                    <UserIcon />
                  </InputGroupText>
                  <span style={{
                    width: '200px',
                  }}
                  >
                    <TypeaheadSelectInput
                      emptyText="Select an employee..."
                      options={employeeList}
                      value={stub.stub}
                      optionToStringMap={o => ((typeof o === 'string') ? 'Unassigned' : o.name)}
                      autoSize={false}
                      onChange={(employee) => {
                        setEditedStubList([
                          ...editedStubList.filter((_, i) => i < index),
                          { key: stub.key, stub: employee || 'SHIFT_WITH_NO_EMPLOYEE' },
                          ...editedStubList.filter((_, i) => i > index),
                        ]);
                      }}
                    />
                  </span>
                  {showValidationErrors(index, 'noDuplicateStub', 'noUnassignedStub')}
                </InputGroup>
              </SplitItem>
              <SplitItem isFilled><span /></SplitItem>
              <SplitItem>
                <Button
                  variant="link"
                  onClick={() => {
                    setEditedStubList(editedStubList.filter(item => item !== stub));
                  }}
                >
                  <TrashIcon />
                </Button>
              </SplitItem>
            </Split>
          </FlexItem>
        ))}
        <FlexItem>
          <Button
            variant="link"
            onClick={() => {
              setEditedStubList([
                ...editedStubList,
                { key: uuid(), stub: 'SHIFT_WITH_NO_EMPLOYEE' },
              ]);
            }}
          >
            + Add Employee
          </Button>
        </FlexItem>
      </Flex>
    </Modal>
  );
};

export interface EmployeeStubListProps {
  selectedStub: Stub;
  stubList: Stub[];
  onStubSelect: (stub: Stub) => void;
  onUpdateStubList: (stubList: Stub[]) => void;
}

export const EmployeeStubList: React.FC<EmployeeStubListProps> = (props) => {
  // const { t } = useTranslation('EmployeeStub');
  const [isEditingEmployeeStubList, setIsEditingEmployeeStubList] = React.useState(false);

  return (
    <>
      <Flex style={{
        alignItems: 'center',
      }}
      >
        <FlexItem><UsersIcon /></FlexItem>
        <FlexItem><Text>Employee Stub:</Text></FlexItem>

        <FlexItem>
          <Flex breakpointMods={[{ modifier: FlexModifiers['align-items-stretch'] }]}>
            <FlexItem>
              <EmployeeStub
                isSelected={props.selectedStub === 'NO_SHIFT'}
                employee={null}
                color="gray"
                onClick={() => props.onStubSelect('NO_SHIFT')}
              />
            </FlexItem>
            <FlexItem>
              <EmployeeStub
                isSelected={props.selectedStub === 'SHIFT_WITH_NO_EMPLOYEE'}
                employee={null}
                color="#FFFFFF"
                onClick={() => props.onStubSelect('SHIFT_WITH_NO_EMPLOYEE')}
              />
            </FlexItem>
            {props.stubList.map(stub => (
              <GridItem
                key={typeof stub === 'string' ? stub : stub.id}
              >
                <EmployeeStub
                  isSelected={props.selectedStub === stub}
                  employee={stub as Employee}
                  color={(stub as Employee).color}
                  onClick={() => props.onStubSelect(stub)}
                />
              </GridItem>
            ))}
          </Flex>
        </FlexItem>
        <FlexItem>
          <Button
            variant="link"
            onClick={() => setIsEditingEmployeeStubList(true)}
          >
          Edit employee stub
            {' '}
            <EditIcon />
          </Button>
        </FlexItem>
      </Flex>
      <EditEmployeeStubListModal
        isVisible={isEditingEmployeeStubList}
        currentStubList={props.stubList}
        onClose={() => setIsEditingEmployeeStubList(false)}
        onUpdateStubList={props.onUpdateStubList}
      />
    </>
  );
};
