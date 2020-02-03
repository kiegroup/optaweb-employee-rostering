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
import { Sorter, ReadonlyPartial } from 'types';
import { useTranslation } from 'react-i18next';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { SkillsPage, Props } from './SkillsPage';

describe('Skills page', () => {
  it('should render correctly with no skills', () => {
    const skillsPage = shallow(<SkillsPage {...noSkills} />);
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few skills', () => {
    const skillsPage = shallow(<SkillsPage {...twoSkills} />);
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    const viewer = shallow(skillsPage.renderViewer(skill));
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    const editor = shallow(skillsPage.renderEditor(skill));
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('should update properties on change', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const setProperty = jest.fn();
    const editor = skillsPage.editDataRow({}, setProperty);
    const nameCol = shallow(editor[0]);
    nameCol.simulate('change', 'Test');
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('name', 'Test');
  });

  it('should an empty object on getInitialStateForNewRow', () => {
    const skillsPage = new SkillsPage(twoSkills);
    expect(skillsPage.getInitialStateForNewRow()).toEqual({});
  });

  it('should call addSkill on addData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = { name: 'Skill', tenantId: 0 };
    skillsPage.addData(skill);
    expect(twoSkills.addSkill).toBeCalled();
    expect(twoSkills.addSkill).toBeCalledWith(skill);
  });

  it('should call updateSkill on updateData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    skillsPage.updateData(skill);
    expect(twoSkills.updateSkill).toBeCalled();
    expect(twoSkills.updateSkill).toBeCalledWith(skill);
  });

  it('should call removeSkill on removeData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    skillsPage.removeData(skill);
    expect(twoSkills.removeSkill).toBeCalled();
    expect(twoSkills.removeSkill).toBeCalledWith(skill);
  });

  it('should return a filter that match by name', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const filter = skillsPage.getFilter();

    expect(twoSkills.tableData.filter(filter('1'))).toEqual([twoSkills.tableData[0]]);
    expect(twoSkills.tableData.filter(filter('2'))).toEqual([twoSkills.tableData[1]]);
  });

  it('should return a sorter that sort by name', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const sorter = skillsPage.getSorters()[0] as Sorter<Skill>;
    const list = [twoSkills.tableData[1], twoSkills.tableData[0]];
    expect(list.sort(sorter)).toEqual(twoSkills.tableData);
  });

  it('should treat incompleted data as incomplete', () => {
    const skillsPage = new SkillsPage(twoSkills);

    const noName: ReadonlyPartial<Skill> = { tenantId: 0 };
    const result1 = skillsPage.isDataComplete(noName);
    expect(result1).toEqual(false);

    const completed: ReadonlyPartial<Skill> = { tenantId: 0, name: 'Name' };
    const result2 = skillsPage.isDataComplete(completed);
    expect(result2).toEqual(true);
  });

  it('should treat empty name as invalid', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const components: Skill = { tenantId: 0, name: '' };
    const result = skillsPage.isValid(components);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const components: Skill = { tenantId: 0, name: 'Skill' };
    const result = skillsPage.isValid(components);
    expect(result).toEqual(true);
  });
});

const noSkills: Props = {
  ...useTranslation(),
  tReady: true,
  tenantId: 0,
  title: 'Skills',
  columnTitles: ['Name'],
  tableData: [],
  addSkill: jest.fn(),
  updateSkill: jest.fn(),
  removeSkill: jest.fn(),
  ...getRouterProps('/skills', {}),
};

const twoSkills: Props = {
  ...useTranslation(),
  tReady: true,
  tenantId: 0,
  title: 'Skills',
  columnTitles: ['Name'],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: 'Skill 1',
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: 'Skill 2',
  }],
  addSkill: jest.fn(),
  updateSkill: jest.fn(),
  removeSkill: jest.fn(),
  ...getRouterProps('/skills', {}),
};
