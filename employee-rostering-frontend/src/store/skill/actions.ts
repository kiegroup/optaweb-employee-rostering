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

import { Skill } from 'domain/Skill';
import { ActionFactory } from '../types';
import {
  ActionType, SetSkillListLoadingAction, AddSkillAction, UpdateSkillAction, RemoveSkillAction,
  RefreshSkillListAction,
} from './types';

export const setIsSkillListLoading: ActionFactory<boolean, SetSkillListLoadingAction> = isLoading => ({
  type: ActionType.SET_SKILL_LIST_LOADING,
  isLoading,
});

export const addSkill: ActionFactory<Skill, AddSkillAction> = newSkill => ({
  type: ActionType.ADD_SKILL,
  skill: newSkill,
});

export const removeSkill: ActionFactory<Skill, RemoveSkillAction> = deletedSkill => ({
  type: ActionType.REMOVE_SKILL,
  skill: deletedSkill,
});

export const updateSkill: ActionFactory<Skill, UpdateSkillAction> = updatedSkill => ({
  type: ActionType.UPDATE_SKILL,
  skill: updatedSkill,
});

export const refreshSkillList: ActionFactory<Skill[], RefreshSkillListAction> = newSkillList => ({
  type: ActionType.REFRESH_SKILL_LIST,
  skillList: newSkillList,
});
