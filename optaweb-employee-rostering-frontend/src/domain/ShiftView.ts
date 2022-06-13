import { objectWithout } from 'util/ImmutableCollectionOperations';
import { DomainObject } from './DomainObject';
import DomainObjectView from './DomainObjectView';
import { Shift } from './Shift';
import { HardMediumSoftScore } from './HardMediumSoftScore';
import { RequiredSkillViolation } from './indictment/RequiredSkillViolation';
import { UnavailableEmployeeViolation } from './indictment/UnavailableEmployeeViolation';
import { DesiredTimeslotForEmployeeReward } from './indictment/DesiredTimeslotForEmployeeReward';
import { UndesiredTimeslotForEmployeePenalty } from './indictment/UndesiredTimeslotForEmployeePenalty';
import { RotationViolationPenalty } from './indictment/RotationViolationPenalty';
import { UnassignedShiftPenalty } from './indictment/UnassignedShiftPenalty';
import { ContractMinutesViolation } from './indictment/ContractMinutesViolation';
import { ShiftEmployeeConflictViolation } from './indictment/ShiftEmployeeConflictViolation';
import { NoBreakViolation } from './indictment/NoBreakViolation';
import { PublishedShiftReassignedPenalty } from './indictment/PublishedShiftReassignedPenalty';

export const shiftToShiftView = (shift: Shift): ShiftView => ({
  id: shift.id,
  version: shift.version,
  tenantId: shift.tenantId,
  pinnedByUser: shift.pinnedByUser,
  startDateTime: shift.startDateTime,
  endDateTime: shift.endDateTime,
  spotId: shift.spot.id as number,
  requiredSkillSetIdList: shift.requiredSkillSet.map(skill => skill.id as number),
  employeeId: shift.employee ? shift.employee.id as number : null,
  originalEmployeeId: shift.originalEmployee ? shift.originalEmployee.id as number : null,
  rotationEmployeeId: shift.rotationEmployee ? shift.rotationEmployee.id as number : null,
  indictmentScore: shift.indictmentScore,
});

export const shiftViewToDomainObjectView = (view: ShiftView): DomainObjectView<Shift> => ({
  ...objectWithout(view, 'employeeId', 'spotId', 'rotationEmployeeId', 'requiredSkillSetIdList', 'originalEmployeeId'),
  employee: view.employeeId,
  spot: view.spotId,
  rotationEmployee: view.rotationEmployeeId,
  requiredSkillSet: view.requiredSkillSetIdList,
  originalEmployee: view.originalEmployeeId,
});

export interface ShiftView extends DomainObject {
  startDateTime: Date;
  endDateTime: Date;
  spotId: number;
  requiredSkillSetIdList: number[];
  rotationEmployeeId: number | null;
  employeeId: number | null;
  originalEmployeeId: number | null;
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
