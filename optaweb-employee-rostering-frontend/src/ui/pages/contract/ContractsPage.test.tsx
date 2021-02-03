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
});
