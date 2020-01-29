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
import { ShiftTemplate } from 'domain/ShiftTemplate';
import DomainObjectView from 'domain/DomainObjectView';

export enum ActionType {
  ADD_SHIFT_TEMPLATE = 'ADD_SHIFT_TEMPLATE',
  REMOVE_SHIFT_TEMPLATE = 'REMOVE_SHIFT_TEMPLATE',
  UPDATE_SHIFT_TEMPLATE = 'UPDATE_SHIFT_TEMPLATE',
  REFRESH_SHIFT_TEMPLATE_LIST = 'REFRESH_SHIFT_TEMPLATE_LIST',
  SET_SHIFT_TEMPLATE_LIST_LOADING = 'SET_SHIFT_TEMPLATE_LIST_LOADING'
}

export interface SetShiftTemplateListLoadingAction extends Action<ActionType.SET_SHIFT_TEMPLATE_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddShiftTemplateAction extends Action<ActionType.ADD_SHIFT_TEMPLATE> {
  readonly shiftTemplate: DomainObjectView<ShiftTemplate>;
}

export interface RemoveShiftTemplateAction extends Action<ActionType.REMOVE_SHIFT_TEMPLATE> {
  readonly shiftTemplate: DomainObjectView<ShiftTemplate>;
}

export interface UpdateShiftTemplateAction extends Action<ActionType.UPDATE_SHIFT_TEMPLATE> {
  readonly shiftTemplate: DomainObjectView<ShiftTemplate>;
}

export interface RefreshShiftTemplateListAction extends Action<ActionType.REFRESH_SHIFT_TEMPLATE_LIST> {
  readonly shiftTemplateList: DomainObjectView<ShiftTemplate>[];
}

export type ShiftTemplateAction = SetShiftTemplateListLoadingAction | AddShiftTemplateAction |
RemoveShiftTemplateAction | UpdateShiftTemplateAction | RefreshShiftTemplateListAction;

export interface ShiftTemplateList {
  readonly isLoading: boolean;
  readonly shiftTemplateMapById: Map<number, DomainObjectView<ShiftTemplate>>;
}
