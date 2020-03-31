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

import * as React from 'react';

import 'react-big-calendar/lib/css/react-big-calendar.css';
import './ReactBigCalendarOverrides.css';
import { Popover } from '@patternfly/react-core';

// Workaround for https://github.com/intljusticemission/react-big-calendar/issues/1397,
// and helps with styling the event component and allows the popup to show even if you click
// the time (necessary for small events)
export default function EventWrapper(props: React.PropsWithChildren<{
  continuesEarlier: boolean;
  continuesLater: boolean;
  className: string;
  popoverHeader: React.ReactNode;
  popoverBody: React.ReactNode;
  style?: React.CSSProperties;
}>): JSX.Element {
  let { className } = props;
  const style: React.CSSProperties = {
    backgroundColor: 'transparent',
    border: 'none',
    ...props.style,
  };

  if (props.style !== undefined && props.style.top !== undefined && props.style.height !== undefined) {
    if (props.continuesEarlier) {
      className += ' continues-from-previous-day';
    }
    if (props.continuesLater) {
      className += ' continues-next-day';
    }
  }

  return (
    <div
      className={className}
      style={style}
    >
      <Popover
        headerContent={props.popoverHeader}
        bodyContent={props.popoverBody}
        boundary="viewport"
      >
        <div style={{ maxHeight: '200px' }}>{props.children as React.ReactElement}</div>
      </Popover>
    </div>
  );
}
