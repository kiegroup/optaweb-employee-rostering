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
import { Spot } from 'domain/Spot';
import DomainObjectView from 'domain/DomainObjectView';

export enum ActionType {
  ADD_SPOT = 'ADD_SPOT',
  REMOVE_SPOT = 'REMOVE_SPOT',
  UPDATE_SPOT = 'UPDATE_SPOT',
  REFRESH_SPOT_LIST = 'REFRESH_SPOT_LIST',
  SET_SPOT_LIST_LOADING = 'SET_SPOT_LIST_LOADING'
}

export interface SetSpotListLoadingAction extends Action<ActionType.SET_SPOT_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddSpotAction extends Action<ActionType.ADD_SPOT> {
  readonly spot: Spot;
}

export interface RemoveSpotAction extends Action<ActionType.REMOVE_SPOT> {
  readonly spot: Spot;
}

export interface UpdateSpotAction extends Action<ActionType.UPDATE_SPOT> {
  readonly spot: Spot;
}

export interface RefreshSpotListAction extends Action<ActionType.REFRESH_SPOT_LIST> {
  readonly spotList: Spot[];
}

export type SpotAction = SetSpotListLoadingAction | AddSpotAction | RemoveSpotAction |
UpdateSpotAction | RefreshSpotListAction;

export interface SpotList {
  readonly isLoading: boolean;
  readonly spotMapById: Map<number, DomainObjectView<Spot>>;
}
