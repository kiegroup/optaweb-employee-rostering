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
