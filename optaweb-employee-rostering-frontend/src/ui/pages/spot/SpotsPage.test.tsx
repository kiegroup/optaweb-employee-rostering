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
import { spotOperations, spotSelectors } from 'store/spot';
import { mockRedux } from 'setupTests';
import { RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { doNothing } from 'types';
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
  const addSpot = (spot: Spot) => ['add', spot];
  const updateSpot = (spot: Spot) => ['update', spot];
  const removeSpot = (spot: Spot) => ['remove', spot];

  beforeEach(() => {
    jest.spyOn(spotOperations, 'addSpot').mockImplementation(spot => addSpot(spot) as any);
    jest.spyOn(spotOperations, 'updateSpot').mockImplementation(spot => updateSpot(spot) as any);
    jest.spyOn(spotOperations, 'removeSpot').mockImplementation(spot => removeSpot(spot) as any);
    jest.spyOn(twoSpotsStore, 'dispatch').mockImplementation(doNothing);
  });

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

  it('no name should be invalid', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: '',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 3,
      name: 'Spot 1',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new spot should call add spot', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(spotOperations.addSpot).toBeCalledWith(spot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(addSpot(spot));
  });

  it('saving updated spot should call update spot', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(spotOperations.updateSpot).toBeCalledWith(spot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(updateSpot(spot));
  });

  it('deleting should call delete spot', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const viewer = shallow(<SpotRow {...spot} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(spotOperations.removeSpot).toBeCalledWith(spot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(removeSpot(spot));
  });
});
