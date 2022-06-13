import { objectWithout } from 'util/ImmutableCollectionOperations';
import { DomainObject } from './DomainObject';
import { EmployeeAvailability } from './EmployeeAvailability';
import DomainObjectView from './DomainObjectView';

export const availabilityToAvailabilityView = (availability: EmployeeAvailability): EmployeeAvailabilityView => ({
  ...objectWithout(availability, 'employee'),
  employeeId: availability.employee.id as number,
});

export const availabilityViewToDomainObjectView = (view: EmployeeAvailabilityView):
DomainObjectView<EmployeeAvailability> => ({
  ...objectWithout(view, 'employeeId'),
  employee: view.employeeId,
});

export interface EmployeeAvailabilityView extends DomainObject {
  employeeId: number;
  startDateTime: Date;
  endDateTime: Date;
  state: 'UNAVAILABLE'|'UNDESIRED'|'DESIRED';
}
