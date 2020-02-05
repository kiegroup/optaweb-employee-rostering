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
import { act } from 'react-dom/test-utils';
import { Tenant } from 'domain/Tenant';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { DataTableUrlProps } from 'ui/components/DataTable';
import { Button } from '@patternfly/react-core';
import { AdminPage, Props } from './AdminPage';

describe('Admin Page', () => {
  it('should render correctly with no tenants', () => {
    const noTenants = generateProps(0, {});
    const adminPage = shallow(<AdminPage {...noTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with 2 tenants', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with many tenants', () => {
    const manyTenants = generateProps(20, {});
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should go to the correct page when the page is changed', () => {
    const manyTenants = generateProps(20, { page: '2' });
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should show the desired number of tenants per page', () => {
    const manyTenants = generateProps(20, { itemsPerPage: '5' });
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should filter by name', () => {
    const manyTenants = generateProps(20, { filter: '5' });
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should display modal when the Add Tenant button is clicked', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      adminPage.find('[aria-label="Add Tenant"]').simulate('click');
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });


  it('should close the modal when the modal is closed', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      adminPage.find('[aria-label="Add Tenant"]').simulate('click');
      adminPage.find('[aria-label="Add Tenant Modal"]').simulate('close');
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should call remove tenant when the delete tenant button is clicked', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      shallow((adminPage.find('[caption="Trans(i18nKey=tenants)"]').prop('rows') as unknown as any[]
      )[0].cells[1]).find(Button).simulate('click');
    });
    expect(twoTenants.removeTenant).toBeCalled();
    expect(twoTenants.removeTenant).toBeCalledWith(twoTenants.tenantList[0]);
  });

  it('should show confirm dialog when the reset button is clicked', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    adminPage.find('[data-cy="reset-application"]').simulate('click');
    expect(twoTenants.resetApplication).not.toBeCalled();
    expect(adminPage).toMatchSnapshot();
  });

  it('confirm dialog should reset application', () => {
    const twoTenants = generateProps(2, {});
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    adminPage.find("ConfirmDialog[title='Trans(i18nKey=confirmResetTitle)']").simulate('confirm');
    expect(twoTenants.resetApplication).toBeCalled();
  });
});

function generateProps(numberOfTenants: number, urlProps: Partial<DataTableUrlProps>): Props {
  const tenants: Tenant[] = new Array(numberOfTenants);
  for (let i = 0; i < numberOfTenants; i += 1) {
    tenants[i] = { name: `Tenant ${i + 1}`, id: i, version: 0 };
  }
  return {
    tenantList: tenants,
    removeTenant: jest.fn(),
    resetApplication: jest.fn(),
    ...getRouterProps('/admin', { ...urlProps as any }),
  };
}
