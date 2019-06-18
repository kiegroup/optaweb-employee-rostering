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
  IRow,
  ICell,
  sortable,
  SortByDirection,
  ISortBy
} from '@patternfly/react-table';
import {Button, ButtonVariant, Pagination, Card } from '@patternfly/react-core';
import { SaveIcon, CloseIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import { EditableComponent } from './EditableComponent';
import FilterComponent, { Filter } from './FilterComponent';
import { toggleElement } from 'util/ImmutableCollectionOperations';

export interface DataTableProps<T> {
  title: string;
  columnTitles: string[];
  tableData: T[];
}

interface DataTableState<T> {
  newRowData: Partial<T>|null;
  editedRows: T[]; 
  page: number;
  perPage: number;
  currentFilter: (rowData: T) => boolean;
  sortBy: ISortBy
}

export type ReadonlyPartial<T> = { readonly [P in keyof T]?: T[P] };
export type PropertySetter<T> = (propertyName: keyof T, value: T[keyof T]|undefined) => void;
export type Sorter<T> = (a: T, b: T) => number;

export abstract class DataTable<T, P extends DataTableProps<T>> extends React.Component<P, DataTableState<T>> {
  [x: string]: any;
  constructor(props: P) {
    super(props);
    this.createNewRow = this.createNewRow.bind(this);
    this.cancelAddingRow = this.cancelAddingRow.bind(this);
    this.convertDataToTableRow = this.convertDataToTableRow.bind(this);
    this.state = {
      editedRows: [],
      newRowData: null,
      currentFilter: (t) => true,
      page: 1,
      perPage: 10,
      sortBy: {}
    };
    this.onSetPage = this.onSetPage.bind(this);
    this.onPerPageSelect = this.onPerPageSelect.bind(this);
    this.onSort = this.onSort.bind(this);
  }

  abstract displayDataRow(data: T): JSX.Element[];
  abstract editDataRow(data: ReadonlyPartial<T>, setProperty: (propertyName: keyof T, value: T[keyof T]|undefined) => void): JSX.Element[];
  abstract isValid(editedValue: ReadonlyPartial<T>): editedValue is T;
  
  abstract getFilters(): Filter<T>[];
  abstract getSorters(): (Sorter<T>|null)[]

  abstract updateData(data: T): void;
  abstract addData(data: T): void;
  abstract removeData(data: T): void;

  onSetPage(event: any, pageNumber: number): void {
    this.setState({
      editedRows: [],
      page: pageNumber
    });
  }

  onPerPageSelect(event: any, perPage: number): void {
    const newPage = Math.floor(((this.state.page - 1) * this.state.perPage) / perPage) + 1;
    this.setState({
      editedRows: [],
      page: newPage,
      perPage
    });
  }

  createNewRow() {
    if (this.state.newRowData === null) {
      this.setState({newRowData: {}});
    }
  }

  cancelAddingRow() {
    this.setState({newRowData: null});
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

  getEditButtons(originalData: T, editedData: Partial<T>, isEditing: boolean, toggleEditing: () => void): JSX.Element {
    return <EditableComponent
      isEditing={isEditing}
      viewer={<span>
        <Button aria-label="Edit"
          variant={ButtonVariant.link}
          onClick={toggleEditing}>
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
              toggleEditing();
            }
          }}>
          <SaveIcon />
        </Button>
        <Button aria-label="Cancel"
          variant={ButtonVariant.link}
          onClick={toggleEditing}>
          <CloseIcon />
        </Button>
      </span>}/>;
  }

  convertDataToTableRow(data: T): IRow {
    const isEditing = this.state.editedRows.indexOf(data) !== -1;
    const editedData: Partial<T> = {...data};
    const setProperty = (key: keyof T, value: T[keyof T]|undefined) => editedData[key] = value;

    const viewers = this.displayDataRow(data);
    const editors = this.editDataRow(editedData, setProperty);
    const cellContents = viewers.map((viewer, index) => {return { title:
      <EditableComponent viewer={viewer}
        editor={editors[index]}
        isEditing={isEditing}
      />} }).concat([{
        title: this.getEditButtons(data, editedData, isEditing,
          () => {
            this.setState(prevState => ({
              editedRows: toggleElement(prevState.editedRows, data)
            }));
          })
      }]);
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


  onSort(event: any, index: number, direction: any) {
    this.setState({
      editedRows: [],
      sortBy: {
        index,
        direction: direction
      },
    });
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
    const sorters = this.getSorters();
    let sortedRows = [...this.props.tableData];
    if (this.state.sortBy.index !== undefined) {
      sortedRows.sort(sorters[this.state.sortBy.index as number] as Sorter<T>);
      // @ts-ignore
      if (this.state.sortBy.direction === SortByDirection.desc) {
        console.log("Okay");
        sortedRows.reverse();
      }
    }

    const filteredRows = sortedRows.filter(this.state.currentFilter).map(this.convertDataToTableRow);
    const rowsOnPage = filteredRows.filter((row, index) => this.state.perPage * (this.state.page - 1) <= index &&
          index <= this.state.perPage * this.state.page);

    const rows = additionalRows.concat(rowsOnPage);

    const columns: ICell[] = this.props.columnTitles.map((t, index) => { 
      return { title: t, cellTransforms: [headerCol], props: {}, transforms: (sorters[index] !== null)? [sortable] : undefined };
    }).concat([{title: '', cellTransforms: [headerCol], props: {}, transforms: undefined}]);
    return (
      <Card>
        <div style={{display: "grid", gridTemplateColumns: "min-content auto min-content max-content"}}>
          <FilterComponent filters={this.getFilters()}
            onChange={(currentFilter) => this.setState({editedRows: [], currentFilter})}
            filterListParentId={"filter-list"}/>
          <span />
          <Button isDisabled={this.state.newRowData !== null} onClick={this.createNewRow}>Add</Button>
          <Pagination
            itemCount={filteredRows.length}
            perPage={this.state.perPage}
            page={this.state.page}
            onSetPage={this.onSetPage}
            widgetId="pagination-options-menu-top"
            onPerPageSelect={this.onPerPageSelect}
          />
        </div>
        <Table caption={this.props.title} sortBy={this.state.sortBy} onSort={this.onSort} cells={columns} rows={rows}>
          <TableHeader />
          <TableBody />
        </Table>
      </Card>
    );
  }
}

export default DataTable
