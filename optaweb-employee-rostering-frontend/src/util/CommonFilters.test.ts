/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import * as filters from './CommonFilters';

describe('Common Filters', () => {
  it('should call every mapper until a match is found for every element for string filter', () => {
    const toFilter = [1, 2];
    const map1 = jest.fn()
      .mockReturnValueOnce('a')
      .mockReturnValue('b');
    const map2 = jest.fn()
      .mockReturnValue(['b', 'a']);
    const filter = filters.stringFilter(map1, map2);
    const filtered = toFilter.filter(filter('a'));
    expect(map1).toBeCalledTimes(2);
    expect(map2).toBeCalledTimes(1);

    expect(map1).toHaveBeenNthCalledWith(1, 1);
    expect(map1).toHaveBeenNthCalledWith(2, 2);
    expect(map2).toHaveBeenNthCalledWith(1, 2);

    expect(filtered).toEqual([1, 2]);
  });

  it('should be a case insensitive filter for string filter', () => {
    const toFilter1 = ['Hello', 'Ha', 'Abc'];
    const filter1 = filters.stringFilter((s: string) => s);
    const filtered1 = toFilter1.filter(filter1('a'));
    expect(filtered1).toEqual(['Ha', 'Abc']);
    const filtered2 = toFilter1.filter(filter1('h'));
    expect(filtered2).toEqual(['Hello', 'Ha']);
    const filtered3 = toFilter1.filter(filter1('Bob'));
    expect(filtered3).toEqual([]);

    const toFilter2 = [['Hello'], ['Ha'], ['Abc', 'bob']];
    const filter2 = filters.stringFilter((s: string[]) => s);
    const filtered4 = toFilter2.filter(filter2('a'));
    expect(filtered4).toEqual([['Ha'], ['Abc', 'bob']]);
    const filtered5 = toFilter2.filter(filter2('h'));
    expect(filtered5).toEqual([['Hello'], ['Ha']]);
    const filtered6 = toFilter2.filter(filter2('Bob'));
    expect(filtered6).toEqual([['Abc', 'bob']]);
  });
});
