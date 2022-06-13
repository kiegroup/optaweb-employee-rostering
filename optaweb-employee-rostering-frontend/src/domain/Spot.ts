
import { DomainObject } from './DomainObject';
import { Skill } from './Skill';

export interface Spot extends DomainObject {
  name: string;
  requiredSkillSet: Skill[];
}
