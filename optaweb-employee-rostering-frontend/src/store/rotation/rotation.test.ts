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
  mapDomainObjectToView,
} from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete, onPut } from 'store/rest/RestTestUtils';
import { ShiftTemplate } from 'domain/ShiftTemplate';
import {
  shiftTemplateViewToDomainObjectView,
  shiftTemplateToShiftTemplateView,
  ShiftTemplateView,
} from 'store/rotation/ShiftTemplateView';
import moment from 'moment';
import reducer, { shiftTemplateSelectors, shiftTemplateOperations } from './index';
import * as actions from './actions';
import { AppState } from '../types';
import { mockStore } from '../mockStore';

describe('Rotation operations', () => {
  it('should dispatch actions and call client on refresh shift template list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockShiftTemplateList: ShiftTemplateView[] = [{
      tenantId,
      id: 0,
      version: 0,
      spotId: 1,
      requiredSkillSetIdList: [],
      rotationEmployeeId: 2,
      durationBetweenRotationStartAndTemplateStart: 'PT3D',
      shiftTemplateDuration: 'PT8H',
    }];

    onGet(`/tenant/${tenantId}/rotation/`, mockShiftTemplateList);
    await store.dispatch(shiftTemplateOperations.refreshShiftTemplateList());
    expect(store.getActions()).toEqual([
      actions.setIsShiftTemplateListLoading(true),
      actions.refreshShiftTemplateList(mockShiftTemplateList.map(shiftTemplateViewToDomainObjectView)),
      actions.setIsShiftTemplateListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/`);
  });

  it('should dispatch actions and call client on a successful delete shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftTemplateToDelete: ShiftTemplate = {
      tenantId,
      id: 0,
      version: 0,
      spot: {
        tenantId,
        id: 1,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
      shiftTemplateDuration: moment.duration('PT8H'),
    };
    onDelete(`/tenant/${tenantId}/rotation/${shiftTemplateToDelete.id}`, true);
    await store.dispatch(shiftTemplateOperations.removeShiftTemplate(shiftTemplateToDelete));
    expect(store.getActions()).toEqual([
      actions.removeShiftTemplate(mapDomainObjectToView(shiftTemplateToDelete)),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/${shiftTemplateToDelete.id}`);
  });

  it('should call client but not dispatch actions on a failed delete shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftTemplateToDelete: ShiftTemplate = {
      tenantId,
      id: 0,
      version: 0,
      spot: {
        tenantId,
        id: 1,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
      shiftTemplateDuration: moment.duration('PT8H'),
    };
    onDelete(`/tenant/${tenantId}/rotation/${shiftTemplateToDelete.id}`, false);
    await store.dispatch(shiftTemplateOperations.removeShiftTemplate(shiftTemplateToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeShiftTemplateError', { id: shiftTemplateToDelete.id }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/${shiftTemplateToDelete.id}`);
  });

  it('should dispatch actions and call client on add shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftTemplateToAdd: ShiftTemplate = {
      tenantId,
      spot: {
        tenantId,
        id: 1,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
      shiftTemplateDuration: moment.duration('PT8H'),
    };
    const shiftTemplateWithUpdatedId: ShiftTemplate = {
      ...shiftTemplateToAdd,
      id: 4,
      version: 0,
    };
    onPost(`/tenant/${tenantId}/rotation/add`,
      shiftTemplateToShiftTemplateView(shiftTemplateToAdd),
      shiftTemplateToShiftTemplateView(shiftTemplateWithUpdatedId));
    await store.dispatch(shiftTemplateOperations.addShiftTemplate(shiftTemplateToAdd));
    expect(store.getActions()).toEqual([
      actions.addShiftTemplate(mapDomainObjectToView(shiftTemplateWithUpdatedId)),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/add`,
      shiftTemplateToShiftTemplateView(shiftTemplateToAdd));
  });

  it('should dispatch actions and call client on update shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftTemplateToUpdate: ShiftTemplate = {
      tenantId,
      id: 4,
      version: 0,
      spot: {
        tenantId,
        id: 1,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
      shiftTemplateDuration: moment.duration('PT8H'),
    };
    const shiftTemplateWithUpdatedVersion: ShiftTemplate = {
      ...shiftTemplateToUpdate,
      version: 1,
    };
    onPut(`/tenant/${tenantId}/rotation/update`, shiftTemplateToShiftTemplateView(shiftTemplateToUpdate),
      shiftTemplateToShiftTemplateView(shiftTemplateWithUpdatedVersion));
    await store.dispatch(shiftTemplateOperations.updateShiftTemplate(shiftTemplateToUpdate));
    expect(store.getActions()).toEqual([
      actions.updateShiftTemplate(mapDomainObjectToView(shiftTemplateWithUpdatedVersion)),
    ]);
    expect(client.put).toHaveBeenCalledTimes(1);
    expect(client.put).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/update`,
      shiftTemplateToShiftTemplateView(shiftTemplateToUpdate));
  });
});

describe('Rotation reducers', () => {
  const addedShiftTemplate: ShiftTemplate = {
    tenantId: 0,
    id: 10,
    version: 0,
    spot: {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
      covidWard: false,
    },
    requiredSkillSet: [],
    rotationEmployee: null,
    durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
    shiftTemplateDuration: moment.duration('PT8H'),
  };
  const updatedShiftTemplate: ShiftTemplate = {
    tenantId: 0,
    id: 2,
    version: 1,
    spot: {
      tenantId: 0,
      id: 3,
      name: 'New Spot',
      requiredSkillSet: [],
      covidWard: false,
    },
    requiredSkillSet: [],
    rotationEmployee: null,
    durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
    shiftTemplateDuration: moment.duration('PT8H'),
  };
  const deletedShiftTemplate: ShiftTemplate = {
    tenantId: 0,
    id: 2,
    version: 1,
    spot: {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
      covidWard: false,
    },
    requiredSkillSet: [],
    rotationEmployee: null,
    durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
    shiftTemplateDuration: moment.duration('PT8H'),
  };

  it('set is loading', () => {
    expect(
      reducer(state.shiftTemplateList, actions.setIsShiftTemplateListLoading(true)),
    ).toEqual({ ...state.shiftTemplateList, isLoading: true });
  });
  it('add shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.addShiftTemplate(mapDomainObjectToView(addedShiftTemplate))),
    ).toEqual({ ...state.shiftTemplateList,
      shiftTemplateMapById:
        mapWithElement(state.shiftTemplateList.shiftTemplateMapById, mapDomainObjectToView(addedShiftTemplate)) });
  });
  it('remove shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.removeShiftTemplate(mapDomainObjectToView(updatedShiftTemplate))),
    ).toEqual({ ...state.shiftTemplateList,
      shiftTemplateMapById:
        mapWithoutElement(state.shiftTemplateList.shiftTemplateMapById, mapDomainObjectToView(updatedShiftTemplate)) });
  });
  it('update shift template', () => {
    expect(
      reducer(state.shiftTemplateList, actions.updateShiftTemplate(mapDomainObjectToView(deletedShiftTemplate))),
    ).toEqual({ ...state.shiftTemplateList,
      shiftTemplateMapById:
        mapWithUpdatedElement(state.shiftTemplateList.shiftTemplateMapById,
          mapDomainObjectToView(deletedShiftTemplate)) });
  });
  it('refresh shift template list', () => {
    expect(
      reducer(state.shiftTemplateList, actions.refreshShiftTemplateList([addedShiftTemplate]
        .map(mapDomainObjectToView))),
    ).toEqual({ ...state.shiftTemplateList,
      shiftTemplateMapById:
        createIdMapFromList([addedShiftTemplate].map(mapDomainObjectToView)) });
  });
});

describe('Rotation selectors', () => {
  it('should throw an error if shift template list is loading', () => {
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      shiftTemplateList: { ...state.shiftTemplateList, isLoading: true },
    }, 2)).toThrow();
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      spotList: { ...state.spotList, isLoading: true },
    }, 2)).toThrow();
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      employeeList: { ...state.employeeList, isLoading: true },
    }, 2)).toThrow();
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    }, 2)).toThrow();
    expect(() => shiftTemplateSelectors.getShiftTemplateById({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    }, 2)).toThrow();
  });

  it('should get a shift template by id', () => {
    const shiftTemplate = shiftTemplateSelectors.getShiftTemplateById(state, 2);
    expect(shiftTemplate).toEqual({
      tenantId: 0,
      id: 2,
      version: 0,
      spot: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      rotationEmployee: null,
      durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
      shiftTemplateDuration: moment.duration('PT8H'),
    });
  });

  it('should return an empty list if shift template list is loading', () => {
    expect(shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      shiftTemplateList: { ...state.shiftTemplateList, isLoading: true },
    })).toEqual([]);
    expect(shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      spotList: { ...state.spotList, isLoading: true },
    })).toEqual([]);
    expect(shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      employeeList: { ...state.employeeList, isLoading: true },
    })).toEqual([]);
    expect(shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      contractList: { ...state.contractList, isLoading: true },
    })).toEqual([]);
    expect(shiftTemplateSelectors.getShiftTemplateList({
      ...state,
      skillList: { ...state.skillList, isLoading: true },
    })).toEqual([]);
  });

  it('should return a list of all skills', () => {
    const skillList = shiftTemplateSelectors.getShiftTemplateList(state);
    expect(skillList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 2,
        version: 0,
        spot: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Spot',
          requiredSkillSet: [],
          covidWard: false,
        },
        requiredSkillSet: [],
        rotationEmployee: null,
        durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
        shiftTemplateDuration: moment.duration('PT8H'),
      },
      {
        tenantId: 0,
        id: 4,
        version: 0,
        spot: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Spot',
          requiredSkillSet: [],
          covidWard: false,
        },
        requiredSkillSet: [],
        rotationEmployee: {
          tenantId: 0,
          id: 3,
          version: 0,
          name: 'Employee',
          contract: {
            tenantId: 0,
            id: 10,
            version: 0,
            name: 'Contract',
            maximumMinutesPerDay: null,
            maximumMinutesPerWeek: null,
            maximumMinutesPerMonth: null,
            maximumMinutesPerYear: null,
          },
          skillProficiencySet: [],
          covidRiskType: 'INOCULATED',
        },
        durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
        shiftTemplateDuration: moment.duration('PT8H'),
      },
    ]));
    expect(skillList.length).toEqual(2);
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
    employeeMapById: new Map([
      [3, {
        tenantId: 0,
        id: 3,
        version: 0,
        name: 'Employee',
        contract: 10,
        skillProficiencySet: [],
        covidRiskType: 'INOCULATED',
      }],
    ]),
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map([
      [10, {
        tenantId: 0,
        id: 10,
        version: 0,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      }],
    ]),
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map([
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      }],
    ]),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map(),
  },
  shiftTemplateList: {
    isLoading: false,
    shiftTemplateMapById: new Map([
      [2, {
        tenantId: 0,
        id: 2,
        version: 0,
        spot: 1,
        requiredSkillSet: [],
        rotationEmployee: null,
        durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
        shiftTemplateDuration: moment.duration('PT8H'),
      }],
      [4, {
        tenantId: 0,
        id: 4,
        version: 0,
        spot: 1,
        requiredSkillSet: [],
        rotationEmployee: 3,
        durationBetweenRotationStartAndTemplateStart: moment.duration('PT3D'),
        shiftTemplateDuration: moment.duration('PT8H'),
      }],
    ]),
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
