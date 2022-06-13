
import { createIdMapFromList } from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { Skill } from 'domain/Skill';
import { Map } from 'immutable';
import { ActionType, SkillList, SkillAction } from './types';

export const initialState: SkillList = {
  isLoading: true,
  skillMapById: Map<number, DomainObjectView<Skill>>(),
};

const skillReducer = (state = initialState, action: SkillAction): SkillList => {
  switch (action.type) {
    case ActionType.SET_SKILL_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_SKILL:
    case ActionType.UPDATE_SKILL: {
      return { ...state, skillMapById: state.skillMapById.set(action.skill.id as number, action.skill) };
    }
    case ActionType.REMOVE_SKILL: {
      return { ...state, skillMapById: state.skillMapById.remove(action.skill.id as number) };
    }
    case ActionType.REFRESH_SKILL_LIST: {
      return { ...state, skillMapById: createIdMapFromList(action.skillList) };
    }
    default:
      return state;
  }
};

export default skillReducer;
