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
import * as rosterOperations from 'store/roster/operations';
import { onPost, onPut, onDelete } from 'store/rest/RestTestUtils';
import { Shift } from 'domain/Shift';
import moment from 'moment';
import { serializeLocalDateTime } from 'store/rest/DataSerialization';
import { shiftAdapter, KindaShiftView, kindaShiftViewAdapter } from './KindaShiftView';
import { shiftOperations } from './index';
import { AppState } from '../types';
import { mockStore } from '../mockStore';

describe('Shift operations', () => {
  it('should dispatch actions and call client on addShift', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster');

    const addedShift: Shift = {
      tenantId,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      spot: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: true,
    };

    onPost(`/tenant/${tenantId}/shift/add`, shiftAdapter(addedShift), shiftAdapter(addedShift));

    await store.dispatch(shiftOperations.addShift(addedShift));

    expect(client.post).toBeCalled();
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/shift/add`, shiftAdapter(addedShift));
    expect(mockRefreshShiftRoster).toBeCalled();

    expect(store.getActions()).toEqual([]);
  });

  it('should dispatch actions and call client on a successful delete shift', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster');

    const deletedShift: Shift = {
      tenantId,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      id: 10,
      version: 0,
      spot: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: true,
    };

    onDelete(`/tenant/${tenantId}/shift/${deletedShift.id}`, true);

    await store.dispatch(shiftOperations.removeShift(deletedShift));

    expect(client.delete).toBeCalled();
    expect(client.delete).toBeCalledWith(`/tenant/${tenantId}/shift/${deletedShift.id}`);
    expect(mockRefreshShiftRoster).toBeCalled();

    expect(store.getActions()).toEqual([]);
  });

  it('should call client but not dispatch actions on a failed delete shift', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster');

    const deletedShift: Shift = {
      tenantId,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      id: 10,
      version: 0,
      spot: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: true,
    };

    onDelete(`/tenant/${tenantId}/shift/${deletedShift.id}`, false);

    await store.dispatch(shiftOperations.removeShift(deletedShift));

    expect(client.delete).toBeCalled();
    expect(client.delete).toBeCalledWith(`/tenant/${tenantId}/shift/${deletedShift.id}`);
    expect(mockRefreshShiftRoster).not.toBeCalled();

    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeShiftError', {
        id: deletedShift.id,
        startDateTime: moment(deletedShift.startDateTime).format('LLL'),
        endDateTime: moment(deletedShift.endDateTime).format('LLL'),
      }),
    ]);
  });

  it('should dispatch actions and call client on updateShift', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster');

    const updatedShift: Shift = {
      tenantId,
      id: 11,
      version: 0,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      spot: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: true,
    };

    const updatedShiftWithUpdatedVersion: Shift = {
      ...updatedShift,
      version: 1,
    };

    onPut(`/tenant/${tenantId}/shift/update`, shiftAdapter(updatedShift), shiftAdapter(updatedShiftWithUpdatedVersion));

    await store.dispatch(shiftOperations.updateShift(updatedShift));

    expect(client.put).toBeCalled();
    expect(client.put).toBeCalledWith(`/tenant/${tenantId}/shift/update`, shiftAdapter(updatedShift));
    expect(mockRefreshShiftRoster).toBeCalled();
  });
});

describe('shift adapters', () => {
  it('shiftAdapter should convert a Shift to a KindaShiftView', () => {
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();
    const shift: Shift = {
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      spot: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
      requiredSkillSet: [],
      originalEmployee: null,
      employee: {
        tenantId: 10,
        id: 20,
        version: 0,
        name: 'Bill',
        contract: {
          tenantId: 0,
          id: 100,
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
      rotationEmployee: null,
      pinnedByUser: true,
      indictmentScore: { hardScore: 0, mediumScore: 0, softScore: 0 },
      desiredTimeslotForEmployeeRewardList: [],
      shiftEmployeeConflictList: [],
    };

    expect(shiftAdapter(shift)).toEqual({
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: serializeLocalDateTime(shiftStartTime),
      endDateTime: serializeLocalDateTime(shiftEndTime),
      spotId: 20,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: 20,
      rotationEmployeeId: null,
      pinnedByUser: true,
    });
  });

  it('kindaShiftAdapter should convert a KindaShiftView to a ShiftView', () => {
    const shiftStartTime = moment('2018-01-01T09:00').toDate();
    const shiftEndTime = moment('2018-01-01T12:00').toDate();
    const kindaShiftView: KindaShiftView = {
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: serializeLocalDateTime(shiftStartTime),
      endDateTime: serializeLocalDateTime(shiftEndTime),
      spotId: 20,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: null,
      rotationEmployeeId: null,
      pinnedByUser: true,
      indictmentScore: '5hard/0medium/-14soft',
      // @ts-ignore
      unassignedShiftPenaltyList: [{
        score: '5hard/0medium/-14soft',
        shift: {
          tenantId: 0,
          id: 11,
          version: 0,
          startDateTime: shiftStartTime,
          endDateTime: shiftEndTime,
          spot: {
            tenantId: 0,
            id: 20,
            version: 0,
            name: 'Spot',
            requiredSkillSet: [],
            covidWard: false,
          },
          requiredSkillSet: [],
          originalEmployee: null,
          employee: null,
          rotationEmployee: null,
          pinnedByUser: true,
        },
      }],
    };

    expect(kindaShiftViewAdapter(kindaShiftView)).toEqual({
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: shiftStartTime,
      endDateTime: shiftEndTime,
      spotId: 20,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: null,
      rotationEmployeeId: null,
      pinnedByUser: true,
      indictmentScore: { hardScore: 5, mediumScore: 0, softScore: -14 },
      // @ts-ignore
      unassignedShiftPenaltyList: [{
        score: { hardScore: 5, mediumScore: 0, softScore: -14 },
        shift: {
          tenantId: 0,
          id: 11,
          version: 0,
          startDateTime: shiftStartTime,
          endDateTime: shiftEndTime,
          spot: {
            tenantId: 0,
            id: 20,
            version: 0,
            name: 'Spot',
            requiredSkillSet: [],
            covidWard: false,
          },
          requiredSkillSet: [],
          originalEmployee: null,
          employee: null,
          rotationEmployee: null,
          pinnedByUser: true,
        },
      }],
    });
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
    spotMapById: new Map(),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map([
      [1234, {
        tenantId: 0,
        id: 1234,
        version: 0,
        name: 'Skill 2',
      }],
      [2312, {
        tenantId: 0,
        id: 2312,
        version: 1,
        name: 'Skill 3',
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
