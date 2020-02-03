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
import { BasicObject } from 'types';

export enum ActionType {
  ADD_ALERT = 'ADD_ALERT',
  REMOVE_ALERT = 'REMOVE_ALERT'
}

export interface AddAlertAction extends Action<ActionType.ADD_ALERT> {
  readonly alertInfo: AlertInfo;
}

export interface RemoveAlertAction extends Action<ActionType.REMOVE_ALERT> {
  readonly id: number;
}

export type AlertAction = AddAlertAction | RemoveAlertAction;

export enum AlertComponent {
  SERVER_SIDE_EXCEPTION_DIALOG = 'SERVER_SIDE_EXCEPTION_DIALOG'
}

export interface AlertInfo {
  id?: number;
  createdAt?: Date;
  i18nKey: string;
  variant: 'success' | 'danger' | 'warning' | 'info';
  params: BasicObject;
  components: AlertComponent[];
  componentProps: BasicObject[];
}

export interface AlertList {
  readonly alertList: AlertInfo[];
  readonly idGeneratorIndex: number;
}
