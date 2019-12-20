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
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { DomainObject } from './DomainObject';
import { EmployeeAvailability } from './EmployeeAvailability';
import DomainObjectView from './DomainObjectView';

export const availabilityToAvailabilityView = (availability: EmployeeAvailability): EmployeeAvailabilityView => ({
  ...objectWithout(availability, 'employee'),
  employeeId: availability.employee.id as number,
});

export const availabilityViewToDomainObjectView = (view: EmployeeAvailabilityView):
DomainObjectView<EmployeeAvailability> => ({
  ...objectWithout(view, 'employeeId'),
  employee: view.employeeId,
});

export interface EmployeeAvailabilityView extends DomainObject {
  employeeId: number;
  startDateTime: Date;
  endDateTime: Date;
  state: 'UNAVAILABLE'|'UNDESIRED'|'DESIRED';
}
