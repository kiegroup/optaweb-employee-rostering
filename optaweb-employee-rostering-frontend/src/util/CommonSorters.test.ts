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

import * as sorters from './CommonSorters';

describe('Common Sorters', () => {
  it('should call mapToString on each object in the collection for string sorter', () => {
    const toSort = [5, 3, 1, 2, 4];
    const copy = [...toSort];
    const map = jest.fn(n => String(n));
    const sorter = sorters.stringSorter(map);
    toSort.sort(sorter);
    copy.forEach(e => expect(map).toBeCalledWith(e));
  });

  it('should be a case insensitive sort for string sorter', () => {
    const toSort = ['B', 'e', 'c', 'D', 'a'];
    const sorter = sorters.stringSorter((s: string) => s);
    toSort.sort(sorter);
    expect(toSort).toEqual(['a', 'B', 'c', 'D', 'e']);
  });

  it('should return 0 for equal elements for string sorter', () => {
    const sorter = sorters.stringSorter((s: string) => s);
    expect(sorter('a', 'A')).toEqual(0);
    expect(sorter('A', 'a')).toEqual(0);
    expect(sorter('A', 'A')).toEqual(0);
  });
});
