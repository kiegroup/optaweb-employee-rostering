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

import { ThunkCommandFactory } from '../types';
import EmployeeAvailability from 'domain/EmployeeAvailability';
import EmployeeAvailabilityView, { availabilityToAvailabilityView } from 'domain/EmployeeAvailabilityView';
import moment from 'moment';
import { showSuccessMessage, showErrorMessage } from 'ui/Alerts';
import { refreshShiftRoster, refreshAvailabilityRoster } from 'store/roster/operations';
import { objectWithout } from 'util/ImmutableCollectionOperations';

export interface KindaEmployeeAvailabilityView extends Omit<EmployeeAvailabilityView, "startDateTime" | "endDateTime"> {
  startDateTime: string,
  endDateTime: string,
}

export function availabilityAdapter(employeeAvailability: EmployeeAvailability): KindaEmployeeAvailabilityView {
  return {
    ...objectWithout(availabilityToAvailabilityView(employeeAvailability), "startDateTime", "endDateTime"),
    startDateTime: moment(employeeAvailability.startDateTime).local().format("YYYY-MM-DDTHH:mm:ss"),
    endDateTime: moment(employeeAvailability.endDateTime).local().format("YYYY-MM-DDTHH:mm:ss")
  };
}

export function kindaAvailabilityViewAdapter(kindaAvailabilityView: KindaEmployeeAvailabilityView): EmployeeAvailabilityView {
  return {
    ...kindaAvailabilityView,
    startDateTime: moment(kindaAvailabilityView.startDateTime).toDate(),
    endDateTime: moment(kindaAvailabilityView.endDateTime).toDate(),
  };
}

export const addEmployeeAvailability: ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability =>
  (dispatch, state, client) => {
    const tenantId = employeeAvailability.tenantId;
    return client.post<KindaEmployeeAvailabilityView>(`/tenant/${tenantId}/shift/add`, availabilityAdapter(employeeAvailability)).then(newEmployeeAvailability => {
      showSuccessMessage("Successfully added Availability", `A new Availability for ${employeeAvailability.employee.name} starting at ${moment(newEmployeeAvailability.startDateTime).format("LLL")} and ending at ${moment(newEmployeeAvailability.endDateTime).format("LLL")} was successfully added.`)
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    });
  };

export const removeEmployeeAvailability: ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability =>
  (dispatch, state, client) => {
    const tenantId = employeeAvailability.tenantId;
    const shiftId = employeeAvailability.id;
    return client.delete<boolean>(`/tenant/${tenantId}/shift/${shiftId}`).then(isSuccess => {
      if (isSuccess) {
        showSuccessMessage("Successfully deleted Availability", `The Availability with id ${employeeAvailability.id} starting at ${moment(employeeAvailability.startDateTime).format("LLL")} and ending at ${moment(employeeAvailability.endDateTime).format("LLL")} was successfully deleted.`)
        dispatch(refreshShiftRoster());
        dispatch(refreshAvailabilityRoster());
      }
      else {
        showErrorMessage("Error deleting Availability", `The Availability with id ${employeeAvailability.id} starting at ${moment(employeeAvailability.startDateTime).format("LLL")} and ending at ${moment(employeeAvailability.endDateTime).format("LLL")} could not be deleted.`);
      }
    });
  };

export const updateEmployeeAvailability: ThunkCommandFactory<EmployeeAvailability, any> = employeeAvailability =>
  (dispatch, state, client) => {
    const tenantId = employeeAvailability.tenantId;
    return client.put<KindaEmployeeAvailabilityView>(`/tenant/${tenantId}/shift/update`, availabilityAdapter(employeeAvailability)).then(updatedAvailability => {
      showSuccessMessage("Successfully updated Availability", `The Availability with id "${updatedAvailability.id}" was successfully updated.`);
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    });
  };