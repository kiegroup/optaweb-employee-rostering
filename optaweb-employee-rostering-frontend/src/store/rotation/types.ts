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
import DomainObjectView from 'domain/DomainObjectView';
import { TimeBucket } from 'domain/TimeBucket';
import { Map } from 'immutable';

export enum ActionType {
  ADD_TIME_BUCKET = 'ADD__TIME_BUCKET',
  REMOVE_TIME_BUCKET = 'REMOVE_TIME_BUCKET',
  UPDATE_TIME_BUCKET = 'UPDATE_TIME_BUCKET',
  REFRESH_TIME_BUCKET_LIST = 'REFRESH_TIME_BUCKET_LIST',
  SET_TIME_BUCKET_LIST_LOADING = 'SET_TIME_BUCKET_LIST_LOADING'
}

export interface SetTimeBucketListLoadingAction extends Action<ActionType.SET_TIME_BUCKET_LIST_LOADING> {
  readonly isLoading: boolean;
}

export interface AddTimeBucketAction extends Action<ActionType.ADD_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface RemoveTimeBucketAction extends Action<ActionType.REMOVE_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface UpdateTimeBucketAction extends Action<ActionType.UPDATE_TIME_BUCKET> {
  readonly timeBucket: DomainObjectView<TimeBucket>;
}

export interface RefreshTimeBucketListAction extends Action<ActionType.REFRESH_TIME_BUCKET_LIST> {
  readonly timeBucketList: DomainObjectView<TimeBucket>[];
}

export type TimeBucketAction = SetTimeBucketListLoadingAction | AddTimeBucketAction |
RemoveTimeBucketAction | UpdateTimeBucketAction | RefreshTimeBucketListAction;

export interface TimeBucketList {
  readonly isLoading: boolean;
  readonly timeBucketMapById: Map<number, DomainObjectView<TimeBucket>>;
}
