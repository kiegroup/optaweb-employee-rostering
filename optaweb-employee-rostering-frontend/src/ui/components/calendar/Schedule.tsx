// @ts-nocheck
// TODO: Re-enable typescript validation on this file when
// @types/react-big-calendar is updated to have prop dayLayoutAlgorithm
import * as React from 'react';
import { Calendar, momentLocalizer, EventProps } from 'react-big-calendar';
import moment from 'moment';
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop';
import { doNothing } from 'types';
import EventWrapper from './EventWrapper';

import 'react-big-calendar/lib/addons/dragAndDrop/styles.scss';
import './ReactBigCalendarOverrides.css';

const DragAndDropCalendar = withDragAndDrop(Calendar);

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
  onAddEvent: (start: Date, end: Date) => void;
  onUpdateEvent: (event: T, start: Date, end: Date) => void;
  eventStyle: StyleSupplier<T>;
  wrapperStyle: StyleSupplier<T>;
  dayStyle: StyleSupplier<Date>;
  popoverHeader: (event: T) => React.ReactNode;
  popoverBody: (event: T) => React.ReactNode;
  eventComponent: (props: React.PropsWithChildren<EventProps<T>>) => React.ReactNode;
}

export function isDay(start: Date, end: Date) {
  return start.getHours() === 0 && start.getMinutes() === 0
    && end.getHours() === 0 && end.getMinutes() === 0;
}

const localizer = momentLocalizer(moment);
export default function Schedule<T extends object>(props: Props<T>): React.ReactElement<Props<T>> {
  const length = Math.ceil(moment(props.endDate).diff(moment(props.startDate), 'days')) + 1;
  const ref = React.useRef<HTMLElement>();
  return (
    <div
      style={{
        height: 'calc(100% - 20px)',
      }}
      ref={ref}
    >
      <DragAndDropCalendar
        className={(props.showAllDayCell) ? undefined : 'rbc-no-allday-cell'}
        dayLayoutAlgorithm="no-overlap"
        date={props.startDate}
        length={length}
        localizer={localizer}
        events={props.events}
        titleAccessor={props.titleAccessor}
        allDayAccessor={event => (props.showAllDayCell
          ? isDay(props.startAccessor(event), props.endAccessor(event)) : false)}
        startAccessor={props.startAccessor}
        endAccessor={props.endAccessor}
        toolbar={false}
        view="week"
        views={['week']}
        formats={props.dateFormat ? {
          dayFormat: props.dateFormat,
        } : undefined}
        onSelectSlot={(slotInfo: { start: string|Date; end: string|Date;
          action: 'select'|'click'|'doubleClick'; }) => {
          if (slotInfo.action === 'select' || slotInfo.action === 'click') {
            if (isDay(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate())) {
              props.onAddEvent(moment(slotInfo.start).toDate(), moment(slotInfo.end).add(1, 'day').toDate());
            } else {
              props.onAddEvent(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate());
            }
          }
        }
        }
        onEventDrop={(dropLocation: { event: T; start: string|Date; end: string|Date }) => {
          if (isDay(moment(dropLocation.start).toDate(), moment(dropLocation.end).toDate())) {
            props.onUpdateEvent(dropLocation.event, moment(dropLocation.start).toDate(),
              moment(dropLocation.end).toDate());
          } else if (moment(dropLocation.start).dayOfYear() !== moment(dropLocation.end).dayOfYear()) {
            props.onUpdateEvent(dropLocation.event, moment(dropLocation.start).toDate(), moment(dropLocation.start)
              .add(
                moment(props.endAccessor(dropLocation.event))
                  .diff(moment(props.startAccessor(dropLocation.event)),
                    'minutes'),
                'minutes',
              )
              .toDate());
          } else {
            props.onUpdateEvent(dropLocation.event, moment(dropLocation.start).toDate(), moment(dropLocation.end)
              .toDate());
          }
        }}
        onEventResize={(resizeInfo: { event: T; start: string|Date; end: string|Date }) => {
          const origEventStart = moment(props.startAccessor(resizeInfo.event));
          const origEventEnd = moment(props.startAccessor(resizeInfo.event));
          if (isDay(moment(resizeInfo.start).toDate(), moment(resizeInfo.end).toDate())) {
            props.onUpdateEvent(resizeInfo.event, moment(resizeInfo.start).toDate(), moment(resizeInfo.end)
              .toDate());
          } else if (origEventStart.dayOfYear() !== moment(resizeInfo.start).dayOfYear()) {
            props.onUpdateEvent(resizeInfo.event, origEventStart.toDate(), moment(resizeInfo.end).toDate());
          } else if (origEventEnd.dayOfYear() !== moment(resizeInfo.end).dayOfYear()) {
            props.onUpdateEvent(resizeInfo.event, moment(resizeInfo.start).toDate(), origEventEnd.toDate());
          } else {
            props.onUpdateEvent(resizeInfo.event, moment(resizeInfo.start).toDate(), moment(resizeInfo.end).toDate());
          }
        }}
        onView={doNothing}
        onNavigate={doNothing}
        timeslots={4}
        eventPropGetter={props.eventStyle}
        dayPropGetter={props.dayStyle}
        selectable
        resizable
        showMultiDayTimes
        components={{
          eventWrapper: (wrapperProps) => {
            const style = props.wrapperStyle(wrapperProps.event);
            return EventWrapper({
              ...wrapperProps,
              style: {
                ...wrapperProps.style,
                ...((style.style) ? style.style : {}),
              },
              boundary: ref,
              className: style.className ? style.className : '',
              popoverHeader: props.popoverHeader(wrapperProps.event),
              popoverBody: props.popoverBody(wrapperProps.event),
            });
          },
          event: eventProps => props.eventComponent(eventProps) as React.ReactElement,
        }}
      />
    </div>
  );
}
