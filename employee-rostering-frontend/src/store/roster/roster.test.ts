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
import * as alerts from 'ui/Alerts';
import { rosterStateReducer, shiftRosterViewReducer, rosterSelectors, rosterOperations, solverReducer } from './index';
import { resetRestClientMock, onGet, onPost } from 'store/rest/RestTestUtils';
import MockDate from 'mockdate';
import moment from 'moment';
import Spot from 'domain/Spot';
import ShiftRosterView from 'domain/ShiftRosterView';

const mockShiftRoster: ShiftRosterView = {
  tenantId: 0,
  startDate: moment("2018-01-01", "YYYY-MM-DD").toISOString(),
  endDate: moment("2018-01-01", "YYYY-MM-DD").toISOString(),
  spotList: [{
    tenantId: 0,
    id: 10,
    version: 0,
    name: "Spot",
    requiredSkillSet: []
  }],
  employeeList: [],
  rosterState: {
    publishNotice: 0,
    firstDraftDate: moment("2018-01-01", "YYYY-MM-DD").toDate(),
    publishLength: 0,
    draftLength: 0,
    unplannedRotationOffset: 5,
    rotationLength: 10,
    lastHistoricDate: moment("2018-01-01", "YYYY-MM-DD").toDate(),
    timeZone: "",
    tenant: {
      name: "Tenant"
    }
  },
  spotIdToShiftViewListMap: {
    10: [{
      tenantId: 0,
      startDateTime: moment("2018-01-01", "YYYY-MM-DD").toDate(),
      endDateTime: moment("2018-01-01", "YYYY-MM-DD").toDate(),
      spotId: 10,
      employeeId: null,
      rotationEmployeeId: null,
      indictmentScore: {
        hardScore: 0,
        mediumScore: 0,
        softScore: 0
      },
      pinnedByUser: false
    }]
  }
};

describe('Roster operations', () => {
  it('should dispatch actions and call client on solve roster', async () => {
    const showInfoMessageMock = jest.spyOn(alerts, "showInfoMessage");
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, "refreshShiftRoster").mockImplementation(() => () => {});
    jest.useFakeTimers();
    const solvingStartTime = moment("2018-01-01", "YYYY-MM-DD").toDate();
    MockDate.set(solvingStartTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    onPost(`/tenant/${tenantId}/roster/solve`, {}, {});
    await store.dispatch(rosterOperations.solveRoster());
    expect(store.getActions()).toEqual([actions.solveRoster()]);
    expect(showInfoMessageMock).toBeCalled();
    expect(showInfoMessageMock).toBeCalledWith("Started Solving Roster", `Started Solving Roster at ${moment(solvingStartTime).format("LLL")}.`);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/solve`, {});

    jest.advanceTimersByTime(1000);
    await Promise.resolve(); // hack to wait for the refresh action to finish

    expect(mockRefreshShiftRoster).toBeCalled();
  });

  it('should dispatch actions and call client on terminate solving roster', async () => {
    const showInfoMessageMock = jest.spyOn(alerts, "showInfoMessage");
    const solvingEndTime = moment("2018-01-01", "YYYY-MM-DD").toDate();
    MockDate.set(solvingEndTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, "refreshShiftRoster").mockImplementation(() => () => {});

    onPost(`/tenant/${tenantId}/roster/terminate`, {}, {});
    await(store.dispatch(rosterOperations.terminateSolvingRosterEarly()));

    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/terminate`, {});
    
    expect(store.getActions()).toEqual([
      actions.terminateSolvingRosterEarly()
    ]);

    expect(mockRefreshShiftRoster).toBeCalled();
    expect(showInfoMessageMock).toBeCalled();
    expect(showInfoMessageMock).toBeCalledWith("Finished Solving Roster", `Finished Solving Roster at ${moment(solvingEndTime).format("LLL")}`);
  });

  it('should dispatch the last shift roster REST call on refreshShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    jest.spyOn(rosterOperations, "refreshShiftRoster").mockRestore();

    const testOperation = async (operation: () => void, method: 'get'|'post', restURL: string, restArg?: any) => {
      store.clearActions();
      resetRestClientMock(client);

      switch (method) {
        case 'get': {
          onGet(restURL, { ...mockShiftRoster, spotIdToShiftViewListMap: {} });
          break;
        }

        case 'post': {
          onPost(restURL, restArg, { ...mockShiftRoster, spotIdToShiftViewListMap: {} });
          break;
        }
      }

      await operation();
      store.clearActions();

      await store.dispatch(rosterOperations.refreshShiftRoster());

      expect(store.getActions()).toEqual([
        actions.setShiftRosterIsLoading(true),
        actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
        actions.setShiftRosterIsLoading(false)
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
      }
    };

    const paginationInfo = {
      pageNumber: 0,
      itemsPerPage: 10
    };
    const fromDate = moment("2018-01-01", "YYYY-MM-DD").toDate();
    const toDate = moment("2018-01-01", "YYYY-MM-DD").toDate();
    
    await testOperation(async () => await store.dispatch(rosterOperations.getCurrentShiftRoster(paginationInfo)),
      'get',
      `/tenant/${tenantId}/roster/shiftRosterView/current?p=${paginationInfo.pageNumber}&n=${paginationInfo.itemsPerPage}`
    );

    await testOperation(async () => await store.dispatch(rosterOperations.getShiftRoster({
      fromDate: fromDate,
      toDate: toDate,
      pagination: paginationInfo
    })),
    'get',
    `/tenant/${tenantId}/roster/shiftRosterView?` +
      `p=${paginationInfo.pageNumber}&n=${paginationInfo.itemsPerPage}` +
      `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`
    );

    await testOperation(async () => await store.dispatch(rosterOperations.getShiftRosterFor({
      fromDate: fromDate,
      toDate: toDate,
      spotList: []
    })),
    'post',
    `/tenant/${tenantId}/roster/shiftRosterView/for?` +
      `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`,
    []
    );
  });

  it('should dispatch actions and call client on getRosterState', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const rosterState = mockShiftRoster.rosterState;
    
    onGet(`/tenant/${tenantId}/roster/state`, rosterState);
    await store.dispatch(rosterOperations.getRosterState());

    expect(store.getActions()).toEqual([
      actions.setRosterStateIsLoading(true),
      actions.setRosterState(rosterState),
      actions.setRosterStateIsLoading(false)
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/state`);
  });


  it('should dispatch actions and call client on publish', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockShowSuccessMessage = jest.spyOn(alerts, "showSuccessMessage");
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, "refreshShiftRoster").mockImplementation(() => () => {});

    onPost(`/tenant/${tenantId}/roster/publishAndProvision`, {}, {
      publishedFromDate: "2018-01-01",
      publishedToDate: "2018-01-08"
    });

    await store.dispatch(rosterOperations.publish());

    expect(store.getActions()).toEqual([
      actions.publishRoster({
        publishedFromDate: moment("2018-01-01").toDate(),
        publishedToDate: moment("2018-01-08").toDate()
      })
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/publishAndProvision`, {});

    expect(mockRefreshShiftRoster).toBeCalled();
    expect(mockShowSuccessMessage).toBeCalled();
    expect(mockShowSuccessMessage).toBeCalledWith("Published Roster",
      `Published from ${moment("2018-01-01").format("LL")} to ${moment("2018-01-08").format("LL")}.`);
  });

  it('should dispatch actions and call client on getCurrentShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10
    };
    
    onGet(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`, { ...mockShiftRoster, spotIdToShiftViewListMap: {} });
    await store.dispatch(rosterOperations.getCurrentShiftRoster(pagination));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false)
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`);
  });

  it('should dispatch actions and call client on getShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const fromDate = moment("2018-01-01", "YYYY-MM-DD").toDate();
    const toDate = moment("2018-01-02", "YYYY-MM-DD").toDate();

    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10
    };
    
    onGet(`/tenant/${tenantId}/roster/shiftRosterView?` +
    `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}` +
    `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`, { ...mockShiftRoster, spotIdToShiftViewListMap: {} });
    await store.dispatch(rosterOperations.getShiftRoster({
      fromDate: fromDate,
      toDate: toDate,
      pagination: pagination
    }));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false)
    ]);

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView?` +
    `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}` +
    `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`);
  });

  it('should dispatch actions and call client on getShiftRosterFor', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const spotList: Spot[] = [];
    const fromDate = moment("2018-01-01", "YYYY-MM-DD").toDate();
    const toDate = moment("2018-01-02", "YYYY-MM-DD").toDate();
    
    onPost(`/tenant/${tenantId}/roster/shiftRosterView/for?` +
    `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`,
    spotList, { ...mockShiftRoster, spotIdToShiftViewListMap: {} });
    await store.dispatch(rosterOperations.getShiftRosterFor({
      fromDate: fromDate,
      toDate: toDate,
      spotList: spotList
    }));

    expect(store.getActions()).toEqual([
      actions.setShiftRosterIsLoading(true),
      actions.setShiftRosterView({ ...mockShiftRoster, spotIdToShiftViewListMap: {} }),
      actions.setShiftRosterIsLoading(false)
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/shiftRosterView/for?` +
    `&startDate=${moment(fromDate).format("YYYY-MM-DD")}&endDate=${moment(toDate).add(1, "day").format("YYYY-MM-DD")}`, []);
  });
});

describe('Roster reducers', () => {
  it('set is roster state loading', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.setRosterStateIsLoading(true))
    ).toEqual({ ...state.rosterState, isLoading: true });
  });

  it('set roster state', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.setRosterState(mockShiftRoster.rosterState))
    ).toEqual({ ...state.rosterState, rosterState: mockShiftRoster.rosterState });
  });

  it('publishes correctly', () => {
    expect(
      rosterStateReducer(state.rosterState, actions.publishRoster({
        publishedFromDate: moment("2019-01-01", "YYYY-MM-DD").toDate(),
        publishedToDate: moment("2019-01-08", "YYYY-MM-DD").toDate()
      }))
    ).toEqual({ ...state.rosterState, rosterState: {
      ...mockShiftRoster.rosterState,
      firstDraftDate: moment("2019-01-08", "YYYY-MM-DD").toDate(),
      unplannedRotationOffset: (mockShiftRoster.rosterState.unplannedRotationOffset + 7) % mockShiftRoster.rosterState.rotationLength
    }
    });
  });

  it('set is shift roster loading', () => {
    expect(
      shiftRosterViewReducer(state.shiftRoster, actions.setShiftRosterIsLoading(true))
    ).toEqual({ ...state.shiftRoster, isLoading: true });
  });

  it('set shift roster', () => {
    expect(
      shiftRosterViewReducer(state.shiftRoster, actions.setShiftRosterView({
        ...mockShiftRoster, tenantId: 1
      }))
    ).toEqual({ ...state.shiftRoster, shiftRosterView: {
      ...mockShiftRoster,
      tenantId: 1
    } });
  });

  it('set solving during solving', () => {
    expect(
      solverReducer(state.solverState, actions.solveRoster())
    ).toEqual({
      isSolving: true
    });
  });

  it('unset solving after termination', () => {
    expect(
      solverReducer(state.solverState, actions.terminateSolvingRosterEarly())
    ).toEqual({
      isSolving: false
    });
  });
});

describe('Roster selectors', () => {
  it('should return an empty list if loading on getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      skillList: { 
        ...state.skillList, isLoading: true
      }
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      spotList: { 
        ...state.spotList, isLoading: true
      }
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      employeeList: { 
        ...state.employeeList, isLoading: true
      }
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      contractList: { 
        ...state.contractList, isLoading: true
      }
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      shiftRoster: { 
        ...state.shiftRoster, isLoading: true
      }
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({ 
      ...state,
      rosterState: { 
        ...state.rosterState, isLoading: true
      }
    })).toEqual([]);
  });

  it('should return the spotList in shift roster in getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster(state)).toEqual(mockShiftRoster.spotList);
  });

  it('should throw an exception if loading in getShiftListForSpot', () => {
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      skillList: { 
        ...state.skillList, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      spotList: { 
        ...state.spotList, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      employeeList: { 
        ...state.employeeList, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      contractList: { 
        ...state.contractList, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      shiftRoster: { 
        ...state.shiftRoster, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({ 
      ...state,
      rosterState: { 
        ...state.rosterState, isLoading: true
      }
    }, state.spotList.spotMapById.get(10) as any as Spot)).toThrow();
  });

  it('should return the shift list for a given spot in getShiftListForSpot', () => {
    expect(rosterSelectors.getShiftListForSpot(state, mockShiftRoster.spotList[0]))
      .toEqual([
        {
          tenantId: 0,
          startDateTime: moment("2018-01-01", "YYYY-MM-DD").toDate(),
          endDateTime: moment("2018-01-01", "YYYY-MM-DD").toDate(),
          spot: mockShiftRoster.spotList[0],
          employee: null,
          rotationEmployee: null,
          indictmentScore: {
            hardScore: 0,
            mediumScore: 0,
            softScore: 0
          },
          pinnedByUser: false
        }
      ]);
  });

  it('should return an empty list if spot not in roster getShiftListForSpot', () => {
    expect(rosterSelectors.getShiftListForSpot(state, {
      tenantId: 0,
      id: 999,
      version: 0,
      name: "Missing Spot",
      requiredSkillSet: []
    }))
      .toEqual([]);
  });
});

const state: AppState = {
  tenantData: {
    currentTenantId: 0,
    tenantList: []
  },
  employeeList: {
    isLoading: false,
    employeeMapById: new Map()
  },
  contractList: {
    isLoading: false,
    contractMapById: new Map()
  },
  spotList: {
    isLoading: false,
    spotMapById: new Map([[
      10, {
        tenantId: 0,
        id: 10,
        version: 0,
        name: "Spot",
        requiredSkillSet: []
      }
    ]])
  },
  skillList: {
    isLoading: false,
    skillMapById: new Map()
  },
  rosterState: {
    isLoading: false,
    rosterState: mockShiftRoster.rosterState 
  },
  shiftRoster: {
    isLoading: false,
    shiftRosterView: mockShiftRoster
  },
  solverState: {
    isSolving: false
  }
};