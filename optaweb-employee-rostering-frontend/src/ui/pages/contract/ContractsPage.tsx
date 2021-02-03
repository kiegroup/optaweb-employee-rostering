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
import {
  TableRow, TableCell, RowViewButtons, RowEditButtons,
  DataTableUrlProps, setSorterInUrl, TheTable,
} from 'ui/components/DataTable';
import { TextInput, Text } from '@patternfly/react-core';
import { useDispatch, useSelector } from 'react-redux';
import { Sorter } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { useTranslation } from 'react-i18next';
import { RouteComponentProps, withRouter } from 'react-router';
import { tenantSelectors } from 'store/tenant';
import { useValidators } from 'util/ValidationUtils';
import { getPropsFromUrl } from 'util/BookmarkableUtils';
import { usePagableData } from 'util/FunctionalComponentUtils';
import { contractOperations, contractSelectors } from 'store/contract';
import { Contract } from 'domain/Contract';

export type Props = RouteComponentProps;

export const ContractRow = (contract: Contract) => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useDispatch();

  if (isEditing) {
    return (<EditableContractRow contract={contract} isNew={false} onClose={() => setIsEditing(false)} />);
  }

  return (
    <TableRow>
      <TableCell>
        <Text>{contract.name}</Text>
      </TableCell>
      <TableCell>
        <Text>{contract.maximumMinutesPerDay}</Text>
      </TableCell>
      <TableCell>
        <Text>{contract.maximumMinutesPerWeek}</Text>
      </TableCell>
      <TableCell>
        <Text>{contract.maximumMinutesPerMonth}</Text>
      </TableCell>
      <TableCell>
        <Text>{contract.maximumMinutesPerYear}</Text>
      </TableCell>
      <RowViewButtons
        onEdit={() => setIsEditing(true)}
        onDelete={() => dispatch(contractOperations.removeContract(contract))}
      />
    </TableRow>
  );
};

export const EditableContractRow = (props: { contract: Contract; isNew: boolean; onClose: () => void }) => {
  const [name, setName] = useState(props.contract.name);
  const [maximumMinutesPerDay, setMaximumMinutesPerDay] = useState(props.contract.maximumMinutesPerDay);
  const [maximumMinutesPerWeek, setMaximumMinutesPerWeek] = useState(props.contract.maximumMinutesPerWeek);
  const [maximumMinutesPerMonth, setMaximumMinutesPerMonth] = useState(props.contract.maximumMinutesPerMonth);
  const [maximumMinutesPerYear, setMaximumMinutesPerYear] = useState(props.contract.maximumMinutesPerYear);

  const dispatch = useDispatch();
  const contractList = useSelector(contractSelectors.getContractList);
  // const { t } = useTranslation("ContractsPage");

  const validators = {
    nameMustNotBeEmpty: {
      predicate: (contract: Contract) => contract.name.length > 0,
      errorMsg: () => 'Contract cannot have an empty name',
    },
    nameAlreadyTaken: {
      predicate: (contract: Contract) => contractList.filter(otherContract => otherContract.name === contract.name
        && otherContract.id !== contract.id).length === 0,
      errorMsg: (contract: Contract) => `Name (${contract.name}) is already taken by another contract`,
    },
  };

  const updatedContract: Contract = {
    ...props.contract,
    name,
    maximumMinutesPerDay,
    maximumMinutesPerWeek,
    maximumMinutesPerMonth,
    maximumMinutesPerYear,
  };

  const validationErrors = useValidators(updatedContract, validators);

  return (
    <TableRow>
      <TableCell>
        <TextInput value={name} onChange={setName} />
        {validationErrors.showValidationErrors('nameMustNotBeEmpty', 'nameAlreadyTaken')}
      </TableCell>
      <TableCell>
        <TextInput
          value={maximumMinutesPerDay || ''}
          onChange={(value) => {
            setMaximumMinutesPerDay(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell>
        <TextInput
          value={maximumMinutesPerWeek || ''}
          onChange={(value) => {
            setMaximumMinutesPerWeek(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell>
        <TextInput
          value={maximumMinutesPerMonth || ''}
          onChange={(value) => {
            setMaximumMinutesPerMonth(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell>
        <TextInput
          value={maximumMinutesPerYear || ''}
          onChange={(value) => {
            setMaximumMinutesPerYear(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <RowEditButtons
        isValid={validationErrors.isValid}
        onSave={() => {
          if (props.isNew) {
            dispatch(contractOperations.addContract(updatedContract));
          } else {
            dispatch(contractOperations.updateContract(updatedContract));
          }
        }}
        onClose={() => props.onClose()}
      />
    </TableRow>
  );
};

export const ContractsPage: React.FC<Props> = (props) => {
  const contractList = useSelector(contractSelectors.getContractList);
  const tenantId = useSelector(tenantSelectors.getTenantId);

  const { t } = useTranslation('ContractsPage');

  const columns = [
    { name: t('name'), sorter: stringSorter<Contract>(spot => spot.name) },
    { name: t('maxMinutesPerDay') },
    { name: t('maxMinutesPerWeek') },
    { name: t('maxMinutesPerMonth') },
    { name: t('maxMinutesPerYear') },
  ];

  const urlProps = getPropsFromUrl<DataTableUrlProps>(props, {
    page: '1',
    itemsPerPage: '10',
    filter: null,
    sortBy: '0',
    asc: 'true',
  });

  const sortBy = parseInt(urlProps.sortBy || '-1', 10);
  const sorter = columns[sortBy].sorter as Sorter<Contract>;

  const pagableData = usePagableData(urlProps, contractList, contract => [contract.name,
    `${contract.maximumMinutesPerDay || ''}`, `${contract.maximumMinutesPerWeek || ''}`,
    `${contract.maximumMinutesPerMonth || ''}`, `${contract.maximumMinutesPerYear || ''}`],
  sorter);

  return (
    <TheTable
      {...props}
      {...pagableData}
      title={t('contracts')}
      columns={columns}
      rowWrapper={contract => (<ContractRow key={contract.id} {...contract} />)}
      sortByIndex={sortBy}
      onSorterChange={index => setSorterInUrl(props, urlProps, sortBy, index)}
      newRowWrapper={removeRow => (
        <EditableContractRow
          isNew
          onClose={removeRow}
          contract={{
            tenantId,
            name: '',
            maximumMinutesPerDay: null,
            maximumMinutesPerWeek: null,
            maximumMinutesPerMonth: null,
            maximumMinutesPerYear: null,
          }}
        />
      )}
    />
  );
};

export default withRouter(ContractsPage);
