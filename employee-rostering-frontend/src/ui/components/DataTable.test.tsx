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
  createNewDataInstance = jest.fn(() => ({name: '', number: 0}));
  displayDataRow = jest.fn((data) => [<span key={0} id="viewer">{data.name}</span>,
    <span key={1}>{data.number}</span>]);
  editDataRow = jest.fn((data) => [<input key={0} id="editor"/>,
    <input key={1} />]);
  isValid = jest.fn();

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
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect(dataTable.editDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
  });

  it('should render viewer initially', () => {
    const dataTable = new MockDataTable(twoRows);
    const table = shallow(dataTable.render());
    expect(toJson(table)).toMatchSnapshot();
  });

  it('should render editor for new row', () => {
    const dataTable = mount(<MockDataTable {...twoRows} />);
    (dataTable.instance() as MockDataTable).createNewRow();

    expect((dataTable.instance() as MockDataTable).createNewDataInstance).toBeCalled();
    expect((dataTable.instance() as MockDataTable).displayDataRow).toBeCalledTimes(4);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(3, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).displayDataRow).toHaveBeenNthCalledWith(4, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toBeCalledTimes(5);
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(1, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(2, twoRows.tableData[1]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(3, {name: '', number: 0});
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(4, twoRows.tableData[0]);
    expect((dataTable.instance() as MockDataTable).editDataRow).toHaveBeenNthCalledWith(5, twoRows.tableData[1]);
    expect(toJson(shallow(dataTable.instance().render() as JSX.Element))).toMatchSnapshot();
  });

  it('clicking edit button should edit row', () => {
    const dataTable = new MockDataTable(twoRows);
    const editableComponent1 = new EditableComponent({editor: <input />, viewer: <span>text 1</span>});
    const editableComponent2 = new EditableComponent({editor: <input />, viewer: <span>text 2</span>});
    const editableComponents: any[] = [editableComponent1, editableComponent2, null];
    const data: MockData = {name: "Hello", number: 1};
    const buttons = mount(dataTable.getEditButtons(data, {...data}, editableComponents));

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
    const data: MockData = {name: "Hello", number: 1};
    const buttons = mount(dataTable.getEditButtons(data, {...data}, editableComponents));

    buttons.find('button[aria-label="Delete"]').simulate('click');

    expect(dataTable.removeData).toBeCalled();
    expect(dataTable.removeData).toBeCalledWith(data);
  });

  it('clicking cancel button on edited row should stop editing', () => {
    const dataTable = new MockDataTable(twoRows);
    const editableComponent1 = new EditableComponent({editor: <input />, viewer: <span>text 1</span>});
    const editableComponent2 = new EditableComponent({editor: <input />, viewer: <span>text 2</span>});
    const editableComponents: any[] = [editableComponent1, editableComponent2, null];
    const data: MockData = {name: "Hello", number: 1};
    const buttons = mount(dataTable.getEditButtons(data, {...data}, editableComponents));

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
    const data: MockData = {name: "Hello", number: 1};
    const buttons = mount(dataTable.getEditButtons(data, {...data}, editableComponents));

    buttons.find(EditableComponent).instance().setState({isEditing: true});
    buttons.update();
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(data);
    expect(dataTable.updateData).toBeCalledTimes(0);
  });

  it('clicking save button on edited row should save if valid', () => {
    const dataTable = new MockDataTable(twoRows);
    dataTable.isValid.mockReturnValue(true);
    const editableComponents: any[] = [null];
    const oldValue: MockData = {name: "Hello", number: 1};
    const newValue: MockData = {name: "New Data", number: 2};
    const buttons = mount(dataTable.getEditButtons(oldValue, newValue, editableComponents));

    buttons.find(EditableComponent).instance().setState({isEditing: true});
    buttons.update();

    // In test, buttons is not an EditableComponent (despite being an EditableComponent),
    // so override it
    const editableComponentMock = new EditableComponent({editor: <input />, viewer: <span>text 3</span>});
    editableComponents[0] = editableComponentMock;
    editableComponentMock.stopEditing = jest.fn();
    buttons.find('button[aria-label="Save"]').simulate('click');

    expect(dataTable.isValid).toBeCalled();
    expect(dataTable.isValid).toBeCalledWith(newValue);
    expect(dataTable.updateData).toBeCalled();
    expect(dataTable.updateData).toBeCalledWith(newValue);
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