
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
