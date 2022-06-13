import { ConstraintMatch } from './ConstraintMatch';
import { EmployeeAvailability } from '../EmployeeAvailability';
import { Shift } from '../Shift';


export interface UndesiredTimeslotForEmployeePenalty extends ConstraintMatch {
  employeeAvailability: EmployeeAvailability;
  shift: Shift;
}
