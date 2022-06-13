import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Tenant } from 'domain/Tenant';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { DataTableUrlProps, DataTable } from 'ui/components/DataTable';
import { Button } from '@patternfly/react-core';
import { List } from 'immutable';
import { useSelector } from 'react-redux';
import { mockStore } from 'store/mockStore';
import { Store } from 'redux';
import { RouteComponentProps } from 'react-router';
import { mockRedux } from 'setupTests';
import * as adminOperations from 'store/admin/operations';
import * as tenantOperations from 'store/tenant/operations';
import { tenantSelectors } from 'store/tenant';
import { AppState } from 'store/types';
import { AdminPage, TenantRow } from './AdminPage';

describe('Admin Page', () => {
  beforeEach(() => {
    jest.spyOn(tenantOperations, 'removeTenant').mockImplementation(() => ({ type: '' }) as any);
    jest.spyOn(adminOperations, 'resetApplication').mockImplementation(() => ({ type: '' }) as any);
  });

  it('should render correctly with no tenants', () => {
    const noTenants = generateProps(0, {});
    mockRedux(noTenants.store);
    const adminPage = shallow(<AdminPage {...noTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with 2 tenants', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const adminPage = shallow(<AdminPage {...twoTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with many tenants', () => {
    const manyTenants = generateProps(20, {});
    mockRedux(manyTenants.store);
    const adminPage = shallow(<AdminPage {...manyTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should go to the correct page when the page is changed', () => {
    const manyTenants = generateProps(20, { page: '2' });
    mockRedux(manyTenants.store);
    const adminPage = shallow(<AdminPage {...manyTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should show the desired number of tenants per page', () => {
    const manyTenants = generateProps(20, { itemsPerPage: '5' });
    mockRedux(manyTenants.store);
    const adminPage = shallow(<AdminPage {...manyTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should filter by name', () => {
    const manyTenants = generateProps(20, { filter: '5' });
    mockRedux(manyTenants.store);
    const adminPage = shallow(<AdminPage {...manyTenants.props} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should display modal when the Add Tenant button is clicked', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const adminPage = shallow(<AdminPage {...twoTenants.props} />);
    adminPage.find(DataTable).simulate('addButtonClick');
    expect(toJson(adminPage)).toMatchSnapshot();
  });


  it('should close the modal when the modal is closed', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const adminPage = shallow(<AdminPage {...twoTenants.props} />);
    adminPage.find(DataTable).simulate('addButtonClick');
    adminPage.find('[aria-label="Add Tenant Modal"]').simulate('close');
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should call remove tenant when the delete tenant button is clicked', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const tenant = useSelector(tenantSelectors.getTenantList).get(1) as Tenant;
    const tenantRow = shallow(<TenantRow {...tenant} />);
    tenantRow.find(Button).simulate('click');
    expect(tenantOperations.removeTenant).toBeCalled();
    expect(tenantOperations.removeTenant).toBeCalledWith(twoTenants.store.getState().tenantData.tenantList.get(1));
  });

  it('should not call remove tenant when the delete tenant button is clicked for the current tenant', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const tenant = useSelector(tenantSelectors.getTenantList).get(0) as Tenant;
    const tenantRow = shallow(<TenantRow {...tenant} />);
    expect(tenantRow.find(Button).prop('isDisabled')).toEqual(true);
  });

  it('should show confirm dialog when the reset button is clicked', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const adminPage = shallow(<AdminPage {...twoTenants.props} />);
    adminPage.find('[data-cy="reset-application"]').simulate('click');
    expect(adminOperations.resetApplication).not.toBeCalled();
    expect(adminPage).toMatchSnapshot();
  });

  it('confirm dialog should reset application', () => {
    const twoTenants = generateProps(2, {});
    mockRedux(twoTenants.store);
    const adminPage = shallow(<AdminPage {...twoTenants.props} />);
    adminPage.find("ConfirmDialog[title='Trans(i18nKey=confirmResetTitle)']").simulate('confirm');
    expect(adminOperations.resetApplication).toBeCalled();
  });
});

function generateProps(numberOfTenants: number, urlProps: Partial<DataTableUrlProps>):
{ store: Store<AppState>; props: RouteComponentProps } {
  const tenants: Tenant[] = new Array(numberOfTenants);
  for (let i = 0; i < numberOfTenants; i += 1) {
    tenants[i] = { name: `Tenant ${i + 1}`, id: i, version: 0 };
  }
  return {
    store: mockStore({
      tenantData: {
        currentTenantId: 0,
        timezoneList: ['UTC'],
        tenantList: List(tenants),
      },
    }).store,
    props: getRouterProps('/admin', urlProps),
  };
}
