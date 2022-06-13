
import { RosterState } from 'domain/RosterState';
import { ShiftRosterView } from 'domain/ShiftRosterView';
import { PaginationData, ObjectNumberMap, mapObjectNumberMap, mapObjectStringMap } from 'types';
import moment from 'moment';
import { Spot } from 'domain/Spot';
import { alert } from 'store/alert';
import { ThunkDispatch } from 'redux-thunk';
import { KindaShiftView, kindaShiftViewAdapter } from 'store/shift/KindaShiftView';
import { KindaEmployeeAvailabilityView, kindaAvailabilityViewAdapter } from 'store/availability/operations';
import RestServiceClient from 'store/rest';
import { AddAlertAction } from 'store/alert/types';
import { Employee } from 'domain/Employee';
import { AvailabilityRosterView } from 'domain/AvailabilityRosterView';
import { employeeSelectors } from 'store/employee';
import { spotSelectors } from 'store/spot';
import { serializeLocalDate } from 'store/rest/DataSerialization';
import { getHardMediumSoftScoreFromString } from 'domain/HardMediumSoftScore';
import { TimeBucket } from 'domain/TimeBucket';
import {
  SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction,
  TerminateSolvingRosterEarlyAction, PublishRosterAction, PublishResult,
  SetAvailabilityRosterIsLoadingAction, SetAvailabilityRosterViewAction, ShiftRosterViewAction,
  AvailabilityRosterViewAction,
  UpdateSolverStatusAction,
  SolverStatus,
} from './types';
import * as operations from './operations'; // Hack used for mocking
import * as actions from './actions';
import { ThunkCommandFactory, AppState } from '../types';

export interface RosterSliceInfo {
  fromDate: Date;
  toDate: Date;
}

interface KindaShiftRosterView extends Omit<ShiftRosterView, 'spotIdToShiftViewListMap' | 'score' |
'indictmentSummary'> {
  spotIdToShiftViewListMap: ObjectNumberMap<KindaShiftView[]>;
  score: string;
  indictmentSummary: {
    constraintToCountMap: Record<string, number>;
    constraintToScoreImpactMap: Record<string, string>;
  };
}

interface KindaAvailabilityRosterView extends Omit<AvailabilityRosterView,
'employeeIdToShiftViewListMap' | 'employeeIdToAvailabilityViewListMap' | 'unassignedShiftViewList' | 'score' |
'indictmentSummary'> {
  employeeIdToShiftViewListMap: ObjectNumberMap<KindaShiftView[]>;
  employeeIdToAvailabilityViewListMap: ObjectNumberMap<KindaEmployeeAvailabilityView[]>;
  unassignedShiftViewList: KindaShiftView[];
  score: string;
  indictmentSummary: {
    constraintToCountMap: Record<string, number>;
    constraintToScoreImpactMap: Record<string, string>;
  };
}

let lastCalledShiftRosterArgs: any | null;
let lastCalledShiftRoster:
ThunkCommandFactory<any, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> | null = null;

let lastCalledAvailabilityRosterArgs: any | null;
let lastCalledAvailabilityRoster: ThunkCommandFactory<any, SetAvailabilityRosterIsLoadingAction |
SetAvailabilityRosterViewAction> | null = null;

let stopSolvingRosterTimeout: NodeJS.Timeout|null = null;
let autoRefreshShiftRosterDuringSolvingIntervalTimeout: NodeJS.Timeout|null = null;

export function resetSolverStatus() {
  lastCalledShiftRosterArgs = null;
  lastCalledShiftRoster = null;
  lastCalledAvailabilityRosterArgs = null;
  lastCalledAvailabilityRoster = null;
}

function stopSolvingRoster(dispatch: ThunkDispatch<AppState, RestServiceClient,
AddAlertAction | TerminateSolvingRosterEarlyAction>) {
  if (stopSolvingRosterTimeout !== null) {
    clearTimeout(stopSolvingRosterTimeout);
    stopSolvingRosterTimeout = null;
  }
  if (autoRefreshShiftRosterDuringSolvingIntervalTimeout !== null) {
    clearInterval(autoRefreshShiftRosterDuringSolvingIntervalTimeout);
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = null;
  }
  dispatch(actions.terminateSolvingRosterEarly());
  Promise.all([
    dispatch(operations.refreshShiftRoster()),
  ]).then(() => {
    dispatch(alert.showInfoMessage('finishSolvingRoster', { finishSolvingTime: moment(new Date()).format('LLL') }));
  });
}

const updateInterval = 1000;

function refresh(dispatch: ThunkDispatch<AppState, RestServiceClient, any>) {
  autoRefreshShiftRosterDuringSolvingIntervalTimeout = null;
  Promise.all([
    dispatch(operations.refreshShiftRoster()),
    dispatch(operations.refreshAvailabilityRoster()),
    dispatch(operations.getSolverStatus()),
  ]);
}

export const solveRoster:
ThunkCommandFactory<void, AddAlertAction | SolveRosterAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  return client.post(`/tenant/${tenantId}/roster/solve`, {}).then(() => {
    const solvingStartTime: number = new Date().getTime();
    dispatch(actions.solveRoster());
    dispatch(alert.showInfoMessage('startSolvingRoster', {
      startSolvingTime: moment(solvingStartTime).format('LLL'),
    }));
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = setTimeout(() => refresh(dispatch), updateInterval);
  });
};

export const replanRoster:
ThunkCommandFactory<void, AddAlertAction | SolveRosterAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.post(`/tenant/${tenantId}/roster/replan`, {}).then(() => {
    const solvingStartTime: number = new Date().getTime();
    dispatch(actions.solveRoster());
    dispatch(alert.showInfoMessage('startSolvingRoster', {
      startSolvingTime: moment(solvingStartTime).format('LLL'),
    }));
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = setTimeout(() => refresh(dispatch), updateInterval);
  });
};


export const terminateSolvingRosterEarly:
ThunkCommandFactory<void, TerminateSolvingRosterEarlyAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.post(`/tenant/${tenantId}/roster/terminate`, {}).then(() => stopSolvingRoster(dispatch));
};

export const getSolverStatus:
ThunkCommandFactory<void, AddAlertAction | UpdateSolverStatusAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.get<SolverStatus>(`/tenant/${tenantId}/roster/status`).then((status) => {
    dispatch(actions.updateSolverStatus({ solverStatus: status }));
    if (status === 'NOT_SOLVING' && autoRefreshShiftRosterDuringSolvingIntervalTimeout !== null) {
      stopSolvingRoster(dispatch);
    } else if (status === 'SOLVING_ACTIVE' && autoRefreshShiftRosterDuringSolvingIntervalTimeout === null) {
      autoRefreshShiftRosterDuringSolvingIntervalTimeout = setTimeout(() => refresh(dispatch), updateInterval);
    }
  });
};

export const getInitialShiftRoster:
ThunkCommandFactory<void, ShiftRosterViewAction> = () => (dispatch, state) => {
  const { rosterState, isLoading } = state().rosterState;
  if (rosterState !== null && !isLoading) {
    const startDate = moment(rosterState.firstDraftDate).startOf('week').toDate();
    const endDate = moment(rosterState.firstDraftDate).endOf('week').toDate();
    const spotList = spotSelectors.getSpotList(state());
    const shownSpots = (spotList.length > 0) ? [spotList[0]] : [];

    if (shownSpots.length > 0) {
      dispatch(getShiftRosterFor({
        fromDate: startDate,
        toDate: endDate,
        spotList: shownSpots,
      }));
    } else {
      dispatch(actions.setShiftRosterIsLoading(false));
    }
  } else {
    dispatch(actions.setShiftRosterIsLoading(false));
  }
};

export const getInitialAvailabilityRoster:
ThunkCommandFactory<void, AvailabilityRosterViewAction> = () => (dispatch, state) => {
  const { rosterState, isLoading } = state().rosterState;
  if (rosterState !== null && !isLoading) {
    const startDate = moment(rosterState.firstDraftDate).startOf('week').toDate();
    const endDate = moment(rosterState.firstDraftDate).endOf('week').toDate();
    const employeeList = employeeSelectors.getEmployeeList(state());
    const shownEmployees = (employeeList.length > 0) ? [employeeList[0]] : [];

    if (shownEmployees.length > 0) {
      dispatch(getAvailabilityRosterFor({
        fromDate: startDate,
        toDate: endDate,
        employeeList: shownEmployees,
      }));
    } else {
      dispatch(actions.setAvailabilityRosterIsLoading(false));
    }
  } else {
    dispatch(actions.setAvailabilityRosterIsLoading(false));
  }
};

export const refreshShiftRoster:
ThunkCommandFactory<void, SetShiftRosterIsLoadingAction |
SetShiftRosterViewAction> = () => (dispatch) => {
  if (lastCalledShiftRosterArgs !== null && lastCalledShiftRoster !== null) {
    dispatch(lastCalledShiftRoster(lastCalledShiftRosterArgs));
  }
};

export const refreshAvailabilityRoster:
ThunkCommandFactory<void, SetAvailabilityRosterIsLoadingAction |
SetAvailabilityRosterViewAction> = () => (dispatch) => {
  if (lastCalledAvailabilityRosterArgs !== null && lastCalledAvailabilityRoster !== null) {
    dispatch(lastCalledAvailabilityRoster(lastCalledAvailabilityRosterArgs));
  }
};

export const getRosterState:
ThunkCommandFactory<void, SetRosterStateIsLoadingAction | SetRosterStateAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setRosterStateIsLoading(true));
  if (tenantId < 0) {
    return Promise.resolve();
  }
  return client.get<RosterState>(`/tenant/${tenantId}/roster/state`).then((newRosterState) => {
    dispatch(actions.setRosterState({
      ...newRosterState,
      firstDraftDate: new Date(newRosterState.firstDraftDate),
      lastHistoricDate: new Date(newRosterState.lastHistoricDate),
    }));
    dispatch(actions.setRosterStateIsLoading(false));
  });
};

export interface ProvisionParams {
  startRotationOffset: number;
  fromDate: Date;
  toDate: Date;
  timeBucketList: TimeBucket[];
}
export const provision:
ThunkCommandFactory<ProvisionParams, AddAlertAction> = params => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.post<void>(`/tenant/${tenantId}/roster/provision?startRotationOffset=${params.startRotationOffset
  }&fromDate=${moment(params.fromDate).format('YYYY-MM-DD')}&toDate=${
    moment(params.toDate).format('YYYY-MM-DD')}`, params.timeBucketList.map(tb => tb.id)).then(() => {
    dispatch(operations.refreshShiftRoster());
    dispatch(alert.showSuccessMessage('provision', {
      from: moment(params.fromDate).format('LL'),
      to: moment(params.toDate).format('LL'),
    }));
  });
};

export const publish:
ThunkCommandFactory<void, AddAlertAction | PublishRosterAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.post<PublishResult>(`/tenant/${tenantId}/roster/publishAndProvision`, {}).then((pr) => {
    dispatch(actions.publishRoster({
      publishedFromDate: moment(pr.publishedFromDate).toDate(),
      publishedToDate: moment(pr.publishedToDate).toDate(),
    }));
    dispatch(operations.refreshShiftRoster());
    dispatch(alert.showSuccessMessage('publish', {
      from: moment(pr.publishedFromDate).format('LL'),
      to: moment(pr.publishedToDate).format('LL'),
    }));
  });
};

export const commitChanges:
ThunkCommandFactory<void, AddAlertAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  return client.post<PublishResult>(`/tenant/${tenantId}/roster/commitChanges`, {}).then(() => {
    dispatch(operations.refreshShiftRoster());
    dispatch(alert.showSuccessMessage('commitChanges'));
  });
};

function convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView: KindaShiftRosterView): ShiftRosterView {
  return {
    ...newShiftRosterView,
    spotIdToShiftViewListMap: mapObjectNumberMap(newShiftRosterView.spotIdToShiftViewListMap,
      shiftViewList => shiftViewList.map(kindaShiftViewAdapter)),
    score: getHardMediumSoftScoreFromString(newShiftRosterView.score),
    indictmentSummary: {
      constraintToCountMap: newShiftRosterView.indictmentSummary.constraintToCountMap,
      constraintToScoreImpactMap: mapObjectStringMap(newShiftRosterView.indictmentSummary
        .constraintToScoreImpactMap, getHardMediumSoftScoreFromString),
    },
  };
}

function convertKindaAvailabilityRosterViewToAvailabilityRosterView(
  newAvailabilityRosterView: KindaAvailabilityRosterView,
): AvailabilityRosterView {
  return {
    ...newAvailabilityRosterView,
    employeeIdToAvailabilityViewListMap: mapObjectNumberMap(
      newAvailabilityRosterView.employeeIdToAvailabilityViewListMap,
      availabilityViewList => availabilityViewList.map(kindaAvailabilityViewAdapter),
    ),
    employeeIdToShiftViewListMap: mapObjectNumberMap(
      newAvailabilityRosterView.employeeIdToShiftViewListMap, shiftViewList => (
        shiftViewList.map(kindaShiftViewAdapter)
      ),
    ),
    unassignedShiftViewList: newAvailabilityRosterView.unassignedShiftViewList.map(kindaShiftViewAdapter),
    score: getHardMediumSoftScoreFromString(newAvailabilityRosterView.score),
    indictmentSummary: {
      constraintToCountMap: newAvailabilityRosterView.indictmentSummary.constraintToCountMap,
      constraintToScoreImpactMap: mapObjectStringMap(newAvailabilityRosterView.indictmentSummary
        .constraintToScoreImpactMap, getHardMediumSoftScoreFromString),
    },
  };
}

export const getCurrentShiftRoster: ThunkCommandFactory<PaginationData,
SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = pagination => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  dispatch(actions.setShiftRosterIsLoading(true));
  lastCalledShiftRoster = getCurrentShiftRoster;
  lastCalledShiftRosterArgs = pagination;
  return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/current?`
    + `p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then((newShiftRosterView) => {
    const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
    dispatch(actions.setShiftRosterView(shiftRosterView));
    dispatch(actions.setShiftRosterIsLoading(false));
  });
};

export const getShiftRoster: ThunkCommandFactory<RosterSliceInfo & { pagination: PaginationData },
SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  const fromDateAsString = serializeLocalDate(params.fromDate);
  const toDateAsString = serializeLocalDate(moment(params.toDate).add(1, 'day').toDate());
  dispatch(actions.setShiftRosterIsLoading(true));
  lastCalledShiftRoster = getShiftRoster;
  lastCalledShiftRosterArgs = params;
  return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView?`
    + `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}`
    + `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then((newShiftRosterView) => {
    const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
    dispatch(actions.setShiftRosterView(shiftRosterView));
    dispatch(actions.setShiftRosterIsLoading(false));
  });
};

export const getShiftRosterFor: ThunkCommandFactory<RosterSliceInfo & { spotList: Spot[] },
SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  const fromDateAsString = serializeLocalDate(params.fromDate);
  const toDateAsString = serializeLocalDate(moment(params.toDate).add(1, 'day').toDate());
  dispatch(actions.setShiftRosterIsLoading(true));
  lastCalledShiftRoster = getShiftRosterFor;
  lastCalledShiftRosterArgs = params;
  return client.post<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/for?`
    + `&startDate=${fromDateAsString}&endDate=${toDateAsString}`, params.spotList).then((newShiftRosterView) => {
    const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
    dispatch(actions.setShiftRosterView(shiftRosterView));
    dispatch(actions.setShiftRosterIsLoading(false));
  });
};

export const getCurrentAvailabilityRoster: ThunkCommandFactory<PaginationData,
SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = pagination => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  dispatch(actions.setAvailabilityRosterIsLoading(true));
  lastCalledAvailabilityRoster = getCurrentAvailabilityRoster;
  lastCalledAvailabilityRosterArgs = pagination;
  return client.get<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView/`
    + `current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then((newAvailabilityRosterView) => {
    const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(
      newAvailabilityRosterView,
    );
    dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
    dispatch(actions.setAvailabilityRosterIsLoading(false));
  });
};

export const getAvailabilityRoster: ThunkCommandFactory<RosterSliceInfo & { pagination: PaginationData },
SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = params => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  const fromDateAsString = serializeLocalDate(params.fromDate);
  const toDateAsString = serializeLocalDate(moment(params.toDate).add(1, 'day').toDate());
  dispatch(actions.setAvailabilityRosterIsLoading(true));
  lastCalledAvailabilityRoster = getAvailabilityRoster;
  lastCalledAvailabilityRosterArgs = params;
  return client.get<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView?`
    + `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}`
    + `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then((newAvailabilityRosterView) => {
    const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(
      newAvailabilityRosterView,
    );
    dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
    dispatch(actions.setAvailabilityRosterIsLoading(false));
  });
};

export const getAvailabilityRosterFor: ThunkCommandFactory<RosterSliceInfo & { employeeList: Employee[] },
SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = params => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  if (tenantId < 0) {
    return Promise.resolve();
  }
  const fromDateAsString = serializeLocalDate(params.fromDate);
  const toDateAsString = serializeLocalDate(moment(params.toDate).add(1, 'day').toDate());
  dispatch(actions.setAvailabilityRosterIsLoading(true));
  lastCalledAvailabilityRoster = getAvailabilityRosterFor;
  lastCalledAvailabilityRosterArgs = params;
  return client.post<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView/for?`
    + `&startDate=${fromDateAsString}&endDate=${toDateAsString}`,
  params.employeeList).then((newAvailabilityRosterView) => {
    const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(
      newAvailabilityRosterView,
    );
    dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
    dispatch(actions.setAvailabilityRosterIsLoading(false));
  });
};
