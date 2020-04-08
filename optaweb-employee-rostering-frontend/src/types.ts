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
export type Predicate<T> = (value: T) => boolean;
export type ReadonlyPartial<T> = { readonly [P in keyof T]?: T[P] };
export type Sorter<T> = (a: T, b: T) => number;
export interface PaginationData {
  itemsPerPage: number;
  pageNumber: number;
}

export const doNothing = () => { /* Intentionally Empty */ };

export interface ObjectNumberMap<T> {
  [index: number]: T;
}

type Basic = number|string|boolean|null|undefined;

export interface BasicObject {
  [property: string]: Basic|Basic[]|BasicObject|BasicObject[];
}

export interface ServerSideExceptionInfo {
  i18nKey: string;
  exceptionMessage: string;
  messageParameters: string[];
  exceptionClass: string;
  stackTrace: string[];
  exceptionCause: ServerSideExceptionInfo|null;
}

export function mapObjectStringMap<F, T>(map: Record<string, F>, mapper: (value: F) => T): Record<string, T> {
  const out: Record<string, T> = {};
  // eslint-disable-next-line no-return-assign
  Object.keys(map).forEach(key => out[key] = mapper(map[key]));
  return out;
}


export function mapObjectNumberMap<F, T>(map: ObjectNumberMap<F>, mapper: (value: F) => T): ObjectNumberMap<T> {
  const out: ObjectNumberMap<T> = {};
  // eslint-disable-next-line no-return-assign
  Object.keys(map).forEach(key => out[parseInt(key, 10)] = mapper(map[parseInt(key, 10)]));
  return out;
}
