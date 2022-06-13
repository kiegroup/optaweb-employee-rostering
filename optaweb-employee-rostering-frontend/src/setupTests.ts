import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import MockDate from 'mockdate';
import moment from 'moment';
import { Store } from 'redux';

// @ts-ignore
import setTZ from 'set-tz';
import { AppState } from 'store/types';
import * as redux from 'react-redux';

setTZ('UTC');
configure({ adapter: new Adapter() });

const mockDate = moment('2018-01-01', 'YYYY-MM-DD').toDate();
MockDate.set(mockDate);

const mockTranslate = jest.fn().mockImplementation((k, p) => `Trans(i18nKey=${k}${p ? `, ${JSON.stringify(p)}` : ''})`);
export { mockTranslate };

// it appears await does not work with promises that have a then
const flushPromises = () => new Promise(setImmediate);
export { flushPromises };

jest.mock('react-i18next', () => ({
  // this mock makes sure any components using the translate HoC receive the t function as a prop
  withTranslation: () => (Component: any) => {
    // eslint-disable-next-line no-param-reassign
    Component.defaultProps = { ...Component.defaultProps, t: mockTranslate, tReady: true };
    return () => Component;
  },

  useTranslation: () => ({ t: mockTranslate }),
  Trans: jest.requireActual('react-i18next').Trans,
}));

// Need deterministic UUID
jest.mock('uuid', () => {
  let count = 0;
  return {
    v4: jest.fn(() => {
      count += 1;
      return `uuid-${count}`;
    }),
  };
});

function mockFunctions() {
  const original = jest.requireActual('react-responsive');
  return {
    ...original, // Pass down all the exported objects
    useMediaQuery: jest.fn(() => true),
  };
}

jest.mock('react-responsive', () => mockFunctions());
jest.mock('util/BookmarkableUtils');

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom') as any,
  __esModule: true,
  useHistory: () => ({
    push: jest.fn(),
  }),
}));


export function mockRedux(store: Store<AppState>) {
  jest
    .spyOn(redux, 'useSelector')
    .mockImplementation(callback => callback(store.getState()));

  jest
    .spyOn(redux, 'useDispatch')
    .mockImplementation(() => store.dispatch);
}
