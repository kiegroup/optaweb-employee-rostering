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

import { ActionType, ShiftAction, ShiftList } from './types';
import { createIdMapFromList, mapWithElement, mapWithoutElement, mapWithUpdatedElement } from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import Shift from 'domain/Shift';

export const initialState: ShiftList = {
  isLoading: true,
  shiftMapById: new Map<number, DomainObjectView<Shift>>()
};

const shiftReducer = (state = initialState, action: ShiftAction): ShiftList => {
  switch (action.type) {
    case ActionType.SET_SHIFT_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_SHIFT: {
      return { ...state, shiftMapById: mapWithElement(state.shiftMapById, action.shift) };
    }
    case ActionType.REMOVE_SHIFT: {
      return { ...state, shiftMapById: mapWithoutElement(state.shiftMapById, action.shift) };
    }
    case ActionType.UPDATE_SHIFT: {
      return { ...state, shiftMapById: mapWithUpdatedElement(state.shiftMapById, action.shift) };
    }
    case ActionType.REFRESH_SHIFT_LIST: {
      return { ...state, shiftMapById: createIdMapFromList(action.shiftList) };
    }
    default:
      return state;
  }
};

export default shiftReducer;
