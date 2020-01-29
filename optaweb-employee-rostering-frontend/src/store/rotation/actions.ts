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

import { ShiftTemplate } from 'domain/ShiftTemplate';
import DomainObjectView from 'domain/DomainObjectView';
import { ActionFactory } from '../types';
import {
  ActionType, SetShiftTemplateListLoadingAction, AddShiftTemplateAction,
  UpdateShiftTemplateAction, RemoveShiftTemplateAction, RefreshShiftTemplateListAction,
} from './types';

export const setIsShiftTemplateListLoading: ActionFactory<boolean, SetShiftTemplateListLoadingAction> = isLoading => ({
  type: ActionType.SET_SHIFT_TEMPLATE_LIST_LOADING,
  isLoading,
});

export const addShiftTemplate:
ActionFactory<DomainObjectView<ShiftTemplate>, AddShiftTemplateAction> = newShiftTemplate => ({
  type: ActionType.ADD_SHIFT_TEMPLATE,
  shiftTemplate: newShiftTemplate,
});

export const removeShiftTemplate: ActionFactory<DomainObjectView<ShiftTemplate>,
RemoveShiftTemplateAction> = deletedShiftTemplate => ({
  type: ActionType.REMOVE_SHIFT_TEMPLATE,
  shiftTemplate: deletedShiftTemplate,
});

export const updateShiftTemplate: ActionFactory<DomainObjectView<ShiftTemplate>,
UpdateShiftTemplateAction> = updatedShiftTemplate => ({
  type: ActionType.UPDATE_SHIFT_TEMPLATE,
  shiftTemplate: updatedShiftTemplate,
});

export const refreshShiftTemplateList: ActionFactory<DomainObjectView<ShiftTemplate>[],
RefreshShiftTemplateListAction> = newShiftTemplateList => ({
  type: ActionType.REFRESH_SHIFT_TEMPLATE_LIST,
  shiftTemplateList: newShiftTemplateList,
});
