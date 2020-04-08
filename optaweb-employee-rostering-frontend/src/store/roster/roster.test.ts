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
import { resetRestClientMock, onGet, onPost } from 'store/rest/RestTestUtils';
import { spotSelectors } from 'store/spot';
import { employeeSelectors } from 'store/employee';
import MockDate from 'mockdate';
import moment from 'moment';
import { Spot } from 'domain/Spot';
import { ShiftRosterView } from 'domain/ShiftRosterView';
import { AvailabilityRosterView } from 'domain/AvailabilityRosterView';
import { Employee } from 'domain/Employee';
import DomainObjectView from 'domain/DomainObjectView';
import { RosterState } from 'domain/RosterState';
import { serializeLocalDate } from 'store/rest/DataSerialization';
import { flushPromises } from 'setupTests';
import { doNothing } from 'types';
import { availabilityRosterReducer } from './reducers';
import { rosterStateReducer, shiftRosterViewReducer, rosterSelectors, rosterOperations, solverReducer } from './index';
import * as actions from './actions';
import { AppState } from '../types';
import { mockStore } from '../mockStore';

const mockShiftRoster: ShiftRosterView = {
  tenantId: 0,
  startDate: moment('2018-01-01', 'YYYY-MM-DD').toISOString(),
  endDate: moment('2018-01-01', 'YYYY-MM-DD').toISOString(),
  spotList: [{
    tenantId: 0,
    id: 10,
    version: 0,
    name: 'Spot',
    requiredSkillSet: [],
    covidWard: false,
  }],
  employeeList: [
    {
      tenantId: 0,
      id: 20,
      version: 0,
      name: 'Employee',
      skillProficiencySet: [],
      contract: {
        tenantId: 0,
        id: 30,
        version: 0,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    },
  ],
  rosterState: {
    publishNotice: 0,
    firstDraftDate: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
    publishLength: 0,
    draftLength: 0,
    unplannedRotationOffset: 5,
    rotationLength: 10,
    lastHistoricDate: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
    timeZone: '',
    tenant: {
      name: 'Tenant',
    },
  },
  spotIdToShiftViewListMap: {
    10: [{
      tenantId: 0,
      startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      spotId: 10,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: 20,
      rotationEmployeeId: null,
      indictmentScore: {
        hardScore: 0,
        mediumScore: 0,
        softScore: 0,
      },
      pinnedByUser: false,
    }],
  },
  score: {
    hardScore: 0,
    mediumScore: 0,
    softScore: 0,
  },
  indictmentSummary: {
    constraintToCountMap: {},
    constraintToScoreImpactMap: {},
  },
};

const mockAvailabilityRoster: AvailabilityRosterView = {
  tenantId: 0,
  startDate: moment('2018-01-01', 'YYYY-MM-DD').toISOString(),
  endDate: moment('2018-01-01', 'YYYY-MM-DD').toISOString(),
  spotList: [{
    tenantId: 0,
    id: 10,
    version: 0,
    name: 'Spot',
    requiredSkillSet: [],
    covidWard: false,
  }],
  employeeList: [
    {
      tenantId: 0,
      id: 20,
      version: 0,
      name: 'Employee',
      skillProficiencySet: [],
      contract: {
        tenantId: 0,
        id: 30,
        version: 0,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    },
  ],
  rosterState: {
    publishNotice: 0,
    firstDraftDate: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
    publishLength: 0,
    draftLength: 0,
    unplannedRotationOffset: 5,
    rotationLength: 10,
    lastHistoricDate: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
    timeZone: '',
    tenant: {
      name: 'Tenant',
    },
  },
  employeeIdToShiftViewListMap: {
    20: [{
      tenantId: 0,
      startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      spotId: 10,
      requiredSkillSetIdList: [],
      originalEmployeeId: null,
      employeeId: 20,
      rotationEmployeeId: null,
      indictmentScore: {
        hardScore: 0,
        mediumScore: 0,
        softScore: 0,
      },
      pinnedByUser: false,
    }],
  },
  employeeIdToAvailabilityViewListMap: {
    20: [{
      tenantId: 0,
      startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
      employeeId: 20,
      state: 'DESIRED',
    }],
  },
  unassignedShiftViewList: [],
  score: {
    hardScore: 0,
    mediumScore: 0,
    softScore: 0,
  },
  indictmentSummary: {
    constraintToCountMap: {},
    constraintToScoreImpactMap: {},
  },
};

describe('Roster operations', () => {
  it('should dispatch actions and call client on solve roster', async () => {
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);
    jest.useFakeTimers();
    const solvingStartTime = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    MockDate.set(solvingStartTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    onPost(`/tenant/${tenantId}/roster/solve`, {}, {});
    await store.dispatch(rosterOperations.solveRoster());
    expect(store.getActions()).toEqual([
      actions.solveRoster(),
      alert.showInfoMessage('startSolvingRoster', {
        startSolvingTime: moment(solvingStartTime).format('LLL'),
      }),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/solve`, {});

    jest.advanceTimersByTime(1000);
    await Promise.resolve(); // hack to wait for the refresh action to finish

    expect(mockRefreshShiftRoster).toBeCalled();
  });

  it('should dispatch actions and call client on terminate solving roster', async () => {
    const solvingEndTime = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    MockDate.set(solvingEndTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);

    onPost(`/tenant/${tenantId}/roster/terminate`, {}, {});
    await (store.dispatch(rosterOperations.terminateSolvingRosterEarly()));
    await flushPromises();

    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/terminate`, {});

    expect(store.getActions()).toEqual([
      actions.terminateSolvingRosterEarly(),
      alert.showInfoMessage('finishSolvingRoster', { finishSolvingTime: moment(solvingEndTime).format('LLL') }),
    ]);

    expect(mockRefreshShiftRoster).toBeCalled();
  });

  it('should dispatch the last shift roster REST call on refreshShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    jest.spyOn(rosterOperations, 'refreshShiftRoster').mockRestore();

    const testOperation = async (operation: () => Promise<void>, method: 'get'|'post',
      restURL: string, restArg?: any) => {
      store.clearActions();
      resetRestClientMock(client);

      switch (method) {
        case 'get': {
          onGet(restURL, { ...mockShiftRoster, spotIdToShiftViewListMap: {}, score: '0hard/0medium/0soft' });
          break;
        }

        case 'post': {
          onPost(restURL, restArg, { ...mockShiftRoster, spotIdToShiftViewListMap: {}, score: '0hard/0medium/0soft' });
          break;
        }

        default: throw new Error();
      }

      await operation();
      store.clearActions();

      await store.dispatch(rosterOperations.refreshShiftRoster());

      expect(store.getActions()).toEqual([
        actions.setShiftRosterIsLoading(true),
        actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
        actions.setShiftRosterIsLoading(false),
      ]);

      switch (method) {
        case 'get': {
          expect(client.get).toBeCalledTimes(2);
          expect(client.get).toHaveBeenNthCalledWith(1, restURL);
          expect(client.get).toHaveBeenNthCalledWith(2, restURL);
          break;
        }

        case 'post': {
          expect(client.post).toBeCalledTimes(2);
          expect(client.post).toHaveBeenNthCalledWith(1, restURL, restArg);
          expect(client.post).toHaveBeenNthCalledWith(2, restURL, restArg);
          break;
        }

        default: throw new Error();
      }
    };

    const paginationInfo = {
      pageNumber: 0,
      itemsPerPage: 10,
    };
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();

    await testOperation(async () => store.dispatch(rosterOperations.getCurrentShiftRoster(paginationInfo)),
      'get',
      `/tenant/${tenantId}/roster/shiftRosterView/current?p=${paginationInfo.pageNumber}`
      + `&n=${paginationInfo.itemsPerPage}`);

    await testOperation(async () => store.dispatch(rosterOperations.getShiftRoster({
      fromDate,
      toDate,
      pagination: paginationInfo,
    })),
    'get',
    `/tenant/${tenantId}/roster/shiftRosterView?`
      + `p=${paginationInfo.pageNumber}&n=${paginationInfo.itemsPerPage}`
      + `&startDate=${serializeLocalDate(fromDate)}&endDate=${
        serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`);

    await testOperation(async () => store.dispatch(rosterOperations.getShiftRosterFor({
      fromDate,
      toDate,
      spotList: [],
    })),
    'post',
    `/tenant/${tenantId}/roster/shiftRosterView/for?`
      + `&startDate=${serializeLocalDate(fromDate)}`
      + `&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    []);
  });

  it('should dispatch the last shift roster REST call on refreshAvailabilityRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    jest.spyOn(rosterOperations, 'refreshAvailabilityRoster').mockRestore();

    const testOperation = async (operation: () => void, method: 'get'|'post', restURL: string, restArg?: any) => {
      store.clearActions();
      resetRestClientMock(client);

      switch (method) {
        case 'get': {
          onGet(restURL, {
            ...mockAvailabilityRoster,
            employeeIdToAvailabilityViewListMap: {},
            employeeIdToShiftViewListMap: {},
            score: '0hard/0medium/0soft',
          });
          break;
        }

        case 'post': {
          onPost(restURL, restArg, {
            ...mockAvailabilityRoster,
            employeeIdToAvailabilityViewListMap: {},
            employeeIdToShiftViewListMap: {},
            score: '0hard/0medium/0soft',
          });
          break;
        }

        default: throw new Error();
      }

      await operation();
      store.clearActions();

      await store.dispatch(rosterOperations.refreshAvailabilityRoster());

      expect(store.getActions()).toEqual([
        actions.setAvailabilityRosterIsLoading(true),
        actions.setAvailabilityRosterView({ ...mockAvailabilityRoster,
          employeeIdToAvailabilityViewListMap: {},
          employeeIdToShiftViewListMap: {} }),
        actions.setAvailabilityRosterIsLoading(false),
      ]);

      switch (method) {
        case 'get': {
          expect(client.get).toBeCalledTimes(2);
          expect(client.get).toHaveBeenNthCalledWith(1, restURL);
          expect(client.get).toHaveBeenNthCalledWith(2, restURL);
          break;
        }

        case 'post': {
          expect(client.post).toBeCalledTimes(2);
          expect(client.post).toHaveBeenNthCalledWith(1, restURL, restArg);
          expect(client.post).toHaveBeenNthCalledWith(2, restURL, restArg);
          break;
        }

        default: throw new Error();
      }
    };

    const paginationInfo = {
      pageNumber: 0,
      itemsPerPage: 10,
    };
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();

    await testOperation(async () => store.dispatch(rosterOperations.getCurrentAvailabilityRoster(paginationInfo)),
      'get',
      `/tenant/${tenantId}/roster/availabilityRosterView/current?p=${paginationInfo.pageNumber}`
      + `&n=${paginationInfo.itemsPerPage}`);

    await testOperation(async () => store.dispatch(rosterOperations.getAvailabilityRoster({
      fromDate,
      toDate,
      pagination: paginationInfo,
    })),
    'get',
    `/tenant/${tenantId}/roster/availabilityRosterView?`
      + `p=${paginationInfo.pageNumber}&n=${paginationInfo.itemsPerPage}`
      + `&startDate=${serializeLocalDate(fromDate)}&endDate=${
        serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`);

    await testOperation(async () => store.dispatch(rosterOperations.getAvailabilityRosterFor({
      fromDate,
      toDate,
      employeeList: [],
    })),
    'post',
    `/tenant/${tenantId}/roster/availabilityRosterView/for?`
      + `&startDate=${serializeLocalDate(fromDate)}`
      + `&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    []);
  });

  it('should dispatch actions and call client on getRosterState', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const { rosterState } = mockShiftRoster;

    onGet(`/tenant/${tenantId}/roster/state`, rosterState);
    await store.dispatch(rosterOperations.getRosterState());

    expect(store.getActions()).toEqual([
      actions.setRosterStateIsLoading(true),
      actions.setRosterState(rosterState),
      actions.setRosterStateIsLoading(false),
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/state`);
  });


  it('should dispatch actions and call client on publish', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);

    onPost(`/tenant/${tenantId}/roster/publishAndProvision`, {}, {
      publishedFromDate: '2018-01-01',
      publishedToDate: '2018-01-08',
    });

    await store.dispatch(rosterOperations.publish());

    expect(store.getActions()).toEqual([
      actions.publishRoster({
        publishedFromDate: moment('2018-01-01').toDate(),
        publishedToDate: moment('2018-01-08').toDate(),
      }),
      alert.showSuccessMessage('publish',
        { from: moment('2018-01-01').format('LL'), to: moment('2018-01-08').format('LL') }),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/publishAndProvision`, {});

    expect(mockRefreshShiftRoster).toBeCalled();
  });


  it('should dispatch actions and call client on getInitialShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const spotList: Spot[] = [spotSelectors.getSpotList(store.getState())[0]];
    const fromDate = moment((state.rosterState.rosterState as RosterState).firstDraftDate)
      .startOf('week').toDate();
    const toDate = moment((state.rosterState.rosterState as RosterState).firstDraftDate)
      .endOf('week').toDate();

    onPost(`/tenant/${tenantId}/roster/shiftRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    spotList, { ...mockShiftRoster, spotIdToShiftViewListMap: {}, score: '0hard/0medium/0soft' });
    await store.dispatch(rosterOperations.getInitialShiftRoster());

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&`
    + `endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, spotList);
  });

  it('should not dispatch actions and call client on getInitialShiftRoster if no spots', async () => {
    const { store, client } = mockStore({
      ...state,
      spotList: {
        spotMapById: new Map<number, DomainObjectView<Spot>>(),
        isLoading: false,
      },
    });
    await store.dispatch(rosterOperations.getInitialShiftRoster());

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.post).not.toBeCalled();
  });

  it('should not dispatch actions and call client on getInitialShiftRoster if roster state is null', async () => {
    const { store, client } = mockStore({
      ...state,
      rosterState: {
        rosterState: null,
        isLoading: false,
      },
    });
    await store.dispatch(rosterOperations.getInitialShiftRoster());

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.post).not.toBeCalled();
  });

  it('should dispatch actions and call client on getCurrentShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10,
    };

    onGet(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}`
    + `&n=${pagination.itemsPerPage}`, {
      ...mockShiftRoster,
      spotIdToShiftViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getCurrentShiftRoster(pagination));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView/current?`
      + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`);
  });

  it('should dispatch actions and call client on getShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-02', 'YYYY-MM-DD').toDate();

    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10,
    };

    onGet(`/tenant/${tenantId}/roster/shiftRosterView?`
    + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`
    + `&startDate=${serializeLocalDate(fromDate)}&`
    + `endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, {
      ...mockShiftRoster,
      spotIdToShiftViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getShiftRoster({
      fromDate,
      toDate,
      pagination,
    }));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView?`
    + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`
    + `&startDate=${serializeLocalDate(fromDate)}`
    + `&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`);
  });

  it('should dispatch actions and call client on getShiftRosterFor', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const spotList: Spot[] = [];
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-02', 'YYYY-MM-DD').toDate();

    onPost(`/tenant/${tenantId}/roster/shiftRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    spotList, { ...mockShiftRoster, spotIdToShiftViewListMap: {}, score: '0hard/0medium/0soft' });
    await store.dispatch(rosterOperations.getShiftRosterFor({
      fromDate,
      toDate,
      spotList,
    }));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&`
    + `endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, []);
  });

  it('should dispatch actions and call client on getInitialAvailabilityRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const employeeList: Employee[] = [employeeSelectors.getEmployeeList(store.getState())[0]];
    const fromDate = moment((state.rosterState.rosterState as RosterState).firstDraftDate)
      .startOf('week').toDate();
    const toDate = moment((state.rosterState.rosterState as RosterState).firstDraftDate)
      .endOf('week').toDate();

    onPost(`/tenant/${tenantId}/roster/availabilityRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    employeeList, {
      ...mockAvailabilityRoster,
      employeeIdToShiftViewListMap: {},
      employeeIdToAvailabilityViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getInitialAvailabilityRoster());

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(true),
      actions.setAvailabilityRosterView({
        ...mockAvailabilityRoster,
        employeeIdToShiftViewListMap: {},
        employeeIdToAvailabilityViewListMap: {},
      }),
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/availabilityRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&`
    + `endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, employeeList);
  });

  it('should not dispatch actions and call client on getInitialAvailabilityRoster if no employees', async () => {
    const { store, client } = mockStore({
      ...state,
      employeeList: {
        employeeMapById: new Map<number, DomainObjectView<Employee>>(),
        isLoading: false,
      },
    });
    await store.dispatch(rosterOperations.getInitialAvailabilityRoster());

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.post).not.toBeCalled();
  });

  it('should not dispatch actions and call client on getInitialAvailabilityRoster if'
  + 'roster state is null', async () => {
    const { store, client } = mockStore({
      ...state,
      rosterState: {
        rosterState: null,
        isLoading: false,
      },
    });
    await store.dispatch(rosterOperations.getInitialAvailabilityRoster());

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.post).not.toBeCalled();
  });

  it('should dispatch actions and call client on getCurrentAvailabilityRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10,
    };

    onGet(`/tenant/${tenantId}/roster/availabilityRosterView/current?p=${pagination.pageNumber}`
    + `&n=${pagination.itemsPerPage}`, {
      ...mockAvailabilityRoster,
      employeeIdToShiftViewListMap: {},
      employeeIdToAvailabilityViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getCurrentAvailabilityRoster(pagination));

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(true),
      actions.setAvailabilityRosterView({
        ...mockAvailabilityRoster,
        employeeIdToShiftViewListMap: {},
        employeeIdToAvailabilityViewListMap: {},
      }),
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/availabilityRosterView/current?`
      + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`);
  });

  it('should dispatch actions and call client on getAvailabilityRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-02', 'YYYY-MM-DD').toDate();

    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10,
    };

    onGet(`/tenant/${tenantId}/roster/availabilityRosterView?`
    + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`
    + `&startDate=${serializeLocalDate(fromDate)}`
    + `&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, {
      ...mockAvailabilityRoster,
      employeeIdToShiftViewListMap: {},
      employeeIdToAvailabilityViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getAvailabilityRoster({
      fromDate,
      toDate,
      pagination,
    }));

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(true),
      actions.setAvailabilityRosterView({
        ...mockAvailabilityRoster,
        employeeIdToShiftViewListMap: {},
        employeeIdToAvailabilityViewListMap: {},
      }),
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/availabilityRosterView?`
    + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`
    + `&startDate=${serializeLocalDate(fromDate)}&endDate=${
      serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`);
  });

  it('should dispatch actions and call client on getAvailabilityRosterFor', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const employeeList: Employee[] = [];
    const fromDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const toDate = moment('2018-01-02', 'YYYY-MM-DD').toDate();

    onPost(`/tenant/${tenantId}/roster/availabilityRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`,
    employeeList, {
      ...mockAvailabilityRoster,
      employeeIdToShiftViewListMap: {},
      employeeIdToAvailabilityViewListMap: {},
      score: '0hard/0medium/0soft',
    });
    await store.dispatch(rosterOperations.getAvailabilityRosterFor({
      fromDate,
      toDate,
      employeeList: [],
    }));

    expect(store.getActions()).toEqual([
      actions.setAvailabilityRosterIsLoading(true),
      actions.setAvailabilityRosterView({
        ...mockAvailabilityRoster,
        employeeIdToShiftViewListMap: {},
        employeeIdToAvailabilityViewListMap: {},
      }),
      actions.setAvailabilityRosterIsLoading(false),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/availabilityRosterView/for?`
    + `&startDate=${serializeLocalDate(fromDate)}&`
    + `endDate=${serializeLocalDate(moment(toDate).add(1, 'day').toDate())}`, []);
  });
});

describe('Roster reducers', () => {
  it('set is roster state loading', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.setRosterStateIsLoading(true)),
    ).toEqual({ ...state.rosterState, isLoading: true });
  });

  it('set roster state', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.setRosterState(mockShiftRoster.rosterState)),
    ).toEqual({ ...state.rosterState, rosterState: mockShiftRoster.rosterState });
  });

  it('publishes correctly', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.publishRoster({
        publishedFromDate: moment('2019-01-01', 'YYYY-MM-DD').toDate(),
        publishedToDate: moment('2019-01-08', 'YYYY-MM-DD').toDate(),
      })),
    ).toEqual({ ...state.rosterState,
      rosterState: {
        ...mockShiftRoster.rosterState,
        firstDraftDate: moment('2019-01-08', 'YYYY-MM-DD').toDate(),
        unplannedRotationOffset: (mockShiftRoster.rosterState.unplannedRotationOffset + 7)
      % mockShiftRoster.rosterState.rotationLength,
      } });
  });

  it('set is shift roster loading', () => {
    expect(
      shiftRosterViewReducer(state.shiftRoster, actions.setShiftRosterIsLoading(true)),
    ).toEqual({ ...state.shiftRoster, isLoading: true });
  });

  it('set shift roster', () => {
    expect(
      shiftRosterViewReducer(state.shiftRoster, actions.setShiftRosterView({
        ...mockShiftRoster, tenantId: 1,
      })),
    ).toEqual({ ...state.shiftRoster,
      shiftRosterView: {
        ...mockShiftRoster,
        tenantId: 1,
      } });
  });

  it('set is availability roster loading', () => {
    expect(
      availabilityRosterReducer(state.availabilityRoster, actions.setAvailabilityRosterIsLoading(true)),
    ).toEqual({ ...state.availabilityRoster, isLoading: true });
  });

  it('set availability roster', () => {
    expect(
      availabilityRosterReducer(state.availabilityRoster, actions.setAvailabilityRosterView({
        ...mockAvailabilityRoster, tenantId: 1,
      })),
    ).toEqual({ ...state.availabilityRoster,
      availabilityRosterView: {
        ...mockAvailabilityRoster,
        tenantId: 1,
      } });
  });

  it('set solving during solving', () => {
    expect(
      solverReducer(state.solverState, actions.solveRoster()),
    ).toEqual({
      solverStatus: 'SOLVING',
    });
  });

  it('unset solving after termination', () => {
    expect(
      solverReducer(state.solverState, actions.terminateSolvingRosterEarly()),
    ).toEqual({
      solverStatus: 'TERMINATED',
    });
  });
});

describe('Roster selectors', () => {
  it('should return an empty list if loading on getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      skillList: {
        ...state.skillList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      spotList: {
        ...state.spotList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      employeeList: {
        ...state.employeeList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      contractList: {
        ...state.contractList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      shiftRoster: {
        ...state.shiftRoster, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...state,
      rosterState: {
        ...state.rosterState, isLoading: true,
      },
    })).toEqual([]);
  });

  it('should return the spotList in shift roster in getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster(state)).toEqual(mockShiftRoster.spotList);
  });

  it('should return an empty list if loading on getEmployeeListInAvailabilityRoster', () => {
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      skillList: {
        ...state.skillList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      spotList: {
        ...state.spotList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      employeeList: {
        ...state.employeeList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      contractList: {
        ...state.contractList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      availabilityRoster: {
        ...state.availabilityRoster, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...state,
      rosterState: {
        ...state.rosterState, isLoading: true,
      },
    })).toEqual([]);
  });

  it('should return the spotList in availability roster in getEmployeeListInAvailabilityRoster', () => {
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster(state)).toEqual(mockAvailabilityRoster.employeeList);
  });

  it('should throw an exception if loading in getShiftListForSpot', () => {
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      skillList: {
        ...state.skillList, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      spotList: {
        ...state.spotList, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      employeeList: {
        ...state.employeeList, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      contractList: {
        ...state.contractList, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      shiftRoster: {
        ...state.shiftRoster, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...state,
      rosterState: {
        ...state.rosterState, isLoading: true,
      },
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
  });

  it('should return the shift list for a given spot in getShiftListForSpot', () => {
    expect(rosterSelectors.getShiftListForSpot(state, mockShiftRoster.spotList[0]))
      .toEqual([
        {
          tenantId: 0,
          startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          spot: mockShiftRoster.spotList[0],
          requiredSkillSet: [],
          originalEmployee: null,
          employee: {
            tenantId: 0,
            id: 20,
            version: 0,
            name: 'Employee',
            skillProficiencySet: [],
            contract: {
              tenantId: 0,
              id: 30,
              version: 0,
              name: 'Contract',
              maximumMinutesPerDay: null,
              maximumMinutesPerWeek: null,
              maximumMinutesPerMonth: null,
              maximumMinutesPerYear: null,
            },
            covidRiskType: 'INOCULATED',
          },
          rotationEmployee: null,
          indictmentScore: {
            hardScore: 0,
            mediumScore: 0,
            softScore: 0,
          },
          pinnedByUser: false,
        },
      ]);
  });

  it('should return an empty list if spot not in roster getShiftListForSpot', () => {
    expect(rosterSelectors.getShiftListForSpot(state, {
      tenantId: 0,
      id: 999,
      version: 0,
      name: 'Missing Spot',
      requiredSkillSet: [],
      covidWard: false,
    }))
      .toEqual([]);
  });

  it('should throw an exception if loading in getShiftListForEmployee', () => {
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...state,
      skillList: {
        ...state.skillList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...state,
      spotList: {
        ...state.spotList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...state,
      employeeList: {
        ...state.employeeList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...state,
      contractList: {
        ...state.contractList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      availabilityRoster: {
        ...state.availabilityRoster, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...state,
      rosterState: {
        ...state.rosterState, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
  });

  it('should return the shift list for a given employee in getShiftListForEmployee', () => {
    expect(rosterSelectors.getShiftListForEmployee(state, mockAvailabilityRoster.employeeList[0]))
      .toEqual([
        {
          tenantId: 0,
          startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          spot: mockShiftRoster.spotList[0],
          requiredSkillSet: [],
          originalEmployee: null,
          employee: {
            tenantId: 0,
            id: 20,
            version: 0,
            name: 'Employee',
            skillProficiencySet: [],
            contract: {
              tenantId: 0,
              id: 30,
              version: 0,
              name: 'Contract',
              maximumMinutesPerDay: null,
              maximumMinutesPerWeek: null,
              maximumMinutesPerMonth: null,
              maximumMinutesPerYear: null,
            },
            covidRiskType: 'INOCULATED',
          },
          rotationEmployee: null,
          indictmentScore: {
            hardScore: 0,
            mediumScore: 0,
            softScore: 0,
          },
          pinnedByUser: false,
        },
      ]);
  });

  it('should return an empty list if spot not in roster getShiftListForEmployee', () => {
    expect(rosterSelectors.getShiftListForEmployee(state, {
      tenantId: 0,
      id: 999,
      version: 0,
      name: 'Missing Employee',
      skillProficiencySet: [],
      contract: {
        tenantId: 0,
        id: 9999,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    }))
      .toEqual([]);
  });

  it('should throw an exception if loading in getAvailabilityListForEmployee', () => {
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      skillList: {
        ...state.skillList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      spotList: {
        ...state.spotList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      employeeList: {
        ...state.employeeList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      contractList: {
        ...state.contractList, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      availabilityRoster: {
        ...state.availabilityRoster, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...state,
      rosterState: {
        ...state.rosterState, isLoading: true,
      },
    }, state.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
  });

  it('should return the shift list for a given employee in getAvailabilityListForEmployee', () => {
    expect(rosterSelectors.getAvailabilityListForEmployee(state, mockAvailabilityRoster.employeeList[0]))
      .toEqual([
        {
          tenantId: 0,
          startDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          endDateTime: moment('2018-01-01', 'YYYY-MM-DD').toDate(),
          employee: {
            tenantId: 0,
            id: 20,
            version: 0,
            name: 'Employee',
            skillProficiencySet: [],
            contract: {
              tenantId: 0,
              id: 30,
              version: 0,
              name: 'Contract',
              maximumMinutesPerDay: null,
              maximumMinutesPerWeek: null,
              maximumMinutesPerMonth: null,
              maximumMinutesPerYear: null,
            },
            covidRiskType: 'INOCULATED',
          },
          state: 'DESIRED',
        },
      ]);
  });

  it('should return an empty list if spot not in roster getAvailabilityListForEmployee', () => {
    expect(rosterSelectors.getShiftListForEmployee(state, {
      tenantId: 0,
      id: 999,
      version: 0,
      name: 'Missing Employee',
      skillProficiencySet: [],
      contract: {
        tenantId: 0,
        id: 9999,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerYear: null,
      },
      covidRiskType: 'INOCULATED',
    }))
      .toEqual([]);
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
    employeeMapById: new Map([[
      20, {
        tenantId: 0,
        id: 20,
        version: 0,
        name: 'Employee',
        skillProficiencySet: [],
        contract: 30,
        covidRiskType: 'INOCULATED',
      },
    ]]),
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map([[
      30, {
        tenantId: 0,
        id: 30,
        version: 0,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
    ]]),
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map([[
      10, {
        tenantId: 0,
        id: 10,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
        covidWard: false,
      },
    ]]),
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map(),
  },
  shiftTemplateList: {
    isLoading: false,
    shiftTemplateMapById: new Map(),
  },
  rosterState: {
    isLoading: false,
    rosterState: mockShiftRoster.rosterState,
  },
  shiftRoster: {
    isLoading: false,
    shiftRosterView: mockShiftRoster,
  },
  availabilityRoster: {
    isLoading: false,
    availabilityRosterView: mockAvailabilityRoster,
  },
  solverState: {
    solverStatus: 'TERMINATED',
  },
  alerts: {
    alertList: [],
    idGeneratorIndex: 0,
  },
};
