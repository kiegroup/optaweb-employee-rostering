import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { stringSorter } from 'util/CommonSorters';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { usePageableData } from 'util/FunctionalComponentUtils';
import { CloseIcon, EditIcon, SaveIcon, TrashIcon } from '@patternfly/react-icons';
import { List } from 'immutable';
import { Button, Pagination } from '@patternfly/react-core';
import { Th } from '@patternfly/react-table';
import { FilterComponent } from './FilterComponent';
import {
  DataTable, DataTableProps, PaginationControls, RowEditButtons,
  RowViewButtons, setSorterInUrl, TableCell, TableRow,
} from './DataTable';

describe('RowViewButtons', () => {
  it('should call onEdit when the edit icon is clicked', () => {
    const onDelete = jest.fn();
    const onEdit = jest.fn();
    const viewButtons = shallow(<RowViewButtons onEdit={onEdit} onDelete={onDelete} />);
    viewButtons.findWhere(component => component.children(EditIcon).length > 0).simulate('click');
    expect(onEdit).toBeCalled();
    expect(onDelete).not.toBeCalled();
  });

  it('should call onDelete when the trash icon is clicked', () => {
    const onDelete = jest.fn();
    const onEdit = jest.fn();
    const viewButtons = shallow(<RowViewButtons onEdit={onEdit} onDelete={onDelete} />);
    viewButtons.findWhere(component => component.children(TrashIcon).length > 0).simulate('click');
    expect(onEdit).not.toBeCalled();
    expect(onDelete).toBeCalled();
  });

  it('should render correctly', () => {
    const onDelete = jest.fn();
    const onEdit = jest.fn();
    const viewButtons = shallow(<RowViewButtons onEdit={onEdit} onDelete={onDelete} />);
    expect(viewButtons).toMatchSnapshot();
  });
});

describe('RowEditButtons', () => {
  it('should call onClose when the close icon is clicked', () => {
    const onClose = jest.fn();
    const onSave = jest.fn();
    const editButtons = shallow(<RowEditButtons isValid onSave={onSave} onClose={onClose} />);
    editButtons.findWhere(component => component.children(CloseIcon).length > 0).simulate('click');
    expect(onClose).toBeCalled();
    expect(onSave).not.toBeCalled();
  });

  it('should call onSave and onClose when the save icon is clicked', () => {
    const onClose = jest.fn();
    const onSave = jest.fn();
    const editButtons = shallow(<RowEditButtons isValid onSave={onSave} onClose={onClose} />);
    editButtons.findWhere(component => component.children(SaveIcon).length > 0).simulate('click');

    // saving invokes both onClose and onSave
    expect(onClose).toBeCalled();
    expect(onSave).toBeCalled();
  });

  it('save button should be disabled if it is invalid', () => {
    const onClose = jest.fn();
    const onSave = jest.fn();
    const editButtons = shallow(<RowEditButtons isValid={false} onSave={onSave} onClose={onClose} />);
    expect(editButtons.findWhere(component => component.children(SaveIcon).length > 0)
      .prop('isDisabled')).toEqual(true);
  });

  it('save button should not be disabled if it is valid', () => {
    const onClose = jest.fn();
    const onSave = jest.fn();
    const editButtons = shallow(<RowEditButtons isValid onSave={onSave} onClose={onClose} />);
    expect(editButtons.findWhere(component => component.children(SaveIcon).length > 0)
      .prop('isDisabled')).toEqual(false);
  });

  it('should render correctly', () => {
    const onClose = jest.fn();
    const onSave = jest.fn();
    const editButtons = shallow(<RowEditButtons isValid onSave={onSave} onClose={onClose} />);
    expect(editButtons).toMatchSnapshot();
  });
});

describe('PaginationControls', () => {
  it('should render correctly', () => {
    const routerProps = getRouterProps('/table', {});
    const paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={1}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );
    expect(paginationControls).toMatchSnapshot();
  });

  it('changing filter should change props in the url', () => {
    const routerProps = getRouterProps('/table', {});
    const paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={1}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );

    const newFilter = 'New Text';
    paginationControls.find(FilterComponent).simulate('change', newFilter);
    const searchParams = new URLSearchParams(routerProps.location.search);
    searchParams.set('page', '1');
    searchParams.set('filter', newFilter);
    expect(routerProps.history.push).toBeCalledWith(`/table?${searchParams.toString()}`);
  });

  it('clicking the add button should call onCreateNewRow', () => {
    const routerProps = getRouterProps('/table', {});
    const onCreateNewRow = jest.fn();
    const paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={1}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={onCreateNewRow}
      />,
    );
    expect(paginationControls.find(Button).prop('isDisabled')).toEqual(false);
    paginationControls.find(Button).simulate('click');
    expect(onCreateNewRow).toBeCalled();
  });

  it('add button should be disabled if is creating new row', () => {
    const routerProps = getRouterProps('/table', {});
    const onCreateNewRow = jest.fn();
    const paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={1}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow
        onCreateNewRow={onCreateNewRow}
      />,
    );
    expect(paginationControls.find(Button).prop('isDisabled')).toEqual(true);
  });

  it('setting page should change props in the url', () => {
    const routerProps = getRouterProps('/table', {});
    const paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={1}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );

    const newPage = 3;
    paginationControls.find(Pagination).simulate('setPage', null, newPage);
    const searchParams = new URLSearchParams(routerProps.location.search);
    searchParams.set('page', `${newPage}`);
    expect(routerProps.history.push).toBeCalledWith(`/table?${searchParams.toString()}`);
  });

  it('setting items per page should change props in the url', () => {
    const routerProps = getRouterProps('/table', {});
    let paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={2}
        itemsPerPage={10}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );

    let newItemsPerPage = 5;
    paginationControls.find(Pagination).simulate('perPageSelect', null, newItemsPerPage);
    let searchParams = new URLSearchParams(routerProps.location.search);
    // first item on page 2 with 10 per page is the 11th item;
    // the 11th item is on the 3rd page with 5 per page
    searchParams.set('page', `${3}`);
    searchParams.set('itemsPerPage', `${newItemsPerPage}`);
    expect(routerProps.history.push).toBeCalledWith(`/table?${searchParams.toString()}`);
    jest.clearAllMocks();

    paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={3}
        itemsPerPage={5}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );
    newItemsPerPage = 10;
    paginationControls.find(Pagination).simulate('perPageSelect', null, newItemsPerPage);
    searchParams = new URLSearchParams(routerProps.location.search);
    // first item on page 3 with 5 per page is the 11th item;
    // the 11th item is on the 2nd page with 10 per page
    searchParams.set('page', `${2}`);
    searchParams.set('itemsPerPage', `${newItemsPerPage}`);
    expect(routerProps.history.push).toBeCalledWith(`/table?${searchParams.toString()}`);
    jest.clearAllMocks();

    paginationControls = shallow(
      <PaginationControls
        filterText="My filter"
        page={5}
        itemsPerPage={3}
        filteredRows={List()}
        rowsInPage={List()}
        numOfFilteredRows={100}
        isReversed={false}
        {...routerProps}
        isCreatingNewRow={false}
        onCreateNewRow={jest.fn()}
      />,
    );
    newItemsPerPage = 10;
    paginationControls.find(Pagination).simulate('perPageSelect', null, newItemsPerPage);
    searchParams = new URLSearchParams(routerProps.location.search);
    // first item on page 5 with 3 per page is the 13th item;
    // the 13th item is on the 2nd page with 10 per page
    searchParams.set('page', `${2}`);
    searchParams.set('itemsPerPage', `${newItemsPerPage}`);
    expect(routerProps.history.push).toBeCalledWith(`/table?${searchParams.toString()}`);
  });
});

describe('DataTable component', () => {
  interface MockData {name: string; number: number}

  const tableProps: DataTableProps<MockData> = {
    title: 'Data Table',
    columns: [{ name: 'Name' }, { name: 'Number', sorter: (a, b) => b.number - a.number }],
    sortByIndex: 0,
    onSorterChange: jest.fn(),
    rowWrapper: (row: MockData) => (
      <TableRow key={row.name}>
        <TableCell columnName="Name">{row.name}</TableCell>
        <TableCell columnName="Number">{row.number}</TableCell>
      </TableRow>
    ),
    newRowWrapper: removeRow => (
      <TableRow>
        <TableCell columnName="Name">New Data Name</TableCell>
        <TableCell columnName="Number">New Data Number</TableCell>
        <TableCell columnName="Number">
          <Button onClick={removeRow}>Remove</Button>
        </TableCell>
      </TableRow>
    ),
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

  it('should render correctly with no rows', () => {
    const dataTable = shallow(<DataTable {...tableProps} {...routerProps} {...useTableRows([])} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render correctly with a few rows', () => {
    const dataTable = shallow(<DataTable {...tableProps} {...routerProps} {...useTableRows(exampleData)} />);
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should render correctly when adding a row', () => {
    const dataTable = shallow(<DataTable {...tableProps} {...routerProps} {...useTableRows(exampleData)} />);
    dataTable.find(PaginationControls).simulate('createNewRow');
    expect(toJson(dataTable)).toMatchSnapshot();

    dataTable.find(TableRow).filterWhere(wrapper => wrapper.contains('New Data Name')).find(Button).simulate('click');
    expect(toJson(dataTable)).toMatchSnapshot();
  });

  it('should call onAddButtonClick if prop is set', () => {
    const onAddButtonClick = jest.fn();
    const dataTable = shallow(<DataTable
      onAddButtonClick={onAddButtonClick}
      {...tableProps}
      {...routerProps}
      {...useTableRows(exampleData)}
    />);
    dataTable.find(PaginationControls).simulate('createNewRow');
    expect(onAddButtonClick).toBeCalled();
  });

  it('should call onSorterChange when a header is clicked', () => {
    const dataTable = shallow(<DataTable {...tableProps} {...routerProps} {...useTableRows(exampleData)} />);
    const header = dataTable.find(Th).filterWhere(wrapper => wrapper.contains('Number'));
    (header.prop('sort')?.onSort as Function)();

    expect(tableProps.onSorterChange).toBeCalledWith(1);
  });

  it('setSorterInUrl should set sortBy and asc', () => {
    setSorterInUrl(routerProps, { asc: null }, 0, 1);
    expect(routerProps.history.push).toBeCalledWith('/table?sortBy=1&asc=true');

    jest.clearAllMocks();

    setSorterInUrl(routerProps, { asc: 'true' }, 1, 1);
    expect(routerProps.history.push).toBeCalledWith('/table?sortBy=1&asc=false');

    jest.clearAllMocks();

    setSorterInUrl(routerProps, { asc: 'false' }, 1, 1);
    expect(routerProps.history.push).toBeCalledWith('/table?sortBy=1&asc=true');
  });
});
