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

import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { availabilityToAvailabilityView, EmployeeAvailabilityView } from 'domain/EmployeeAvailabilityView';
import moment from 'moment';
import { alert } from 'store/alert';
import { refreshShiftRoster, refreshAvailabilityRoster } from 'store/roster/operations';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { serializeLocalDateTime } from 'store/rest/DataSerialization';
import { ThunkCommandFactory } from '../types';

export interface KindaEmployeeAvailabilityView extends Omit<EmployeeAvailabilityView, 'startDateTime' | 'endDateTime'> {
  startDateTime: string;
  endDateTime: string;
}

export function availabilityAdapter(employeeAvailability: EmployeeAvailability): KindaEmployeeAvailabilityView {
  return {
    ...objectWithout(availabilityToAvailabilityView(employeeAvailability), 'startDateTime', 'endDateTime'),
    startDateTime: serializeLocalDateTime(employeeAvailability.startDateTime),
    endDateTime: serializeLocalDateTime(employeeAvailability.endDateTime),
  };
}

export function kindaAvailabilityViewAdapter(kindaAvailabilityView: KindaEmployeeAvailabilityView):
EmployeeAvailabilityView {
  return {
    ...kindaAvailabilityView,
    startDateTime: moment(kindaAvailabilityView.startDateTime).toDate(),
    endDateTime: moment(kindaAvailabilityView.endDateTime).toDate(),
  };
}

export const addEmployeeAvailability:
ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability => (dispatch, state, client) => {
  const { tenantId } = employeeAvailability;
  return client.post<KindaEmployeeAvailabilityView>(`/tenant/${tenantId}/employee/availability/add`,
    availabilityAdapter(employeeAvailability)).then(() => {
    dispatch(refreshShiftRoster());
    dispatch(refreshAvailabilityRoster());
  });
};

export const removeEmployeeAvailability:
ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability => (dispatch, state, client) => {
  const { tenantId } = employeeAvailability;
  const shiftId = employeeAvailability.id;
  return client.delete<boolean>(`/tenant/${tenantId}/employee/availability/${shiftId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    } else {
      dispatch(alert.showErrorMessage('removeAvailabilityError', {
        employeeName: employeeAvailability.employee.name,
        startDateTime: moment(employeeAvailability.startDateTime).format('LLL'),
        endDateTime: moment(employeeAvailability.endDateTime).format('LLL'),
      }));
    }
  });
};

export const updateEmployeeAvailability:
ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability => (dispatch, state, client) => {
  const { tenantId } = employeeAvailability;
  return client.put<KindaEmployeeAvailabilityView>(`/tenant/${tenantId}/employee/availability/update`,
    availabilityAdapter(employeeAvailability)).then(() => {
    dispatch(refreshShiftRoster());
    dispatch(refreshAvailabilityRoster());
  });
};
