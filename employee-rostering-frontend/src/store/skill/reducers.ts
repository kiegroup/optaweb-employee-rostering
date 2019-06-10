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

import { ActionType, SkillList, SkillAction } from './types';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';

export const initialState: SkillList = {
  skillList: []
};

const skillReducer = (state = initialState, action: SkillAction): SkillList => {
  switch (action.type) {
    case ActionType.ADD_SKILL: {
      return { ...initialState, skillList: withElement(state.skillList, action.skill) };
    }
    case ActionType.REMOVE_SKILL: {
      return { ...initialState, skillList: withoutElement(state.skillList, action.skill) };
    }
    case ActionType.UPDATE_SKILL: {
      return { ...initialState, skillList: withUpdatedElement(state.skillList, action.skill) };
    }
    case ActionType.REFRESH_SKILL_LIST: {
      return { ...initialState, skillList: action.skillList };
    }
    default:
      return state;
  }
};

export default skillReducer;
