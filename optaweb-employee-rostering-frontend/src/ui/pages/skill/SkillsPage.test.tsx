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
import { Skill } from 'domain/Skill';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Map } from 'immutable';
import { mockRedux } from 'setupTests';
import { SkillsPage, SkillRow, EditableSkillRow } from './SkillsPage';

const noSkillsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
}).store;

const twoSkillsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map<number, Skill>()
      .set(0, {
        id: 0,
        version: 0,
        tenantId: 0,
        name: 'Skill 1',
      })
      .set(1,
        {
          id: 1,
          version: 0,
          tenantId: 0,
          name: 'Skill 2',
        }),
  },
}).store;

describe('Skills page', () => {
  it('should render correctly with no skills', () => {
    mockRedux(noSkillsStore);
    const skillsPage = shallow(<SkillsPage {...getRouterProps('/0/skill', {})} />);
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few skills', () => {
    mockRedux(twoSkillsStore);
    const skillsPage = shallow(
      <SkillsPage {...getRouterProps('/0/skill', {})} />,
    );
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const viewer = shallow(<SkillRow {...skill} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });
});
