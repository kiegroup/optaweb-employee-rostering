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

import {
  createIdMapFromList, mapWithElement, mapWithoutElement,
  mapWithUpdatedElement,
} from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { Skill } from 'domain/Skill';
import { ActionType, SkillList, SkillAction } from './types';

export const initialState: SkillList = {
  isLoading: true,
  skillMapById: new Map<number, DomainObjectView<Skill>>(),
};

const skillReducer = (state = initialState, action: SkillAction): SkillList => {
  switch (action.type) {
    case ActionType.SET_SKILL_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_SKILL: {
      return { ...state, skillMapById: mapWithElement(state.skillMapById, action.skill) };
    }
    case ActionType.REMOVE_SKILL: {
      return { ...state, skillMapById: mapWithoutElement(state.skillMapById, action.skill) };
    }
    case ActionType.UPDATE_SKILL: {
      return { ...state, skillMapById: mapWithUpdatedElement(state.skillMapById, action.skill) };
    }
    case ActionType.REFRESH_SKILL_LIST: {
      return { ...state, skillMapById: createIdMapFromList(action.skillList) };
    }
    default:
      return state;
  }
};

export default skillReducer;
