
import { Employee } from './Employee';
import { RosterState } from './RosterState';
import { Spot } from './Spot';
import { HardMediumSoftScore } from './HardMediumSoftScore';
import { IndictmentSummary } from './indictment/IndictmentSummary';

export interface RosterView {
  tenantId: number;
  startDate: string;
  endDate: string;
  score: HardMediumSoftScore;
  spotList: Spot[];
  employeeList: Employee[];
  rosterState: RosterState;
  indictmentSummary: IndictmentSummary;
}
