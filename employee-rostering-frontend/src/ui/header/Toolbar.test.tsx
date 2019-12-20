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
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { ToolbarComponent, Props } from './Toolbar';

describe('Toolbar Component', () => {
  beforeAll(() => {
    process.env.REACT_APP_BACKEND_URL = 'backend';
  });

  it('should render correctly with no tenants', () => {
    const toolbarComponent = shallow(<ToolbarComponent {...noTenants} />);
    expect(toJson(toolbarComponent)).toMatchSnapshot();
  });

  it('should render correctly with a few tenants', () => {
    const toolbarComponent = shallow(<ToolbarComponent {...twoTenants} />);
    expect(toJson(toolbarComponent)).toMatchSnapshot();
  });

  it('should render correctly when tenant select is open', () => {
    const toolbarComponent = shallow(<ToolbarComponent {...twoTenants} />);
    (toolbarComponent.instance() as ToolbarComponent).setIsTenantSelectOpen(true);
    expect(toJson(toolbarComponent)).toMatchSnapshot();
  });

  it('should refresh tenant list on mount', async () => {
    shallow(<ToolbarComponent {...twoTenants} />);
    expect(twoTenants.refreshTenantList).toBeCalled();
  });

  it('should call change tenant on tenant change', () => {
    const toolbarComponent = shallow(<ToolbarComponent {...twoTenants} />);
    (toolbarComponent.instance() as ToolbarComponent).setCurrentTenant(2);
    expect(twoTenants.changeTenant).toBeCalled();
    expect(twoTenants.changeTenant).toBeCalledWith({ routeProps: twoTenants, tenantId: 2 });
  });

  it('should redirect to admin page when you click on the gear', () => {
    const toolbarComponent = shallow(<ToolbarComponent {...twoTenants} />);
    toolbarComponent.find('[aria-label="Settings"]').simulate('click');
    expect(twoTenants.history.push).toBeCalled();
    expect(twoTenants.history.push).toBeCalledWith('/admin');
  });
});

const noTenants: Props = {
  tenantList: [],
  currentTenantId: 0,
  refreshTenantList: jest.fn(),
  changeTenant: jest.fn(),
  ...getRouterProps('/toolbar', {}),
};

const twoTenants: Props = {
  tenantList: [
    {
      id: 1,
      version: 0,
      name: 'Tenant 1',
    },
    {
      id: 2,
      version: 0,
      name: 'Tenant 2',
    },
  ],
  currentTenantId: 2,
  refreshTenantList: jest.fn(),
  changeTenant: jest.fn(),
  ...getRouterProps('/toolbar', {}),
};
