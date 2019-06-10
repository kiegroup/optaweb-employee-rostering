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
import { DataTable, DataTableProps } from './DataTable';

describe('DataTable component', () => {
  it('should render correctly with no rows', () => {
    const dataTable = shallow(<DataTable {...noRows} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render correctly with a few rows', () => {
    const dataTable = shallow(<DataTable {...twoRows} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });
});

const noRows: DataTableProps<[string, number]> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [],
  // @ts-ignore
  createRow: jest.fn,
  // @ts-ignore
  rowDataToRow: jest.fn
};

const twoRows: DataTableProps<[string, number]> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [["Some Data", 1], ["More Data", 2]],
  // @ts-ignore
  createRow: jest.fn,
  // @ts-ignore
  rowDataToRow: (data) => {return {props: {}, cells: [data[0], data[1]]} }
};