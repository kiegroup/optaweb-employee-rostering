import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface UnassignedShiftPenalty extends ConstraintMatch {
  shift: Shift;
}
