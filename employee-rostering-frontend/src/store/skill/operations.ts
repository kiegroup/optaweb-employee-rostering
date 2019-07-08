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

import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import Skill from 'domain/Skill';
import { showSuccessMessage, showErrorMessage } from 'ui/Alerts';
import { SetSkillListLoadingAction, AddSkillAction, RemoveSkillAction, UpdateSkillAction, RefreshSkillListAction } from './types';

export const addSkill: ThunkCommandFactory<Skill, AddSkillAction> = skill =>
  (dispatch, state, client) => {
    const tenantId = skill.tenantId;
    return client.post<Skill>(`/tenant/${tenantId}/skill/add`, skill).then(newSkill => {
      showSuccessMessage("Successfully added Skill", `The Skill "${newSkill.name}" was successfully added.`);
      dispatch(actions.addSkill(newSkill))
    });
  };

export const removeSkill: ThunkCommandFactory<Skill, RemoveSkillAction> = skill =>
  (dispatch, state, client) => {
    const tenantId = skill.tenantId;
    const skillId = skill.id;
    return client.delete<boolean>(`/tenant/${tenantId}/skill/${skillId}`).then(isSuccess => {
      if (isSuccess) {
        showSuccessMessage("Successfully deleted Skill", `The Skill "${skill.name}" was successfully deleted.`);
        dispatch(actions.removeSkill(skill));
      }
      else {
        showErrorMessage("Error deleting Skill", `The Skill "${skill.name}" could not be deleted.`);
      }
    });
  };

export const updateSkill: ThunkCommandFactory<Skill, UpdateSkillAction> = skill =>
  (dispatch, state, client) => {
    const tenantId = skill.tenantId;
    return client.post<Skill>(`/tenant/${tenantId}/skill/update`, skill).then(updatedSkill => {
      showSuccessMessage("Successfully updated Skill", `The Skill with id "${skill.id}" was successfully updated.`);
      dispatch(actions.updateSkill(updatedSkill));
    });
  };

export const refreshSkillList: ThunkCommandFactory<void, SetSkillListLoadingAction | RefreshSkillListAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setIsSkillListLoading(true));
    return client.get<Skill[]>(`/tenant/${tenantId}/skill/`).then(skillList => {
      dispatch(actions.refreshSkillList(skillList));
      dispatch(actions.setIsSkillListLoading(false));
    });
  };
