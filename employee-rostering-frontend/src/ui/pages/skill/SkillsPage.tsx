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
import {DataTable, DataTableProps} from 'ui/components/DataTable';
import {skillOperations} from 'store/skill';
import Skill from 'domain/Skill';
import { AppState } from 'store/types';
import { TextInput, Text } from '@patternfly/react-core';
import { connect } from 'react-redux';

export interface SkillComponents {
  name: string;
}

interface StateProps extends DataTableProps<Skill> {
  tenantId: number;
}

const mapStateToProps = ({ tenantData, skillList }: AppState): StateProps => ({
  title: "Skills",
  columnTitles: ["Name"],
  tableData: skillList.skillList,
  tenantId: tenantData.currentTenantId
}); 

export interface DispatchProps {
  addSkill: typeof skillOperations.addSkill;
  updateSkill: typeof skillOperations.updateSkill;
  removeSkill: typeof skillOperations.removeSkill;
}

const mapDispatchToProps: DispatchProps = {
  addSkill: skillOperations.addSkill,
  updateSkill: skillOperations.updateSkill,
  removeSkill: skillOperations.removeSkill
};

export type Props = StateProps & DispatchProps;

export class SkillsPage extends DataTable<Skill,SkillComponents, Props> {
  constructor(props: Props) {
    super(props);
    this.extractDataFromRow = this.extractDataFromRow.bind(this);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  displayDataRow(data: Skill): JSX.Element[] {
    return [<Text key={0}>{data.name}</Text>];
  }

  createNewDataRow(dataStore: SkillComponents): JSX.Element[] {
    dataStore.name = "";
    return [<TextInput key={0} name="name"
      aria-label="Name"
      onChange={(value) => dataStore.name = value}/>];
  }
  
  editDataRow(dataStore: SkillComponents, data: Skill): JSX.Element[] {
    dataStore.name = data.name;
    return [<TextInput key={0} name="name"
      aria-label="Name"
      defaultValue={data.name}
      onChange={(value) => dataStore.name = value}/>];
  }
  
  isValid(editedValue: SkillComponents): boolean {
    return editedValue.name.length > 0;
  }
  
  extractDataFromRow(oldValue: Skill|{}, editedValue: SkillComponents): Skill {
    return {...oldValue, name: editedValue.name, tenantId: this.props.tenantId};
  }

  updateData(data: Skill): void {
    this.props.updateSkill(data);
  }
  
  addData(data: Skill): void {
    this.props.addSkill(data);
  }

  removeData(data: Skill): void {
    this.props.removeSkill(data);
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SkillsPage);
