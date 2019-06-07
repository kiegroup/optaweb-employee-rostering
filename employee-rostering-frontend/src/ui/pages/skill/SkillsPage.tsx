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
import {DataTable} from 'ui/components/DataTable';
import {skillOperations} from 'store/skill';
import Skill from 'domain/Skill';
import { AppState } from 'store/types';
import {Button, TextInput, ButtonVariant} from '@patternfly/react-core';
import { IRow } from '@patternfly/react-table';
import { TrashIcon, EditIcon, SaveIcon, CloseIcon } from '@patternfly/react-icons';
import { connect } from 'react-redux';

interface StateProps {
  skillList: Skill[];
  tenantId: number;
}

const mapStateToProps = ({ tenantData, skillList }: AppState): StateProps => ({
  tenantId: tenantData.currentTenantId,
  skillList: skillList.skillList
}); 

export interface DispatchProps {
  addSkill: typeof skillOperations.addSkill;
  removeSkill: typeof skillOperations.removeSkill;
}

const mapDispatchToProps: DispatchProps = {
  addSkill: skillOperations.addSkill,
  removeSkill: skillOperations.removeSkill
};

export type Props = StateProps & DispatchProps;

class SkillsPage extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    this.createSkillRow = this.createSkillRow.bind(this);
    this.skillToTableRow = this.skillToTableRow.bind(this);
  }

  createSkillRow(table: DataTable<Skill>) : IRow {
    var currentName : string = "";
    const name =  <TextInput onChange={(newValue) => currentName = newValue}
                             aria-label="name">
                  </TextInput>;
    // TODO: Validate before saving
    //@ts-ignore 
    return {
      isOpen: true,
      props: {},
      cells: [
        {title: name},
        {
          title: <span>
                   <Button aria-label="Save"
                           variant={ButtonVariant.link}
                           onClick={() => {this.props.addSkill({
                             tenantId: this.props.tenantId,
                             name: currentName
                           }); table.cancelAddingRow()}}><SaveIcon /></Button>
                   <Button aria-label="Cancel"
                           variant={ButtonVariant.link}
                           onClick={table.cancelAddingRow}><CloseIcon /></Button>
                 </span>
        }
      ]
    };
  }

  skillToTableRow(rowData: Skill) : IRow {
    // @ts-ignore
    return { 
      isOpen: true,
      props: {},
      cells: [
        rowData.name,
        {
          title: <span>
                   <Button aria-label="Edit"
                           variant={ButtonVariant.link}><EditIcon /></Button>
                   <Button aria-label="Delete"
                           variant={ButtonVariant.link}
                           onClick={() => {this.props.removeSkill(rowData)}}><TrashIcon /></Button>
                 </span>
        }
      ]
    };
  }

  render() {
    return <DataTable title="Skills" tenantId={this.props.tenantId} columnTitles={['Name']} 
      tableData={this.props.skillList} createRow={this.createSkillRow} rowDataToRow={this.skillToTableRow}>
      </DataTable>;
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SkillsPage);
