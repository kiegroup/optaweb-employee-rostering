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
