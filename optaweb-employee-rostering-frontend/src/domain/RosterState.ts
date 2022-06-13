
import { Tenant } from './Tenant';

export interface RosterState {
  publishNotice: number;
  firstDraftDate: Date;
  publishLength: number;
  draftLength: number;
  unplannedRotationOffset: number;
  rotationLength: number;
  lastHistoricDate: Date;
  timeZone: string;
  tenant: Tenant;
}
