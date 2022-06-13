
import { getRouterProps } from './BookmarkableTestUtils';

const waitForAnimationFrame = async () => {
  jest.runAllTimers();
};

const bookmarkableUtils = jest.requireActual('util/BookmarkableUtils');

describe('Bookmarkable Utils', () => {
  const requestAnimationFrame = jest.spyOn(window, 'requestAnimationFrame');

  beforeAll(() => {
    jest.useFakeTimers();
    requestAnimationFrame.mockImplementation((cb) => { cb(0); return 0; });
  });

  afterAll(() => {
    requestAnimationFrame.mockRestore();
  });

  it('should do nothing if url has no tenant id in setTenantIdInUrl', () => {
    const routerProps = getRouterProps('/test', {});
    bookmarkableUtils.setTenantIdInUrl(routerProps, 1);
    expect(routerProps.history.push).not.toBeCalled();
  });

  it('should replace tenantId in url in setTenantIdInUrl', () => {
    const routerProps = getRouterProps('/0/test', {});
    bookmarkableUtils.setTenantIdInUrl(routerProps, 123);
    expect(routerProps.history.push).toBeCalledWith('/123/test');
  });

  it('should get props in url if they exist, else use default value in getPropsFromUrl', async () => {
    const routerProps = getRouterProps('/0/test', { a: '1' });
    const props = bookmarkableUtils.getPropsFromUrl(routerProps, { a: '2', b: '3' });
    await waitForAnimationFrame();
    expect(routerProps.history.push).not.toBeCalled();
    expect(props).toEqual({ a: '1', b: '3' });
  });

  it('should use previous url parameters on subsequent visits if no url parameters in getPropsFromUrl', async () => {
    const routerProps = getRouterProps('/0/test', { a: '100', b: '200' });
    const props = bookmarkableUtils.getPropsFromUrl(routerProps, { a: '2', b: '3' });
    await waitForAnimationFrame();
    expect(routerProps.history.push).not.toBeCalled();
    expect(props).toEqual({ a: '100', b: '200' });

    bookmarkableUtils.setPropsInUrl(routerProps, { a: '100', b: '200' });
    const routerPropsNoParams = getRouterProps('/0/test', {});
    const newProps = bookmarkableUtils.getPropsFromUrl(routerPropsNoParams, { a: '2', b: '3' });
    await waitForAnimationFrame();
    expect(routerPropsNoParams.history.push).toBeCalledWith('/0/test?a=100&b=200');
    expect(newProps).toEqual({ a: '100', b: '200' });

    const differentRouteProps = getRouterProps('/5/test', { });
    const differentProps = bookmarkableUtils.getPropsFromUrl(differentRouteProps, { a: '2', b: '3' });
    await waitForAnimationFrame();
    expect(differentRouteProps.history.push).not.toBeCalled();
    expect(differentProps).toEqual({ a: '2', b: '3' });
  });

  it('should set props in url in setPropsFromUrl', () => {
    const routerProps = getRouterProps('/0/test', { a: 'a', c: 'c', d: 'd' });
    bookmarkableUtils.setPropsInUrl(routerProps, { a: '1', b: '2', d: null });
    expect(routerProps.history.push).toBeCalledWith('/0/test?a=1&c=c&b=2');
  });
});
