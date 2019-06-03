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
  cellWidth
} from '@patternfly/react-table';

interface DataTableProps {
  columnTitles: string[];
  dataSupplier: () => string[][];
}

class DataTable extends React.Component<DataTableProps> {
  state : {'columns' : string[], 'rows' : string[][], 'actions' : {'title' : string, 'onClick' : any}[]};

  constructor(props : DataTableProps) {
    super(props);

    this.state = {
      columns: props.columnTitles,
      rows: props.dataSupplier(),
      actions: [
        {
          title: 'Edit',
          onClick: (event : any, rowId : number, rowData : string[], extra : any) => console.log('clicked on Some action, on row: ', rowId)
        },
        {
          title: 'Delete',
          onClick: (event : any, rowId : number, rowData : string[], extra : any) => console.log('clicked on Another action, on row: ', rowId)
        }
      ]
    };
  }

  render() {
    const { columns, rows, actions } = this.state;
    return (
      <Table caption="Actions Table" actions={actions} cells={columns} rows={rows}>
        <TableHeader />
        <TableBody />
      </Table>
    );
  }
}

export default DataTable
