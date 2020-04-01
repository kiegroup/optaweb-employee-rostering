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
import { DomainObject } from 'domain/DomainObject';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import moment from 'moment';
import DomainObjectView from 'domain/DomainObjectView';

export function shiftTemplateToShiftTemplateView(shiftTemplate: ShiftTemplate): ShiftTemplateView {
  return {
    ...objectWithout(shiftTemplate, 'spot', 'rotationEmployee', 'shiftTemplateDuration',
      'durationBetweenRotationStartAndTemplateStart', 'requiredSkillSet'),
    spotId: shiftTemplate.spot.id as number,
    requiredSkillSetIdList: shiftTemplate.requiredSkillSet.map(skill => skill.id as number),
    rotationEmployeeId: shiftTemplate.rotationEmployee ? shiftTemplate.rotationEmployee.id as number : null,
    shiftTemplateDuration: shiftTemplate.shiftTemplateDuration.toISOString(),
    durationBetweenRotationStartAndTemplateStart:
      shiftTemplate.durationBetweenRotationStartAndTemplateStart.toISOString(),
  };
}

export function shiftTemplateViewToDomainObjectView(view: ShiftTemplateView): DomainObjectView<ShiftTemplate> {
  return {
    ...objectWithout(view, 'spotId', 'rotationEmployeeId', 'shiftTemplateDuration',
      'durationBetweenRotationStartAndTemplateStart', 'requiredSkillSetIdList'),
    spot: view.spotId,
    requiredSkillSet: view.requiredSkillSetIdList,
    rotationEmployee: view.rotationEmployeeId,
    shiftTemplateDuration: moment.duration(view.shiftTemplateDuration),
    durationBetweenRotationStartAndTemplateStart: moment.duration(view.durationBetweenRotationStartAndTemplateStart),
  };
}

export interface ShiftTemplateView extends DomainObject {
  spotId: number;
  requiredSkillSetIdList: number[];
  rotationEmployeeId: number | null;
  shiftTemplateDuration: string;
  durationBetweenRotationStartAndTemplateStart: string;
}
