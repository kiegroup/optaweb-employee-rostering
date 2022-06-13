import { ConstraintMatch } from './ConstraintMatch';
import { Employee } from '../Employee';


export interface ContractMinutesViolation extends ConstraintMatch {
  employee: Employee;
  type: 'DAY'|'WEEK'|'MONTH'|'YEAR';
  minutesWorked: number;
}
