/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import { RouteComponentProps } from 'react-router';
import { UrlProps } from './BookmarkableUtils';

export function getRouterProps<T extends UrlProps<any>>(pathname: string, props: Partial<T>): RouteComponentProps {
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
  return {
    history: {
      location,
      push: jest.fn(),
      replace: jest.fn(),
      go: jest.fn(),
      goBack: jest.fn(),
      goForward: jest.fn(),
      block: jest.fn(),
      listen: jest.fn(),
      createHref: jest.fn(),
      action: 'PUSH',
      length: 1,
    },
    location,
    match: {
      isExact: true,
      path: pathname,
      url: `localhost:8080${pathname}`,
      params: {},
    },
  };
}
