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
import ShiftView from 'domain/ShiftView';

export enum ActionType {
  ADD_SHIFT = 'ADD_SHIFT',
  REMOVE_SHIFT = 'REMOVE_SHIFT',
  UPDATE_SHIFT = 'UPDATE_SHIFT',
  REFRESH_SHIFT_LIST = 'REFRESH_SHIFT_LIST',
  SET_SHIFT_LIST_LOADING = 'SET_SHIFT_LIST_LOADING'
}

export interface SetShiftListLoadingAction extends Action<ActionType.SET_SHIFT_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddShiftAction extends Action<ActionType.ADD_SHIFT> {
  readonly shift: ShiftView;
}

export interface RemoveShiftAction extends Action<ActionType.REMOVE_SHIFT> {
  readonly shift: ShiftView;
}

export interface UpdateShiftAction extends Action<ActionType.UPDATE_SHIFT> {
  readonly shift: ShiftView;
}

export interface RefreshShiftListAction extends Action<ActionType.REFRESH_SHIFT_LIST> {
  readonly shiftList: ShiftView[];
}

export type ShiftAction = SetShiftListLoadingAction | AddShiftAction | RemoveShiftAction |
  UpdateShiftAction | RefreshShiftListAction;

export interface ShiftList {
  readonly isLoading: boolean;
  readonly shiftMapById: Map<number, ShiftView>;
}
