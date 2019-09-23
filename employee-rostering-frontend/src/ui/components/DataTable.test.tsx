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
import { stringSorter} from 'util/CommonSorters';
import FilterComponent from './FilterComponent';
import { useTranslation } from 'react-i18next';

interface MockData {name: string; number: number}
class MockDataTable extends DataTable<MockData, DataTableProps<MockData>> {
  displayDataRow = jest.fn((data) => [<span key={0} id="viewer">{data.name}</span>,
    <span key={1}>{data.number}</span>]);
  getInitialStateForNewRow = jest.fn(() => ({}));
  editDataRow = jest.fn((data) => [<input key={0} id="editor" />,
    <input key={1} />]);
  isValid = jest.fn();
  isDataComplete = jest.fn() as any;
  getSorters = jest.fn(() => [null, stringSorter((d: MockData) => String(d.number))]);
  getFilter = jest.fn((() => (filter: string) => (t: MockData) => Boolean(true)));

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
    const table = shallow(<div>{dataTable.render()}</div>);
    expect(toJson(table)).toMatchSnapshot();
  });

  it('should set new row data to intial state if no row is being added', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.getInitialStateForNewRow.mockReturnValue({ name: "Hi" });
    dataTable.setState = jest.fn();
    dataTable.createNewRow();

    expect(dataTable.getInitialStateForNewRow).toBeCalled();
    expect(dataTable.setState).toBeCalled();
    expect(dataTable.setState).toBeCalledWith({ newRowData: {name: "Hi" } });
  });

  it('should not set new row data to intial state if no row is being added', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    dataTable.setState({ newRowData: { name: "Hi" } });
    (dataTable.instance() as MockDataTable).setState = jest.fn();
    (dataTable.instance() as MockDataTable).createNewRow();

    expect((dataTable.instance() as MockDataTable).getInitialStateForNewRow).not.toBeCalled();
    expect((dataTable.instance() as MockDataTable).setState).not.toBeCalled();
  });

  it('should render editor for new row', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).createNewRow();

    expect((dataTable.instance() as MockDataTable).getInitialStateForNewRow).toBeCalled();
    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(2);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(3);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, {}, expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(2, twoRows.tableData[0], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(3, twoRows.tableData[1], expect.any(Function));
    expect(toJson(shallow(<div>{dataTable.instance().render()}</div>))).toMatchSnapshot();
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

  it('should not save edited row when save button is clicked if incomplete', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isDataComplete.mockReturnValue(false);

    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, true, toggleEditing));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(data);
    expect(dataTable.isValid).not.toBeCalled();
    expect(dataTable.addData).not.toBeCalled();
  });

  it('clicking save button on edited row should not save if invalid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(false);
    dataTable.isDataComplete.mockReturnValue(true);
    const data: MockData = {name: "Hello", number: 1};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(data, {...data}, true, toggleEditing));
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(data);
    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(data);
    expect(dataTable.updateData).not.toBeCalled();
    expect(toggleEditing).not.toBeCalled();
  });

  it('clicking save button on edited row should save if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(true);
    dataTable.isDataComplete.mockReturnValue(true);
    const oldValue: MockData = {name: "Hello", number: 1};
    const newValue: MockData = {name: "New Data", number: 2};
    const toggleEditing = jest.fn();
    const buttons = mount(dataTable.getEditButtons(oldValue, newValue, true, toggleEditing));
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(newValue);
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

  it('should not save new row when save button is clicked if incomplete', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isDataComplete.mockReturnValue(false);

    const dataStore: any = ['New Data', 1];
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(dataStore);
    expect(dataTable.isValid).not.toBeCalled();
    expect(dataTable.addData).not.toBeCalled();
  });

  it('should not save new row when save button is clicked if invalid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isDataComplete.mockReturnValue(true);
    dataTable.isValid.mockReturnValue(false);

    const dataStore: any = ['New Data', 1];
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(dataStore);
    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.addData).not.toBeCalled();
  });

  it('should save new row when save button is clicked if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isDataComplete.mockReturnValue(true);
    dataTable.isValid.mockReturnValue(true);
    dataTable.cancelAddingRow = jest.fn();

    const dataStore: any = ['New Data', 1];
    const buttons = mount(dataTable.getAddButtons(dataStore));
    buttons.find('button[aria-label="Save"]').simulate('click');
    expect(dataTable.isDataComplete).toBeCalled();
    expect(dataTable.isDataComplete).toBeCalledWith(dataStore);
    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.addData).toBeCalled();
    expect(dataTable.addData).toBeCalledWith(dataStore);
    expect(dataTable.cancelAddingRow).toBeCalled();
  });

  it('should pass getFilter to the FilterComponent', () => {
    const dataTable = shallow(<MockDataTable {...twoRows} />);
    const filter = jest.fn();
    (dataTable.instance() as MockDataTable).getFilter.mockReturnValue(filter);
    (dataTable.instance() as MockDataTable).forceUpdate();

    const filterComponent = dataTable.find(FilterComponent);
    const filterMap = filterComponent.props().filter;
    expect(filterMap).toEqual(filter);
  });

  it('should only render rows that match filter', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).setState({ currentFilter: v => v.number === 2 });

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).displayDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[1], expect.any(Function));

    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should have perPage set to 10 and page set to 1 initially', () => {
    const dataTable = new MockDataTable(twoRows);
    expect(dataTable.state.perPage).toEqual(10);
    expect(dataTable.state.page).toEqual(1);
  });

  it('should update page on set page', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.setState = jest.fn();
    dataTable.onSetPage({}, 3);
    expect(dataTable.setState).toBeCalled();
    expect(dataTable.setState).toBeCalledWith({ editedRows: [], page: 3 });
  });

  it('should update perPage on set per page', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.setState = jest.fn();
    dataTable.onPerPageSelect({}, 50);
    expect(dataTable.setState).toBeCalled();
    expect(dataTable.setState).toBeCalledWith(expect.any(Function));
    const value = (dataTable.setState as jest.Mock).mock.calls[0][0]({ page: 2, perPage: 25 });
    expect(value.editedRows).toEqual([]);
    expect(value.perPage).toEqual(50);
    expect(value.page).toEqual(1);
  });

  it('should only render data on page', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).setState({ perPage: 1, page: 1 });

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[0], expect.any(Function));

    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).setState({ perPage: 1, page: 2 });

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(1);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[1], expect.any(Function));

    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should update sortBy on sort', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.setState = jest.fn();
    dataTable.onSort({}, 0, "asc");
    expect(dataTable.setState).toBeCalled();
    expect(dataTable.setState).toBeCalledWith({ editedRows: [], sortBy: { index: 0, direction: "asc" } });
  });

  it('should render rows in sorted order when ascending', () => {
    const dataTable = mount(<MockDataTable {...twoRows} tableData={[twoRows.tableData[1], twoRows.tableData[0]]} />);
    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).onSort({}, 1, "asc");

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(2);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(2);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[0], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(2, twoRows.tableData[1], expect.any(Function));

    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render rows in reverse of sorted order when descending', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).displayDataRow.mockClear();
    (dataTable.instance() as MockDataTable).editDataRow.mockClear();
    (dataTable.instance() as MockDataTable).onSort({}, 1, "desc");

    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(2);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(2);
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(1, twoRows.tableData[1], expect.any(Function));
    expect((dataTable.instance() as MockDataTable).editDataRow)
      .toHaveBeenNthCalledWith(2, twoRows.tableData[0], expect.any(Function));

    expect(toJson(dataTable)).toMatchSnapshot();
  });
});

const noRows: DataTableProps<MockData> = {
  ...useTranslation(),
  tReady: true,
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [],
};

const twoRows: DataTableProps<MockData> = {
  ...useTranslation(),
  tReady: true,
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [{name: "Some Data", number: 1}, {name: "More Data", number: 2}],
};
