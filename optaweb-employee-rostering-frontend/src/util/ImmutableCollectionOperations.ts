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

import { DomainObject } from 'domain/DomainObject';
import DomainObjectView from 'domain/DomainObjectView';
import { Map, Seq } from 'immutable';

export function toggleElement<T>(collection: T[], element: T): T[] {
  if (collection.indexOf(element) !== -1) {
    return collection.filter(e => e !== element);
  }

  return [...collection, element];
}

interface ObjectWithKeys {
  [key: string]: any;
}

export function objectWithout<F>(obj: F, ...without: (keyof F)[]): F {
  const copy: ObjectWithKeys = {
    ...obj,
  };
  without.forEach(key => delete copy[key as string]);
  return copy as F;
}

export function mapDomainObjectToView<T extends DomainObject>(obj: T|DomainObjectView<T>): DomainObjectView<T> {
  const result = {} as ObjectWithKeys;
  const objWithKeys = obj as ObjectWithKeys;
  Object.keys(objWithKeys).forEach((key) => {
    if (objWithKeys[key] !== null && objWithKeys[key].id !== undefined) {
      result[key] = objWithKeys[key].id;
    } else if (Array.isArray(objWithKeys[key]) && objWithKeys[key].length > 0
        && objWithKeys[key].find((item: any) => item && item.id !== undefined) !== undefined) {
      result[key] = objWithKeys[key].map((ele: DomainObject) => ele.id);
    } else {
      result[key] = objWithKeys[key];
    }
  });
  return result as DomainObjectView<T>;
}

export function createIdMapFromList<T extends DomainObject>(collection: T[]): Map<number, DomainObjectView<T>> {
  let map = Map<number, DomainObjectView<T>>();
  collection.forEach((ele) => { (map = map.set(ele.id as number, mapDomainObjectToView(ele))); });
  return map;
}

export interface FluentValue<V, M> {
  result: V;
  then: (map: M) => FluentValue<V, M>;
}
export function conditionally<K, V>(seq: Seq<K, V>, mapper: (seq: Seq<K, V>) => Seq<K, V> | undefined):
FluentValue<Seq<K, V>, (seq: Seq<K, V>) => Seq<K, V> | undefined> {
  const out = mapper(seq) || seq;
  return {
    result: out,
    then: newMapper => conditionally(out, newMapper),
  };
}
