
import { ObjectNumberMap } from 'types';
import { ShiftView } from './ShiftView';
import { RosterView } from './RosterView';
import { EmployeeAvailabilityView } from './EmployeeAvailabilityView';

export interface AvailabilityRosterView extends RosterView {
  employeeIdToShiftViewListMap: ObjectNumberMap<ShiftView[]>;
  employeeIdToAvailabilityViewListMap: ObjectNumberMap<EmployeeAvailabilityView[]>;
  unassignedShiftViewList: ShiftView[];
}
