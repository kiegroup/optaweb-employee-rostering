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
import { Contract } from 'domain/Contract';
import { ActionType, ContractList, ContractAction } from './types';

export const initialState: ContractList = {
  isLoading: true,
  contractMapById: new Map<number, DomainObjectView<Contract>>(),
};

const contractReducer = (state = initialState, action: ContractAction): ContractList => {
  switch (action.type) {
    case ActionType.SET_CONTRACT_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_CONTRACT: {
      return { ...state, contractMapById: mapWithElement(state.contractMapById, action.contract) };
    }
    case ActionType.REMOVE_CONTRACT: {
      return { ...state, contractMapById: mapWithoutElement(state.contractMapById, action.contract) };
    }
    case ActionType.UPDATE_CONTRACT: {
      return { ...state, contractMapById: mapWithUpdatedElement(state.contractMapById, action.contract) };
    }
    case ActionType.REFRESH_CONTRACT_LIST: {
      return { ...state, contractMapById: createIdMapFromList(action.contractList) };
    }
    default:
      return state;
  }
};

export default contractReducer;
