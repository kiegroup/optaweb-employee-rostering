import { List } from 'immutable';
import { useEffect, useRef, useState } from 'react';
import { useHistory } from 'react-router';
import { Sorter } from 'types';
import { DataTableUrlProps } from 'ui/components/DataTable';

export function useUrlState(urlProperty: string, initialValue?: string):
[string|null, (newValue: string|null) => void] {
  const history = useHistory();
  const searchParams = new URLSearchParams(history.location.search);
  const [state, setState] = useState(searchParams.get(urlProperty) || initialValue || null);
  return [
    state || null,
    (newValue) => {
      setState(newValue);
      const newSearchParams = new URLSearchParams(history.location.search);
      if (newValue !== null) {
        newSearchParams.set(urlProperty, newValue);
      } else {
        newSearchParams.delete(urlProperty);
      }
      history.push(`${history.location.pathname}?${newSearchParams.toString()}`);
    }];
}

// From https://overreacted.io/making-setinterval-declarative-with-react-hooks/
export function useInterval(callback: Function, delay: number|null) {
  const savedCallback = useRef<Function>();

  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  // Set up the interval.
  // eslint-disable-next-line consistent-return
  useEffect(() => {
    function tick() {
      (savedCallback.current as Function)();
    }
    if (delay !== null) {
      const id = setInterval(tick, delay);
      return () => clearInterval(id);
    }
  }, [delay]);
}


export interface PaginationData<T> {
  filterText: string;
  page: number;
  itemsPerPage: number;
  filteredRows: List<T>;
  numOfFilteredRows: number;
  rowsInPage: List<T>;
  isReversed: boolean;
}

export function usePageableData<T>(urlProps: Omit<DataTableUrlProps, 'sortBy'>, data: T[],
  valueToText: (value: T) => string[], sortBy: Sorter<T>): PaginationData<T> {
  const filterText = urlProps.filter || '';
  const page = parseInt(urlProps.page || '1', 10);
  const itemsPerPage = parseInt(urlProps.itemsPerPage || '10', 10);
  const isReversed = urlProps.asc === 'false';

  const sortedRows = List.of(...data)
    .sort(isReversed ? (a, b) => sortBy(b, a) : sortBy);

  const filteredRows = sortedRows
    .filter(value => valueToText(value)
      .filter(text => text.toLowerCase().indexOf(filterText.toLowerCase()) !== -1).length > 0);

  const numOfFilteredRows = filteredRows.size;

  const rowsInPage = filteredRows
    .skip((page - 1) * itemsPerPage)
    .take(itemsPerPage);

  return {
    filterText,
    page,
    itemsPerPage,
    filteredRows,
    numOfFilteredRows,
    rowsInPage,
    isReversed,
  };
}
