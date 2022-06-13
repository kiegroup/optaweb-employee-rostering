import { DomainObject } from './DomainObject';

export interface Contract extends DomainObject {
  name: string;
  maximumMinutesPerDay: number|null;
  maximumMinutesPerWeek: number|null;
  maximumMinutesPerMonth: number|null;
  maximumMinutesPerYear: number|null;
}
