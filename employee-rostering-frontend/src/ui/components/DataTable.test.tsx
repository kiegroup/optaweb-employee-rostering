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
import { mount, shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { DataTable, DataTableProps } from './DataTable';
import { EditableComponent } from './EditableComponent';

interface MockData {name: string; number: number}
class MockDataTable extends DataTable<MockData, DataTableProps<MockData>> {
  displayDataRow = jest.fn((data) => [<span key={0} id="viewer">{data.name}</span>,
    <span key={1}>{data.number}</span>]);
  editDataRow = jest.fn((data) => [<input key={0} id="editor" />,
    <input key={1} />]);
  // @ts-ignore
  isValid = jest.fn();
  getSorters = jest.fn(() => [null, null]);
  getFilters = jest.fn(() => []);

  updateData = jest.fn();
  addData = jest.fn();
  removeData = jest.fn();
}

describe('DataTable component', () => {
  it('should render correctly with no rows', () => {
    const dataTable = shallow(<MockDataTable {...noRows} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render correctly with a few rows', () => {
    const dataTable = shallow(<MockDataTable {...twoRows} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should call display data row and edit data row for each row in render', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.render();
    expect(dataTable.displayDataRow).toBeCalledTimes(2);
    expect(dataTable.displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect(dataTable.displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect(dataTable.editDataRow).toBeCalledTimes(2);
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0], expect.any(Function));
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1], expect.any(Function));
  });

  it('should render viewer initially', () => {
    const dataTable = new MockDataTable(twoRows);
    const table = shallow(dataTable.render());
    expect(toJson(table)).toMatchSnapshot();
  });

  it('should render editor for new row', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).createNewRow();

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(4);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(3, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(4, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(5);
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(3, {}, expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(4, twoRows.tableData[0], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(5, twoRows.tableData[1], expect.any(Function));
    expect(toJson(shallow(dataTable.instance().render() as JSX.Element))).toMatchSnapshot();
  });

  it('clicking edit button should edit row', () => {
    const dataTable = new MockDataTable(twoRows);
    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, false, toggleEditing));
    buttons.find('button[aria-label="Edit"]').simulate('click');
    expect(toggleEditing).toBeCalled();
  });

  it('clicking delete button should delete row', () => {
    const dataTable = new MockDataTable(twoRows);
    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, false, toggleEditing));
    buttons.find('button[aria-label="Delete"]').simulate('click');

    expect(dataTable.removeData).toBeCalled();
    expect(dataTable.removeData).toBeCalledWith(data);
  });

  it('clicking cancel button on edited row should stop editing', () => {
    const dataTable = new MockDataTable(twoRows);
    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, true, toggleEditing));
    buttons.find('button[aria-label="Cancel"]').simulate('click');

    expect(toggleEditing).toBeCalled();
  });

  it('clicking save button on edited row should not save if invalid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(false);
    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, true, toggleEditing));
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(data);
    expect(dataTable.updateData).not.toBeCalled();
    expect(toggleEditing).not.toBeCalled();
  });

  it('clicking save button on edited row should save if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(true);
    const oldValue: MockData = {name: "Hello", number: 1};
    const newValue: MockData = {name: "New Data", number: 2};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(oldValue, newValue, true, toggleEditing));
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(newValue);
    expect(dataTable.updateData).toBeCalled();
    expect(dataTable.updateData).toBeCalledWith(newValue);
    expect(toggleEditing).toBeCalled();
  });

  it('should remove new row when cancel button is clicked', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.cancelAddingRow = jest.fn();

    const dataStore: any = {};
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Cancel"]').simulate('click');
    expect(dataTable.cancelAddingRow).toBeCalled();
  });

  it('should not save new row when save button is clicked if invalid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(false);

    const dataStore: any = ['New Data', 1];
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.addData).toBeCalledTimes(0);
  });

  it('should save new row when save button is clicked if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(true);
    dataTable.cancelAddingRow = jest.fn();

    const dataStore: any = ['New Data', 1];
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.addData).toBeCalled();
    expect(dataTable.addData).toBeCalledWith(dataStore);
    expect(dataTable.cancelAddingRow).toBeCalled();
  });
});

const noRows: DataTableProps<MockData> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [],
};

const twoRows: DataTableProps<MockData> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [{name: "Some Data", number: 1}, {name: "More Data", number: 2}],
};
