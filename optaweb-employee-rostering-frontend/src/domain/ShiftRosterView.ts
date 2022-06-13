
import { ObjectNumberMap } from 'types';
import { ShiftView } from './ShiftView';
import { RosterView } from './RosterView';

export interface ShiftRosterView extends RosterView {
  spotIdToShiftViewListMap: ObjectNumberMap<ShiftView[]>;
}
