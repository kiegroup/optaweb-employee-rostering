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

import { createIdMapFromList, mapDomainObjectToView } from 'util/ImmutableCollectionOperations';
import DomainObjectView from 'domain/DomainObjectView';
import { TimeBucket } from 'domain/TimeBucket';
import { Map } from 'immutable';
import { ActionType, TimeBucketList, TimeBucketAction } from './types';

export const initialState: TimeBucketList = {
  isLoading: true,
  timeBucketMapById: Map<number, DomainObjectView<TimeBucket>>(),
};

const timeBucketReducer = (state = initialState, action: TimeBucketAction): TimeBucketList => {
  switch (action.type) {
    case ActionType.SET_TIME_BUCKET_LIST_LOADING: {
      return { ...state, isLoading: action.isLoading };
    }
    case ActionType.ADD_TIME_BUCKET:
    case ActionType.UPDATE_TIME_BUCKET: {
      return { ...state,
        timeBucketMapById: state.timeBucketMapById.set(action.timeBucket.id as number,
          mapDomainObjectToView(action.timeBucket)) };
    }
    case ActionType.REMOVE_TIME_BUCKET: {
      return { ...state, timeBucketMapById: state.timeBucketMapById.remove(action.timeBucket.id as number) };
    }
    case ActionType.REFRESH_TIME_BUCKET_LIST: {
      return { ...state, timeBucketMapById: createIdMapFromList(action.timeBucketList) };
    }
    default:
      return state;
  }
};

export default timeBucketReducer;
