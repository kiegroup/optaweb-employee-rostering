import { Duration } from 'moment';
import { DomainObject } from './DomainObject';
import { Employee } from './Employee';
import { Spot } from './Spot';
import { Skill } from './Skill';

export interface ShiftTemplate extends DomainObject {
  spot: Spot;
  requiredSkillSet: Skill[];
  rotationEmployee: Employee | null;
  shiftTemplateDuration: Duration;
  durationBetweenRotationStartAndTemplateStart: Duration;
}
