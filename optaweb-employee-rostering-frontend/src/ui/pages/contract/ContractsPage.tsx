
import React, { useState } from 'react';
import {
  TableRow, TableCell, RowViewButtons, RowEditButtons,
  DataTableUrlProps, setSorterInUrl, DataTable,
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
import { usePageableData } from 'util/FunctionalComponentUtils';
import { contractOperations, contractSelectors } from 'store/contract';
import { Contract } from 'domain/Contract';

export type Props = RouteComponentProps;

export const ContractRow = (contract: Contract) => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useDispatch();
  const { t } = useTranslation('ContractsPage');

  if (isEditing) {
    return (<EditableContractRow contract={contract} isNew={false} onClose={() => setIsEditing(false)} />);
  }

  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <Text>{contract.name}</Text>
      </TableCell>
      <TableCell columnName={t('maxMinutesPerDay')}>
        <Text>{contract.maximumMinutesPerDay}</Text>
      </TableCell>
      <TableCell columnName={t('maxMinutesPerWeek')}>
        <Text>{contract.maximumMinutesPerWeek}</Text>
      </TableCell>
      <TableCell columnName={t('maxMinutesPerMonth')}>
        <Text>{contract.maximumMinutesPerMonth}</Text>
      </TableCell>
      <TableCell columnName={t('maxMinutesPerYear')}>
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
  const { t } = useTranslation('ContractsPage');

  const validators = {
    nameMustNotBeEmpty: {
      predicate: (contract: Contract) => contract.name.length > 0,
      errorMsg: () => t('contractEmptyNameError'),
    },
    nameAlreadyTaken: {
      predicate: (contract: Contract) => contractList.filter(otherContract => otherContract.name === contract.name
        && otherContract.id !== contract.id).length === 0,
      errorMsg: (contract: Contract) => t('contractNameAlreadyTakenError', { name: contract.name }),
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
      <TableCell columnName={t('name')}>
        <TextInput value={name} onChange={setName} />
        {validationErrors.showValidationErrors('nameMustNotBeEmpty', 'nameAlreadyTaken')}
      </TableCell>
      <TableCell columnName={t('maxMinutesPerDay')}>
        <TextInput
          value={maximumMinutesPerDay || ''}
          onChange={(value) => {
            setMaximumMinutesPerDay(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell columnName={t('maxMinutesPerWeek')}>
        <TextInput
          value={maximumMinutesPerWeek || ''}
          onChange={(value) => {
            setMaximumMinutesPerWeek(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell columnName={t('maxMinutesPerMonth')}>
        <TextInput
          value={maximumMinutesPerMonth || ''}
          onChange={(value) => {
            setMaximumMinutesPerMonth(value ? parseInt(value, 10) : null);
          }}
          type="number"
          min={0}
        />
      </TableCell>
      <TableCell columnName={t('maxMinutesPerYear')}>
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

  const pageableData = usePageableData(urlProps, contractList, contract => [contract.name,
    `${contract.maximumMinutesPerDay || ''}`, `${contract.maximumMinutesPerWeek || ''}`,
    `${contract.maximumMinutesPerMonth || ''}`, `${contract.maximumMinutesPerYear || ''}`],
  sorter);

  return (
    <DataTable
      {...props}
      {...pageableData}
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
