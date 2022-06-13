import { ConstraintMatch } from './ConstraintMatch';
import { EmployeeAvailability } from '../EmployeeAvailability';
import { Shift } from '../Shift';


export interface DesiredTimeslotForEmployeeReward extends ConstraintMatch {
  employeeAvailability: EmployeeAvailability;
  shift: Shift;
}
