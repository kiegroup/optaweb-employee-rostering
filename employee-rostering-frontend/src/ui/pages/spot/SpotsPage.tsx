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
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput'
import {spotOperations} from 'store/spot';
import Spot from 'domain/Spot';
import { AppState } from 'store/types';
import { TextInput, Text, Chip, ChipGroup } from '@patternfly/react-core';
import { connect } from 'react-redux';
import Skill from 'domain/Skill';

interface StateProps extends DataTableProps<Spot> {
  tenantId: number;
  skillList: Skill[];
}

const mapStateToProps = ({ tenantData, skillList, spotList }: AppState): StateProps => ({
  title: "Spots",
  columnTitles: ["Name", "Required Skill Set"],
  tableData: spotList.spotList,
  skillList: skillList.skillList,
  tenantId: tenantData.currentTenantId
}); 

export interface DispatchProps {
  addSpot: typeof spotOperations.addSpot;
  updateSpot: typeof spotOperations.updateSpot;
  removeSpot: typeof spotOperations.removeSpot;
}

const mapDispatchToProps: DispatchProps = {
  addSpot: spotOperations.addSpot,
  updateSpot: spotOperations.updateSpot,
  removeSpot: spotOperations.removeSpot
};

export type Props = StateProps & DispatchProps;

export class SpotsPage extends DataTable<Spot, Props> {
  constructor(props: Props) {
    super(props);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  createNewDataInstance(): Spot {
    return {
      tenantId: this.props.tenantId,
      name: "",
      requiredSkillSet: [] 
    };
  }

  displayDataRow(data: Spot): JSX.Element[] {
    return [
      <Text key={0}>{data.name}</Text>,
      <ChipGroup key={1}>{data.requiredSkillSet.map(skill => (
        <Chip key={skill.name} isReadOnly>
          {skill.name}
        </Chip>
      ))}
      </ChipGroup>
    ];
  }
  
  editDataRow(data: Spot): JSX.Element[] {
    return [
      <TextInput key={0}
        name="name"
        defaultValue={data.name}
        aria-label="Name"
        onChange={(value) => data.name = value}
      />,
      <MultiTypeaheadSelectInput key={1}
        emptyText={"Select required skills"}
        options={this.props.skillList}
        optionToStringMap={skill => skill.name}
        defaultValue={data.requiredSkillSet}
        onChange={selected => data.requiredSkillSet = selected}
      />
    ];
  }
  
  isValid(editedValue: Spot): boolean {
    return editedValue.name.length > 0;
  }
  
  updateData(data: Spot): void {
    this.props.updateSpot(data);
  }
  
  addData(data: Spot): void {
    this.props.addSpot(data);
  }

  removeData(data: Spot): void {
    this.props.removeSpot(data);
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SpotsPage);
