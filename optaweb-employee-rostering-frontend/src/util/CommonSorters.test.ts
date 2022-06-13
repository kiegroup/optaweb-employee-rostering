
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
