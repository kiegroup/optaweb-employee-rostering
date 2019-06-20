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

import * as immutableCollectionOperations from './ImmutableCollectionOperations';
import DomainObject from 'domain/DomainObject';

describe('Immutable Collection Operations', () => {
  it('should not modify the collection on without element', () => {
    const object1: DomainObject = {
      tenantId: 0,
      id: 0,
      version: 0
    };
    const object2: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0
    };
    const collection: DomainObject[] = [object1, object2];
    const copy: DomainObject[] = JSON.parse(JSON.stringify(collection));
    const without1 = immutableCollectionOperations.withoutElement(collection, object1);
    expect(collection).toEqual(copy);
    expect(without1).toEqual([object2]);

    const without2 = immutableCollectionOperations.withoutElement(collection, object2);
    expect(collection).toEqual(copy);
    expect(without2).toEqual([object1]);
  });

  it('should not modify the collection on with element', () => {
    const object1: DomainObject = {
      tenantId: 0,
      id: 0,
      version: 0
    };
    const addedObject: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0
    };
    const collection: DomainObject[] = [object1];
    const copy: DomainObject[] = JSON.parse(JSON.stringify(collection));
    const withAdded = immutableCollectionOperations.withElement(collection, addedObject);
    expect(collection).toEqual(copy);
    expect(withAdded).toEqual([object1, addedObject]);
  });

  it('should not modify the collection on with updated element', () => {
    const object1: DomainObject = {
      tenantId: 0,
      id: 0,
      version: 0
    };
    const object2: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0
    };
    const updatedObject1: DomainObject = {...object1, version: 1};
    const updatedObject2: DomainObject = {...object2, version: 1};
    const collection: DomainObject[] = [object1, object2];
    const copy: DomainObject[] = JSON.parse(JSON.stringify(collection));
    const withUpdated1 = immutableCollectionOperations.withUpdatedElement(collection, updatedObject1);
    expect(collection).toEqual(copy);
    expect(withUpdated1).toEqual([object2, updatedObject1]);

    const withUpdated2 = immutableCollectionOperations.withUpdatedElement(collection, updatedObject2);
    expect(collection).toEqual(copy);
    expect(withUpdated2).toEqual([object1, updatedObject2]);
  });

  it('should not modify the collection when element is not present in the collection in toggleElement', () => {
    const obj1 = 0;
    const obj2 = 1;

    const collection = [obj1];
    const copy = JSON.parse(JSON.stringify(collection));
    const toggledObj2On =  immutableCollectionOperations.toggleElement(collection, obj2);

    expect(collection).toEqual(copy);
    expect(toggledObj2On).toEqual([obj1, obj2]);
  });

  it('should not modify the collection when element is present in the collection in toggleElement', () => {
    const obj1 = 0;
    const obj2 = 1;

    const collection = [obj1, obj2];
    const copy = JSON.parse(JSON.stringify(collection));
    const toggledObj2On =  immutableCollectionOperations.toggleElement(collection, obj2);

    expect(collection).toEqual(copy);
    expect(toggledObj2On).toEqual([obj1]);
  });
});
