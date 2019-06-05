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
  sortable,
  SortByDirection,
  headerCol,
  TableVariant,
  expandable,
  cellWidth,
  ICell
} from '@patternfly/react-table';

interface DataTableProps {
  title: string;
  columnTitles: string[];
  dataSupplier: (_: ((_: string[][]) => void)) => void;
}

interface DataTableState {
  columns : ICell[];
  data : string[][];
  actions : {'title' : string, 'onClick' : (event: any, rowId: number, rowData: string[], extra: any) => void}[];
  currentFilter : (_: string[]) => boolean;
}

class DataTable extends React.Component<DataTableProps, DataTableState> {
  tableState : DataTableState;

  constructor(props : DataTableProps) {
    super(props);
    let columns : ICell[] = props.columnTitles.map(t => {return {title: t, cellTransforms: [headerCol] , props: {}};});
    this.tableState = {
      columns: columns,
      data: [],
      actions: [
        {
          title: 'Edit',
          onClick: (event : any, rowId : number, rowData : string[], extra : any) => console.log('clicked on Some action, on row: ', rowId)
        },
        {
          title: 'Delete',
          onClick: (event : any, rowId : number, rowData : string[], extra : any) => console.log('clicked on Another action, on row: ', rowId)
        }
      ],
      currentFilter: (row) => true
    };
    this.refresh();
  }

  refresh() {
      this.props.dataSupplier(rows => {
        this.tableState = {
          columns: this.tableState.columns,
          data: rows,
          actions: this.tableState.actions,
          currentFilter: this.tableState.currentFilter
         };
        this.setState(this.tableState);
      })
  }

  render() {
    const { columns, actions } = this.tableState;
    const rows = this.tableState.data.filter(this.tableState.currentFilter);
    return (
      <Table caption={this.props.title} actions={actions} cells={columns} rows={rows}>
        <TableHeader />
        <TableBody />
      </Table>
    );
  }
}

export default DataTable
