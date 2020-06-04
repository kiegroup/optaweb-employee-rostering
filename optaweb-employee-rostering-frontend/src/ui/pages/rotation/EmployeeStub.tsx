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
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { Text, Grid, GridItem, Modal, Stack, StackItem, InputGroup, Label, Button, Popover, SplitItem, Split, InputGroupText, Gallery, FlexItem, Flex, FlexModifiers } from '@patternfly/react-core';
import { UsersIcon, UserIcon, AngleDownIcon, TrashIcon, EditIcon, PaletteIcon, ExclamationTriangleIcon } from '@patternfly/react-icons';
import { Employee } from 'domain/Employee';
import { v4 as uuidv4 } from 'uuid';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';

export const EmployeeStub: React.FC<{
  isSelected: boolean,
  employee: Employee | null,
  color: string,
  onClick: () => void,
}> = props => {
  return (
    <div
      style={{
        width: '20px',
        height: '30px',
        marginTop: '10px',
        border: '1px solid black',
        backgroundColor: props.color,
      }}
      title={props.employee !== null? props.employee.name : 'Unassigned'}
    />
  );
};

export interface Stub {
  employee: Employee | null;
  color: string;
}

export const ColorPicker: React.FC<{
  currentColor: string,
  onChangeColor: (newColor: string) => void
}> = props => {
  const defaultColorList = ['red', 'orange', 'gold', 'green', 'cyan', 'blue', 'purple'].flatMap(colorFamily => (
    ['600', '500', '400', '300', '200', '100'].map(value => `var(--pf-global--palette--${colorFamily}-${value})`)
  ));
  return (
    <Popover
      aria-label='color-select'
      bodyContent={(
        <Grid>
          {defaultColorList.map(color => (
            <GridItem key={color} span={2} rowSpan={2} onClick={() => {
              props.onChangeColor(color);
            }}>
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
      position='bottom'
    >
      <InputGroup
        style={{
          width: '100px',
        }}
      >
        <Button
          variant='control'
          style={{
            backgroundColor: props.currentColor
          }}
        >
        </Button>
        <Button
          variant='control'
        >
          <AngleDownIcon/>
        </Button>
      </InputGroup>
    </Popover>
  );
};

export const EditEmployeeStubListModal: React.FC<{
  isVisible: boolean,
  currentStubList: Stub[],
  onClose: () => void,
  onUpdateStubList: (stubList: Stub[]) => void,
}> = props => {
  const [editedStubList, setEditedStubList] = React.useState([...props.currentStubList]);
  const employeeList = useSelector(employeeSelectors.getEmployeeList);
  
  React.useEffect(() => {
    setEditedStubList([...props.currentStubList]);
  }, [props.currentStubList, props.isVisible]);
  
  return (
    <Modal
      title='Edit Employee Stub List'
      isSmall
      onClose={props.onClose}
      isOpen={props.isVisible}
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
              props.onUpdateStubList(editedStubList);
              props.onClose();
             }}
           >
             Save
           </Button>
         ),  
      ]}
    >
      <Flex breakpointMods={[{modifier: FlexModifiers.column}]}>
        {editedStubList.map((stub, index) => (
          <FlexItem key={uuidv4()}>
            <Split>
              <SplitItem>
                <InputGroup>
                  <InputGroupText>
                    <UserIcon />
                  </InputGroupText>
                  <span style={{
                    width: '200px',
                  }}>
                    <TypeaheadSelectInput
                      emptyText='Select an employee...'
                      options={employeeList}
                      value={stub.employee}
                      optionToStringMap={o => o? o.name : 'Unassigned'}
                      autoSize={false}
                      onChange={employee => {
                        setEditedStubList([
                          ...editedStubList.filter((_, i) => i < index),
                          {
                            ...stub,
                            employee: employee? employee : null,
                          },
                          ...editedStubList.filter((_, i) => i > index) 
                        ]);
                      }}
                    />
                  </span>
                  <InputGroupText>
                    <PaletteIcon />
                  </InputGroupText>
                  <ColorPicker
                    currentColor={stub.color}
                    onChangeColor={color => {
                      setEditedStubList([
                        ...editedStubList.filter((_, i) => i < index),
                        {
                          ...stub,
                          color,
                        },
                        ...editedStubList.filter((_, i) => i > index)
                      ]);
                    }}
                  />
                  { (stub.employee === null || stub.color === '' ||
                     editedStubList.filter(s => s.employee === stub.employee).length !== 1 ||
                     editedStubList.filter(s => s.color === stub.color).length !== 1) &&
                    <InputGroupText>
                      <ExclamationTriangleIcon />
                    </InputGroupText>
                  }
                </InputGroup>
              </SplitItem>
              <SplitItem isFilled><span/></SplitItem>
              <SplitItem>
                <Button
                  variant='link'
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
            variant='link'
            onClick={() => {
              setEditedStubList([
              ...editedStubList,
                {
                  employee: null,
                  color: '',
                }
              ]);  
            }}
          >
            + New Employee Stub
          </Button>
        </FlexItem>
      </Flex>
    </Modal>
  );
};

export const EmployeeStubList: React.FC<{
  selectedEmployee: Employee | null,
  stubList: Stub[],
  onStubSelect: (stub: Stub) => void,
  onUpdateStubList: (stubList: Stub[]) => void,
}> = props => {
  const { t } = useTranslation('EmployeeStub');
  const [isEditingEmployeeStubList, setIsEditingEmployeeStubList] = React.useState(false);
  
  return (<>
    <Flex style={{
      alignItems: 'center',
    }}>
      <FlexItem><UsersIcon /></FlexItem>
      <FlexItem><Text>Employee Stub:</Text></FlexItem>

      <FlexItem>
        <Flex>
          <FlexItem>
            <EmployeeStub
              isSelected={props.selectedEmployee === null}
              employee={null}
              color='#FFFFFF'
              onClick={() => props.onStubSelect({ color: '#FFFFFF', employee: null })}
            />
          </FlexItem>
          {props.stubList.map(stub => (
            <GridItem>
              <EmployeeStub
                isSelected={props.selectedEmployee === stub.employee}
                employee={stub.employee}
                color={stub.color}
                onClick={() => props.onStubSelect(stub)}
              />
            </GridItem>
          ))}
        </Flex>
      </FlexItem>
      <FlexItem>
        <Button
          variant='link'
          onClick={() => setIsEditingEmployeeStubList(true)}
        >
          Edit employee stub <EditIcon/>
        </Button>
      </FlexItem>
    </Flex>
    <EditEmployeeStubListModal
      isVisible={isEditingEmployeeStubList}
      currentStubList={props.stubList}
      onClose={() => setIsEditingEmployeeStubList(false)}
      onUpdateStubList={props.onUpdateStubList}
      />
  </>); 
};