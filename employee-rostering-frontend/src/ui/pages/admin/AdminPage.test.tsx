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
import { AdminPage, Props } from './AdminPage';
import { act } from 'react-dom/test-utils';
import Tenant from 'domain/Tenant';

describe('Admin Page', () => {
  it('should render correctly with no tenants', () => {
    const noTenants = generateProps(0);
    const adminPage = shallow(<AdminPage {...noTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with 2 tenants', () => {
    const twoTenants = generateProps(2);
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should render correctly with many tenants', () => {
    const manyTenants = generateProps(20);
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should go to the correct page when the page is changed', () => {
    const manyTenants = generateProps(20);
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    act(() => {
      adminPage.find('[aria-label="Change Page"]').simulate("setPage", {}, 2);
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should show the desired number of tenants per page', () => {
    const manyTenants = generateProps(20);
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    act(() => {
      adminPage.find('[aria-label="Change Page"]').simulate("perPageSelect", {}, 5);
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should filter by name', () => {
    const manyTenants = generateProps(20);
    const adminPage = shallow(<AdminPage {...manyTenants} />);
    act(() => {
      const filter = adminPage.find('[aria-label="Filter by Name"]');
      adminPage.find('[aria-label="Filter by Name"]').simulate("change",
        (filter.prop('filter') as unknown as Function)('5'));
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should display modal when the Add Tenant button is clicked', () => {
    const twoTenants = generateProps(2);
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      adminPage.find('[aria-label="Add Tenant"]').simulate('click');
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });


  it('should close the modal when the modal is closed', () => {
    const twoTenants = generateProps(2);
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      adminPage.find('[aria-label="Add Tenant"]').simulate('click');
      adminPage.find('[aria-label="Add Tenant Modal"]').simulate('close');
    });
    expect(toJson(adminPage)).toMatchSnapshot();
  });

  it('should call remove tenant when the delete tenant button is clicked', () => {
    const twoTenants = generateProps(2);
    const adminPage = shallow(<AdminPage {...twoTenants} />);
    act(() => {
      shallow((adminPage.find('[caption="Trans(i18nKey=tenants)"]').prop('rows') as unknown as any[]
      )[0].cells[1]).find('Button').simulate('click');
    });
    expect(twoTenants.removeTenant).toBeCalled();
    expect(twoTenants.removeTenant).toBeCalledWith(twoTenants.tenantList[0]);
  });
});

function generateProps(numberOfTenants: number): Props {
  const tenants: Tenant[] = new Array(numberOfTenants);
  for (let i = 0; i < numberOfTenants; i += 1) {
    tenants[i] = { name: `Tenant ${i + 1}`, id: i, version: 0 };
  }
  return {
    tenantList: tenants,
    removeTenant: jest.fn()
  }
}