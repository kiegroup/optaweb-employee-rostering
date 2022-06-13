
import { alert } from 'store/alert';
import {
  createIdMapFromList,
  mapDomainObjectToView,
} from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete, onPut } from 'store/rest/RestTestUtils';
import { TimeBucket } from 'domain/TimeBucket';
import {
  timeBucketViewToDomainObjectView,
  timeBucketToTimeBucketView,
  TimeBucketView,
} from 'store/rotation/TimeBucketView';
import moment from 'moment';
import { Map } from 'immutable';
import reducer, { timeBucketSelectors, timeBucketOperations } from './index';
import * as actions from './actions';
import { AppState } from '../types';
import { mockStore } from '../mockStore';

const state: Partial<AppState> = {
  skillList: {
    isLoading: false,
    skillMapById: Map(),
  },
  employeeList: {
    isLoading: false,
    employeeMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 3,
        version: 0,
        name: 'Employee',
        contract: 10,
        skillProficiencySet: [],
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
        id: 10,
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
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
    ]),
  },
  timeBucketList: {
    isLoading: false,
    timeBucketMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 2,
        version: 0,
        spot: 1,
        additionalSkillSet: [],
        repeatOnDaySetList: ['MONDAY'],
        seatList: [{ dayInRotation: 0, employee: 3 }],
        startTime: moment('09:00', 'HH:mm').toDate(),
        endTime: moment('17:00', 'HH:mm').toDate(),
      },
      {
        tenantId: 0,
        id: 4,
        version: 0,
        spot: 1,
        additionalSkillSet: [],
        repeatOnDaySetList: ['MONDAY'],
        seatList: [{ dayInRotation: 0, employee: 3 }],
        startTime: moment('17:00', 'HH:mm').toDate(),
        endTime: moment('09:00', 'HH:mm').toDate(),
      },
    ]),
  },
};

describe('Rotation operations', () => {
  it('should dispatch actions and call client on refresh time bucket list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockTimeBucketList: TimeBucketView[] = [{
      tenantId: 0,
      id: 2,
      version: 0,
      spotId: 1,
      additionalSkillSetIdList: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{ dayInRotation: 0, employeeId: 3 }],
      startTime: '09:00:00',
      endTime: '17:00:00',
    }];

    onGet(`/tenant/${tenantId}/rotation/`, mockTimeBucketList);
    await store.dispatch(timeBucketOperations.refreshTimeBucketList());
    expect(store.getActions()).toEqual([
      actions.setIsTimeBucketListLoading(true),
      actions.refreshTimeBucketList(mockTimeBucketList.map(timeBucketViewToDomainObjectView)),
      actions.setIsTimeBucketListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/`);
  });

  it('should dispatch actions and call client on a successful delete time bucket', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const timeBucketToDelete: TimeBucket = {
      tenantId: 0,
      id: 2,
      version: 0,
      spot: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
      additionalSkillSet: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{ dayInRotation: 0, employee: null }],
      startTime: moment('09:00', 'HH:mm').toDate(),
      endTime: moment('17:00', 'HH:mm').toDate(),
    };
    onDelete(`/tenant/${tenantId}/rotation/${timeBucketToDelete.id}`, true);
    await store.dispatch(timeBucketOperations.removeTimeBucket(timeBucketToDelete));
    expect(store.getActions()).toEqual([
      actions.removeTimeBucket(mapDomainObjectToView(timeBucketToDelete)),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/${timeBucketToDelete.id}`);
  });

  it('should call client but not dispatch actions on a failed delete time bucket', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const timeBucketToDelete: TimeBucket = {
      tenantId: 0,
      id: 2,
      version: 0,
      spot: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
      additionalSkillSet: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{ dayInRotation: 0, employee: null }],
      startTime: moment('09:00', 'HH:mm').toDate(),
      endTime: moment('17:00', 'HH:mm').toDate(),
    };
    onDelete(`/tenant/${tenantId}/rotation/${timeBucketToDelete.id}`, false);
    await store.dispatch(timeBucketOperations.removeTimeBucket(timeBucketToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeShiftTemplateError', { id: timeBucketToDelete.id }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/${timeBucketToDelete.id}`);
  });

  it('should dispatch actions and call client on add time bucket', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const timeBucketToAdd: TimeBucket = {
      tenantId: 0,
      spot: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
      additionalSkillSet: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{ dayInRotation: 0, employee: null }],
      startTime: moment('09:00', 'HH:mm').toDate(),
      endTime: moment('17:00', 'HH:mm').toDate(),
    };
    const timeBucketToAddWithUpdatedId: TimeBucket = {
      ...timeBucketToAdd,
      id: 4,
      version: 0,
    };
    onPost(`/tenant/${tenantId}/rotation/add`,
      timeBucketToTimeBucketView(timeBucketToAdd),
      timeBucketToTimeBucketView(timeBucketToAddWithUpdatedId));
    await store.dispatch(timeBucketOperations.addTimeBucket(timeBucketToAdd));
    expect(store.getActions()).toEqual([
      actions.addTimeBucket(mapDomainObjectToView(timeBucketToAddWithUpdatedId)),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/add`,
      timeBucketToTimeBucketView(timeBucketToAdd));
  });

  it('should dispatch actions and call client on update shift template', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const timeBucketToUpdate: TimeBucket = {
      tenantId: 0,
      id: 4,
      version: 0,
      spot: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Spot',
        requiredSkillSet: [],
      },
      additionalSkillSet: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{ dayInRotation: 0, employee: null }],
      startTime: moment('09:00', 'HH:mm').toDate(),
      endTime: moment('17:00', 'HH:mm').toDate(),
    };
    const timeBucketWithUpdatedVersion: TimeBucket = {
      ...timeBucketToUpdate,
      version: 1,
    };
    onPut(`/tenant/${tenantId}/rotation/update`, timeBucketToTimeBucketView(timeBucketToUpdate),
      timeBucketToTimeBucketView(timeBucketWithUpdatedVersion));
    await store.dispatch(timeBucketOperations.updateTimeBucket(timeBucketToUpdate));
    expect(store.getActions()).toEqual([
      actions.updateTimeBucket(mapDomainObjectToView(timeBucketWithUpdatedVersion)),
    ]);
    expect(client.put).toHaveBeenCalledTimes(1);
    expect(client.put).toHaveBeenCalledWith(`/tenant/${tenantId}/rotation/update`,
      timeBucketToTimeBucketView(timeBucketToUpdate));
  });
});

describe('Rotation reducers', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();
  const addedTimeBucket: TimeBucket = {
    tenantId: 0,
    id: 10,
    version: 0,
    spot: {
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Spot',
      requiredSkillSet: [],
    },
    additionalSkillSet: [],
    repeatOnDaySetList: ['MONDAY'],
    seatList: [{ dayInRotation: 0, employee: null }],
    startTime: moment('09:00', 'HH:mm').toDate(),
    endTime: moment('17:00', 'HH:mm').toDate(),
  };
  const updatedTimeBucket: TimeBucket = {
    tenantId: 0,
    id: 2,
    version: 0,
    spot: {
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Spot',
      requiredSkillSet: [],
    },
    additionalSkillSet: [],
    repeatOnDaySetList: ['MONDAY'],
    seatList: [{ dayInRotation: 0, employee: null }],
    startTime: moment('09:00', 'HH:mm').toDate(),
    endTime: moment('17:00', 'HH:mm').toDate(),
  };
  const deletedTimeBucket: TimeBucket = {
    tenantId: 0,
    id: 2,
    version: 0,
    spot: {
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Spot',
      requiredSkillSet: [],
    },
    additionalSkillSet: [],
    repeatOnDaySetList: ['MONDAY'],
    seatList: [{ dayInRotation: 0, employee: null }],
    startTime: moment('09:00', 'HH:mm').toDate(),
    endTime: moment('17:00', 'HH:mm').toDate(),
  };

  it('set is loading', () => {
    expect(
      reducer(storeState.timeBucketList, actions.setIsTimeBucketListLoading(true)),
    ).toEqual({ ...storeState.timeBucketList, isLoading: true });
  });
  it('add shift template', () => {
    expect(
      reducer(storeState.timeBucketList, actions.addTimeBucket(mapDomainObjectToView(addedTimeBucket))),
    ).toEqual({ ...storeState.timeBucketList,
      timeBucketMapById:
        storeState.timeBucketList.timeBucketMapById
          .set(addedTimeBucket.id as number, mapDomainObjectToView(addedTimeBucket)) });
  });
  it('remove shift template', () => {
    expect(
      reducer(storeState.timeBucketList, actions.removeTimeBucket(mapDomainObjectToView(deletedTimeBucket))),
    ).toEqual({ ...storeState.timeBucketList,
      timeBucketMapById:
        storeState.timeBucketList.timeBucketMapById.delete(deletedTimeBucket.id as number) });
  });
  it('update shift template', () => {
    expect(
      reducer(storeState.timeBucketList, actions.updateTimeBucket(mapDomainObjectToView(updatedTimeBucket))),
    ).toEqual({ ...storeState.timeBucketList,
      timeBucketMapById:
        storeState.timeBucketList.timeBucketMapById.set(updatedTimeBucket.id as number,
          mapDomainObjectToView(updatedTimeBucket)) });
  });
  it('refresh shift template list', () => {
    expect(
      reducer(storeState.timeBucketList, actions.refreshTimeBucketList([addedTimeBucket]
        .map(mapDomainObjectToView))),
    ).toEqual({ ...storeState.timeBucketList,
      timeBucketMapById:
        createIdMapFromList([addedTimeBucket].map(mapDomainObjectToView)) });
  });
});

describe('Rotation selectors', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();
  it('should throw an error if time bucket list is loading', () => {
    expect(() => timeBucketSelectors.getTimeBucketById({
      ...storeState,
      timeBucketList: { ...storeState.timeBucketList, isLoading: true },
    }, 2)).toThrow();
    expect(() => timeBucketSelectors.getTimeBucketById({
      ...storeState,
      spotList: { ...storeState.spotList, isLoading: true },
    }, 2)).toThrow();
    expect(() => timeBucketSelectors.getTimeBucketById({
      ...storeState,
      employeeList: { ...storeState.employeeList, isLoading: true },
    }, 2)).toThrow();
    expect(() => timeBucketSelectors.getTimeBucketById({
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    }, 2)).toThrow();
    expect(() => timeBucketSelectors.getTimeBucketById({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    }, 2)).toThrow();
  });

  it('should get a time bucket by id', () => {
    const shiftTemplate = timeBucketSelectors.getTimeBucketById(storeState, 2);
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
      },
      additionalSkillSet: [],
      repeatOnDaySetList: ['MONDAY'],
      seatList: [{
        dayInRotation: 0,
        employee: {
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
          shortId: 'e',
          color: '#FFFFFF',
        },
      }],
      startTime: moment('09:00', 'HH:mm').toDate(),
      endTime: moment('17:00', 'HH:mm').toDate(),
    });
  });

  it('should return an empty list if time bucket list is loading', () => {
    expect(timeBucketSelectors.getTimeBucketList({
      ...storeState,
      timeBucketList: { ...storeState.timeBucketList, isLoading: true },
    })).toEqual([]);
    expect(timeBucketSelectors.getTimeBucketList({
      ...storeState,
      spotList: { ...storeState.spotList, isLoading: true },
    })).toEqual([]);
    expect(timeBucketSelectors.getTimeBucketList({
      ...storeState,
      employeeList: { ...storeState.employeeList, isLoading: true },
    })).toEqual([]);
    expect(timeBucketSelectors.getTimeBucketList({
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    })).toEqual([]);
    expect(timeBucketSelectors.getTimeBucketList({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    })).toEqual([]);
  });

  it('should return a list of all time buckets', () => {
    const timeBucketList = timeBucketSelectors.getTimeBucketList(storeState);
    expect(timeBucketList).toEqual(expect.arrayContaining([
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
        },
        additionalSkillSet: [],
        repeatOnDaySetList: ['MONDAY'],
        seatList: [{
          dayInRotation: 0,
          employee: {
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
            shortId: 'e',
            color: '#FFFFFF',
          },
        }],
        startTime: moment('09:00', 'HH:mm').toDate(),
        endTime: moment('17:00', 'HH:mm').toDate(),
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
        },
        additionalSkillSet: [],
        repeatOnDaySetList: ['MONDAY'],
        seatList: [{
          dayInRotation: 0,
          employee: {
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
            shortId: 'e',
            color: '#FFFFFF',
          },
        }],
        startTime: moment('17:00', 'HH:mm').toDate(),
        endTime: moment('09:00', 'HH:mm').toDate(),
      },
    ]));
    expect(timeBucketList.length).toEqual(2);
  });
});
