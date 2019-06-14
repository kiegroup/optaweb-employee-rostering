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

import React from 'react';
import {
  Table,
  TableHeader,
  TableBody,
  headerCol,
  IRow
} from '@patternfly/react-table';
import {Button, ButtonVariant} from '@patternfly/react-core';
import { SaveIcon, CloseIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import { EditableComponent } from './EditableComponent';

export interface DataTableProps<T> {
  title: string;
  columnTitles: string[];
  tableData: T[];
}

interface DataTableState<T> {
  newRowData: Partial<T>|null;
  currentFilter: (rowData: T) => boolean;
}

export type ReadonlyPartial<T> = { readonly [P in keyof T]?: T[P] };
export type PropertySetter<T> = (propertyName: keyof T, value: T[keyof T]|undefined) => void;

export abstract class DataTable<T, P extends DataTableProps<T>> extends React.Component<P, DataTableState<T>> {
  constructor(props: P) {
    super(props);
    this.createNewRow = this.createNewRow.bind(this);
    this.cancelAddingRow = this.cancelAddingRow.bind(this);
    this.convertDataToTableRow = this.convertDataToTableRow.bind(this);
    this.state = {newRowData: null, currentFilter: (t) => true};
  }

  abstract displayDataRow(data: T): JSX.Element[];
  abstract editDataRow(data: ReadonlyPartial<T>, setProperty: (propertyName: keyof T, value: T[keyof T]|undefined) => void): JSX.Element[];
  abstract isValid(editedValue: ReadonlyPartial<T>): editedValue is T;

  abstract updateData(data: T): void;
  abstract addData(data: T): void;
  abstract removeData(data: T): void;

  createNewRow() {
    if (this.state.newRowData === null) {
      this.setState({...this.state, newRowData: {}});
    }
  }

  cancelAddingRow() {
    this.setState({...this.state, newRowData: null});
  }

  getAddButtons(newData: Partial<T>): JSX.Element {
    return <span>
      <Button aria-label="Save"
        variant={ButtonVariant.link}
        onClick={() => {
          if (this.isValid(newData)) {
            this.addData(newData);
            this.cancelAddingRow();
          }
        }}>
        <SaveIcon />
      </Button>
      <Button aria-label="Cancel"
        variant={ButtonVariant.link}
        onClick={this.cancelAddingRow}>
        <CloseIcon />
      </Button>
    </span>;
  }

  getEditButtons(originalData: T, editedData: Partial<T>, editableComponents: EditableComponent[]): JSX.Element {
    return <EditableComponent ref={(c) => {editableComponents[editableComponents.length-1] = c as EditableComponent;}}
      viewer={<span>
        <Button aria-label="Edit"
          variant={ButtonVariant.link}
          onClick={() => {
            editableComponents.forEach(c => c.startEditing());
          }}>
          <EditIcon />
        </Button>
        <Button aria-label="Delete"
          variant={ButtonVariant.link}
          onClick={() => {
            this.removeData(originalData);
          }}>
          <TrashIcon />
        </Button>
      </span>}
      editor={<span>
        <Button aria-label="Save"
          variant={ButtonVariant.link}
          onClick={() => {
            if (this.isValid(editedData)) {
              this.updateData(editedData);
              editableComponents.forEach(c => c.stopEditing());
            }
          }}>
          <SaveIcon />
        </Button>
        <Button aria-label="Cancel"
          variant={ButtonVariant.link}
          onClick={() => {
            editableComponents.forEach(c => c.stopEditing());
          }}>
          <CloseIcon />
        </Button>
      </span>}/>;
  }

  convertDataToTableRow(data: T): IRow {
    const editedData: Partial<T> = {...data};
    const viewers = this.displayDataRow(data);

    const setProperty = (key: keyof T, value: T[keyof T]|undefined) => editedData[key] = value;
    const editors = this.editDataRow(editedData, setProperty);
    const length = viewers.length
    const editableComponents: EditableComponent[] = new Array(length + 1);
    const cellContents = viewers.map((viewer, index) => {return { title:
      <EditableComponent viewer={viewer}
        editor={editors[index]}
        ref={(c) => editableComponents[index] = c as EditableComponent}
      />} }).concat([{title: this.getEditButtons(data, editedData, editableComponents)}]);
    return {
      cells: cellContents
    };
  }

  // Use for SNAPSHOT testing
  renderViewer(data: T): JSX.Element {
    return <tr>{this.displayDataRow(data).map((c,index) => <td key={index}>{c}</td>)}</tr>;
  }

  // Use for SNAPSHOT testing
  renderEditor(data: T): JSX.Element {
    const editedData: T = {...data};
    const setProperty = (key: keyof T, value: any) => editedData[key] = value;
    return <tr>{this.editDataRow(editedData, setProperty).map((c,index) => <td key={index}>{c}</td>)}</tr>;
  }

  render() {
    const setProperty = (key: keyof T,value: T[keyof T]|undefined) => this.setState(prevState => 
      ({...prevState, newRowData: {...prevState.newRowData, [key]: value}}));
    const additionalRows: IRow[] = (this.state.newRowData !== null)?
      [
        {
          cells: this.editDataRow(this.state.newRowData, setProperty).map(c => {return {title: c}})
            .concat([{title: this.getAddButtons(this.state.newRowData)}])
        }
      ] : [];
    const rows = additionalRows.concat(this.props.tableData
      .filter(this.state.currentFilter).map(this.convertDataToTableRow));
    const columns = this.props.columnTitles.map(t => { 
      return { title: t, cellTransforms: [headerCol], props: {} };
    }).concat([{title: '', cellTransforms: [headerCol], props: {}}]);
    return (
      <div>
        <Button isDisabled={this.state.newRowData !== null} onClick={this.createNewRow}>Add</Button>
        <Table caption={this.props.title} cells={columns} rows={rows}>
          <TableHeader />
          <TableBody />
        </Table>
      </div>
    );
  }
}

export default DataTable
