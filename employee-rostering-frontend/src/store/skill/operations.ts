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
import { spotOperations } from 'store/spot';
import Skill from 'domain/Skill';
import { AddSkillAction, RemoveSkillAction, UpdateSkillAction, RefreshSkillListAction } from './types';

export const addSkill: ThunkCommandFactory<Skill, AddSkillAction> = skill =>
  (dispatch, state, client) => {
    let tenantId = skill.tenantId;
    return client.post<Skill>(`/tenant/${tenantId}/skill/add`, skill).then(newSkill => {
      dispatch(actions.addSkill(newSkill))
    });
  };

export const removeSkill: ThunkCommandFactory<Skill, RemoveSkillAction> = skill =>
  (dispatch, state, client) => {
    let tenantId = skill.tenantId;
    let skillId = skill.id;
    return client.delete<boolean>(`/tenant/${tenantId}/skill/${skillId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeSkill(skill));
      }
    });
  };

export const updateSkill: ThunkCommandFactory<Skill, UpdateSkillAction> = skill =>
  (dispatch, state, client) => {
    let tenantId = skill.tenantId;
    return client.post<Skill>(`/tenant/${tenantId}/skill/update`, skill).then(updatedSkill => {
      dispatch(actions.updateSkill(updatedSkill));
      // Need to update spot list, which reference the old skill
      // (also need to update employee list when it is added)
      dispatch(spotOperations.refreshSpotList());
    });
  };

export const refreshSkillList: ThunkCommandFactory<void, RefreshSkillListAction> = () =>
  (dispatch, state, client) => {
    let tenantId = state().tenantData.currentTenantId;
    return client.get<Skill[]>(`/tenant/${tenantId}/skill/`).then(skillList => {
      dispatch(actions.refreshSkillList(skillList));
    });
  };
