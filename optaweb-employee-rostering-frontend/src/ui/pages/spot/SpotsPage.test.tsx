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
import { Spot } from 'domain/Spot';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Skill } from 'domain/Skill';
import { Map } from 'immutable';
import DomainObjectView from 'domain/DomainObjectView';
import { spotSelectors } from 'store/spot';
import { mockRedux } from 'setupTests';
import { SpotsPage, SpotRow, EditableSpotRow } from './SpotsPage';

const noSpotsStore = mockStore({
  spotList: {
    isLoading: false,
    spotMapById: Map(),
  },
}).store;

const twoSpotsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map<number, Skill>()
      .set(0, {
        id: 0,
        version: 0,
        tenantId: 0,
        name: 'Skill 1',
      }),
  },
  spotList: {
    isLoading: false,
    spotMapById: Map<number, DomainObjectView<Spot>>()
      .set(1, {
        id: 1,
        version: 0,
        tenantId: 0,
        name: 'Spot 1',
        requiredSkillSet: [],
      })
      .set(2, {
        id: 2,
        version: 0,
        tenantId: 0,
        name: 'Spot 2',
        requiredSkillSet: [0],
      }),
  },
}).store;

describe('Spots page', () => {
  it('should render correctly with no spots', () => {
    mockRedux(noSpotsStore);
    const spotsPage = shallow(<SpotsPage {...getRouterProps('/0/spot', {})} />);
    expect(toJson(spotsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few spots', () => {
    mockRedux(twoSpotsStore);
    const skillsPage = shallow(
      <SpotsPage {...getRouterProps('/0/spot', {})} />,
    );
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const spot = spotSelectors.getSpotById(twoSpotsStore.getState(), 2);
    mockRedux(twoSpotsStore);
    getRouterProps('/0/spot', {});
    const viewer = shallow(<SpotRow {...spot} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });
});
