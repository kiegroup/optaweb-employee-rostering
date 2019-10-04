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
import { Calendar, momentLocalizer, EventProps } from 'react-big-calendar'
import moment from 'moment';
import EventWrapper from './EventWrapper';

import 'react-big-calendar/lib/css/react-big-calendar.css';
import './ReactBigCalendarOverrides.css';

export interface StyleContainer {
  style?: React.CSSProperties;
  className?: string;
}

export type StyleSupplier<T> = (params: T) => StyleContainer;

export interface Props<T extends object> {
  startDate: Date;
  endDate: Date;
  events: T[];
  showAllDayCell?: boolean;
  dateFormat?: (date: Date) => string;
  startAccessor: (event: T) => Date;
  endAccessor: (event: T) => Date;
  titleAccessor: (event: T) => string;
  addEvent: (start: Date, end: Date) => void;
  eventStyle: StyleSupplier<T>;
  wrapperStyle: StyleSupplier<T>;
  dayStyle: StyleSupplier<Date>;
  popoverHeader: (event: T) => React.ReactNode;
  popoverBody: (event: T) => React.ReactNode;
  eventComponent: (props: React.PropsWithChildren<EventProps<T>>) => React.ReactNode;
}

export function isDay(start: Date, end: Date) {
  return start.getHours() === 0 && start.getMinutes() === 0 &&
    end.getHours() === 0 && end.getMinutes() === 0
}

const localizer = momentLocalizer(moment);
export default function Schedule<T extends object>(props: Props<T>): React.ReactElement<Props<T>> {
  const length = Math.ceil(moment(props.endDate).diff(moment(props.startDate), "days")) + 1;
  return (
    <div style={{
      height: "calc(100% - 20px)"
    }}
    >
      <Calendar
        className={(props.showAllDayCell)? undefined : "rbc-no-allday-cell"}
        date={props.startDate}
        length={length}
        localizer={localizer}
        events={props.events}
        titleAccessor={props.titleAccessor}
        allDayAccessor={event => props.showAllDayCell?
          isDay(props.startAccessor(event), props.endAccessor(event)) : false}
        startAccessor={props.startAccessor}
        endAccessor={props.endAccessor}
        toolbar={false}
        view="week"
        views={["week"]}
        formats={props.dateFormat? {
          dayFormat: props.dateFormat
        } : undefined}
        onSelectSlot={(slotInfo: { start: string|Date; end: string|Date;
          action: "select"|"click"|"doubleClick"; }) => {
          if (slotInfo.action === "select" || slotInfo.action === "click") {
            if (isDay(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate())) {
              props.addEvent(moment(slotInfo.start).toDate(), moment(slotInfo.end).add(1, "day").toDate());
            }
            else {
              props.addEvent(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate());
            }
          }
        }
        }
        onView={() => {}}
        onNavigate={() => {}}
        timeslots={4}
        eventPropGetter={props.eventStyle}
        dayPropGetter={props.dayStyle}
        selectable
        showMultiDayTimes
        components={{
          eventWrapper: (wrapperProps) => {
            const style = props.wrapperStyle(wrapperProps.event);
            return EventWrapper({
              ...wrapperProps,
              style: {
                ...wrapperProps.style,
                ...((style.style)? style.style : {}) 
              },
              className: style.className? style.className : "",
              popoverHeader: props.popoverHeader(wrapperProps.event),
              popoverBody: props.popoverBody(wrapperProps.event)
            })
          },
          event: (eventProps) => props.eventComponent(eventProps) as React.ReactElement
        }}
      />
    </div>
  )
}