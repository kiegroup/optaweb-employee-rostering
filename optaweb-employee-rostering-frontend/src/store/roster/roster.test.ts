
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
import { RosterState } from 'domain/RosterState';
import { serializeLocalDate } from 'store/rest/DataSerialization';
import { flushPromises } from 'setupTests';
import { doNothing } from 'types';
import { Map, List } from 'immutable';
import { createIdMapFromList } from 'util/ImmutableCollectionOperations';
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
      shortId: 'e',
      color: '#FFFFFF',
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
      shortId: 'e',
      color: '#FFFFFF',
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

const state: Partial<AppState> = {
  tenantData: {
    currentTenantId: 0,
    tenantList: List(),
    timezoneList: ['America/Toronto'],
  },
  employeeList: {
    isLoading: false,
    employeeMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 20,
        version: 0,
        name: 'Employee',
        skillProficiencySet: [],
        contract: 30,
        shortId: 'e',
        color: '#FFFFFF',
      },
    ]),
  },
  contractList: {
    isLoading: false,
    contractMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 30,
        version: 0,
        name: 'Contract',
        maximumMinutesPerDay: null,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      },
    ]),
  },
  spotList: {
    isLoading: false,
    spotMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 10,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
    ]),
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
  skillList: {
    isLoading: false,
    skillMapById: Map(),
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

  it('should not dispatch actions or call client if tenantId is negative', async () => {
    const { store, client } = mockStore({ tenantData: { currentTenantId: -1, tenantList: List(), timezoneList: [] } });
    const pagination = {
      pageNumber: 0,
      itemsPerPage: 10,
    };
    const fromDate = new Date();
    const toDate = new Date();

    const rosterSlice = {
      pagination,
      fromDate,
      toDate,
    };

    await store.dispatch(rosterOperations.getAvailabilityRoster(rosterSlice));
    await store.dispatch(rosterOperations.getInitialAvailabilityRoster());
    await store.dispatch(rosterOperations.getCurrentAvailabilityRoster(pagination));
    await store.dispatch(rosterOperations.getAvailabilityRosterFor({ ...rosterSlice, employeeList: [] }));

    await store.dispatch(rosterOperations.getShiftRoster(rosterSlice));
    await store.dispatch(rosterOperations.getInitialShiftRoster());
    await store.dispatch(rosterOperations.getCurrentShiftRoster(pagination));
    await store.dispatch(rosterOperations.getShiftRosterFor({ ...rosterSlice, spotList: [] }));

    await flushPromises();

    expect(client.get).not.toBeCalled();
    expect(client.post).not.toBeCalled();
    expect(client.delete).not.toBeCalled();
    expect(client.put).not.toBeCalled();
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

  it('should dispatch actions and call client on replan roster', async () => {
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);
    jest.useFakeTimers();
    const solvingStartTime = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    MockDate.set(solvingStartTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    onPost(`/tenant/${tenantId}/roster/replan`, {}, {});
    await store.dispatch(rosterOperations.replanRoster());
    expect(store.getActions()).toEqual([
      actions.solveRoster(),
      alert.showInfoMessage('startSolvingRoster', {
        startSolvingTime: moment(solvingStartTime).format('LLL'),
      }),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/replan`, {});

    jest.advanceTimersByTime(1000);
    await Promise.resolve(); // hack to wait for the refresh action to finish

    expect(mockRefreshShiftRoster).toBeCalled();
  });

  it('should dispatch actions and call client on get solver status', async () => {
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);
    jest.useFakeTimers();
    const solvingStartTime = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    MockDate.set(solvingStartTime);

    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    onGet(`/tenant/${tenantId}/roster/status`, 'SOLVING_ACTIVE');
    await store.dispatch(rosterOperations.getSolverStatus());
    expect(store.getActions()).toEqual([
      actions.updateSolverStatus({ solverStatus: 'SOLVING_ACTIVE' }),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/roster/status`);

    jest.advanceTimersByTime(1000);
    await Promise.resolve(); // hack to wait for the refresh action to finish

    expect(mockRefreshShiftRoster).toBeCalled();

    mockRefreshShiftRoster.mockClear();
    store.clearActions();

    onGet(`/tenant/${tenantId}/roster/status`, 'NOT_SOLVING');
    await store.dispatch(rosterOperations.getSolverStatus());
    expect(store.getActions()).toEqual([
      actions.updateSolverStatus({ solverStatus: 'NOT_SOLVING' }),
      actions.terminateSolvingRosterEarly(),
    ]);

    expect(mockRefreshShiftRoster).toBeCalledTimes(1);
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

    const testOperation = async (operation: () => Promise<void>, method: 'get'|'post',
      restURL: string, restArg?: any) => {
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

  it('should dispatch actions and call client on commitChanges', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockRefreshShiftRoster = jest.spyOn(rosterOperations, 'refreshShiftRoster')
      .mockImplementation(() => doNothing);

    onPost(`/tenant/${tenantId}/roster/commitChanges`, {}, {});

    await store.dispatch(rosterOperations.commitChanges());

    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('commitChanges'),
    ]);

    expect(client.post).toBeCalledTimes(1);
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/roster/commitChanges`, {});

    expect(mockRefreshShiftRoster).toBeCalled();
  });


  it('should dispatch actions and call client on getInitialShiftRoster', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const spotList: Spot[] = [spotSelectors.getSpotList(store.getState())[0]];
    const fromDate = moment((store.getState().rosterState.rosterState as RosterState).firstDraftDate)
      .startOf('week').toDate();
    const toDate = moment((store.getState().rosterState.rosterState as RosterState).firstDraftDate)
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
        spotMapById: Map(),
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
    const fromDate = moment((store.getState().rosterState.rosterState as RosterState).firstDraftDate)
      .startOf('week').toDate();
    const toDate = moment((store.getState().rosterState.rosterState as RosterState).firstDraftDate)
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
        employeeMapById: Map(),
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
      solverStatus: 'SOLVING_ACTIVE',
    });
  });

  it('unset solving after termination', () => {
    expect(
      solverReducer(state.solverState, actions.terminateSolvingRosterEarly()),
    ).toEqual({
      solverStatus: 'NOT_SOLVING',
    });
  });
});

describe('Roster selectors', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('should return null if rosterState is loading in getRosterState', () => {
    expect(rosterSelectors.getRosterState({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    })).toEqual(null);
  });

  it('should return the rosterState if rosterState is not loading in getRosterState', () => {
    expect(rosterSelectors.getRosterState(storeState)).toEqual(storeState.rosterState.rosterState);
  });

  it('should return an empty list if loading on getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      skillList: {
        ...storeState.skillList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      spotList: {
        ...storeState.spotList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      employeeList: {
        ...storeState.employeeList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      contractList: {
        ...storeState.contractList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      shiftRoster: {
        ...storeState.shiftRoster, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getSpotListInShiftRoster({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    })).toEqual([]);
  });

  it('should return the spotList in shift roster in getSpotListInShiftRoster', () => {
    expect(rosterSelectors.getSpotListInShiftRoster(storeState)).toEqual(mockShiftRoster.spotList);
  });

  it('should return an empty list if loading on getEmployeeListInAvailabilityRoster', () => {
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      skillList: {
        ...storeState.skillList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      spotList: {
        ...storeState.spotList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      employeeList: {
        ...storeState.employeeList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      contractList: {
        ...storeState.contractList, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      availabilityRoster: {
        ...storeState.availabilityRoster, isLoading: true,
      },
    })).toEqual([]);
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    })).toEqual([]);
  });

  it('should return the spotList in availability roster in getEmployeeListInAvailabilityRoster', () => {
    expect(rosterSelectors.getEmployeeListInAvailabilityRoster(storeState))
      .toEqual(mockAvailabilityRoster.employeeList);
  });

  it('should throw an exception if loading in getShiftListForSpot', () => {
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      skillList: {
        ...storeState.skillList, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      spotList: {
        ...storeState.spotList, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      employeeList: {
        ...storeState.employeeList, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      contractList: {
        ...storeState.contractList, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      shiftRoster: {
        ...storeState.shiftRoster, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
    expect(() => rosterSelectors.getShiftListForSpot({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    }, storeState.spotList.spotMapById.get(10) as any as Spot)).toThrow();
  });

  it('should return the shift list for a given spot in getShiftListForSpot', () => {
    expect(rosterSelectors.getShiftListForSpot(storeState, mockShiftRoster.spotList[0]))
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
            shortId: 'e',
            color: '#FFFFFF',
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
    expect(rosterSelectors.getShiftListForSpot(storeState, {
      tenantId: 0,
      id: 999,
      version: 0,
      name: 'Missing Spot',
      requiredSkillSet: [],
    }))
      .toEqual([]);
  });

  it('should throw an exception if loading in getShiftListForEmployee', () => {
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...storeState,
      skillList: {
        ...storeState.skillList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...storeState,
      spotList: {
        ...storeState.spotList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...storeState,
      employeeList: {
        ...storeState.employeeList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...storeState,
      contractList: {
        ...storeState.contractList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      availabilityRoster: {
        ...storeState.availabilityRoster, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getShiftListForEmployee({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
  });

  it('should return the shift list for a given employee in getShiftListForEmployee', () => {
    expect(rosterSelectors.getShiftListForEmployee(storeState, mockAvailabilityRoster.employeeList[0]))
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
            shortId: 'e',
            color: '#FFFFFF',
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
    expect(rosterSelectors.getShiftListForEmployee(storeState, {
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
      shortId: 'me',
      color: '#000000',
    }))
      .toEqual([]);
  });

  it('should throw an exception if loading in getAvailabilityListForEmployee', () => {
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      skillList: {
        ...storeState.skillList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      spotList: {
        ...storeState.spotList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      employeeList: {
        ...storeState.employeeList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      contractList: {
        ...storeState.contractList, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      availabilityRoster: {
        ...storeState.availabilityRoster, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
    expect(() => rosterSelectors.getAvailabilityListForEmployee({
      ...storeState,
      rosterState: {
        ...storeState.rosterState, isLoading: true,
      },
    }, storeState.employeeList.employeeMapById.get(20) as any as Employee)).toThrow();
  });

  it('should return the shift list for a given employee in getAvailabilityListForEmployee', () => {
    expect(rosterSelectors.getAvailabilityListForEmployee(storeState, mockAvailabilityRoster.employeeList[0]))
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
            shortId: 'e',
            color: '#FFFFFF',
          },
          state: 'DESIRED',
        },
      ]);
  });

  it('should return an empty list if spot not in roster getAvailabilityListForEmployee', () => {
    expect(rosterSelectors.getShiftListForEmployee(storeState, {
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
      shortId: 'me',
      color: '#000000',
    }))
      .toEqual([]);
  });
});
