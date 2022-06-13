import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Skill } from 'domain/Skill';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Map } from 'immutable';
import { mockRedux } from 'setupTests';
import { DataTable, RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { skillOperations } from 'store/skill';
import { doNothing } from 'types';
import { TextInput } from '@patternfly/react-core';
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
  const addSkill = (skill: Skill) => ['add', skill];
  const updateSkill = (skill: Skill) => ['update', skill];
  const removeSkill = (skill: Skill) => ['remove', skill];

  beforeEach(() => {
    jest.spyOn(skillOperations, 'addSkill').mockImplementation(skill => addSkill(skill) as any);
    jest.spyOn(skillOperations, 'updateSkill').mockImplementation(skill => updateSkill(skill) as any);
    jest.spyOn(skillOperations, 'removeSkill').mockImplementation(skill => removeSkill(skill) as any);
    jest.spyOn(twoSkillsStore, 'dispatch').mockImplementation(doNothing);
  });

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

  it('clicking on the edit button should show editor', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const viewer = shallow(<SkillRow {...skill} />);
    viewer.find(RowViewButtons).simulate('edit');

    expect(viewer).toMatchSnapshot();
    viewer.find(EditableSkillRow).simulate('close');
    expect(viewer).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(true);
  });

  it('no name should be invalid', () => {
    const skill = { name: '', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const skill = { name: 'Skill 1', tenantId: 0, id: 2, version: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new skill should call add skill', () => {
    const skill = { name: 'New Skill', tenantId: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew onClose={jest.fn()} />);
    const name = 'New Skill Name';
    editor.find(TextInput).simulate('change', name);
    editor.find(RowEditButtons).prop('onSave')();
    const newSkill = { ...skill, name };
    editor.find(RowEditButtons).prop('onSave')();
    expect(skillOperations.addSkill).toBeCalledWith(newSkill);
    expect(twoSkillsStore.dispatch).toBeCalledWith(addSkill(newSkill));
  });

  it('saving updated skill should call update skill', () => {
    const skill = { name: 'Updated Skill', tenantId: 0, id: 0, version: 0 };
    mockRedux(twoSkillsStore);
    const editor = shallow(<EditableSkillRow skill={skill} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(skillOperations.updateSkill).toBeCalledWith(skill);
    expect(twoSkillsStore.dispatch).toBeCalledWith(updateSkill(skill));
  });

  it('clicking on the edit button in the viewer should show the editor', () => {
    const skill = { name: 'Updated Skill', tenantId: 0, id: 0, version: 0 };
    mockRedux(twoSkillsStore);
    const viewer = shallow(<SkillRow {...skill} />);

    // Clicking the edit button should show the editor
    viewer.find(RowViewButtons).prop('onEdit')();
    expect(viewer).toMatchSnapshot();

    // Clicking the close button should show the viwer
    viewer.find(EditableSkillRow).prop('onClose')();
    expect(viewer).toMatchSnapshot();
  });

  it('deleting should call delete skill', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const viewer = shallow(<SkillRow {...skill} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(skillOperations.removeSkill).toBeCalledWith(skill);
    expect(twoSkillsStore.dispatch).toBeCalledWith(removeSkill(skill));
  });

  it('DataTable rowWrapper should be SkillRow', () => {
    const skill = { name: 'Skill', tenantId: 0, id: 1, version: 0 };
    mockRedux(twoSkillsStore);
    const skillsPage = shallow(<SkillsPage {...getRouterProps('/0/skill', {})} />);
    const rowWrapper = shallow(skillsPage.find(DataTable).prop('rowWrapper')(skill));
    expect(rowWrapper).toMatchSnapshot();
  });

  it('DataTable newRowWrapper should be EditableSkillRow', () => {
    mockRedux(twoSkillsStore);
    const skillsPage = shallow(<SkillsPage {...getRouterProps('/0/skill', {})} />);
    const removeRow = jest.fn();
    const newRowWrapper = shallow((skillsPage.find(DataTable).prop('newRowWrapper') as any)(removeRow));
    expect(newRowWrapper).toMatchSnapshot();
    newRowWrapper.find(RowEditButtons).prop('onClose')();
    expect(removeRow).toBeCalled();
  });
});
