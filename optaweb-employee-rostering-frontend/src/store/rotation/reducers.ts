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
import { ShiftTemplate } from 'domain/ShiftTemplate';
import { ActionType, ShiftTemplateList, ShiftTemplateAction } from './types';

export const initialState: ShiftTemplateList = {
  isLoading: true,
  shiftTemplateMapById: new Map<number, DomainObjectView<ShiftTemplate>>(),
};

const shiftTemplateReducer = (state = initialState, action: ShiftTemplateAction): ShiftTemplateList => {
  switch (action.type) {
    case ActionType.SET_SHIFT_TEMPLATE_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_SHIFT_TEMPLATE: {
      return { ...state, shiftTemplateMapById: mapWithElement(state.shiftTemplateMapById, action.shiftTemplate) };
    }
    case ActionType.REMOVE_SHIFT_TEMPLATE: {
      return { ...state, shiftTemplateMapById: mapWithoutElement(state.shiftTemplateMapById, action.shiftTemplate) };
    }
    case ActionType.UPDATE_SHIFT_TEMPLATE: {
      return { ...state,
        shiftTemplateMapById: mapWithUpdatedElement(state.shiftTemplateMapById,
          action.shiftTemplate) };
    }
    case ActionType.REFRESH_SHIFT_TEMPLATE_LIST: {
      return { ...state, shiftTemplateMapById: createIdMapFromList(action.shiftTemplateList) };
    }
    default:
      return state;
  }
};

export default shiftTemplateReducer;
