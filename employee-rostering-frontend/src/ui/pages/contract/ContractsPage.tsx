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

import * as React from 'react';
import { DataTable, DataTableProps, PropertySetter } from 'ui/components/DataTable';
import { contractSelectors, contractOperations } from 'store/contract';
import { AppState } from 'store/types';
import { TextInput, Text } from '@patternfly/react-core';
import { connect } from 'react-redux';
import { Contract } from 'domain/Contract';
import OptionalInput from 'ui/components/OptionalInput';
import { Predicate, Sorter, ReadonlyPartial } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { stringFilter } from 'util/CommonFilters';
import { withTranslation, WithTranslation } from 'react-i18next';
import { withRouter } from 'react-router';

interface StateProps extends DataTableProps<Contract> {
  tenantId: number;
}

const mapStateToProps = (state: AppState, ownProps: Props): StateProps => ({
  ...ownProps,
  title: ownProps.t('contracts'),
  columnTitles: [ownProps.t('name'), ownProps.t('maxMinutesPerDay'), ownProps.t('maxMinutesPerWeek'),
    ownProps.t('maxMinutesPerMonth'), ownProps.t('maxMinutesPerYear')],
  tableData: contractSelectors.getContractList(state),
  tenantId: state.tenantData.currentTenantId,
});

export interface DispatchProps {
  addContract: typeof contractOperations.addContract;
  updateContract: typeof contractOperations.updateContract;
  removeContract: typeof contractOperations.removeContract;
}

const mapDispatchToProps: DispatchProps = {
  addContract: contractOperations.addContract,
  updateContract: contractOperations.updateContract,
  removeContract: contractOperations.removeContract,
};

export type Props = StateProps & DispatchProps & WithTranslation;


// TODO: Refactor DataTable to use props instead of methods
/* eslint-disable class-methods-use-this */
export class ContractsPage extends DataTable<Contract, Props> {
  constructor(props: Props) {
    super(props);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  displayDataRow(data: Contract): JSX.Element[] {
    return [
      <Text key={0}>{data.name}</Text>,
      <Text key={1}>{data.maximumMinutesPerDay}</Text>,
      <Text key={2}>{data.maximumMinutesPerWeek}</Text>,
      <Text key={3}>{data.maximumMinutesPerMonth}</Text>,
      <Text key={4}>{data.maximumMinutesPerYear}</Text>,
    ];
  }

  getInitialStateForNewRow(): Partial<Contract> {
    return {
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    };
  }

  editDataRow(data: ReadonlyPartial<Contract>, setProperty: PropertySetter<Contract>): JSX.Element[] {
    const { t } = this.props;
    return [
      <TextInput
        key={0}
        name="name"
        defaultValue={data.name}
        aria-label="Name"
        onChange={value => setProperty('name', value)}
      />,
      <OptionalInput
        key={1}
        label={t('maxMinutesPerDay')}
        valueToString={value => value.toString()}
        defaultValue={data.maximumMinutesPerDay ? data.maximumMinutesPerDay : null}
        onChange={value => setProperty('maximumMinutesPerDay', value)}
        isValid={value => /^\d+$/.test(value)}
        valueMapper={value => parseInt(value, 10)}
      />,
      <OptionalInput
        key={2}
        label={t('maxMinutesPerWeek')}
        valueToString={value => value.toString()}
        defaultValue={data.maximumMinutesPerWeek ? data.maximumMinutesPerWeek : null}
        onChange={value => setProperty('maximumMinutesPerWeek', value)}
        isValid={value => /^\d+$/.test(value)}
        valueMapper={value => parseInt(value, 10)}
      />,
      <OptionalInput
        key={3}
        label={t('maxMinutesPerMonth')}
        valueToString={value => value.toString()}
        defaultValue={data.maximumMinutesPerMonth ? data.maximumMinutesPerMonth : null}
        onChange={value => setProperty('maximumMinutesPerMonth', value)}
        isValid={value => /^\d+$/.test(value)}
        valueMapper={value => parseInt(value, 10)}
      />,
      <OptionalInput
        key={4}
        label={t('maxMinutesPerYear')}
        valueToString={value => value.toString()}
        defaultValue={data.maximumMinutesPerYear ? data.maximumMinutesPerYear : null}
        onChange={value => setProperty('maximumMinutesPerYear', value)}
        isValid={value => /^\d+$/.test(value)}
        valueMapper={value => parseInt(value, 10)}
      />,
    ];
  }

  isDataComplete(editedValue: ReadonlyPartial<Contract>): editedValue is Contract {
    return editedValue.name !== undefined
      && editedValue.maximumMinutesPerDay !== undefined
      && editedValue.maximumMinutesPerWeek !== undefined
      && editedValue.maximumMinutesPerMonth !== undefined
      && editedValue.maximumMinutesPerYear !== undefined;
  }

  isValid(editedValue: Contract): boolean {
    return editedValue.name.trim().length > 0;
  }

  getFilter(): (filter: string) => Predicate<Contract> {
    return stringFilter(contract => contract.name);
  }

  getSorters(): (Sorter<Contract> | null)[] {
    return [stringSorter(c => c.name), null, null, null, null];
  }

  updateData(data: Contract): void {
    this.props.updateContract(data);
  }

  addData(data: Contract): void {
    this.props.addContract({ ...data, tenantId: this.props.tenantId });
  }

  removeData(data: Contract): void {
    this.props.removeContract(data);
  }
}

export default withTranslation('ContractsPage')(connect(mapStateToProps, mapDispatchToProps)(
  withRouter(ContractsPage),
));
