
import { DomainObject } from './DomainObject';
import { Skill } from './Skill';
import { Contract } from './Contract';

export interface Employee extends DomainObject {
  name: string;
  shortId: string;
  color: string;
  contract: Contract;
  skillProficiencySet: Skill[];
}
