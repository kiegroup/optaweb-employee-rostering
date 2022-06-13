import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface ShiftEmployeeConflictViolation extends ConstraintMatch {
  leftShift: Shift;
  rightShift: Shift;
}
