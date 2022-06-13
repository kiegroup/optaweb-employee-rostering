import React, { useState } from 'react';
import { employeeSelectors, employeeOperations } from 'store/employee';
import { alert } from 'store/alert';
import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import { Employee } from 'domain/Employee';
import {
  TextInput,
  Text,
  Chip,
  ChipGroup,
  EmptyState,
  EmptyStateIcon,
  Title,
  EmptyStateVariant,
  EmptyStateBody,
  Button,
  ButtonVariant,
  FileUpload,
  FlexItem, Flex,
} from '@patternfly/react-core';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { Sorter, doNothing } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { CubesIcon, ArrowIcon } from '@patternfly/react-icons';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { Trans, useTranslation } from 'react-i18next';

// Need module import so we can mock getRandomColor in tests
import * as ColorPicker from 'ui/components/ColorPicker';
import {
  TableRow, TableCell, RowViewButtons, RowEditButtons, DataTableUrlProps,
  setSorterInUrl, DataTable,
} from 'ui/components/DataTable';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';

import { useDispatch, useSelector } from 'react-redux';

import * as router from 'react-router';
import { tenantSelectors } from 'store/tenant';
import { useValidators } from 'util/ValidationUtils';
import { getPropsFromUrl } from 'util/BookmarkableUtils';
import { usePageableData } from 'util/FunctionalComponentUtils';

export type Props = RouteComponentProps;

export const EmployeeRow = (employee: Employee) => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useDispatch();
  const tenantId = useSelector(tenantSelectors.getTenantId);
  const history = router.useHistory();
  const { t } = useTranslation('EmployeesPage');

  if (isEditing) {
    return (<EditableEmployeeRow employee={employee} isNew={false} onClose={() => setIsEditing(false)} />);
  }

  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <Flex>
          <FlexItem>
            <Text>{employee.name}</Text>
          </FlexItem>
          <FlexItem>
            <Button
              variant={ButtonVariant.link}
              onClick={() => {
                history.push(`/${tenantId}/availability?employee=${encodeURIComponent(employee.name)}`);
              }}
            >
              <ArrowIcon />
            </Button>
          </FlexItem>
        </Flex>
      </TableCell>
      <TableCell columnName={t('contract')}>
        <Text>{employee.contract.name}</Text>
      </TableCell>
      <TableCell columnName={t('skillProficiencies')}>
        <ChipGroup>
          {employee.skillProficiencySet.map(skill => (
            <Chip key={skill.name} isReadOnly>
              {skill.name}
            </Chip>
          ))}
        </ChipGroup>
      </TableCell>
      <TableCell columnName={t('shortId')}>
        <Text>{employee.shortId}</Text>
      </TableCell>
      <TableCell columnName={t('color')}>
        <ColorPicker.ColorPicker
          currentColor={employee.color}
          onChangeColor={doNothing}
          isDisabled
        />
      </TableCell>
      <RowViewButtons
        onEdit={() => setIsEditing(true)}
        onDelete={() => dispatch(employeeOperations.removeEmployee(employee))}
      />
    </TableRow>
  );
};

export const EditableEmployeeRow = (props: { employee: Employee; isNew: boolean; onClose: () => void }) => {
  const [name, setName] = useState(props.employee.name);
  const [contract, setContract] = useState(props.employee.contract);
  const [skillProficiencySet, setSkillProficiencySet] = useState(props.employee.skillProficiencySet);
  const [shortId, setShortId] = useState(props.employee.shortId);
  const [color, setColor] = useState(props.employee.color);

  const employeeList = useSelector(employeeSelectors.getEmployeeList);
  const contractList = useSelector(contractSelectors.getContractList);
  const skillList = useSelector(skillSelectors.getSkillList);
  const dispatch = useDispatch();
  const { t } = useTranslation('EmployeesPage');

  const validators = {
    nameMustNotBeEmpty: {
      predicate: (employee: Employee) => employee.name.length > 0,
      errorMsg: () => t('employeeEmptyNameError'),
    },
    nameAlreadyTaken: {
      predicate: (employee: Employee) => employeeList.filter(otherEmployee => otherEmployee.name === employee.name
        && otherEmployee.id !== employee.id).length === 0,
      errorMsg: (employee: Employee) => t('employeeNameAlreadyTakenError', { name: employee.name }),
    },
  };

  const updatedEmployee: Employee = {
    ...props.employee,
    name,
    contract,
    skillProficiencySet,
    shortId,
    color,
  };
  const validationErrors = useValidators(updatedEmployee, validators);

  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <TextInput value={name} onChange={setName} />
        {validationErrors.showValidationErrors('nameMustNotBeEmpty', 'nameAlreadyTaken')}
      </TableCell>
      <TableCell columnName={t('contract')}>
        <TypeaheadSelectInput
          value={contract}
          options={contractList}
          optionToStringMap={newContract => newContract.name}
          onChange={(newContract) => {
            if (newContract) {
              setContract(newContract);
            }
          }}
          noClearButton
          emptyText={t('selectAContract')}
        />
      </TableCell>
      <TableCell columnName={t('skillProficiencies')}>
        <MultiTypeaheadSelectInput
          value={skillProficiencySet}
          options={skillList}
          optionToStringMap={skill => skill.name}
          onChange={newSkillList => setSkillProficiencySet(newSkillList)}
          emptyText={t('selectSkillProficiencies')}
        />
      </TableCell>
      <TableCell columnName={t('shortId')}>
        <TextInput value={shortId} onChange={setShortId} />
      </TableCell>
      <TableCell columnName={t('color')}>
        <ColorPicker.ColorPicker
          currentColor={color}
          onChangeColor={setColor}
        />
      </TableCell>
      <RowEditButtons
        isValid={validationErrors.isValid}
        onSave={() => {
          const savedEmployee = {
            ...updatedEmployee,
            shortId: updatedEmployee.shortId || updatedEmployee.name.substring(0, 3),
          };
          if (props.isNew) {
            dispatch(employeeOperations.addEmployee(savedEmployee));
          } else {
            dispatch(employeeOperations.updateEmployee(savedEmployee));
          }
        }}
        onClose={() => props.onClose()}
      />
    </TableRow>
  );
};

export const EmployeesPage: React.FC<Props> = (props) => {
  const employeeList = useSelector(employeeSelectors.getEmployeeList);
  const contractList = useSelector(contractSelectors.getContractList);
  const tenantId = useSelector(tenantSelectors.getTenantId);
  const history = router.useHistory();
  const dispatch = useDispatch();

  const { t } = useTranslation('EmployeesPage');

  const columns = [
    { name: t('name'), sorter: stringSorter<Employee>(employee => employee.name) },
    { name: t('contract'), sorter: stringSorter<Employee>(employee => employee.contract.name) },
    { name: t('skillProficiencies') },
    { name: t('shortId'), sorter: stringSorter<Employee>(employee => employee.shortId) },
    { name: t('color') },
  ];

  const urlProps = getPropsFromUrl<DataTableUrlProps>(props, {
    page: '1',
    itemsPerPage: '10',
    filter: null,
    sortBy: '0',
    asc: 'true',
  });

  const sortBy = parseInt(urlProps.sortBy || '-1', 10);
  const sorter = columns[sortBy].sorter as Sorter<Employee>;

  const pageableData = usePageableData(urlProps, employeeList, employee => [employee.name,
    employee.contract.name,
    ...employee.skillProficiencySet.map(skill => skill.name),
    employee.shortId], sorter);

  const importElement = (
    <div>
      <FileUpload
        id="file"
        name="file"
        dropzoneProps={{
          accept: '.xlsx',
        }}
        onChange={
          (file) => {
            if (file instanceof File) {
              dispatch(employeeOperations.uploadEmployeeList(file));
            } else {
              // If a file with the wrong file extension is selected,
              // file is the empty string instead of a File object
              dispatch(alert.showErrorMessage('badFileType', { fileTypes: 'Excel (.xlsx)' }));
            }
          }}
      />
    </div>
  );

  if (contractList.length > 0) {
    return (
      <>
        {importElement}
        <DataTable
          {...props}
          {...pageableData}
          title={t('employees')}
          columns={columns}
          rowWrapper={employee => (<EmployeeRow key={employee.id} {...employee} />)}
          sortByIndex={sortBy}
          onSorterChange={index => setSorterInUrl(props, urlProps, sortBy, index)}
          newRowWrapper={removeRow => (
            <EditableEmployeeRow
              isNew
              onClose={removeRow}
              employee={{
                tenantId,
                name: '',
                contract: contractList[0],
                skillProficiencySet: [],
                shortId: '',
                color: ColorPicker.getRandomColor(),
              }}
            />
          )}
        />
      </>
    );
  }
  return (
    <EmptyState variant={EmptyStateVariant.full}>
      {importElement}
      <EmptyStateIcon icon={CubesIcon} />
      <Trans
        t={t}
        i18nKey="noContracts"
        components={[
          <Title headingLevel="h5" size="lg" key={0} />,
          <EmptyStateBody key={1} />,
          <Button
            key={2}
            aria-label="Contracts Page"
            variant="primary"
            onClick={() => history.push(`/${tenantId}/contracts`)}
          />,
        ]}
      />
    </EmptyState>
  );
};

export default withRouter(EmployeesPage);
