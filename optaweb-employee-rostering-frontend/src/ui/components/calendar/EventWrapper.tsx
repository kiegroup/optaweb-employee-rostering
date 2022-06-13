
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
  boundary: React.MutableRefObject<HTMLElement | undefined>;
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

  // zIndex need to be higher than .rbc-event:hover:not(.rbc-addons-dnd-drag-preview)
  // in ReactBigCalendarOverrides.css
  return (
    <div
      className={className}
      style={style}
    >
      <Popover
        headerContent={props.popoverHeader}
        bodyContent={props.popoverBody}
        appendTo={props.boundary.current}
        zIndex={1000001}
      >
        {props.children as React.ReactElement}
      </Popover>
    </div>
  );
}
