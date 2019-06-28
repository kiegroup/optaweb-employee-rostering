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

import { Action } from 'redux';
import ShiftRosterView from 'domain/ShiftRosterView';
import RosterView from 'domain/RosterView';
import RosterState from 'domain/RosterState';

export enum ActionType {
  SET_ROSTER_STATE_IS_LOADING = 'SET_ROSTER_STATE_IS_LOADING',
  SET_ROSTER_STATE = 'SET_ROSTER_STATE',
  SET_SHIFT_ROSTER_IS_LOADING = 'SET_SHIFT_ROSTER_IS_LOADING',
  SET_SHIFT_ROSTER_VIEW = 'SET_SHIFT_ROSTER_VIEW',
  SOLVE_ROSTER = 'SOLVE_ROSTER',
  TERMINATE_ROSTER_EARLY = 'TERMINATE_ROSTER_EARLY'
}

export interface SetRosterStateIsLoadingAction extends Action<ActionType.SET_ROSTER_STATE_IS_LOADING> {
  isLoading: boolean;
}


export interface SetRosterStateAction extends Action<ActionType.SET_ROSTER_STATE> {
  readonly rosterState: RosterState;
}

export interface SetShiftRosterIsLoadingAction extends Action<ActionType.SET_SHIFT_ROSTER_IS_LOADING> {
  isLoading: boolean;
}

export interface SetShiftRosterViewAction extends Action<ActionType.SET_SHIFT_ROSTER_VIEW> {
  readonly shiftRoster: ShiftRosterView;
}

export interface SolveRosterAction extends Action<ActionType.SOLVE_ROSTER> {
}

export interface TerminateRosterEarlyAction extends Action<ActionType.TERMINATE_ROSTER_EARLY> {
}

export type RosterAction = SetRosterStateIsLoadingAction | SetRosterStateAction | SetShiftRosterIsLoadingAction
  | SetShiftRosterViewAction | SolveRosterAction | TerminateRosterEarlyAction;

export interface CurrentRosterState {
  readonly isLoading: boolean;
  readonly rosterState: RosterState;
}

export interface CurrentShiftRoster {
  readonly isLoading: boolean;
  readonly shiftRosterView: ShiftRosterView;
}