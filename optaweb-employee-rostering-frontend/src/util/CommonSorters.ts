
import { Sorter } from 'types';

export function stringSorter<T>(mapToString: (obj: T) => string): Sorter<T> {
  // eslint-disable-next-line no-nested-ternary
  return (a, b) => ((mapToString(a).toLowerCase() < mapToString(b).toLowerCase()) ? -1
    : (mapToString(a).toLowerCase() > mapToString(b).toLowerCase()) ? 1 : 0);
}
