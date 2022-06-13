import { ConstraintMatch } from './ConstraintMatch';
import { Shift } from '../Shift';


export interface RequiredSkillViolation extends ConstraintMatch {
  shift: Shift;
}
