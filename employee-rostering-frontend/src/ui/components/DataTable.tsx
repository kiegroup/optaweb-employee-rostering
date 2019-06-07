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
  ICell,
  IRow,
  IAction
} from '@patternfly/react-table';

export interface DataTableProps<T> {
  title: string;
  columnTitles: string[];
  tableData: T[];
  rowDataToRow: (rowData : T) => IRow;
}

interface DataTableState<T> {
  currentFilter: (_: T) => boolean;
}

export abstract class DataTable<T, Props extends DataTableProps<T>> extends React.Component<Props, DataTableState<T>> {
  columns: ICell[];
  actions: IAction[];

  constructor(props: Props) {
    super(props);
    this.columns = props.columnTitles.map(t => { return { title: t, cellTransforms: [headerCol], props: {} }; });
    this.actions = [
      {
        title: 'Edit',
        onClick: (event, rowId, rowData, extra) => {}
      },
      {
        title: 'Delete',
        onClick: (event, rowId, rowData, extra) => {}
      }
    ];
    this.state = {currentFilter: (t) => true};
  }

  render() {
    const rows = this.props.tableData.filter(this.state.currentFilter).map(this.props.rowDataToRow);
    return (
      <Table caption={this.props.title} actions={this.actions} cells={this.columns} rows={rows}>
        <TableHeader />
        <TableBody />
      </Table>
    );
  }
}

export default DataTable
