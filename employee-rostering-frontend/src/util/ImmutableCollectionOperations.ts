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

import DomainObject from 'domain/DomainObject';

export function toggleElement<T>(collection: T[], element: T): T[] {
  if (collection.indexOf(element) !== -1) {
    return collection.filter(e => e !== element);
  }
  else {
    return [...collection, element];
  }
}

export function withoutElement<T extends DomainObject>(collection: T[], removedElement: T): T[] {
  return collection.filter(element => element.id !== removedElement.id);
}

export function withElement<T extends DomainObject>(collection: T[], addedElement: T): T[] {
  return [...collection, addedElement];
}

export function withUpdatedElement<T extends DomainObject>(collection: T[], updatedElement: T): T[] {
  return withElement(withoutElement(collection, updatedElement), updatedElement);
}

export function mapWithoutElement<T extends DomainObject>(map: Map<number, T>, removedElement: T): Map<number, T> {
  return collection.filter(element => element.id !== removedElement.id);
}

export function mapWithElement<T extends DomainObject>(map: Map<number, T>, addedElement: T): Map<number, T> {
  return [...collection, addedElement];
}

export function mapWithUpdatedElement<T extends DomainObject>(map: Map<number, T>, updatedElement: T): Map<number, T> {
  return withElement(withoutElement(collection, updatedElement), updatedElement);
}

