
import { Predicate } from 'types';

export function stringFilter<T>(...mappers: ((obj: T) => string|string[])[]): (filter: string) => Predicate<T> {
  return filter => obj => mappers.find((mapper) => {
    const value = mapper(obj);
    return (typeof value === 'string') ? value.toLowerCase().includes(filter.toLowerCase())
      : value.find(v => v.toLowerCase().includes(filter.toLowerCase())) !== undefined;
  }) !== undefined;
}
