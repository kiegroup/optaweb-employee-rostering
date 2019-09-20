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
  ISortBy,
} from '@patternfly/react-table';
import {
  Button, ButtonVariant, Pagination, Level, LevelItem,
} from '@patternfly/react-core';
import {
  SaveIcon, CloseIcon, EditIcon, TrashIcon,
} from '@patternfly/react-icons';
import { EditableComponent } from './EditableComponent';
import FilterComponent from './FilterComponent';
import { Predicate, ReadonlyPartial, Sorter } from 'types';
import { toggleElement } from 'util/ImmutableCollectionOperations';
import { WithTranslation } from 'react-i18next';

export interface DataTableProps<T> extends WithTranslation {
  title: string;
  columnTitles: string[];
  tableData: T[];
}

interface DataTableState<T> {
  newRowData: Partial<T> | null;
  editedRows: T[];
  page: number;
  perPage: number;
  currentFilter: (rowData: T) => boolean;
  sortBy: ISortBy;
}

export type PropertySetter<T> = (propertyName: keyof T, value: T[keyof T] | undefined) => void;

export abstract class DataTable<T, P extends DataTableProps<T>> extends React.Component<P, DataTableState<T>> {
  constructor(props: P) {
    super(props);
    this.createNewRow = this.createNewRow.bind(this);
    this.cancelAddingRow = this.cancelAddingRow.bind(this);
    this.convertDataToTableRow = this.convertDataToTableRow.bind(this);
    this.state = {
      editedRows: [],
      newRowData: null,
      currentFilter: t => true,
      page: 1,
      perPage: 10,
      sortBy: {},
    };
    this.onSetPage = this.onSetPage.bind(this);
    this.onPerPageSelect = this.onPerPageSelect.bind(this);
    this.onSort = this.onSort.bind(this);
  }

  abstract getInitialStateForNewRow(): Partial<T>;
  abstract displayDataRow(data: T): React.ReactNode[];
  abstract editDataRow(data: ReadonlyPartial<T>, setProperty:  PropertySetter<T>): React.ReactNode[];
  abstract isDataComplete(editedValue: ReadonlyPartial<T>): editedValue is T;

  abstract isValid(editedValue: T): boolean;

  abstract getFilter(): (filter: string) => Predicate<T>;

  abstract getSorters(): (Sorter<T> | null)[]

  abstract updateData(data: T): void;

  abstract addData(data: T): void;

  abstract removeData(data: T): void;

  onSetPage(event: any, pageNumber: number): void {
    this.setState({
      editedRows: [],
      page: pageNumber,
    });
  }

  onPerPageSelect(event: any, perPage: number): void {
    this.setState(prevState => ({
      editedRows: [],
      page: Math.floor(((prevState.page - 1) * prevState.perPage) / perPage) + 1,
      perPage,
    }));
  }

  createNewRow() {
    if (this.state.newRowData === null) {
      this.setState({ newRowData: this.getInitialStateForNewRow() });
    }
  }

  cancelAddingRow() {
    this.setState({ newRowData: null });
  }

  getAddButtons(newData: Partial<T>): JSX.Element {
    return (
      <span>
        <Button
          aria-label="Save"
          variant={ButtonVariant.link}
          onClick={() => {
            if (this.isDataComplete(newData) && this.isValid(newData)) {
              this.addData(newData);
              this.cancelAddingRow();
            }
          }}
        >
          <SaveIcon />
        </Button>
        <Button
          aria-label="Cancel"
          variant={ButtonVariant.link}
          onClick={this.cancelAddingRow}
        >
          <CloseIcon />
        </Button>
      </span>
    );
  }

  getEditButtons(originalData: T, editedData: Partial<T>, isEditing: boolean,
    toggleEditing: () => void): JSX.Element {
    return (
      <EditableComponent
        isEditing={isEditing}
        viewer={(
          <span
            key={0}
            style={{ 
              display: "grid",
              gridTemplateColumns: "1fr auto auto",
              gridColumnGap: "5px"
            }}
          >
            <span />
            <Button
              aria-label="Edit"
              variant={ButtonVariant.link}
              onClick={toggleEditing}
            >
              <EditIcon />
            </Button>
            <Button
              aria-label="Delete"
              variant={ButtonVariant.link}
              onClick={() => {
                this.removeData(originalData);
              }}
            >
              <TrashIcon />
            </Button>
          </span>
        )}
        editor={(
          <span
            key={0}
            style={{ 
              display: "grid",
              gridTemplateColumns: "1fr auto auto",
              gridColumnGap: "5px"
            }}
          >
            <span />
            <Button
              aria-label="Save"
              variant={ButtonVariant.link}
              onClick={() => {
                if (this.isDataComplete(editedData) && this.isValid(editedData)) {
                  this.updateData(editedData);
                  toggleEditing();
                }
              }}
            >
              <SaveIcon />
            </Button>
            <Button
              aria-label="Cancel"
              variant={ButtonVariant.link}
              onClick={toggleEditing}
            >
              <CloseIcon />
            </Button>
          </span>
        )}
      />
    );
  }

  convertDataToTableRow(data: T): IRow {
    const isEditing = this.state.editedRows.indexOf(data) !== -1;
    const editedData: Partial<T> = { ...data };

    const setProperty = (key: keyof T, value: T[keyof T] | undefined) => {
      editedData[key] = value;
    };

    const viewers = this.displayDataRow(data);
    const editors = this.editDataRow(editedData, setProperty);

    const cellContents = viewers.map((viewer, index) => ({
      title:
  <EditableComponent
    viewer={viewer}
    editor={editors[index]}
    isEditing={isEditing}
  />,
    })).concat([{
      title: this.getEditButtons(data, editedData, isEditing,
        () => {
          this.setState(prevState => ({
            editedRows: toggleElement(prevState.editedRows, data),
          }));
        }),
    }]);
    return {
      cells: cellContents,
    };
  }

  /* eslint-disable react/no-array-index-key */
  // Use for SNAPSHOT testing
  renderViewer(data: T): JSX.Element {
    return <tr>{this.displayDataRow(data).map((c, index) => <td key={index}>{c}</td>)}</tr>;
  }

  // Use for SNAPSHOT testing
  renderEditor(data: T): JSX.Element {
    const editedData: T = { ...data };
    const setProperty = (key: keyof T, value: any) => editedData[key] = value;
    return <tr>{this.editDataRow(editedData, setProperty).map((c, index) => <td key={index}>{c}</td>)}</tr>;
  }
  /* eslint-enable react/no-array-index-key */


  onSort(event: any, index: number, direction: any) {
    this.setState({
      editedRows: [],
      sortBy: {
        index,
        direction,
      },
    });
  }

  render() {
    const { t } = this.props;
    const setProperty = (key: keyof T, value: T[keyof T] | undefined) => {
      this.setState(prevState => ({ newRowData: { ...prevState.newRowData, [key]: value } }));
    };

    const additionalRows: IRow[] = (this.state.newRowData !== null)
      ? [
        {
          cells: this.editDataRow(this.state.newRowData, setProperty)
            .map(c => ({ title: c }))
            .concat([{ title: this.getAddButtons(this.state.newRowData) }]),
        },
      ] : [];
    const sorters = this.getSorters();
    const sortedRows = [...this.props.tableData];
    if (this.state.sortBy.index !== undefined) {
      sortedRows.sort(sorters[this.state.sortBy.index] as Sorter<T>);
      // @ts-ignore
      if (this.state.sortBy.direction === SortByDirection.desc) {
        sortedRows.reverse();
      }
    }

    const filteredRows = sortedRows.filter(this.state.currentFilter);
    const rowsOnPage = filteredRows.filter((row, index) => this.state.perPage * (this.state.page - 1) <= index
      && index < this.state.perPage * this.state.page).map(this.convertDataToTableRow);

    const rows = additionalRows.concat(rowsOnPage);

    const columns: ICell[] = this.props.columnTitles.map((t, index) => ({
      title: t,
      cellTransforms: [headerCol],
      props: {},
      transforms: (sorters[index] !== null) ? [sortable] : undefined,
    })).concat([{
      title: '', cellTransforms: [headerCol], props: {}, transforms: undefined,
    }]) as any;
    return (
      <>
        <Level
          gutter="sm"
          style={{
            padding: '5px 5px 5px 5px',
            backgroundColor: 'var(--pf-global--BackgroundColor--200)',
          }}
        >
          <LevelItem>
            <FilterComponent
              filter={this.getFilter()}
              onChange={currentFilter => this.setState({ editedRows: [], currentFilter })}
            />
          </LevelItem>
          <LevelItem style={{ display: 'flex' }}>
            <Button isDisabled={this.state.newRowData !== null} onClick={this.createNewRow}>{t("add")}</Button>
            <Pagination
              itemCount={filteredRows.length}
              perPage={this.state.perPage}
              page={this.state.page}
              onSetPage={this.onSetPage}
              widgetId="pagination-options-menu-top"
              onPerPageSelect={this.onPerPageSelect}
            />
          </LevelItem>
        </Level>
        <Table caption={this.props.title} sortBy={this.state.sortBy} onSort={this.onSort} cells={columns} rows={rows}>
          <TableHeader />
          <TableBody />
        </Table>
      </>
    );
  }
}

export default DataTable;
