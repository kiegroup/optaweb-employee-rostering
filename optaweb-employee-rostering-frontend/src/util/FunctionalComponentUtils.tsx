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
import { useEffect, useRef, useState } from 'react';
import { useHistory } from 'react-router';

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
