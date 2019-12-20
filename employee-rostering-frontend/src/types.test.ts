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
import { ObjectNumberMap, mapObjectNumberMap } from './types';

describe('Type operations', () => {
  it('mapObjectNumberMap should correctly map an ObjectNumberMap to a JavaScript Map', () => {
    const objectNumberMap: ObjectNumberMap<string> = {
      10: 'A',
      1: 'BB',
      4: 'CCC',
    };

    const expectedMap: ObjectNumberMap<number> = {
      10: 1,
      1: 2,
      4: 3,
    };

    const actualMap = mapObjectNumberMap(objectNumberMap, x => x.length);
    expect(actualMap).toEqual(expectedMap);
  });
});
