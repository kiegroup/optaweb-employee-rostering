
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
