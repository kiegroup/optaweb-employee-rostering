/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { ShiftTemplate } from 'domain/ShiftTemplate';
import DomainObjectView from 'domain/DomainObjectView';
import { spotSelectors } from 'store/spot';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { employeeSelectors } from 'store/employee';
import { skillSelectors } from 'store/skill';
import { AppState } from '../types';

function isLoading(state: AppState) {
  return state.shiftTemplateList.isLoading || state.employeeList.isLoading
    || state.contractList.isLoading || state.spotList.isLoading || state.skillList.isLoading;
}

export const getShiftTemplateById = (state: AppState, id: number): ShiftTemplate => {
  if (isLoading(state)) {
    throw Error('Shift Template list is loading');
  }
  const shiftTemplateView = state.shiftTemplateList.shiftTemplateMapById.get(id) as DomainObjectView<ShiftTemplate>;
  return {
    ...objectWithout(shiftTemplateView, 'spot', 'rotationEmployee', 'requiredSkillSet'),
    spot: spotSelectors.getSpotById(state, shiftTemplateView.spot),
    requiredSkillSet: shiftTemplateView.requiredSkillSet.map(skillId => skillSelectors.getSkillById(state, skillId)),
    rotationEmployee: shiftTemplateView.rotationEmployee
      ? employeeSelectors.getEmployeeById(state, shiftTemplateView.rotationEmployee) : null,
  };
};

export const getShiftTemplateList = (state: AppState): ShiftTemplate[] => {
  if (isLoading(state)) {
    return [];
  }
  const out: ShiftTemplate[] = [];
  state.shiftTemplateList.shiftTemplateMapById.forEach((value, key) => out.push(getShiftTemplateById(state, key)));
  return out;
};
