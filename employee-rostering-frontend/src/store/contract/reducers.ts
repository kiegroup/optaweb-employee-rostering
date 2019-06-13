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

import { ActionType, ContractList, ContractAction } from './types';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';

export const initialState: ContractList = {
  contractList: []
};

const contractReducer = (state = initialState, action: ContractAction): ContractList => {
  switch (action.type) {
    case ActionType.ADD_CONTRACT: {
      return { ...initialState, contractList: withElement(state.contractList, action.contract) };
    }
    case ActionType.REMOVE_CONTRACT: {
      return { ...initialState, contractList: withoutElement(state.contractList, action.contract) };
    }
    case ActionType.UPDATE_CONTRACT: {
      return { ...initialState, contractList: withUpdatedElement(state.contractList, action.contract) };
    }
    case ActionType.REFRESH_CONTRACT_LIST: {
      return { ...initialState, contractList: action.contractList };
    }
    default:
      return state;
  }
};

export default contractReducer;
