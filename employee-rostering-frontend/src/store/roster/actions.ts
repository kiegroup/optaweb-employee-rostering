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
import { ActionType, SetShiftListLoadingAction, AddShiftAction, UpdateShiftAction, RemoveShiftAction, RefreshShiftListAction } from './types';
import ShiftView from 'domain/ShiftView';

export const setIsShiftListLoading: ActionFactory<Boolean, SetShiftListLoadingAction> = (isLoading: Boolean) => ({
  type: ActionType.SET_SHIFT_LIST_LOADING,
  isLoading: isLoading.valueOf()
});

export const addShift: ActionFactory<ShiftView, AddShiftAction> = newShift => ({
  type: ActionType.ADD_SHIFT,
  shift: newShift
});

export const removeShift: ActionFactory<ShiftView, RemoveShiftAction> = deletedShift => ({
  type: ActionType.REMOVE_SHIFT,
  shift: deletedShift
});

export const updateShift: ActionFactory<ShiftView, UpdateShiftAction> = updatedShift => ({
  type: ActionType.UPDATE_SHIFT,
  shift: updatedShift
});

export const refreshShiftList: ActionFactory<ShiftView[], RefreshShiftListAction> = newShiftList => ({
  type: ActionType.REFRESH_SHIFT_LIST,
  shiftList: newShiftList
});
