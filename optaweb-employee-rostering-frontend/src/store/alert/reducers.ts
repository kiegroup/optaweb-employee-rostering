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

import { withElement, withoutElementWithId } from 'util/ImmutableCollectionOperations';
import { ActionType, AlertList, AlertAction } from './types';

export const initialState: AlertList = {
  alertList: [],
  idGeneratorIndex: 0,
};

const alertReducer = (state = initialState, action: AlertAction): AlertList => {
  switch (action.type) {
    case ActionType.ADD_ALERT: {
      const alertWithId = { ...action.alertInfo, id: state.idGeneratorIndex };
      const nextIndex = state.idGeneratorIndex + 1;
      return { ...state, idGeneratorIndex: nextIndex, alertList: withElement(state.alertList, alertWithId) };
    }
    case ActionType.REMOVE_ALERT: {
      return { ...state, alertList: withoutElementWithId(state.alertList, action.id) };
    }
    default:
      return state;
  }
};

export default alertReducer;
