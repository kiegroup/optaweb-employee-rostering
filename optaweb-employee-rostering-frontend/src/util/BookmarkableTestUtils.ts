
import * as router from 'react-router';
import { UrlProps } from './BookmarkableUtils';

export function getRouterProps<T extends UrlProps<any>>(pathname: string,
  props: Partial<T>): router.RouteComponentProps {
  const searchParams = new URLSearchParams();
  Object.keys(props).forEach((key) => {
    const value = props[key] as string | null | undefined;
    if (value) {
      searchParams.set(key, value);
    }
  });
  const location = {
    pathname,
    search: Object.keys(props).length > 0 ? `?${searchParams.toString()}` : '',
    hash: '',
    state: undefined,
  };
  const history = {
    location,
    push: jest.fn(),
    replace: jest.fn(),
    go: jest.fn(),
    goBack: jest.fn(),
    goForward: jest.fn(),
    block: jest.fn(),
    listen: jest.fn(),
    createHref: jest.fn(),
    action: 'PUSH' as any,
    length: 1,
  };

  jest
    .spyOn(router, 'useHistory')
    .mockImplementation(() => history);

  return {
    history,
    location,
    match: {
      isExact: true,
      path: pathname,
      url: `localhost:8080${pathname}`,
      params: {},
    },
  };
}
