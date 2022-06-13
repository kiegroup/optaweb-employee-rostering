
import { Action } from 'redux';
import { Skill } from 'domain/Skill';
import DomainObjectView from 'domain/DomainObjectView';
import { Map } from 'immutable';

export enum ActionType {
  ADD_SKILL = 'ADD_SKILL',
  REMOVE_SKILL = 'REMOVE_SKILL',
  UPDATE_SKILL = 'UPDATE_SKILL',
  REFRESH_SKILL_LIST = 'REFRESH_SKILL_LIST',
  SET_SKILL_LIST_LOADING = 'SET_SKILL_LIST_LOADING'
}

export interface SetSkillListLoadingAction extends Action<ActionType.SET_SKILL_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddSkillAction extends Action<ActionType.ADD_SKILL> {
  readonly skill: Skill;
}

export interface RemoveSkillAction extends Action<ActionType.REMOVE_SKILL> {
  readonly skill: Skill;
}

export interface UpdateSkillAction extends Action<ActionType.UPDATE_SKILL> {
  readonly skill: Skill;
}

export interface RefreshSkillListAction extends Action<ActionType.REFRESH_SKILL_LIST> {
  readonly skillList: Skill[];
}

export type SkillAction = SetSkillListLoadingAction | AddSkillAction | RemoveSkillAction |
UpdateSkillAction | RefreshSkillListAction;

export interface SkillList {
  readonly isLoading: boolean;
  readonly skillMapById: Map<number, DomainObjectView<Skill>>;
}
