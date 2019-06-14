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
import {DataTable, DataTableProps, ReadonlyPartial, PropertySetter} from 'ui/components/DataTable';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput'
import {employeeOperations} from 'store/employee';
import Employee from 'domain/Employee';
import { AppState } from 'store/types';
import { TextInput, Text, Chip, ChipGroup } from '@patternfly/react-core';
import { connect } from 'react-redux';
import Skill from 'domain/Skill';
import Contract from 'domain/Contract';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';

interface StateProps extends DataTableProps<Employee> {
  tenantId: number;
  skillList: Skill[];
  contractList: Contract[];
}

const mapStateToProps = ({ tenantData, skillList, contractList, employeeList }: AppState): StateProps => ({
  title: "Spots",
  columnTitles: ["Name", "Contract", "Skill Proficiencies"],
  tableData: employeeList.employeeList,
  skillList: skillList.skillList,
  contractList: contractList.contractList,
  tenantId: tenantData.currentTenantId
}); 

export interface DispatchProps {
  addEmployee: typeof employeeOperations.addEmployee;
  updateEmployee: typeof employeeOperations.updateEmployee;
  removeEmployee: typeof employeeOperations.removeEmployee;
}

const mapDispatchToProps: DispatchProps = {
  addEmployee: employeeOperations.addEmployee,
  updateEmployee: employeeOperations.updateEmployee,
  removeEmployee: employeeOperations.removeEmployee
};

export type Props = StateProps & DispatchProps;

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
      <ChipGroup key={2}>{data.skillProficiencySet.map(skill => (
        <Chip key={skill.name} isReadOnly>
          {skill.name}
        </Chip>
      ))}
      </ChipGroup>
    ];
  }
  
  editDataRow(data: ReadonlyPartial<Employee>, setProperty: PropertySetter<Employee>): JSX.Element[] {
    return [
      <TextInput key={0}
        name="name"
        defaultValue={data.name}
        aria-label="Name"
        onChange={(value) => setProperty("name", value)}
      />,
      <TypeaheadSelectInput key={1}
        emptyText={"Select contract"}
        options={this.props.contractList}
        optionToStringMap={contract => contract.name}
        defaultValue={data.contract}
        onChange={contract => setProperty("contract", contract)}/>,
      <MultiTypeaheadSelectInput key={2}
        emptyText={"Select required skills"}
        options={this.props.skillList}
        optionToStringMap={skill => skill.name}
        defaultValue={data.skillProficiencySet? data.skillProficiencySet : []}
        onChange={selected => setProperty("skillProficiencySet", selected)}
      />
    ];
  }
  
  isValid(editedValue: ReadonlyPartial<Employee>): editedValue is Employee {
    return editedValue.name !== undefined &&
      editedValue.contract !== undefined &&
      editedValue.skillProficiencySet !== undefined &&
      editedValue.name.length > 0;
  }
  
  updateData(data: Employee): void {
    this.props.updateEmployee(data);
  }
  
  addData(data: Employee): void {
    this.props.addEmployee(data);
  }

  removeData(data: Employee): void {
    this.props.removeEmployee(data);
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(EmployeesPage);
