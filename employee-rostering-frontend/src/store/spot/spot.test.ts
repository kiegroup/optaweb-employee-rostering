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

import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { spotOperations } from './index';
import {withElement, withoutElement, withUpdatedElement} from 'util/ImmutableCollectionOperations';
import {onGet, onPost, onDelete} from 'store/rest/RestServiceClient';
import Spot from 'domain/Spot';

describe('Spot operations', () => {
  it('should dispatch actions and call client', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockSpotList: Spot[] = [{
      tenantId: tenantId,
      id: 0,
      version: 0,
      name: "Spot 1",
      requiredSkillSet: []
    },
    {
      tenantId: tenantId,
      id: 1,
      version: 0,
      name: "Spot 2",
      requiredSkillSet: [{tenantId: tenantId, name: "Skill 1", id: 1, version: 0}]
    },
    {
      tenantId: tenantId,
      id: 3,
      version: 0,
      name: "Spot 3",
      requiredSkillSet: [{tenantId: tenantId, name: "Skill 1", id: 1, version: 0},
        {tenantId: tenantId, name: "Skill 2", id: 2, version: 0}]
    }];

    onGet(`/tenant/${tenantId}/spot/`, mockSpotList);
    await store.dispatch(spotOperations.refreshSpotList());
    expect(store.getActions()).toEqual([actions.refreshSpotList(mockSpotList)]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/`);

    store.clearActions();

    const spotToDelete: Spot = mockSpotList[0];
    onDelete(`/tenant/${tenantId}/spot/${spotToDelete.id}`, true);
    await store.dispatch(spotOperations.removeSpot(spotToDelete));
    expect(store.getActions()).toEqual([actions.removeSpot(spotToDelete)]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/${spotToDelete.id}`);

    store.clearActions();

    const spotToAdd: Spot = {tenantId: tenantId, name: "New Spot", requiredSkillSet: []};
    const spotWithUpdatedId: Spot = {...spotToAdd, id: 4, version: 0};
    onPost(`/tenant/${tenantId}/spot/add`, spotToAdd, spotWithUpdatedId);
    await store.dispatch(spotOperations.addSpot(spotToAdd));
    expect(store.getActions()).toEqual([actions.addSpot(spotWithUpdatedId)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/add`, spotToAdd);

    store.clearActions();

    const spotToUpdate: Spot = {tenantId: tenantId, name: "Updated Spot", id: 4, version: 0, requiredSkillSet: []};
    const spotWithUpdatedVersion: Spot = {...spotToAdd, version: 1};
    onPost(`/tenant/${tenantId}/spot/update`, spotToUpdate, spotWithUpdatedVersion);
    await store.dispatch(spotOperations.updateSpot(spotToUpdate));
    expect(store.getActions()).toEqual([actions.updateSpot(spotWithUpdatedVersion)]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/spot/update`, spotToUpdate);
  });
});

describe('Spot reducers', () => {
  const addedSpot: Spot = {tenantId: 0, id: 4321, version: 0, name: "Spot 1", requiredSkillSet: []};
  const updatedSpot: Spot = {tenantId: 0, id: 1234, version: 1, name: "Updated Spot 2", requiredSkillSet: []};
  const deletedSpot: Spot = {tenantId: 0, id: 2312, version: 0, name: "Spot 3", requiredSkillSet: []};
  it('add spot', () => {
    expect(
      reducer(state.spotList, actions.addSpot(addedSpot))
    ).toEqual({skillList: withElement(state.skillList.skillList, addedSpot)})
  });
  it('remove spot', () => {
    expect(
      reducer(state.spotList, actions.removeSpot(deletedSpot)),
    ).toEqual({skillList: withoutElement(state.skillList.skillList, deletedSpot)})
  });
  it('update spot', () => {
    expect(
      reducer(state.spotList, actions.updateSpot(updatedSpot)),
    ).toEqual({skillList: withUpdatedElement(state.skillList.skillList, updatedSpot)})
  });
  it('refresh spot list', () => {
    expect(
      reducer(state.spotList, actions.refreshSpotList([addedSpot])),
    ).toEqual({skillList: [addedSpot]});
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: []
  },
  spotList: {
    spotList: [
      {
        tenantId: 0,
        id: 1234,
        version: 1,
        name: "Spot 2",
        requiredSkillSet: []
      },
      {
        tenantId: 0,
        id: 2312,
        version: 0,
        name: "Spot 3",
        requiredSkillSet: []
      }
    ]
  },
  skillList: {
    skillList: []
  }
};