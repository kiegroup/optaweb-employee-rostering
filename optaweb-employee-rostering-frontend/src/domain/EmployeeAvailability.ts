import { DomainObject } from './DomainObject';
import { Employee } from './Employee';

export interface EmployeeAvailability extends DomainObject {
  employee: Employee;
  startDateTime: Date;
  endDateTime: Date;
  state: 'UNAVAILABLE'|'UNDESIRED'|'DESIRED';
}
