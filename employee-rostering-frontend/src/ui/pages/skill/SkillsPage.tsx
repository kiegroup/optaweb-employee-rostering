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
import { EditableComponent } from 'ui/components/EditableComponent';

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
  updateSkill: typeof skillOperations.updateSkill;
  removeSkill: typeof skillOperations.removeSkill;
}

const mapDispatchToProps: DispatchProps = {
  addSkill: skillOperations.addSkill,
  updateSkill: skillOperations.updateSkill,
  removeSkill: skillOperations.removeSkill
};

export type Props = StateProps & DispatchProps;

export class SkillsPage extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    this.createSkillRow = this.createSkillRow.bind(this);
    this.skillToTableRow = this.skillToTableRow.bind(this);
  }

  createSkillRow(table: DataTable<Skill>): IRow {
    let currentName = "";
    const name =  <TextInput onChange={(newValue) => currentName = newValue}
      aria-label="name">
    </TextInput>;
    // TODO: Validate before saving
    //@ts-ignore 
    return {
      isOpen: true,
      props: {},
      cells: [
        {
          title: name
        },
        {
          title: <span>
            <Button aria-label="Save"
              variant={ButtonVariant.link}
              onClick={() => {this.props.addSkill({
                tenantId: this.props.tenantId,
                name: currentName
              }); table.cancelAddingRow()}}>
              <SaveIcon />
            </Button>
            <Button aria-label="Cancel"
              variant={ButtonVariant.link}
              onClick={table.cancelAddingRow}>
              <CloseIcon />
            </Button>
          </span>
        }
      ]
    };
  }

  skillToTableRow(rowData: Skill): IRow {
    let name: EditableComponent;
    let buttons: EditableComponent;
    let newName = rowData.name;

    const nameElement = <EditableComponent ref={(c) => {name = c as EditableComponent;}}
      viewer={<span>{rowData.name}</span>}
      editor={<TextInput aria-label="Name"
        defaultValue={rowData.name}
        onChange={(v) => {newName = v;}}
      />}
    />;  

    const buttonsElement = <EditableComponent ref={(c) => {buttons = c as EditableComponent;}}
      viewer={<span>
        <Button aria-label="Edit"
          variant={ButtonVariant.link}
          onClick={() => {
            name.startEditing();
            buttons.startEditing();
          }}>
          <EditIcon />
        </Button>
        <Button aria-label="Delete"
          variant={ButtonVariant.link}
          onClick={() => {
            this.props.removeSkill(rowData)
          }}>
          <TrashIcon />
        </Button>
      </span>}
      editor={<span>
        <Button aria-label="Save"
          variant={ButtonVariant.link}
          onClick={() => {
            this.props.updateSkill({...rowData, name: newName});
            name.stopEditing();
            buttons.stopEditing();
          }}>
          <SaveIcon />
        </Button>
        <Button aria-label="Cancel"
          variant={ButtonVariant.link}
          onClick={() => {
            name.stopEditing();
            buttons.stopEditing();
          }}>
          <CloseIcon />
        </Button>
      </span>}/>;

    // @ts-ignore
    return { 
      isOpen: true,
      props: {},
      cells: [
        {
          title: nameElement
        },
        {
          title: buttonsElement
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
