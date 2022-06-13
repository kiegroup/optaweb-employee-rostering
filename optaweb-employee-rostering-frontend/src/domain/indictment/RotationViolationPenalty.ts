import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface RotationViolationPenalty extends ConstraintMatch {
  shift: Shift;
}
