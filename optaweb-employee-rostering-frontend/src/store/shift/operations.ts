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

import { Shift } from 'domain/Shift';
import moment from 'moment';
import { alert } from 'store/alert';
import { refreshShiftRoster, refreshAvailabilityRoster } from 'store/roster/operations';
import { KindaShiftView, shiftAdapter } from './KindaShiftView';
import { ThunkCommandFactory } from '../types';

export const addShift: ThunkCommandFactory<Shift, any> = shift => (dispatch, state, client) => {
  const { tenantId } = shift;
  return client.post<KindaShiftView>(`/tenant/${tenantId}/shift/add`, shiftAdapter(shift)).then(() => {
    dispatch(refreshShiftRoster());
    dispatch(refreshAvailabilityRoster());
  });
};

export const removeShift: ThunkCommandFactory<Shift, any> = shift => (dispatch, state, client) => {
  const { tenantId } = shift;
  const shiftId = shift.id;
  return client.delete<boolean>(`/tenant/${tenantId}/shift/${shiftId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    } else {
      dispatch(alert.showErrorMessage('removeShiftError', {
        id: shift.id,
        startDateTime: moment(shift.startDateTime).format('LLL'),
        endDateTime: moment(shift.endDateTime).format('LLL'),
      }));
    }
  });
};

export const updateShift: ThunkCommandFactory<Shift, any> = shift => (dispatch, state, client) => {
  const { tenantId } = shift;
  return client.put<KindaShiftView>(`/tenant/${tenantId}/shift/update`, shiftAdapter(shift)).then(() => {
    dispatch(refreshShiftRoster());
    dispatch(refreshAvailabilityRoster());
  });
};
