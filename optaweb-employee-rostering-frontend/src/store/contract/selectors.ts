import { Contract } from 'domain/Contract';
import { Map } from 'immutable';
import DomainObjectView from 'domain/DomainObjectView';
import { AppState } from '../types';

export const getContractById = (state: AppState, id: number): Contract => {
  if (state.contractList.isLoading) {
    throw Error('Contract list is loading');
  }
  return state.contractList.contractMapById.get(id) as Contract;
};

let oldContractMapById: Map<number, DomainObjectView<Contract>> | null = null;
let contractListForOldContractMapById: Contract[] | null = null;

export const getContractList = (state: AppState): Contract[] => {
  if (state.contractList.isLoading) {
    return [];
  }
  if (oldContractMapById === state.contractList.contractMapById && contractListForOldContractMapById !== null) {
    return contractListForOldContractMapById;
  }
  const out = state.contractList.contractMapById.keySeq().map(id => getContractById(state, id))
    .sortBy(contract => contract.name).toList();

  oldContractMapById = state.contractList.contractMapById;
  contractListForOldContractMapById = out.toArray();

  return contractListForOldContractMapById;
};
