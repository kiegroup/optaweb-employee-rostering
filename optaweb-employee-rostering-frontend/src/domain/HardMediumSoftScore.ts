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
export function isScoreZero(score: HardMediumSoftScore): boolean {
  return score.hardScore === 0 && score.mediumScore === 0 && score.softScore === 0;
}

export function getHardMediumSoftScoreFromString(hardMediumSoftScore: string): HardMediumSoftScore {
  const components = hardMediumSoftScore.split('/');
  return {
    hardScore: parseInt(components[0], 10),
    mediumScore: parseInt(components[1], 10),
    softScore: parseInt(components[2], 10),
  };
}

export function convertHardMediumSoftScoreToString(hardMediumSoftScore: HardMediumSoftScore) {
  let out = '';
  if (hardMediumSoftScore.hardScore !== 0) {
    out += `${hardMediumSoftScore.hardScore} Hard`;
  }
  if (hardMediumSoftScore.mediumScore !== 0) {
    out = `${out + (out ? '/' : '')}${hardMediumSoftScore.mediumScore} Medium`;
  }
  if (hardMediumSoftScore.softScore !== 0) {
    out = `${out + (out ? '/' : '')}${hardMediumSoftScore.softScore} Soft`;
  }
  return out || '0';
}

export interface HardMediumSoftScore {
  hardScore: number;
  mediumScore: number;
  softScore: number;
}
