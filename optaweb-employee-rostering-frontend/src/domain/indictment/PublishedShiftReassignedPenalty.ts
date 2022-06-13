import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface PublishedShiftReassignedPenalty extends ConstraintMatch {
  shift: Shift;
}
