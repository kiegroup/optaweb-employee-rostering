import { DomainObject } from './DomainObject';
import { Employee } from './Employee';
import { Spot } from './Spot';
import { HardMediumSoftScore } from './HardMediumSoftScore';
import { RequiredSkillViolation } from './indictment/RequiredSkillViolation';
import { UnavailableEmployeeViolation } from './indictment/UnavailableEmployeeViolation';
import { ShiftEmployeeConflictViolation } from './indictment/ShiftEmployeeConflictViolation';
import { DesiredTimeslotForEmployeeReward } from './indictment/DesiredTimeslotForEmployeeReward';
import { UndesiredTimeslotForEmployeePenalty } from './indictment/UndesiredTimeslotForEmployeePenalty';
import { RotationViolationPenalty } from './indictment/RotationViolationPenalty';
import { UnassignedShiftPenalty } from './indictment/UnassignedShiftPenalty';
import { ContractMinutesViolation } from './indictment/ContractMinutesViolation';
import { Skill } from './Skill';
import { NoBreakViolation } from './indictment/NoBreakViolation';
import { PublishedShiftReassignedPenalty } from './indictment/PublishedShiftReassignedPenalty';

export interface Shift extends DomainObject {
  startDateTime: Date;
  endDateTime: Date;
  spot: Spot;
  requiredSkillSet: Skill[];
  rotationEmployee: Employee | null;
  employee: Employee | null;
  originalEmployee: Employee | null;
  pinnedByUser: boolean;
  indictmentScore?: HardMediumSoftScore;
  requiredSkillViolationList?: RequiredSkillViolation[];
  unavailableEmployeeViolationList?: UnavailableEmployeeViolation[];
  shiftEmployeeConflictList?: ShiftEmployeeConflictViolation[];
  desiredTimeslotForEmployeeRewardList?: DesiredTimeslotForEmployeeReward[];
  undesiredTimeslotForEmployeePenaltyList?: UndesiredTimeslotForEmployeePenalty[];
  rotationViolationPenaltyList?: RotationViolationPenalty[];
  unassignedShiftPenaltyList?: UnassignedShiftPenalty[];
  contractMinutesViolationPenaltyList?: ContractMinutesViolation[];
  noBreakViolationList?: NoBreakViolation[];
  publishedShiftReassignedPenaltyList?: PublishedShiftReassignedPenalty[];
}
