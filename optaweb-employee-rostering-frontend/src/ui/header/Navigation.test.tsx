import { NavItem } from '@patternfly/react-core';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { Navigation, NavigationProps } from './Navigation';

describe('Navigation', () => {
  it('should activate a navigation link matching the current path', () => {
    const props: NavigationProps = {
      tenantId: 0,
      variant: 'horizontal',
      ...getRouterProps('/0/skills', {}),
    };

    const navigation = shallow(<Navigation {...props} />);
    expect(toJson(navigation)).toMatchSnapshot();

    // NavItem matching the path should be active
    const navItems = navigation.find(NavItem).filterWhere(navItem => navItem.props().itemId === 'skills');
    expect(navItems).toHaveLength(1);
    expect(navItems.at(0).props().isActive).toEqual(true);

    // Other NavItems should be inactive
    navigation.find(NavItem).filterWhere(navItem => navItem.props().itemId !== 'skills').forEach(
      navItem => expect(navItem.props().isActive).toEqual(false),
    );
  });
});
