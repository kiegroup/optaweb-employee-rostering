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

import React from 'react';
import { CircleIcon, SyringeIcon, MehIcon, FrownIcon, SadTearIcon, BanIcon } from '@patternfly/react-icons';
import { DomainObject } from './DomainObject';
import { Skill } from './Skill';
import { Contract } from './Contract';

export type CovidRiskType = 'INOCULATED' | 'LOW' | 'MODERATE' | 'HIGH' | 'EXTREME';

export function getIconForCovidRisk(covidRiskType: CovidRiskType, size: 'sm' | 'lg'): JSX.Element {
  return (
    <span>
      <span style={{ position: 'absolute', zIndex: 2 }}>
        {covidRiskType === 'INOCULATED' && <SyringeIcon color="#8ae234" size={size} />}
        {covidRiskType === 'LOW' && <MehIcon color="#edd400" size={size} />}
        {covidRiskType === 'MODERATE' && <FrownIcon color="#fcaf3e" size={size} />}
        {covidRiskType === 'HIGH' && <SadTearIcon color="#f57900" size={size} />}
        {covidRiskType === 'EXTREME' && <BanIcon color="#2e3436" size={size} />}
      </span>
      <span style={{ zIndex: 1 }}>
        <CircleIcon size={size} color="#ffffffff" />
      </span>
    </span>
  );
}

export interface Employee extends DomainObject {
  name: string;
  contract: Contract;
  skillProficiencySet: Skill[];
  covidRiskType: CovidRiskType;
}
