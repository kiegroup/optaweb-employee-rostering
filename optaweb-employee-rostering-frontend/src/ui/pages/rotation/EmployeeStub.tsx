import React from 'react';
import { employeeSelectors } from 'store/employee';
import { useSelector } from 'react-redux';
import {
  Text, GridItem, Modal, InputGroup, Button,
  SplitItem, Split, InputGroupText, FlexItem, Flex, Bullseye,
} from '@patternfly/react-core';
import {
  UsersIcon, UserIcon, TrashIcon,
  EditIcon,
} from '@patternfly/react-icons';
import { Employee } from 'domain/Employee';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { v4 as uuid } from 'uuid';
import { useListValidators } from 'util/ValidationUtils';
import { getColor } from 'ui/components/ColorPicker';
import { useTranslation } from 'react-i18next';

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

export interface EmployeeStubProps {
  isSelected: boolean;
  employee: Stub;
  color: string;
  onClick: () => void;
}
export const EmployeeStub: React.FC<EmployeeStubProps> = (props) => {
  const { t } = useTranslation('EmployeeStub');
  return (
    <button
      className="pf-c-button"
      style={{
        display: 'flex',
        width: '30px',
        height: '50px',
        marginTop: '10px',
        overflow: 'hidden',
        outline: `${props.isSelected ? 5 : 1}px solid ${props.isSelected
          ? 'var(--pf-global--primary-color--100)' : 'black'}`,
        cursor: 'pointer',
        writingMode: 'vertical-rl',
        textOrientation: 'upright',
        alignItems: 'center',
        backgroundColor: props.color,
        color: getColor(props.color).isLight() ? 'black' : 'white',
      }}
      type="button"
      onClick={props.onClick}
      title={
        // eslint-disable-next-line no-nested-ternary
        (props.employee === 'NO_SHIFT') ? t('noShift')
          : (props.employee === 'SHIFT_WITH_NO_EMPLOYEE') ? t('unassigned')
            : props.employee.name
      }
    >
      <EmployeeNickName employee={(typeof props.employee !== 'string') ? props.employee : null} />
    </button>
  );
};

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
  const { t } = useTranslation('EmployeeStub');
  const { isValid, showValidationErrors } = useListValidators(editedStubList, {
    noUnassignedStub: {
      predicate: stub => stub.stub !== 'SHIFT_WITH_NO_EMPLOYEE',
      errorMsg: () => t('allStubMustHaveAnEmployee'),
    },
    noDuplicateStub: {
      predicate: stub => typeof stub.stub === 'string'
        || editedStubList.filter(other => typeof other.stub !== 'string'
          && other.stub.id === (stub.stub as Employee).id && stub !== other).length === 0,
      errorMsg: stub => t('stubEmployeeNotUnique', { employee: (stub.stub as Employee).name }),
    },
  });

  React.useEffect(() => {
    setEditedStubList(props.currentStubList.map(stub => ({ key: uuid(), stub })));
  }, [props.currentStubList, props.isVisible]);

  return (
    <Modal
      title={t('editEmployeeStubList')}
      variant="small"
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
      <Flex direction={{ default: 'column' }}>
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
                      emptyText={t('selectAnEmployee')}
                      options={employeeList}
                      value={stub.stub}
                      optionToStringMap={o => ((typeof o === 'string') ? t('unassigned') : o.name)}
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
            {`+ ${t('addEmployee')}`}
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
  const { t } = useTranslation('EmployeeStub');
  const [isEditingEmployeeStubList, setIsEditingEmployeeStubList] = React.useState(false);

  return (
    <>
      <Flex style={{
        alignItems: 'center',
      }}
      >
        <FlexItem><UsersIcon /></FlexItem>
        <FlexItem>
          <Text>
            {t('employeeStub')}
          </Text>
        </FlexItem>

        <FlexItem>
          <Flex>
            <FlexItem>
              <EmployeeStub
                isSelected={props.selectedStub === 'NO_SHIFT'}
                employee="NO_SHIFT"
                color="gray"
                onClick={() => props.onStubSelect('NO_SHIFT')}
              />
            </FlexItem>
            <FlexItem>
              <EmployeeStub
                isSelected={props.selectedStub === 'SHIFT_WITH_NO_EMPLOYEE'}
                employee="SHIFT_WITH_NO_EMPLOYEE"
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
            {t('editEmployeeStubList')}
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
