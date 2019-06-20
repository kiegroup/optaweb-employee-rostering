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
import {DataTable, DataTableProps, ReadonlyPartial, PropertySetter, Sorter} from 'ui/components/DataTable';
import {skillOperations} from 'store/skill';
import Skill from 'domain/Skill';
import { AppState } from 'store/types';
import { TextInput, Text } from '@patternfly/react-core';
import { connect } from 'react-redux';
import { Predicate } from 'ui/components/FilterComponent';
import { stringSorter } from 'util/CommonSorters';
import { stringFilter } from 'util/CommonFilters';

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

export class SkillsPage extends DataTable<Skill, Props> {
  constructor(props: Props) {
    super(props);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  displayDataRow(data: Skill): JSX.Element[] {
    return [<Text key={0}>{data.name}</Text>];
  }

  getInitialStateForNewRow(): Partial<Skill> {
    return {};
  }

  editDataRow(data: ReadonlyPartial<Skill>, setProperty: PropertySetter<Skill>): JSX.Element[] {
    return [<TextInput
      key={0}
      name="name"
      aria-label="Name"
      defaultValue={data.name}
      onChange={(value) => setProperty("name", value)}
    />];
  }
  
  isValid(data: ReadonlyPartial<Skill>): data is Skill {
    return data.name !== undefined && 
      data.name.length > 0;
  }

  getFilter(): (filter: string) => Predicate<Skill> {
    return stringFilter((skill) => skill.name);
  }

  getSorters(): (Sorter<Skill> | null)[] {
    return [stringSorter(s => s.name)];
  }

  updateData(data: Skill): void {
    this.props.updateSkill(data);
  }
  
  addData(data: Skill): void {
    this.props.addSkill({...data, tenantId: this.props.tenantId});
  }

  removeData(data: Skill): void {
    this.props.removeSkill(data);
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SkillsPage);
