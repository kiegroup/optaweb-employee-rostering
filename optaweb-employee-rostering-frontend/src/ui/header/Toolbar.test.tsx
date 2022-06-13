import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { List } from 'immutable';
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
  tenantList: List(),
  currentTenantId: 0,
  refreshTenantList: jest.fn(),
  changeTenant: jest.fn(),
  ...getRouterProps('/toolbar', {}),
};

const twoTenants: Props = {
  tenantList: List([
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
  ]),
  currentTenantId: 2,
  refreshTenantList: jest.fn(),
  changeTenant: jest.fn(),
  ...getRouterProps('/toolbar', {}),
};
