
import moment from 'moment';
import {
  RosterStateActionType, SolverAction, ShiftRosterViewActionType, RosterStateAction,
  ShiftRosterViewAction, CurrentSolverState, CurrentRosterState, CurrentShiftRoster, SolverActionType,
  CurrentAvailabilityRoster, AvailabilityRosterViewActionType, AvailabilityRosterViewAction,
} from './types';

export const initialSolverState: CurrentSolverState = {
  solverStatus: 'NOT_SOLVING',
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
      return { ...state, solverStatus: 'SOLVING_ACTIVE' };
    }
    case SolverActionType.TERMINATE_SOLVING_ROSTER_EARLY: {
      return { ...state, solverStatus: 'NOT_SOLVING' };
    }
    case SolverActionType.UPDATE_SOLVER_STATUS: {
      return { ...state, solverStatus: action.solverStatus };
    }
    default:
      return state;
  }
};
