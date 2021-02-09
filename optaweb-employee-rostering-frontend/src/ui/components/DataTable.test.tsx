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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { stringSorter } from 'util/CommonSorters';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { usePageableData } from 'util/FunctionalComponentUtils';
import { TheTable, TheTableProps } from './DataTable';

interface MockData {name: string; number: number}


const tableProps: TheTableProps<any> = {
  title: 'Data Table',
  columns: [{ name: 'Column 1' }, { name: 'Column 2' }],
  sortByIndex: 0,
  onSorterChange: jest.fn(),
  rowWrapper: jest.fn(),
};

const exampleData = [
  { name: 'Some Data', number: 1 },
  { name: 'More Data', number: 2 },
];

const routerProps = getRouterProps('/table', {});
const pageInfo = {
  page: '1',
  itemsPerPage: '5',
  filter: '',
  sortBy: '0',
  asc: 'true',
};

const useTableRows = (tableData: MockData[]) => usePageableData<MockData>(pageInfo,
  tableData, data => [data.name, `${data.number}`],
  stringSorter(data => data.name));

describe('DataTable component', () => {
  it('should render correctly with no rows', () => {
    const dataTable = shallow(<TheTable {...tableProps} {...routerProps} {...useTableRows([])} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render correctly with a few rows', () => {
    const dataTable = shallow(<TheTable {...tableProps} {...routerProps} {...useTableRows(exampleData)} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });
});
