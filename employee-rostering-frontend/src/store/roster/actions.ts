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

import { ActionFactory } from '../types';
import { ActionType, SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction, TerminateRosterEarlyAction }
  from './types';
import RosterState from 'domain/RosterState';
import ShiftRosterView from 'domain/ShiftRosterView';

export const setRosterStateIsLoading: ActionFactory<boolean, SetRosterStateIsLoadingAction> = isLoading => ({
  type: ActionType.SET_ROSTER_STATE_IS_LOADING,
  isLoading: isLoading
});

export const setRosterState: ActionFactory<RosterState, SetRosterStateAction> = newRosterState => ({
  type: ActionType.SET_ROSTER_STATE,
  rosterState: newRosterState
});

export const setShiftRosterIsLoading: ActionFactory<boolean, SetShiftRosterIsLoadingAction> = isLoading => ({
  type: ActionType.SET_SHIFT_ROSTER_IS_LOADING,
  isLoading: isLoading
});

export const setShiftRosterView: ActionFactory<ShiftRosterView, SetShiftRosterViewAction> = shiftRosterView => ({
  type: ActionType.SET_SHIFT_ROSTER_VIEW,
  shiftRoster: shiftRosterView
});

export const solveRoster: ActionFactory<void, SolveRosterAction> = () => ({
  type: ActionType.SOLVE_ROSTER
});

export const terminateRosterEarly: ActionFactory<void, TerminateRosterEarlyAction> = () => ({
  type: ActionType.TERMINATE_ROSTER_EARLY
});
