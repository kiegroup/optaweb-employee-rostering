
import { alert } from 'store/alert';
import { createIdMapFromList } from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete } from 'store/rest/RestTestUtils';
import { Skill } from 'domain/Skill';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { skillSelectors, skillOperations } from './index';

const state: Partial<AppState> = {
  skillList: {
    isLoading: false,
    skillMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: 'Skill 2',
      },
      {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: 'Skill 3',
      },
    ]),
  },
};

describe('Skill operations', () => {
  it('should dispatch actions and call client on refresh skill list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockSkillList = [{
      tenantId,
      id: 0,
      version: 0,
      name: 'Skill 1',
    },
    {
      tenantId,
      id: 1,
      version: 0,
      name: 'Skill 2',
    },
    {
      tenantId,
      id: 2,
      version: 0,
      name: 'Skill 3',
    }];

    onGet(`/tenant/${tenantId}/skill/`, mockSkillList);
    await store.dispatch(skillOperations.refreshSkillList());
    expect(store.getActions()).toEqual([actions.setIsSkillListLoading(true),
      actions.refreshSkillList(mockSkillList),
      actions.setIsSkillListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/`);
  });

  it('should dispatch actions and call client on a successful delete skill', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToDelete: Skill = { tenantId, name: 'test', id: 12345, version: 0 };
    onDelete(`/tenant/${tenantId}/skill/${skillToDelete.id}`, true);
    await store.dispatch(skillOperations.removeSkill(skillToDelete));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeSkill', { name: skillToDelete.name }),
      actions.removeSkill(skillToDelete),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/${skillToDelete.id}`);
  });

  it('should call client but not dispatch actions on a failed delete skill', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToDelete: Skill = { tenantId, name: 'test', id: 12345, version: 0 };
    onDelete(`/tenant/${tenantId}/skill/${skillToDelete.id}`, false);
    await store.dispatch(skillOperations.removeSkill(skillToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeSkillError', { name: skillToDelete.name }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/${skillToDelete.id}`);
  });

  it('should dispatch actions and call client on add skill', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToAdd: Skill = { tenantId, name: 'test' };
    const skillWithUpdatedId: Skill = { ...skillToAdd, id: 4, version: 0 };
    onPost(`/tenant/${tenantId}/skill/add`, skillToAdd, skillWithUpdatedId);
    await store.dispatch(skillOperations.addSkill(skillToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addSkill', { name: skillToAdd.name }),
      actions.addSkill(skillWithUpdatedId),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/add`, skillToAdd);
  });

  it('should dispatch actions and call client on update skill', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToUpdate: Skill = { tenantId, name: 'test', id: 4, version: 0 };
    const skillWithUpdatedVersion: Skill = { ...skillToUpdate, id: 4, version: 1 };
    onPost(`/tenant/${tenantId}/skill/update`, skillToUpdate, skillWithUpdatedVersion);
    await store.dispatch(skillOperations.updateSkill(skillToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('updateSkill', { id: skillToUpdate.id }),
      actions.updateSkill(skillWithUpdatedVersion),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/update`, skillToUpdate);
  });
});

describe('Skill reducers', () => {
  const addedSkill: Skill = { tenantId: 0, id: 4321, version: 0, name: 'Skill 1' };
  const updatedSkill: Skill = { tenantId: 0, id: 1234, version: 1, name: 'Updated Skill 2' };
  const deletedSkill: Skill = { tenantId: 0, id: 2312, version: 0, name: 'Skill 3' };
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('set is loading', () => {
    expect(
      reducer(storeState.skillList, actions.setIsSkillListLoading(true)),
    ).toEqual({ ...storeState.skillList, isLoading: true });
  });
  it('add skill', () => {
    expect(
      reducer(storeState.skillList, actions.addSkill(addedSkill)),
    ).toEqual({ ...storeState.skillList,
      skillMapById: storeState.skillList.skillMapById
        .set(addedSkill.id as number, addedSkill) });
  });
  it('remove skill', () => {
    expect(
      reducer(storeState.skillList, actions.removeSkill(deletedSkill)),
    ).toEqual({
      ...storeState.skillList,
      skillMapById: storeState.skillList.skillMapById.delete(deletedSkill.id as number),
    });
  });
  it('update skill', () => {
    expect(
      reducer(storeState.skillList, actions.updateSkill(updatedSkill)),
    ).toEqual({
      ...storeState.skillList,
      skillMapById: storeState.skillList.skillMapById.set(updatedSkill.id as number, updatedSkill),
    });
  });
  it('refresh skill list', () => {
    expect(
      reducer(storeState.skillList, actions.refreshSkillList([addedSkill])),
    ).toEqual({ ...storeState.skillList, skillMapById: createIdMapFromList([addedSkill]) });
  });
});

describe('Skill selectors', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();
  it('should throw an error if skill list is loading', () => {
    expect(() => skillSelectors.getSkillById({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    }, 1234)).toThrow();
  });

  it('should get a skill by id', () => {
    const skill = skillSelectors.getSkillById(storeState, 1234);
    expect(skill).toEqual({
      tenantId: 0,
      id: 1234,
      version: 0,
      name: 'Skill 2',
    });
  });

  it('should return an empty list if skill list is loading', () => {
    const skillList = skillSelectors.getSkillList({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    });
    expect(skillList).toEqual([]);
  });

  it('should return a list of all skills', () => {
    const skillList = skillSelectors.getSkillList(storeState);
    expect(skillList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: 'Skill 2',
      },
      {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: 'Skill 3',
      },
    ]));
    expect(skillList.length).toEqual(2);
  });
});
