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
} from '@patternfly/react-table';
import { Button, ButtonVariant, Pagination, Level, LevelItem } from '@patternfly/react-core';
import { SaveIcon, CloseIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import { Predicate, ReadonlyPartial, Sorter } from 'types';
import { toggleElement, Stream } from 'util/ImmutableCollectionOperations';
import { WithTranslation } from 'react-i18next';
import { getPropsFromUrl, setPropsInUrl, UrlProps } from 'util/BookmarkableUtils';
import { RouteComponentProps } from 'react-router';
import FilterComponent from './FilterComponent';
import { EditableComponent } from './EditableComponent';

export interface DataTableProps<T> extends WithTranslation, RouteComponentProps {
  title: string;
  columnTitles: string[];
  tableData: T[];
}

interface DataTableState<T> {
  newRowData: Partial<T> | null;
  editedRows: T[];
}

export type PropertySetter<T> = (propertyName: keyof T, value: T[keyof T] | undefined) => void;
export type DataTableUrlProps = UrlProps<'page'|'itemsPerPage'|'filter'|'sortBy'|'asc'>;

export abstract class DataTable<T, P extends DataTableProps<T>> extends React.Component<P, DataTableState<T>> {
  constructor(props: P) {
    super(props);
    this.createNewRow = this.createNewRow.bind(this);
    this.cancelAddingRow = this.cancelAddingRow.bind(this);
    this.convertDataToTableRow = this.convertDataToTableRow.bind(this);
    this.state = {
      editedRows: [],
      newRowData: null,
    };
    this.onSetPage = this.onSetPage.bind(this);
    this.onPerPageSelect = this.onPerPageSelect.bind(this);
    this.onSort = this.onSort.bind(this);
  }

  abstract getInitialStateForNewRow(): Partial<T>;

  abstract displayDataRow(data: T): React.ReactNode[];

  abstract editDataRow(data: ReadonlyPartial<T>, setProperty: PropertySetter<T>): React.ReactNode[];

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
    });
    setPropsInUrl<DataTableUrlProps>(this.props, { page: pageNumber.toString() });
  }

  onPerPageSelect(event: any, perPage: number, urlProps: DataTableUrlProps): void {
    this.setState({
      editedRows: [],
    });
    const oldPage = parseInt(urlProps.page as string, 10);
    const oldPerPage = parseInt(urlProps.itemsPerPage as string, 10);
    const newPage = Math.floor(((oldPage - 1) * oldPerPage) / perPage) + 1;
    setPropsInUrl<DataTableUrlProps>(this.props, {
      page: newPage.toString(),
      itemsPerPage: perPage.toString(),
    });
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
              display: 'grid',
              gridTemplateColumns: '1fr auto auto',
              gridColumnGap: '5px',
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
              display: 'grid',
              gridTemplateColumns: '1fr auto auto',
              gridColumnGap: '5px',
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
    const setProperty = (key: keyof T, value: any) => {
      editedData[key] = value;
    };

    return <tr>{this.editDataRow(editedData, setProperty).map((c, index) => <td key={index}>{c}</td>)}</tr>;
  }
  /* eslint-enable react/no-array-index-key */


  onSort(event: any, index: number, direction: any) {
    this.setState({
      editedRows: [],
    });

    setPropsInUrl<DataTableUrlProps>(this.props, {
      sortBy: index.toString(),
      asc: (direction === SortByDirection.asc).toString(),
    });
  }

  render() {
    const { t } = this.props;
    const setProperty = (key: keyof T, value: T[keyof T] | undefined) => {
      this.setState(prevState => ({ newRowData: { ...prevState.newRowData, [key]: value } }));
    };
    const urlProps = getPropsFromUrl<DataTableUrlProps>(this.props, {
      page: '1',
      itemsPerPage: '10',
      filter: null,
      sortBy: null,
      asc: 'true',
    });
    const [page, perPage] = [parseInt(urlProps.page as string, 10), parseInt(urlProps.itemsPerPage as string, 10)];
    const filterText = urlProps.filter ? urlProps.filter : '';
    const sortDirection: 'asc'|'desc' = urlProps.asc === 'true' ? 'asc' : 'desc';
    const sortBy = urlProps.sortBy ? { index: parseInt(urlProps.sortBy, 10), direction: sortDirection } : {};

    const additionalRows: IRow[] = (this.state.newRowData !== null)
      ? [
        {
          cells: this.editDataRow(this.state.newRowData, setProperty)
            .map(c => ({ title: c }))
            .concat([{ title: this.getAddButtons(this.state.newRowData) }]),
        },
      ] : [];
    const sorters = this.getSorters();

    const filteredRows = new Stream(this.props.tableData)
      // eslint-disable-next-line consistent-return
      .conditionally((s) => {
        if (urlProps.sortBy !== null) {
          return s.sort(sorters[parseInt(urlProps.sortBy, 10)] as Sorter<T>,
            urlProps.asc === 'true');
        }
      })
      // eslint-disable-next-line consistent-return
      .conditionally((s) => {
        if (urlProps.filter !== null) {
          return s.filter(this.getFilter()(urlProps.filter));
        }
      });

    const rowsThatMatchFilter = filteredRows.collect(c => c.length);

    const rows = additionalRows.concat(filteredRows
      .page(page, perPage)
      .map(this.convertDataToTableRow)
      .collect(c => c));

    const columns: ICell[] = this.props.columnTitles.map((title, index) => ({
      title,
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
              filterText={filterText}
              onChange={(newFilterText) => {
                this.setState({ editedRows: [] });
                setPropsInUrl(this.props, { page: '1', filter: newFilterText });
              }}
            />
          </LevelItem>
          <LevelItem style={{ display: 'flex' }}>
            <Button isDisabled={this.state.newRowData !== null} onClick={this.createNewRow}>{t('add')}</Button>
            <Pagination
              itemCount={rowsThatMatchFilter}
              perPage={perPage}
              page={page}
              onSetPage={this.onSetPage}
              widgetId="pagination-options-menu-top"
              onPerPageSelect={(e, newPerPage) => this.onPerPageSelect(e, newPerPage, urlProps)}
            />
          </LevelItem>
        </Level>
        <Table caption={this.props.title} sortBy={sortBy} onSort={this.onSort} cells={columns} rows={rows}>
          <TableHeader />
          <TableBody />
        </Table>
      </>
    );
  }
}

export default DataTable;
