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
import {Button} from '@patternfly/react-core';

export interface DataTableProps<T> {
  title: string;
  tenantId: number;
  columnTitles: string[];
  tableData: T[];
  createRow: (dataTable: DataTable<T>) => IRow;
  rowDataToRow: (rowData: T) => IRow;
}

interface DataTableState<T> {
  newRow: IRow | null;
  currentFilter: (rowData: T) => boolean;
}

export abstract class DataTable<T> extends React.Component<DataTableProps<T>, DataTableState<T>> {
  constructor(props: DataTableProps<T>) {
    super(props);
    this.createNewRow = this.createNewRow.bind(this);
    this.state = {newRow: null, currentFilter: (t) => true};
    this.createNewRow = this.createNewRow.bind(this);
    this.cancelAddingRow = this.cancelAddingRow.bind(this);
  }

  createNewRow() {
    this.setState({...this.state, newRow: this.props.createRow(this)});
  }

  cancelAddingRow() {
    this.setState({...this.state, newRow: null});
  }

  render() {
    const additionalRows: IRow[] = (this.state.newRow !== null)? [this.state.newRow] : [];
    const rows = additionalRows.concat(this.props.tableData
      .filter(this.state.currentFilter).map(this.props.rowDataToRow));
    const columns = this.props.columnTitles.map(t => { 
      return { title: t, cellTransforms: [headerCol], props: {} };
    }).concat([{title: '', cellTransforms: [headerCol], props: {}}]);
    return (
      <div>
        <Button onClick={this.createNewRow}>Add</Button>
        <Table caption={this.props.title} cells={columns} rows={rows}>
          <TableHeader />
          <TableBody />
        </Table>
      </div>
    );
  }
}

export default DataTable
