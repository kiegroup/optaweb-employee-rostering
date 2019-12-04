/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import * as bookmarkableUtils from './BookmarkableUtils';
import { getRouterProps } from './BookmarkableTestUtils';

// Ugly workaround for having no way to wait for all requestAnimationFrame to finish
const flushPromisesCallback = (cb: () => void) =>
  window.requestAnimationFrame(() => new Promise(setImmediate).then(cb));
describe('Bookmarkable Utils', () => {
  beforeAll(() => {
    jest.unmock("util/BookmarkableUtils");
  });
  
  afterAll(() => {
    jest.mock("util/BookmarkableUtils");
  });
  
  it('should do nothing if url has no tenant id in setTenantIdInUrl', () => {
    const routerProps = getRouterProps("/test", {});
    bookmarkableUtils.setTenantIdInUrl(routerProps, 1);
    expect(routerProps.history.push).not.toBeCalled();
  });
  
  it('should replace tenantId in url in setTenantIdInUrl', () => {
    const routerProps = getRouterProps("/0/test", {});
    bookmarkableUtils.setTenantIdInUrl(routerProps, 123); 
    expect(routerProps.history.push).toBeCalledWith("/123/test");
  });
  
  it('should get props in url if they exist, else use default value in getPropsFromUrl', () => {
    const routerProps = getRouterProps("/0/test", { a: "1" });
    const props = bookmarkableUtils.getPropsFromUrl(routerProps, { a: "2", b: "3" });
    flushPromisesCallback(() => {
      expect(routerProps.history.push).not.toBeCalled();
      expect(props).toEqual({ a: "1", b: "3" });
    });
  });
  
  it('should use previous url parameters on subsequent visits if no url parameters in getPropsFromUrl', () => {
    const routerProps = getRouterProps("/0/test", { a: "100", b: "200" });
    const props = bookmarkableUtils.getPropsFromUrl(routerProps, { a: "2", b: "3" });
    flushPromisesCallback(() => {
      expect(routerProps.history.push).not.toBeCalled();
      expect(props).toEqual({ a: "100", b: "200" });
      const routerPropsNoParams = getRouterProps("/0/test", {});
      const newProps = bookmarkableUtils.getPropsFromUrl(routerPropsNoParams, { a: "2", b: "3" });
      flushPromisesCallback(() => {
        expect(routerPropsNoParams.history.push).toBeCalledWith("/0/test?a=100&b=200");
        expect(newProps).toEqual({ a: "100", b: "200" });
        
        const differentRouteProps = getRouterProps("/5/test", { });
        const differentProps = bookmarkableUtils.getPropsFromUrl(differentRouteProps, { a: "2", b: "3" });
        flushPromisesCallback(() => {
          expect(differentRouteProps.history.push).not.toBeCalled();
          expect(differentProps).toEqual({ a: "2", b: "3" }); 
        });
      });
    });
  });
  
  it('should set props in url in setPropsFromUrl', () => {
    const routerProps = getRouterProps("/0/test", { a: "a", c: "c", d: "d" });
    bookmarkableUtils.setPropsInUrl(routerProps, { a: "1", b: "2", d: null });
    expect(routerProps.history.push).toBeCalledWith("/0/test?a=1&c=c&b=2");
  });
  
});
