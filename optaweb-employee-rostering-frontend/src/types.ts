export type Predicate<T> = (value: T) => boolean;
export type ReadonlyPartial<T> = { readonly [P in keyof T]?: T[P] };
export type Sorter<T> = (a: T, b: T) => number;
export interface PaginationData {
  itemsPerPage: number;
  pageNumber: number;
}

export const doNothing = () => { /* Intentionally Empty */ };

/**
 * Used to throw an error on a condition that should never
 * happen.
 */
export function error(msg?: string): never {
  throw new Error(msg ?? '');
}

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
