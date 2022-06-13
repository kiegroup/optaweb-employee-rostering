import { List } from 'immutable';
import { Sorter } from '../types';
import { usePageableData } from './FunctionalComponentUtils';

interface MockData {
  title: string;
  views: number;
  reviews: string[];
}

describe('Use PageableData', () => {
  it('Should return the specified page from the data array', () => {
    const mockData: MockData[] = [
      {
        title: 'Movie 1',
        views: 15,
        reviews: ['A', 'B', 'C'],
      },
      {
        title: 'Movie 2',
        views: 2,
        reviews: [],
      },
      {
        title: 'Movie 3',
        views: 7,
        reviews: ['Review 1'],
      },
    ];

    const valueToText = (data: MockData) => [data.title, `${data.views}`, ...data.reviews];
    const viewSorter: Sorter<MockData> = (a, b) => b.views - a.views;

    expect(usePageableData({
      page: null,
      itemsPerPage: null,
      filter: null,
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '',
      page: 1,
      itemsPerPage: 10,
      filteredRows: List(mockData).sort(viewSorter),
      numOfFilteredRows: 3,
      rowsInPage: List(mockData).sort(viewSorter),
      isReversed: false,
    });

    expect(usePageableData({
      page: '2',
      itemsPerPage: null,
      filter: null,
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '',
      page: 2,
      itemsPerPage: 10,
      filteredRows: List(mockData).sort(viewSorter),
      numOfFilteredRows: 3,
      rowsInPage: List(),
      isReversed: false,
    });

    expect(usePageableData({
      page: '1',
      itemsPerPage: '2',
      filter: null,
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '',
      page: 1,
      itemsPerPage: 2,
      filteredRows: List(mockData).sort(viewSorter),
      numOfFilteredRows: 3,
      rowsInPage: List(mockData).sort(viewSorter).take(2),
      isReversed: false,
    });

    expect(usePageableData({
      page: '1',
      itemsPerPage: '2',
      filter: '1',
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '1',
      page: 1,
      itemsPerPage: 2,
      filteredRows: List(mockData).remove(1).sort(viewSorter),
      numOfFilteredRows: 2,
      rowsInPage: List(mockData).remove(1).sort(viewSorter).take(2),
      isReversed: false,
    });

    expect(usePageableData({
      page: '1',
      itemsPerPage: '2',
      filter: null,
      asc: 'false',
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '',
      page: 1,
      itemsPerPage: 2,
      filteredRows: List(mockData).sort((a, b) => viewSorter(b, a)),
      numOfFilteredRows: 3,
      rowsInPage: List(mockData).sort((a, b) => viewSorter(b, a)).take(2),
      isReversed: true,
    });

    expect(usePageableData({
      page: '1',
      itemsPerPage: '2',
      filter: null,
      asc: 'true',
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: '',
      page: 1,
      itemsPerPage: 2,
      filteredRows: List(mockData).sort(viewSorter),
      numOfFilteredRows: 3,
      rowsInPage: List(mockData).sort(viewSorter).take(2),
      isReversed: false,
    });

    expect(usePageableData({
      page: null,
      itemsPerPage: null,
      filter: 'a',
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: 'a',
      page: 1,
      itemsPerPage: 10,
      filteredRows: List([mockData[0]]),
      numOfFilteredRows: 1,
      rowsInPage: List([mockData[0]]),
      isReversed: false,
    });

    expect(usePageableData({
      page: null,
      itemsPerPage: null,
      filter: 'view',
      asc: null,
    }, mockData, valueToText, viewSorter)).toEqual({
      filterText: 'view',
      page: 1,
      itemsPerPage: 10,
      filteredRows: List([mockData[2]]),
      numOfFilteredRows: 1,
      rowsInPage: List([mockData[2]]),
      isReversed: false,
    });
  });
});
