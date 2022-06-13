import { Skill } from 'domain/Skill';
import { Map } from 'immutable';
import DomainObjectView from 'domain/DomainObjectView';
import { AppState } from '../types';

export const getSkillById = (state: AppState, id: number): Skill => {
  if (state.skillList.isLoading) {
    throw Error('Skill list is loading');
  }
  return state.skillList.skillMapById.get(id) as Skill;
};


let oldSkillMapById: Map<number, DomainObjectView<Skill>>| null = null;
let skillListForOldSkillMapById: Skill[] | null = null;

export const getSkillList = (state: AppState): Skill[] => {
  if (state.skillList.isLoading) {
    return [];
  }
  if (oldSkillMapById === state.skillList.skillMapById && skillListForOldSkillMapById !== null) {
    return skillListForOldSkillMapById;
  }

  const out = state.skillList.skillMapById.keySeq().map(key => getSkillById(state, key))
    .sortBy(skill => skill.name).toList();

  oldSkillMapById = state.skillList.skillMapById;
  skillListForOldSkillMapById = out.toArray();
  return skillListForOldSkillMapById;
};
