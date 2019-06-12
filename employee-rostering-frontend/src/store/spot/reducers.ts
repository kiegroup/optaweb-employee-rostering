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

import { ActionType, SpotList, SpotAction } from './types';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';

export const initialState: SpotList = {
  spotList: []
};

const spotReducer = (state = initialState, action: SpotAction): SpotList => {
  switch (action.type) {
    case ActionType.ADD_SPOT: {
      return { ...initialState, spotList: withElement(state.spotList, action.spot) };
    }
    case ActionType.REMOVE_SPOT: {
      return { ...initialState, spotList: withoutElement(state.spotList, action.spot) };
    }
    case ActionType.UPDATE_SPOT: {
      return { ...initialState, spotList: withUpdatedElement(state.spotList, action.spot) };
    }
    case ActionType.REFRESH_SPOT_LIST: {
      return { ...initialState, spotList: action.spotList };
    }
    default:
      return state;
  }
};

export default spotReducer;
