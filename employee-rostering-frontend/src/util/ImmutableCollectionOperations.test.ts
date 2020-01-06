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

import { DomainObject } from 'domain/DomainObject';
import DomainObjectView from 'domain/DomainObjectView';
import * as immutableCollectionOperations from './ImmutableCollectionOperations';

describe('Immutable Collection Operations', () => {
  it('should create a copy with element removed in objectWithout', () => {
    const original = {
      a: 10,
      b: 'Hello',
      c: null,
    };
    const expected = {
      a: 10,
      c: null,
    };
    const originalCopy = { ...original };
    const actual = immutableCollectionOperations.objectWithout(original, 'b');
    expect(actual).toEqual(expected);
    expect(original).toEqual(originalCopy);
  });

  it('should not modify the collection on without element with id', () => {
    const object1: DomainObject = {
      tenantId: 0,
      id: 0,
      version: 0,
    };
    const object2: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0,
    };
    const collection: DomainObject[] = [object1, object2];
    const copy: DomainObject[] = JSON.parse(JSON.stringify(collection));
    const without1 = immutableCollectionOperations.withoutElementWithId(collection, 0);
    expect(collection).toEqual(copy);
    expect(without1).toEqual([object2]);

    const without2 = immutableCollectionOperations.withoutElementWithId(collection, 1);
    expect(collection).toEqual(copy);
    expect(without2).toEqual([object1]);
  });

  it('should not modify the collection on without element', () => {
    const object1: DomainObject = {
      tenantId: 0,
      id: 0,
      version: 0,
    };
    const object2: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0,
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
      version: 0,
    };
    const addedObject: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0,
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
      version: 0,
    };
    const object2: DomainObject = {
      tenantId: 0,
      id: 1,
      version: 0,
    };
    const updatedObject1: DomainObject = { ...object1, version: 1 };
    const updatedObject2: DomainObject = { ...object2, version: 1 };
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
    const toggledObj2On = immutableCollectionOperations.toggleElement(collection, obj2);

    expect(collection).toEqual(copy);
    expect(toggledObj2On).toEqual([obj1, obj2]);
  });

  it('should not modify the collection when element is present in the collection in toggleElement', () => {
    const obj1 = 0;
    const obj2 = 1;

    const collection = [obj1, obj2];
    const copy = JSON.parse(JSON.stringify(collection));
    const toggledObj2On = immutableCollectionOperations.toggleElement(collection, obj2);

    expect(collection).toEqual(copy);
    expect(toggledObj2On).toEqual([obj1]);
  });

  it('should convert any DomainObject into its view', () => {
    const obj = {
      tenantId: 0,
      id: 1,
      version: 2,
      nullMem: null,
      domainObjMem: {
        tenantId: 0,
        id: 3,
        version: 4,
        name: 'Hi',
      },
      emptyList: [],
      domainObjMemList: [
        {
          tenantId: 0,
          id: 6,
          version: 1,
          name: 'A',
        },
        {
          tenantId: 0,
          id: 7,
          version: 2,
          name: 'B',
        },
      ],
      otherMem: 'Test',
    };
    const view = immutableCollectionOperations.mapDomainObjectToView(obj);
    expect(view.tenantId).toEqual(0);
    expect(view.id).toEqual(1);
    expect(view.version).toEqual(2);
    expect(view.nullMem).toBeNull();
    expect(view.domainObjMem).toEqual(3);
    expect(view.emptyList).toEqual([]);
    expect(view.domainObjMemList).toEqual([6, 7]);
    expect(view.otherMem).toEqual('Test');
  });

  interface MockDomainObject extends DomainObject {
    domainObj: DomainObject;
  }

  it('should return a new map with the entry added in mapWithElement', () => {
    const map = new Map<number, DomainObjectView<MockDomainObject>>([
      [0, {
        tenantId: 0,
        id: 0,
        version: 0,
        domainObj: 3,
      }],
    ]);

    const obj = {
      tenantId: 0,
      id: 1,
      version: 0,
      domainObj: {
        tenantId: 0,
        id: 2,
        version: 0,
      },
    };

    const copy = new Map(map);
    const mapWithObj = immutableCollectionOperations.mapWithElement(map, obj);

    expect(map).toEqual(copy);
    expect(mapWithObj).toEqual(new Map([
      [0, map.get(0)],
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        domainObj: 2,
      }],
    ]));
  });

  it('should return a new map with the entry removed in mapWithoutElement', () => {
    const map = new Map<number, DomainObjectView<MockDomainObject>>([
      [0, {
        tenantId: 0,
        id: 0,
        version: 0,
        domainObj: 3,
      }],
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        domainObj: 2,
      }],
    ]);

    const obj = {
      tenantId: 0,
      id: 1,
      version: 0,
      domainObj: {
        tenantId: 0,
        id: 2,
        version: 0,
      },
    };

    const copy = new Map(map);
    const mapWithoutObj = immutableCollectionOperations.mapWithoutElement(map, obj);

    expect(map).toEqual(copy);
    expect(mapWithoutObj).toEqual(new Map([
      [0, map.get(0)],
    ]));
  });

  it('should return a new map with the entry update in mapWithUpdatedElement', () => {
    const map = new Map<number, DomainObjectView<MockDomainObject>>([
      [0, {
        tenantId: 0,
        id: 0,
        version: 0,
        domainObj: 3,
      }],
      [1, {
        tenantId: 0,
        id: 1,
        version: 0,
        domainObj: 2,
      }],
    ]);

    const obj = {
      tenantId: 0,
      id: 1,
      version: 1,
      domainObj: {
        tenantId: 0,
        id: 5,
        version: 0,
      },
    };

    const copy = new Map(map);
    const mapWithUpdatedObj = immutableCollectionOperations.mapWithUpdatedElement(map, obj);

    expect(map).toEqual(copy);
    expect(mapWithUpdatedObj).toEqual(new Map([
      [0, map.get(0)],
      [1, {
        tenantId: 0,
        id: 1,
        version: 1,
        domainObj: 5,
      }],
    ]));
  });
});

describe('Stream operations', () => {
  it('should map the collection element in map', () => {
    const orig = [1, 2, 3];
    const result = new immutableCollectionOperations.Stream(orig).map(n => n + 1).collect(r => r);
    expect(result).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(result).toEqual([2, 3, 4]);
  });

  it('should filter the collection element in filter', () => {
    const orig = [1, 2, 3];
    const result = new immutableCollectionOperations.Stream(orig).filter(n => n === 1).collect(r => r);
    expect(result).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(result).toEqual([1]);
  });

  it('should paged the collection element in page', () => {
    const orig = [1, 2, 3];
    const result = new immutableCollectionOperations.Stream(orig).page(1, 2).collect(r => r);
    expect(result).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(result).toEqual([1, 2]);
  });

  it('should sort the collection element in sort', () => {
    const orig = [1, 2, 3];
    const result = new immutableCollectionOperations.Stream(orig).sort((a, b) => b - a).collect(r => r);
    expect(result).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(result).toEqual([3, 2, 1]);

    const revResult = new immutableCollectionOperations.Stream(orig).sort((a, b) => b - a, false).collect(r => r);
    expect(revResult).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(revResult).toEqual([1, 2, 3]);
  });

  it('should replace the stream in conditionally', () => {
    const orig = [1, 2, 3];
    const replaceResult = new immutableCollectionOperations.Stream(orig)
      .conditionally(s => s.filter(x => x === 1)).collect(r => r);
    expect(replaceResult).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(replaceResult).toEqual([1]);

    const noReplaceResult = new immutableCollectionOperations.Stream(orig)
      .conditionally(() => undefined).collect(r => r);
    expect(noReplaceResult).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(noReplaceResult).toEqual([1, 2, 3]);
  });

  it('should collect a result in collect', () => {
    const orig = [1, 2, 3];
    const result = new immutableCollectionOperations.Stream(orig).collect(r => r);
    expect(result).not.toBe(orig);
    expect(orig).toEqual([1, 2, 3]);
    expect(result).toEqual([1, 2, 3]);

    const length = new immutableCollectionOperations.Stream(orig).collect(r => r.length);
    expect(length).toEqual(3);
  });

  it('streams themselves should be immutable', () => {
    const stream = new immutableCollectionOperations.Stream([1, 2, 3]);
    const filterStream = stream.filter(n => n === 1);
    expect(stream).not.toBe(filterStream);
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(filterStream.collect(c => c)).toEqual([1]);

    const mapStream = stream.map(n => n + 1);
    expect(stream).not.toBe(mapStream);
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(mapStream.collect(c => c)).toEqual([2, 3, 4]);

    const pageStream = stream.page(1, 2);
    expect(stream).not.toBe(pageStream);
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(pageStream.collect(c => c)).toEqual([1, 2]);

    const sortStream = stream.sort((a, b) => b - a);
    expect(stream).not.toBe(sortStream);
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(sortStream.collect(c => c)).toEqual([3, 2, 1]);

    const replaceStream = stream.conditionally(s => s.map(x => x + 1));
    expect(stream).not.toBe(replaceStream);
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(replaceStream.collect(c => c)).toEqual([2, 3, 4]);

    const noReplaceStream = stream.conditionally(() => undefined);
    // In the no replace case, the collection doesn't change, so it safe
    // for the stream to be the same
    expect(stream.collect(c => c)).toEqual([1, 2, 3]);
    expect(noReplaceStream.collect(c => c)).toEqual([1, 2, 3]);
  });
});
