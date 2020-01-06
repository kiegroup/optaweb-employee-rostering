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

import { Predicate } from 'types';

export function stringFilter<T>(...mappers: ((obj: T) => string|string[])[]): (filter: string) => Predicate<T> {
  return filter => obj => mappers.find((mapper) => {
    const value = mapper(obj);
    return (typeof value === 'string') ? value.toLowerCase().includes(filter.toLowerCase())
      : value.find(v => v.toLowerCase().includes(filter.toLowerCase())) !== undefined;
  }) !== undefined;
}
