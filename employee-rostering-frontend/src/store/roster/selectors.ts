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
import { AppState } from '../types';
import { employeeSelectors } from '../employee';
import { spotSelectors } from '../spot';
import ShiftView from 'domain/ShiftView';
import Shift from 'domain/Shift';

function isLoading(state: AppState) {
  return state.spotList.isLoading || state.employeeList.isLoading || state.skillList.isLoading ||
    state.shiftList.isLoading;
}

export const getShiftById = (state: AppState, id: number): Shift => {
  if (isLoading(state)) {
      throw Error("Shift list is loading");
  }
  const view = state.shiftList.shiftMapById.get(id) as ShiftView;
  return {
    ...view,
    spot: spotSelectors.getSpotById(state, view.spotId),
    rotationEmployee: (view.rotationEmployeeId !== null)?
      employeeSelectors.getEmployeeById(state, view.rotationEmployeeId) : null,
    employee: (view.employeeId !== null)?
      employeeSelectors.getEmployeeById(state, view.employeeId) : null,
  };
};

export const getShiftList = (state: AppState): Shift[] => {
  if (isLoading(state)) {
    return [];
  }
  const out: Shift[] = [];
  state.shiftList.shiftMapById.forEach((value, key) => out.push(getShiftById(state, key)));
  return out;
};