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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Contract } from 'domain/Contract';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Map } from 'immutable';
import { mockRedux } from 'setupTests';
import { RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { contractOperations } from 'store/contract';
import { doNothing } from 'types';
import { ContractRow, ContractsPage, EditableContractRow } from './ContractsPage';

const noContractsStore = mockStore({
  contractList: {
    isLoading: false,
    contractMapById: Map(),
  },
}).store;

const twoContractsStore = mockStore({
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, {
        id: 0,
        version: 0,
        tenantId: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 1,
        maximumMinutesPerWeek: 2,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      })
      .set(1,
        {
          id: 1,
          version: 0,
          tenantId: 0,
          name: 'Contract 2',
          maximumMinutesPerDay: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 3,
          maximumMinutesPerYear: 4,
        }),
  },
}).store;

describe('Contracts page', () => {
  const addContract = (contract: Contract) => ['add', contract];
  const updateContract = (contract: Contract) => ['update', contract];
  const removeContract = (contract: Contract) => ['remove', contract];

  beforeEach(() => {
    jest.spyOn(contractOperations, 'addContract').mockImplementation(contract => addContract(contract) as any);
    jest.spyOn(contractOperations, 'updateContract').mockImplementation(contract => updateContract(contract) as any);
    jest.spyOn(contractOperations, 'removeContract').mockImplementation(contract => removeContract(contract) as any);
    jest.spyOn(twoContractsStore, 'dispatch').mockImplementation(doNothing);
  });

  it('should render correctly with no skills', () => {
    mockRedux(noContractsStore);
    const skillsPage = shallow(<ContractsPage {...getRouterProps('/0/skill', {})} />);
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few skills', () => {
    mockRedux(twoContractsStore);
    const skillsPage = shallow(
      <ContractsPage {...getRouterProps('/0/skill', {})} />,
    );
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('no name should be invalid', () => {
    const contract = {
      ...twoContractsStore.getState().contractList.contractMapById.get(0) as Contract,
      name: '',
    };
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const contract = {
      ...twoContractsStore.getState().contractList.contractMapById.get(0) as Contract,
      id: 3,
      name: 'Contract 1',
    };
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new contract should call add contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(contractOperations.addContract).toBeCalledWith(contract);
    expect(twoContractsStore.dispatch).toBeCalledWith(addContract(contract));
  });

  it('saving updated contract should call update contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(contractOperations.updateContract).toBeCalledWith(contract);
    expect(twoContractsStore.dispatch).toBeCalledWith(updateContract(contract));
  });

  it('deleting should call delete contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(contractOperations.removeContract).toBeCalledWith(contract);
    expect(twoContractsStore.dispatch).toBeCalledWith(removeContract(contract));
  });
});
