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
import { employeeSelectors, employeeOperations } from 'store/employee';
import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
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
} from '@patternfly/react-core';
import { connect } from 'react-redux';
import { Skill } from 'domain/Skill';
import { Contract } from 'domain/Contract';
import { StatefulTypeaheadSelectInput } from 'ui/components/TypeaheadSelectInput';
import { Predicate, Sorter, ReadonlyPartial } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { stringFilter } from 'util/CommonFilters';
import { StatefulMultiTypeaheadSelectInput } from 'ui/components/MultiTypeaheadSelectInput';
import { CubesIcon } from '@patternfly/react-icons';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';

interface StateProps extends DataTableProps<Employee> {
  tenantId: number;
  skillList: Skill[];
  contractList: Contract[];
}

const mapStateToProps = (state: AppState, ownProps: Props): StateProps => ({
  ...ownProps,
  title: ownProps.t('employees'),
  columnTitles: [ownProps.t('name'), ownProps.t('contract'), ownProps.t('skillProficiencies')],
  tableData: employeeSelectors.getEmployeeList(state),
  skillList: skillSelectors.getSkillList(state),
  contractList: contractSelectors.getContractList(state),
  tenantId: state.tenantData.currentTenantId,
});

export interface DispatchProps {
  addEmployee: typeof employeeOperations.addEmployee;
  updateEmployee: typeof employeeOperations.updateEmployee;
  removeEmployee: typeof employeeOperations.removeEmployee;
}

const mapDispatchToProps: DispatchProps = {
  addEmployee: employeeOperations.addEmployee,
  updateEmployee: employeeOperations.updateEmployee,
  removeEmployee: employeeOperations.removeEmployee,
};

export type Props = RouteComponentProps & StateProps & DispatchProps & WithTranslation;

// TODO: Refactor DataTable to use props instead of methods
/* eslint-disable class-methods-use-this */
export class EmployeesPage extends DataTable<Employee, Props> {
  constructor(props: Props) {
    super(props);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  displayDataRow(data: Employee): JSX.Element[] {
    return [
      <Text key={0}>{data.name}</Text>,
      <Text key={1}>{data.contract.name}</Text>,
      <ChipGroup key={2}>
        {data.skillProficiencySet.map(skill => (
          <Chip key={skill.name} isReadOnly>
            {skill.name}
          </Chip>
        ))}
      </ChipGroup>,
    ];
  }

  getInitialStateForNewRow(): Partial<Employee> {
    return {
      skillProficiencySet: [],
    };
  }

  editDataRow(data: ReadonlyPartial<Employee>, setProperty: PropertySetter<Employee>): React.ReactNode[] {
    return [
      <TextInput
        key={0}
        name="name"
        defaultValue={data.name}
        aria-label="Name"
        onChange={value => setProperty('name', value)}
      />,
      <StatefulTypeaheadSelectInput
        key={1}
        emptyText={this.props.t('selectAContract')}
        optionToStringMap={c => c.name}
        value={data.contract}
        options={this.props.contractList}
        onChange={contract => setProperty('contract', contract)}
      />,
      <StatefulMultiTypeaheadSelectInput
        key={2}
        emptyText={this.props.t('selectSkillProficiencies')}
        options={this.props.skillList}
        optionToStringMap={skill => skill.name}
        value={data.skillProficiencySet ? data.skillProficiencySet : []}
        onChange={selected => setProperty('skillProficiencySet', selected)}
      />,
    ];
  }

  isDataComplete(editedValue: ReadonlyPartial<Employee>): editedValue is Employee {
    return editedValue.name !== undefined
      && editedValue.contract !== undefined
      && editedValue.skillProficiencySet !== undefined;
  }

  isValid(editedValue: Employee): boolean {
    return editedValue.name.trim().length > 0;
  }

  getFilter(): (filter: string) => Predicate<Employee> {
    return stringFilter(employee => employee.name,
      employee => employee.contract.name,
      employee => employee.skillProficiencySet.map(skill => skill.name));
  }

  getSorters(): (Sorter<Employee> | null)[] {
    return [stringSorter(e => e.name), stringSorter(e => e.contract.name), null];
  }

  updateData(data: Employee): void {
    this.props.updateEmployee(data);
  }

  addData(data: Employee): void {
    this.props.addEmployee({ ...data, tenantId: this.props.tenantId });
  }

  removeData(data: Employee): void {
    this.props.removeEmployee(data);
  }

  render(): JSX.Element {
    if (this.props.contractList.length === 0) {
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={CubesIcon} />
          <Trans
            t={this.props.t}
            i18nKey="noContracts"
            components={[
              <Title headingLevel="h5" size="lg" key={0} />,
              <EmptyStateBody key={1} />,
              <Button
                key={2}
                aria-label="Contracts Page"
                variant="primary"
                onClick={() => this.props.history.push(`/${this.props.tenantId}/contracts`)}
              />,
            ]}
          />
        </EmptyState>
      );
    }
    return super.render();
  }
}

export default withTranslation('EmployeesPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(EmployeesPage)),
);
