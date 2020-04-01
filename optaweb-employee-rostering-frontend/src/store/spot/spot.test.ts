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

import { alert } from 'store/alert';
import {
  createIdMapFromList, mapWithElement, mapWithoutElement,
  mapWithUpdatedElement,
} from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete } from 'store/rest/RestTestUtils';
import { Spot } from 'domain/Spot';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { spotSelectors, spotOperations } from './index';

describe('Spot operations', () => {
  it('should dispatch actions and call client on refresh spot list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockSpotList: Spot[] = [{
      tenantId,
      id: 0,
      version: 0,
      name: 'Spot 1',
      requiredSkillSet: [],
      covidWard: false,
    },
    {
      tenantId,
      id: 1,
      version: 0,
      name: 'Spot 2',
      requiredSkillSet: [{ tenantId, name: 'Skill 1', id: 1, version: 0 }],
      covidWard: false,
    },
    {
      tenantId,
      id: 3,
      version: 0,
      name: 'Spot 3',
      requiredSkillSet: [{ tenantId, name: 'Skill 1', id: 1, version: 0 },
        { tenantId, name: 'Skill 2', id: 2, version: 0 }],
      covidWard: false,
    }];

    onGet(`/tenant/${tenantId}/spot/`, mockSpotList);
    await store.dispatch(spotOperations.refreshSpotList());
    expect(store.getActions()).toEqual([
      actions.setIsSpotListLoading(true),
      actions.refreshSpotList(mockSpotList),
      actions.setIsSpotListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/`);
  });

  it('should dispatch actions and call client on successful delete', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const spotToDelete: Spot = {
      tenantId,
      id: 0,
      version: 0,
      name: 'Spot 1',
      requiredSkillSet: [],
      covidWard: false,
    };
    onDelete(`/tenant/${tenantId}/spot/${spotToDelete.id}`, true);
    await store.dispatch(spotOperations.removeSpot(spotToDelete));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeSpot', { name: spotToDelete.name }),
      actions.removeSpot(spotToDelete),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/${spotToDelete.id}`);
  });

  it('should not dispatch actions but call client on a failed delete', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const spotToDelete: Spot = {
      tenantId,
      id: 0,
      version: 0,
      name: 'Spot 1',
      requiredSkillSet: [],
      covidWard: false,
    };

    onDelete(`/tenant/${tenantId}/spot/${spotToDelete.id}`, false);
    await store.dispatch(spotOperations.removeSpot(spotToDelete));
    expect(store.getActions()).toEqual([alert.showErrorMessage('removeSpotError', { name: spotToDelete.name })]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/${spotToDelete.id}`);
  });

  it('should dispatch actions and call client on add', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const spotToAdd: Spot = { tenantId, name: 'New Spot', requiredSkillSet: [], covidWard: false };
    const spotWithUpdatedId: Spot = { ...spotToAdd, id: 4, version: 0 };
    onPost(`/tenant/${tenantId}/spot/add`, spotToAdd, spotWithUpdatedId);
    await store.dispatch(spotOperations.addSpot(spotToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addSpot', { name: spotToAdd.name }),
      actions.addSpot(spotWithUpdatedId),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/add`, spotToAdd);
  });

  it('should dispatch actions and call client on update', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const spotToUpdate: Spot = {
      tenantId,
      name: 'Updated Spot',
      id: 4,
      version: 0,
      requiredSkillSet: [],
      covidWard: false,
    };
    const spotWithUpdatedVersion: Spot = { ...spotToUpdate, version: 1 };
    onPost(`/tenant/${tenantId}/spot/update`, spotToUpdate, spotWithUpdatedVersion);
    await store.dispatch(spotOperations.updateSpot(spotToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('updateSpot', { id: spotToUpdate.id }),
      actions.updateSpot(spotWithUpdatedVersion),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/update`, spotToUpdate);
  });
});

describe('Spot reducers', () => {
  const addedSpot: Spot = { tenantId: 0, id: 4321, version: 0, name: 'Spot 1', requiredSkillSet: [], covidWard: false };
  const updatedSpot: Spot = {
    tenantId: 0,
    id: 1234,
    version: 1,
    name: 'Updated Spot 2',
    requiredSkillSet: [],
    covidWard: false,
  };
  const deletedSpot: Spot = {
    tenantId: 0,
    id: 2312,
    version: 0,
    name: 'Spot 3',
    requiredSkillSet: [],
    covidWard: false,
  };
  it('set loading', () => {
    expect(
      reducer(state.spotList, actions.setIsSpotListLoading(true)),
    ).toEqual({ ...state.spotList,
      isLoading: true });
  });
  it('add spot', () => {
    expect(
      reducer(state.spotList, actions.addSpot(addedSpot)),
    ).toEqual({ ...state.spotList,
      spotMapById: mapWithElement(state.spotList.spotMapById, addedSpot) });
  });
  it('remove spot', () => {
    expect(
      reducer(state.spotList, actions.removeSpot(deletedSpot)),
    ).toEqual({ ...state.spotList,
      spotMapById: mapWithoutElement(state.spotList.spotMapById, deletedSpot) });
  });
  it('update spot', () => {
    expect(
      reducer(state.spotList, actions.updateSpot(updatedSpot)),
    ).toEqual({ ...state.spotList,
      spotMapById: mapWithUpdatedElement(state.spotList.spotMapById, updatedSpot) });
  });
  it('refresh spot list', () => {
    expect(
      reducer(state.spotList, actions.refreshSpotList([addedSpot])),
    ).toEqual({ ...state.spotList,
      spotMapById: createIdMapFromList([addedSpot]) });
  });
});

describe('Spot selectors', () => {
  it('should throw an error if Spot list or Skill is loading', () => {
    expect(() => spotSelectors.getSpotById({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    }, 1234)).toThrow();
    expect(() => spotSelectors.getSpotById({
      ...state,
      spotList: { ...state.spotList, isLoading: true },
    }, 1234)).toThrow();
  });

  it('should get a spot by id', () => {
    const spot = spotSelectors.getSpotById(state, 1234);
    expect(spot).toEqual({
      tenantId: 0,
      id: 1234,
      version: 1,
      name: 'Spot 2',
      requiredSkillSet: [
        {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Skill 1',
        },
      ],
      covidWard: false,
    });
  });

  it('should return an empty list if spot list or skill list is loading', () => {
    let spotList = spotSelectors.getSpotList({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    });
    expect(spotList).toEqual([]);
    spotList = spotSelectors.getSpotList({
      ...state,
      spotList: { ...state.spotList, isLoading: true },
    });
    expect(spotList).toEqual([]);
  });

  it('should return a list of all spots', () => {
    const spotList = spotSelectors.getSpotList(state);
    expect(spotList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 1234,
        version: 1,
        name: 'Spot 2',
        requiredSkillSet: [
          {
            tenantId: 0,
            id: 1,
            version: 0,
            name: 'Skill 1',
          },
        ],
        covidWard: false,
      },
      {
        tenantId: 0,
        id: 2312,
        version: 0,
        name: 'Spot 3',
        requiredSkillSet: [],
        covidWard: false,
      },
    ]));
    expect(spotList.length).toEqual(2);
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: [],
    timezoneList: ['America/Toronto'],
  },
  employeeList: {
    isLoading: false,
    employeeMapById: new Map(),
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map(),
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map([
      [1234, {
        tenantId: 0,
        id: 1234,
        version: 1,
        name: 'Spot 2',
        requiredSkillSet: [1],
        covidWard: false,
      }],
      [2312, {
        tenantId: 0,
        id: 2312,
        version: 0,
        name: 'Spot 3',
        requiredSkillSet: [],
        covidWard: false,
      }],
    ]),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map([
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Skill 1',
      }],
    ]),
  },
  shiftTemplateList: {
    isLoading: false,
    shiftTemplateMapById: new Map(),
  },
  rosterState: {
    isLoading: true,
    rosterState: null,
  },
  shiftRoster: {
    isLoading: true,
    shiftRosterView: null,
  },
  availabilityRoster: {
    isLoading: true,
    availabilityRosterView: null,
  },
  solverState: {
    solverStatus: 'TERMINATED',
  },
  alerts: {
    alertList: [],
    idGeneratorIndex: 0,
  },
};
