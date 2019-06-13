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
import { SkillsPage, Props } from './SkillsPage';
import Skill from 'domain/Skill';

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
    const skill = {name: "Skill", tenantId: 0, id: 1, version: 0};
    const viewer = shallow(skillsPage.renderViewer(skill));
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = {name: "Skill", tenantId: 0, id: 1, version: 0};
    const editor = shallow(skillsPage.renderEditor(skill));
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('should return a skill with the tenantId but no name, id or version on createNewDataInstance', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = skillsPage.createNewDataInstance();
    expect(skill.tenantId).toEqual(twoSkills.tenantId);
    expect(skill.name).toEqual("");
    expect(skill.id).toBeUndefined();
    expect(skill.version).toBeUndefined();
  });

  it('should call addSkill on addData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = {name: "Skill", tenantId: 0};
    skillsPage.addData(skill);
    expect(twoSkills.addSkill).toBeCalled();
    expect(twoSkills.addSkill).toBeCalledWith(skill);
  });

  it('should call updateSkill on updateData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = {name: "Skill", tenantId: 0, id: 1, version: 0};
    skillsPage.updateData(skill);
    expect(twoSkills.updateSkill).toBeCalled();
    expect(twoSkills.updateSkill).toBeCalledWith(skill);
  });

  it('should call removeSkill on removeData', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const skill = {name: "Skill", tenantId: 0, id: 1, version: 0};
    skillsPage.removeData(skill);
    expect(twoSkills.removeSkill).toBeCalled();
    expect(twoSkills.removeSkill).toBeCalledWith(skill);
  });

  it('should treat empty name as invalid', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const components: Skill = {tenantId: 0, name: ""};
    const result = skillsPage.isValid(components);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const skillsPage = new SkillsPage(twoSkills);
    const components: Skill = {tenantId: 0, name: "Skill"};
    const result = skillsPage.isValid(components);
    expect(result).toEqual(true);
  });
});

const noSkills: Props = {
  tenantId: 0,
  title: "Skills",
  columnTitles: ["Name"],
  tableData: [],
  addSkill: jest.fn(),
  updateSkill: jest.fn(),
  removeSkill: jest.fn()
};

const twoSkills: Props = {
  tenantId: 0,
  title: "Skills",
  columnTitles: ["Name"],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: "Skill 1"
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: "Skill 2"
  }],
  addSkill: jest.fn(),
  updateSkill: jest.fn(),
  removeSkill: jest.fn()
};