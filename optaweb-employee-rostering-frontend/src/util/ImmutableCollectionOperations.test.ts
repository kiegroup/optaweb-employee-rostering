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
});
