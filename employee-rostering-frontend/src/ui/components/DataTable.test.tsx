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

type MockData = [string, number];
class MockDataTable extends DataTable<MockData, MockData, DataTableProps<MockData,MockData>> {
  displayDataRow = jest.fn((data) => [<span key={0} id="viewer">data[0]</span>,
    <span key={1}>data[1]</span>]);
  createNewDataRow = jest.fn(() =>  [<input key={0} id="editor"/>,
    <input key={1} />]);
  editDataRow = jest.fn((data) => [<input key={0} id="editor"/>,
    <input key={1} />]);
  isValid = jest.fn();
  extractDataFromRow = jest.fn((oldValue: MockData|{}, editedValue: MockData) => editedValue);
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
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(1, {}, twoRows.tableData[0]);
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(2, {}, twoRows.tableData[1]);
  });

  it('should render viewer initally', () => {
    const dataTable = new MockDataTable(twoRows);
    const table = shallow(dataTable.render());
    expect(toJson(table)).toMatchSnapshot();
  });

  it('should render editor for new row', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.setState = jest.fn();

    const table = shallow(dataTable.render());
    dataTable.createNewRow();
    expect(dataTable.setState).toBeCalled();
    expect(dataTable.createNewDataRow).toBeCalled();
    expect(dataTable.displayDataRow).toBeCalledTimes(2);
    expect(dataTable.displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect(dataTable.displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect(dataTable.editDataRow).toBeCalledTimes(2);
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(1, {}, twoRows.tableData[0]);
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(2, {}, twoRows.tableData[1]);
    expect(toJson(table)).toMatchSnapshot();
  });

  it('clicking edit button should edit row', () => {
    const dataTable = new MockDataTable(twoRows);
    const editableComponent1 = new EditableComponent({editor: <input />, viewer: <span>text 1</span>});
    const editableComponent2 = new EditableComponent({editor: <input />, viewer: <span>text 2</span>});
    const editableComponents: any[] = [editableComponent1, editableComponent2, null];
    const dataStore: any = {};
    const buttons = mount(dataTable.getEditButtons(dataStore, ["Hello", 1], editableComponents));

    // Verify the button added itself to the array (note: will not be an EditableComponent despite
    // the fact it is a EditableComponent)
    expect(editableComponents[2]).toBeTruthy();
    // In test, buttons is not an EditableComponent (despite being an EditableComponent),
    // so override it
    const editableComponent3 = new EditableComponent({editor: <input />, viewer: <span>text 3</span>});
    editableComponents[2] = editableComponent3;

    editableComponent1.startEditing = jest.fn();
    editableComponent2.startEditing = jest.fn();
    editableComponent3.startEditing = jest.fn();

    buttons.find('button[aria-label="Edit"]').simulate('click');

    expect(editableComponent1.startEditing).toBeCalled();
    expect(editableComponent2.startEditing).toBeCalled();
    expect(editableComponent3.startEditing).toBeCalled();
  });

  it('clicking delete button should delete row', () => {
    const dataTable = new MockDataTable(twoRows);
    const editableComponents: any[] = [null];
    const dataStore: any = {};
    const data = ["Hello", 1];
    const buttons = mount(dataTable.getEditButtons(dataStore, ["Hello", 1], editableComponents));

    buttons.find('button[aria-label="Delete"]').simulate('click');

    expect(dataTable.removeData).toBeCalled();
    expect(dataTable.removeData).toBeCalledWith(data);
  });

  it('clicking cancel button on edited row should stop editing', () => {
    const dataTable = new MockDataTable(twoRows);
    const editableComponent1 = new EditableComponent({editor: <input />, viewer: <span>text 1</span>});
    const editableComponent2 = new EditableComponent({editor: <input />, viewer: <span>text 2</span>});
    const editableComponents: any[] = [editableComponent1, editableComponent2, null];
    const dataStore: any = {};
    const buttons = mount(dataTable.getEditButtons(dataStore, ["Hello", 1], editableComponents));

    // Verify the button added itself to the array (note: will not be an EditableComponent despite
    // the fact it is a EditableComponent)
    expect(editableComponents[2]).toBeTruthy();
    buttons.find(EditableComponent).instance().setState({isEditing: true});
    buttons.update();

    // In test, buttons is not an EditableComponent (despite being an EditableComponent),
    // so override it
    const editableComponent3 = new EditableComponent({editor: <input />, viewer: <span>text 3</span>});
    editableComponents[2] = editableComponent3;
    editableComponent1.stopEditing = jest.fn();
    editableComponent2.stopEditing = jest.fn();
    editableComponent3.stopEditing = jest.fn();

    buttons.find('button[aria-label="Cancel"]').simulate('click');

    expect(editableComponent1.stopEditing).toBeCalled();
    expect(editableComponent2.stopEditing).toBeCalled();
    expect(editableComponent3.stopEditing).toBeCalled();
  });

  it('clicking save button on edited row should not save if invalid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(false);
    const editableComponents: any[] = [null];
    const dataStore: any = {};
    const buttons = mount(dataTable.getEditButtons(dataStore, ["Hello", 1], editableComponents));

    buttons.find(EditableComponent).instance().setState({isEditing: true});
    buttons.update();
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.updateData).toBeCalledTimes(0);
  });

  it('clicking save button on edited row should save if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(true);
    const editableComponents: any[] = [null];
    const dataStore: any = ["New Data", 10];
    const oldValue = ["Hello", 1];
    const buttons = mount(dataTable.getEditButtons(dataStore, ["Hello", 1], editableComponents));

    buttons.find(EditableComponent).instance().setState({isEditing: true});
    buttons.update();

    // In test, buttons is not an EditableComponent (despite being an EditableComponent),
    // so override it
    const editableComponentMock = new EditableComponent({editor: <input />, viewer: <span>text 3</span>});
    editableComponents[0] = editableComponentMock;
    editableComponentMock.stopEditing = jest.fn();
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(dataStore);
    expect(dataTable.updateData).toBeCalled();
    expect(dataTable.updateData).toBeCalledWith(dataStore);
    expect(dataTable.extractDataFromRow).toBeCalled();
    expect(dataTable.extractDataFromRow).toBeCalledWith(oldValue, dataStore);
    expect(editableComponentMock.stopEditing).toBeCalled();
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
    expect(dataTable.extractDataFromRow).toBeCalled();
    expect(dataTable.extractDataFromRow).toBeCalledWith({}, dataStore);
    expect(dataTable.cancelAddingRow).toBeCalled();
  });
});

const noRows: DataTableProps<[string, number], [string, number]> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [],
};

const twoRows: DataTableProps<[string, number], [string, number]> = {
  title: "Data Table",
  columnTitles: ["Column 1", "Column 2"],
  tableData: [["Some Data", 1], ["More Data", 2]],
};