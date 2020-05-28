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
import { alert } from 'store/alert';
import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import { Employee, getIconForCovidRisk, CovidRiskType } from 'domain/Employee';
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
  ButtonVariant,
  FileUpload,
} from '@patternfly/react-core';
import { connect } from 'react-redux';
import { Skill } from 'domain/Skill';
import { Contract } from 'domain/Contract';
import { StatefulTypeaheadSelectInput } from 'ui/components/TypeaheadSelectInput';
import { Predicate, Sorter, ReadonlyPartial } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { stringFilter } from 'util/CommonFilters';
import { StatefulMultiTypeaheadSelectInput } from 'ui/components/MultiTypeaheadSelectInput';
import { CubesIcon, ArrowIcon } from '@patternfly/react-icons';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';
import moment from 'moment';

interface StateProps extends DataTableProps<Employee> {
  tenantId: number;
  skillList: Skill[];
  contractList: Contract[];
}

const mapStateToProps = (state: AppState, ownProps: Props): StateProps => ({
  ...ownProps,
  title: ownProps.t('employees'),
  columnTitles: [
    ownProps.t('name'),
    ownProps.t('contract'),
    ownProps.t('skillProficiencies'),
    ownProps.t('covidRiskType'),
  ],
  tableData: employeeSelectors.getEmployeeList(state),
  skillList: skillSelectors.getSkillList(state),
  contractList: contractSelectors.getContractList(state),
  tenantId: state.tenantData.currentTenantId,
});

export interface DispatchProps {
  addEmployee: typeof employeeOperations.addEmployee;
  updateEmployee: typeof employeeOperations.updateEmployee;
  removeEmployee: typeof employeeOperations.removeEmployee;
  uploadEmployeeList: typeof employeeOperations.uploadEmployeeList;
  showErrorMessage: typeof alert.showErrorMessage;
}

const mapDispatchToProps: DispatchProps = {
  addEmployee: employeeOperations.addEmployee,
  updateEmployee: employeeOperations.updateEmployee,
  removeEmployee: employeeOperations.removeEmployee,
  uploadEmployeeList: employeeOperations.uploadEmployeeList,
  showErrorMessage: alert.showErrorMessage,
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
      <span style={{ display: 'grid', gridTemplateColumns: 'max-content min-content' }}>
        <Text key={0}>{data.name}</Text>
        <Button
          variant={ButtonVariant.link}
          onClick={() => {
            this.props.history.push(`/${this.props.tenantId}/availability?employee=${data.name}`
            + `&week=${moment().startOf('week').format('YYYY-MM-DD')}`);
          }}
        >
          <ArrowIcon />
        </Button>
      </span>,
      <Text key={1}>{data.contract.name}</Text>,
      <ChipGroup key={2}>
        {data.skillProficiencySet.map(skill => (
          <Chip key={skill.name} isReadOnly>
            {skill.name}
          </Chip>
        ))}
      </ChipGroup>,
      <span>
        {getIconForCovidRisk(data.covidRiskType, 'lg')}
        <Text key={3}>{this.props.t(`CovidRisk.${data.covidRiskType}`)}</Text>
      </span>,
    ];
  }

  getInitialStateForNewRow(): Partial<Employee> {
    return {
      skillProficiencySet: [],
      covidRiskType: 'LOW',
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
      <StatefulTypeaheadSelectInput
        key={3}
        emptyText={this.props.t('enterCovidRisk')}
        valueComponent={(props) => {
          const selectedOption: { value: CovidRiskType } = props.data;
          return (
            <span style={{ display: 'grid', gridTemplateColumns: 'max-content 5px 1fr' }}>
              {getIconForCovidRisk(selectedOption.value, 'sm')}
              <span />
              <span>{this.props.t(`CovidRisk.${selectedOption.value}`)}</span>
            </span>
          );
        }}
        optionToStringMap={risk => this.props.t(`CovidRisk.${risk}`)}
        value={data.covidRiskType}
        options={['INOCULATED', 'LOW', 'MODERATE', 'HIGH', 'EXTREME']}
        onChange={covidRisk => setProperty('covidRiskType', covidRisk)}
      />,
    ];
  }

  isDataComplete(editedValue: ReadonlyPartial<Employee>): editedValue is Employee {
    return editedValue.name !== undefined
      && editedValue.contract !== undefined
      && editedValue.skillProficiencySet !== undefined
      && editedValue.covidRiskType !== undefined;
  }

  isValid(editedValue: Employee): boolean {
    return editedValue.name.trim().length > 0;
  }

  getFilter(): (filter: string) => Predicate<Employee> {
    return stringFilter(employee => employee.name,
      employee => employee.contract.name,
      employee => employee.skillProficiencySet.map(skill => skill.name),
      employee => employee.covidRiskType);
  }

  getSorters(): (Sorter<Employee> | null)[] {
    return [stringSorter(e => e.name), stringSorter(e => e.contract.name), null, null];
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
                this.props.uploadEmployeeList(file);
              } else {
                // If a file with the wrong file extension is selected,
                // file is the empty string instead of a File object
                this.props.showErrorMessage('badFileType', { fileTypes: 'Excel (.xlsx)' });
              }
            }}
        />
      </div>
    );
    if (this.props.contractList.length === 0) {
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          {importElement}
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
    return (
      <div>
        {importElement}
        {super.render()}
      </div>
    );
  }
}

export default withTranslation('EmployeesPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(EmployeesPage)),
);
