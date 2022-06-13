import { DomainObject } from './DomainObject';
import { Spot } from './Spot';
import { Skill } from './Skill';
import { Employee } from './Employee';

export interface Seat {
  dayInRotation: number;
  employee: Employee|null;
}

export interface TimeBucket extends DomainObject {
  spot: Spot;
  additionalSkillSet: Skill[];
  repeatOnDaySetList: string[];
  startTime: Date;
  endTime: Date;
  seatList: Seat[];
}
