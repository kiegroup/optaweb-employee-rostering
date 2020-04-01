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

import moment from 'moment';
import {
  RosterStateActionType, SolverAction, ShiftRosterViewActionType, RosterStateAction,
  ShiftRosterViewAction, CurrentSolverState, CurrentRosterState, CurrentShiftRoster, SolverActionType,
  CurrentAvailabilityRoster, AvailabilityRosterViewActionType, AvailabilityRosterViewAction,
} from './types';

export const initialSolverState: CurrentSolverState = {
  solverStatus: 'TERMINATED',
};

export const initialRosterState: CurrentRosterState = {
  isLoading: true,
  rosterState: null,
};

export const initialShiftRosterState: CurrentShiftRoster = {
  isLoading: true,
  shiftRosterView: null,
};

export const initialAvailabilityRosterState: CurrentAvailabilityRoster = {
  isLoading: true,
  availabilityRosterView: null,
};

export const rosterStateReducer = (state = initialRosterState, action: RosterStateAction): CurrentRosterState => {
  switch (action.type) {
    case RosterStateActionType.SET_ROSTER_STATE_IS_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case RosterStateActionType.SET_ROSTER_STATE: {
      return { ...state, rosterState: action.rosterState };
    }
    case RosterStateActionType.PUBLISH_ROSTER: {
      if (state.rosterState) {
        const publishedDuration = moment(action.publishResult.publishedToDate)
          .diff(action.publishResult.publishedFromDate, 'days');
        return { ...state,
          rosterState: {
            ...state.rosterState,
            firstDraftDate: action.publishResult.publishedToDate,
            unplannedRotationOffset: (state.rosterState.unplannedRotationOffset + publishedDuration)
          % state.rosterState.rotationLength,
          } };
      }

      return { ...state, rosterState: null };
    }
    default:
      return state;
  }
};

export const shiftRosterViewReducer = (state = initialShiftRosterState,
  action: ShiftRosterViewAction): CurrentShiftRoster => {
  switch (action.type) {
    case ShiftRosterViewActionType.SET_SHIFT_ROSTER_IS_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ShiftRosterViewActionType.SET_SHIFT_ROSTER_VIEW: {
      return { ...state, shiftRosterView: action.shiftRoster };
    }
    default:
      return state;
  }
};

export const availabilityRosterReducer = (state = initialAvailabilityRosterState,
  action: AvailabilityRosterViewAction): CurrentAvailabilityRoster => {
  switch (action.type) {
    case AvailabilityRosterViewActionType.SET_AVAILABILITY_ROSTER_IS_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case AvailabilityRosterViewActionType.SET_AVAILABILITY_ROSTER_VIEW: {
      return { ...state, availabilityRosterView: action.availabilityRoster };
    }
    default:
      return state;
  }
};


export const solverReducer = (state = initialSolverState, action: SolverAction): CurrentSolverState => {
  switch (action.type) {
    case SolverActionType.SOLVE_ROSTER: {
      return { ...state, solverStatus: 'SOLVING' };
    }
    case SolverActionType.TERMINATE_SOLVING_ROSTER_EARLY: {
      return { ...state, solverStatus: 'TERMINATED' };
    }
    case SolverActionType.UPDATE_SOLVER_STATUS: {
      return { ...state, solverStatus: action.solverStatus };
    }
    default:
      return state;
  }
};
