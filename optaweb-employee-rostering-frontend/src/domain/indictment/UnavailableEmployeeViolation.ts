import { ConstraintMatch } from './ConstraintMatch';
import { EmployeeAvailability } from '../EmployeeAvailability';
import { Shift } from '../Shift';


export interface UnavailableEmployeeViolation extends ConstraintMatch {
  employeeAvailability: EmployeeAvailability;
  shift: Shift;
}
