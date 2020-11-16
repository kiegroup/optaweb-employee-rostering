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
import { Contract } from 'domain/Contract';
import { AppState } from '../types';
import { Map, List } from 'immutable';

export const getContractById = (state: AppState, id: number): Contract => {
  if (state.contractList.isLoading) {
    throw Error('Contract list is loading');
  }
  return state.contractList.contractMapById.get(id) as Contract;
};

let oldContractMapById: Map<number, Contract> | null = null;
let contractListForOldContractMapById: List<Contract> | null = null;

export const getContractList = (state: AppState): List<Contract> => {
  if (state.contractList.isLoading) {
    return List();
  }
  if (oldContractMapById === state.contractList.contractMapById && contractListForOldContractMapById !== null) {
    return contractListForOldContractMapById;
  }
  const out = state.contractList.contractMapById.keySeq().map(id => getContractById(state, id)).toList();

  oldContractMapById = state.contractList.contractMapById;
  contractListForOldContractMapById = out;
  
  return out;
};
