
import { RosterState } from 'domain/RosterState';
import { ShiftRosterView } from 'domain/ShiftRosterView';
import { AvailabilityRosterView } from 'domain/AvailabilityRosterView';
import {
  RosterStateActionType, ShiftRosterViewActionType, SolverActionType, PublishRosterAction,
  SetRosterStateIsLoadingAction, SetRosterStateAction, SetShiftRosterIsLoadingAction,
  SetShiftRosterViewAction, SolveRosterAction, TerminateSolvingRosterEarlyAction, PublishResult,
  SetAvailabilityRosterIsLoadingAction, SetAvailabilityRosterViewAction,
  AvailabilityRosterViewActionType, CurrentSolverState, UpdateSolverStatusAction,
} from './types';
import { ActionFactory } from '../types';

export const publishRoster: ActionFactory<PublishResult, PublishRosterAction> = pr => ({
  type: RosterStateActionType.PUBLISH_ROSTER,
  publishResult: pr,
});

export const setRosterStateIsLoading: ActionFactory<boolean, SetRosterStateIsLoadingAction> = isLoading => ({
  type: RosterStateActionType.SET_ROSTER_STATE_IS_LOADING,
  isLoading,
});

export const setRosterState: ActionFactory<RosterState, SetRosterStateAction> = newRosterState => ({
  type: RosterStateActionType.SET_ROSTER_STATE,
  rosterState: newRosterState,
});

export const setShiftRosterIsLoading: ActionFactory<boolean, SetShiftRosterIsLoadingAction> = isLoading => ({
  type: ShiftRosterViewActionType.SET_SHIFT_ROSTER_IS_LOADING,
  isLoading,
});

export const setShiftRosterView: ActionFactory<ShiftRosterView, SetShiftRosterViewAction> = shiftRosterView => ({
  type: ShiftRosterViewActionType.SET_SHIFT_ROSTER_VIEW,
  shiftRoster: shiftRosterView,
});


export const setAvailabilityRosterIsLoading:
ActionFactory<boolean, SetAvailabilityRosterIsLoadingAction> = isLoading => ({
  type: AvailabilityRosterViewActionType.SET_AVAILABILITY_ROSTER_IS_LOADING,
  isLoading,
});

export const setAvailabilityRosterView:
ActionFactory<AvailabilityRosterView, SetAvailabilityRosterViewAction> = availabilityRosterView => ({
  type: AvailabilityRosterViewActionType.SET_AVAILABILITY_ROSTER_VIEW,
  availabilityRoster: availabilityRosterView,
});


export const solveRoster: ActionFactory<void, SolveRosterAction> = () => ({
  type: SolverActionType.SOLVE_ROSTER,
});

export const terminateSolvingRosterEarly: ActionFactory<void, TerminateSolvingRosterEarlyAction> = () => ({
  type: SolverActionType.TERMINATE_SOLVING_ROSTER_EARLY,
});

export const updateSolverStatus:
ActionFactory<CurrentSolverState, UpdateSolverStatusAction> = (solverStatus: CurrentSolverState) => ({
  type: SolverActionType.UPDATE_SOLVER_STATUS,
  solverStatus: solverStatus.solverStatus,
});
