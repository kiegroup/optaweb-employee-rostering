import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface NoBreakViolation extends ConstraintMatch {
  firstShift: Shift;
  secondShift: Shift;
  thirdShift: Shift;
}
