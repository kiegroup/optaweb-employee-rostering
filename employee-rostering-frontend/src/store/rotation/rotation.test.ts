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

import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import { alert } from 'store/alert';
import reducer, { shiftTemplateSelectors, shiftTemplateOperations } from './index';
import { createIdMapFromList, mapWithElement, mapWithoutElement, 
  mapWithUpdatedElement } from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete } from 'store/rest/RestTestUtils';
import ShiftTemplate from 'domain/ShiftTemplate';

describe('Rotation operations', () => {
  it('should dispatch actions and call client on refresh shift template list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockSkillList = [{
      tenantId: tenantId,
      id: 0,
      version: 0,
      name: "Skill 1"
    },
    {
      tenantId: tenantId,
      id: 1,
      version: 0,
      name: "Skill 2"
    },
    {
      tenantId: tenantId,
      id: 2,
      version: 0,
      name: "Skill 3"
    }];

    onGet(`/tenant/${tenantId}/skill/`, mockSkillList);
    await store.dispatch(shiftTemplateOperations.refreshShiftTemplateList());
    expect(store.getActions()).toEqual([
      actions.setIsShiftTemplateListLoading(true),
      actions.refreshShiftTemplateList(mockSkillList),
      actions.setIsShiftTemplateListLoading(false)
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/`);
  });
  
  it('should dispatch actions and call client on a successful delete shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToDelete: ShiftTemplate = { tenantId: tenantId, name: "test", id: 12345, version: 0 };
    onDelete(`/tenant/${tenantId}/skill/${skillToDelete.id}`, true);
    await store.dispatch(shiftTemplateOperations.removeShiftTemplate(skillToDelete));
    expect(store.getActions()).toEqual([alert.showSuccessMessage("removeSkill", { name: skillToDelete.name }), actions.removeSkill(skillToDelete)]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/${skillToDelete.id}`);
  });

  it('should call client but not dispatch actions on a failed delete shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToDelete: ShiftTemplate = { tenantId: tenantId, name: "test", id: 12345, version: 0 };
    onDelete(`/tenant/${tenantId}/skill/${skillToDelete.id}`, false);
    await store.dispatch(shiftTemplateOperations.removeShiftTemplate(skillToDelete));
    expect(store.getActions()).toEqual([alert.showErrorMessage("removeSkillError", { name: skillToDelete.name })]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/${skillToDelete.id}`);
  });
    
  it('should dispatch actions and call client on add shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToAdd: ShiftTemplate = { tenantId: tenantId, name: "test" };
    const skillWithUpdatedId: ShiftTemplate = {...skillToAdd, id: 4, version: 0};
    onPost(`/tenant/${tenantId}/skill/add`, skillToAdd, skillWithUpdatedId);
    await store.dispatch(shiftTemplateOperations.addShiftTemplate(skillToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage("addSkill", { name: skillToAdd.name }),
      actions.addShiftTemplate(skillWithUpdatedId)
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/add`, skillToAdd);
  });

  it('should dispatch actions and call client on update shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const skillToUpdate: ShiftTemplate = { tenantId: tenantId, name: "test" , id: 4, version: 0 };
    const skillWithUpdatedVersion: ShiftTemplate = {...skillToUpdate, id: 4, version: 1};
    onPost(`/tenant/${tenantId}/skill/update`, skillToUpdate, skillWithUpdatedVersion);
    await store.dispatch(shiftTemplateOperations.updateShiftTemplate(skillToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage("updateSkill", { id: skillToUpdate.id }),
      actions.updateShiftTemplate(skillWithUpdatedVersion)
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/skill/update`, skillToUpdate);
  });
});

describe('Rotation reducers', () => {
  const addedShiftTemplate: ShiftTemplate = {tenantId: 0, id: 4321, version: 0, name: "Skill 1"};
  const updatedShiftTemplate: ShiftTemplate = {tenantId: 0, id: 1234, version: 1, name: "Updated Skill 2"};
  const deletedShiftTemplate: ShiftTemplate = {tenantId: 0, id: 2312, version: 0, name: "Skill 3"};
  it('set is loading', () => {
    expect(
      reducer(state.shiftTemplateList, actions.setIsShiftTemplateListLoading(true))
    ).toEqual({ ...state.skillList, isLoading: true })
  });
  it('add shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.addShiftTemplate(addedSkill))
    ).toEqual({ ...state.skillList, skillMapById: mapWithElement(state.skillList.skillMapById, addedSkill)})
  });
  it('remove shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.removeShiftTemplate(deletedSkill)),
    ).toEqual({ ...state.skillList, skillMapById: mapWithoutElement(state.skillList.skillMapById, deletedSkill)})
  });
  it('update shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.updateShiftTemplate(updatedSkill)),
    ).toEqual({ ...state.skillList, skillMapById: mapWithUpdatedElement(state.skillList.skillMapById, updatedSkill)})
  });
  it('refresh shift template list', () => {
    expect(
      reducer(state.shiftTemplateList, actions.refreshShiftTemplateList([addedSkill])),
    ).toEqual({ ...state.skillList, skillMapById: createIdMapFromList([addedSkill]) });
  });
});

describe('Rotation selectors', () => {
  it('should throw an error if shift template list is loading', () => {
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      skillList: { 
        ...state.skillList, isLoading: true }
    }, 1234)).toThrow();
  });

  it('should get a shift template by id', () => {
    const skill = shiftTemplateSelectors.getShiftTemplateById(state, 1234);
    expect(skill).toEqual({
      tenantId: 0,
      id: 1234,
      version: 0,
      name: "Skill 2"
    });
  });

  it('should return an empty list if shift template list is loading', () => {
    const skillList = shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      skillList: { 
        ...state.skillList, isLoading: true }
    });
    expect(skillList).toEqual([]);
  });

  it('should return a list of all skills', () => {
    const skillList = shiftTemplateSelectors.getShiftTemplateList(state);
    expect(skillList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: "Skill 2"     
      },
      {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: "Skill 3"
      }
    ]));
    expect(skillList.length).toEqual(2);
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: []
  },
  employeeList: {
    isLoading: false,
    employeeMapById: new Map()
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map()
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map()
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map([
      [1234, {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: "Skill 2"
      }],
      [2312, {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: "Skill 3"
      }]
    ])
  },
  rosterState: {
    isLoading: true,
    rosterState: null
  },
  shiftRoster: {
    isLoading: true,
    shiftRosterView: null
  },
  availabilityRoster: {
    isLoading: true,
    availabilityRosterView: null
  },
  solverState: {
    isSolving: false
  },
  alerts: {
    alertList: [],
    idGeneratorIndex: 0
  },
  shiftTemplateList: {
    isLoading: true,
    shiftTemplateMapById: new Map()
  }
};
