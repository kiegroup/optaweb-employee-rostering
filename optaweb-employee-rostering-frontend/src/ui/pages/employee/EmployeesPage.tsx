/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import { ColorPicker, defaultColorList } from 'ui/components/ColorPicker';
import {
  TableRow, TableCell, RowViewButtons, RowEditButtons, DataTableUrlProps,
  setSorterInUrl, TheTable,
} from 'ui/components/DataTable';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';

import { useDispatch, useSelector } from 'react-redux';

import * as router from 'react-router';
import { tenantSelectors } from 'store/tenant';
import { useValidators } from 'util/ValidationUtils';
import { getPropsFromUrl } from 'util/BookmarkableUtils';
import { usePagableData } from 'util/FunctionalComponentUtils';

export type Props = RouteComponentProps;

export const EmployeeRow = (employee: Employee) => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useDispatch();
  const tenantId = useSelector(tenantSelectors.getTenantId);
  const history = router.useHistory();

  if (isEditing) {
    return (<EditableEmployeeRow employee={employee} isNew={false} onClose={() => setIsEditing(false)} />);
  }

  return (
    <TableRow>
      <TableCell>
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
      <TableCell>
        <Text>{employee.contract.name}</Text>
      </TableCell>
      <TableCell>
        <ChipGroup>
          {employee.skillProficiencySet.map(skill => (
            <Chip key={skill.name} isReadOnly>
              {skill.name}
            </Chip>
          ))}
        </ChipGroup>
      </TableCell>
      <TableCell>
        <Text>{employee.shortId}</Text>
      </TableCell>
      <TableCell>
        <ColorPicker
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
      errorMsg: () => 'Employee cannot have an empty name',
    },
    nameAlreadyTaken: {
      predicate: (employee: Employee) => employeeList.filter(otherEmployee => otherEmployee.name === employee.name
        && otherEmployee.id !== employee.id).length === 0,
      errorMsg: (employee: Employee) => `Name (${employee.name}) is already taken by another employee`,
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
      <TableCell>
        <TextInput value={name} onChange={setName} />
        {validationErrors.showValidationErrors('nameMustNotBeEmpty', 'nameAlreadyTaken')}
      </TableCell>
      <TableCell>
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
      <TableCell>
        <MultiTypeaheadSelectInput
          value={skillProficiencySet}
          options={skillList}
          optionToStringMap={skill => skill.name}
          onChange={newSkillList => setSkillProficiencySet(newSkillList)}
          emptyText={t('selectSkillProficiencies')}
        />
      </TableCell>
      <TableCell>
        <TextInput value={shortId} onChange={setShortId} />
      </TableCell>
      <TableCell>
        <ColorPicker
          currentColor={color}
          onChangeColor={setColor}
        />
      </TableCell>
      <RowEditButtons
        isValid={validationErrors.isValid}
        onSave={() => {
          if (props.isNew) {
            dispatch(employeeOperations.addEmployee(updatedEmployee));
          } else {
            dispatch(employeeOperations.updateEmployee(updatedEmployee));
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

  const pagableData = usePagableData(urlProps, employeeList, employee => [employee.name,
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
        <TheTable
          {...props}
          {...pagableData}
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
                color: defaultColorList[Math.floor(Math.random() * defaultColorList.length)],
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
