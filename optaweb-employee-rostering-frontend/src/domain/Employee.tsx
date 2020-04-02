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
import { DomainObject } from './DomainObject';
import { Skill } from './Skill';
import { Contract } from './Contract';

export type CovidRiskType = 'INOCULATED' | 'LOW' | 'MODERATE' | 'HIGH' | 'EXTREME';

export function getIconForCovidRisk(covidRiskType: CovidRiskType, size: 'sm' | 'lg'): JSX.Element {
  let imageProps: React.ImgHTMLAttributes<{}> = {};
  switch (size) {
    case 'sm':
      imageProps = { width: 10, height: 10 };
      break;
    case 'lg':
      imageProps = { width: 30, height: 30 };
      break;
    default:
      throw Error(`Unhandled case in getIconForCovidRisk: size = ${size}`);
  }
  return (
    <span>
      {covidRiskType === 'INOCULATED'
      && (
        <img
          {...imageProps}
          src={`${process.env.PUBLIC_URL}/assets/images/inoculated.svg`}
          alt=""
        />
      )}
      {covidRiskType === 'LOW'
      && (
        <img
          {...imageProps}
          src={`${process.env.PUBLIC_URL}/assets/images/low-risk.svg`}
          alt=""
        />
      )}
      {covidRiskType === 'MODERATE'
      && (
        <img
          {...imageProps}
          src={`${process.env.PUBLIC_URL}/assets/images/moderate-risk.svg`}
          alt=""
        />
      )}
      {covidRiskType === 'HIGH'
      && (
        <img
          {...imageProps}
          src={`${process.env.PUBLIC_URL}/assets/images/high-risk.svg`}
          alt=""
        />
      )}
      {covidRiskType === 'EXTREME'
      && (
        <img
          {...imageProps}
          src={`${process.env.PUBLIC_URL}/assets/images/extreme.svg`}
          alt=""
        />
      )}
    </span>
  );
}

export interface Employee extends DomainObject {
  name: string;
  contract: Contract;
  skillProficiencySet: Skill[];
  covidRiskType: CovidRiskType;
}
