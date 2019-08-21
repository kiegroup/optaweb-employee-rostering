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
import Schedule, { isDay, Props } from './Schedule';
import { shallow, ShallowWrapper } from 'enzyme';
import { CalendarProps, EventProps, EventWrapperProps } from 'react-big-calendar';

function calendarProps(wrapper: ShallowWrapper): CalendarProps<{ start: Date; end: Date; title: string }> {
  return (wrapper.find("ForwardRef").props() as CalendarProps<{ start: Date; end: Date; title: string }>);
}

function reportError(prop: string): never {
  throw new Error(prop + "  should be defined.");
}

describe('Schedule', () => {
  it('isDay should return true if both start and end are on midnight', () => {
    const startDate = new Date("2018-01-01T00:00");
    const endDate = new Date("2018-01-02T00:00");
    expect(isDay(startDate, endDate)).toEqual(true);
  });

  it('isDay should return false if either start or end are not midnight', () => {
    const startDate1 = new Date("2018-01-01T12:00");
    const endDate1 = new Date("2018-01-02T00:00");
    expect(isDay(startDate1, endDate1)).toEqual(false);

    const startDate2 = new Date("2018-01-01T00:00");
    const endDate2 = new Date("2018-01-02T12:00");
    expect(isDay(startDate2, endDate2)).toEqual(false);
  });

  it('should render correctly without an all-day cell', () => {
    const schedule = shallow(<Schedule {...props} />);
    const allDayAccessor: Function = calendarProps(schedule).allDayAccessor as Function;
    expect(allDayAccessor(props.events[0])).toEqual(false);
    expect(allDayAccessor(props.events[1])).toEqual(false);
    expect(allDayAccessor(props.events[2])).toEqual(false);
  });

  it('should render correctly with an all-day cell', () => {
    const schedule = shallow(<Schedule {...props} showAllDayCell />);
    const allDayAccessor: Function = calendarProps(schedule).allDayAccessor as Function;
    expect(allDayAccessor(props.events[0])).toEqual(false);
    expect(allDayAccessor(props.events[1])).toEqual(true);
    expect(allDayAccessor(props.events[2])).toEqual(false);
  });

  it('length should be correct', () => {
    const schedule = shallow(<Schedule {...props} />);
    const length = calendarProps(schedule).length;
    expect(length).toEqual(7);
  });

  it('title accessor should be prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const titleAccessor = calendarProps(schedule).titleAccessor;
    expect(titleAccessor).toBe(props.titleAccessor);
  });

  it('start accessor should be prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const startAccessor = calendarProps(schedule).startAccessor;
    expect(startAccessor).toBe(props.startAccessor);
  });

  it('end accessor should be prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const endAccessor = calendarProps(schedule).endAccessor;
    expect(endAccessor).toBe(props.endAccessor);
  });

  it('day format should be date format if provided', () => {
    const dateFormat = jest.fn();
    const schedule = shallow(<Schedule {...props} dateFormat={dateFormat} />);
    const { dayFormat } = calendarProps(schedule).formats || reportError("formats");
    expect(dayFormat).toBe(dateFormat);
  });
  
  it('formats should be undefined if date format not provided', () => {
    const schedule = shallow(<Schedule {...props} />);
    expect(calendarProps(schedule).formats).toBeUndefined();
  });

  it('should not create event on double click', () => {
    const schedule = shallow(<Schedule {...props} />);
    const onSelectSlot = calendarProps(schedule).onSelectSlot || reportError("onSelectSlot");
    onSelectSlot({
      start: props.events[0].start,
      end: props.events[0].end,
      slots: [],
      action: "doubleClick"
    });
    expect(props.addEvent).not.toBeCalled();
  });

  it('should create event on click', () => {
    const schedule = shallow(<Schedule {...props} />);
    const onSelectSlot = calendarProps(schedule).onSelectSlot || reportError("onSelectSlot");
    onSelectSlot({
      start: props.events[0].start,
      end: props.events[0].end,
      slots: [],
      action: "click"
    });
    expect(props.addEvent).toBeCalled();
    expect(props.addEvent).toBeCalledWith(props.events[0].start, props.events[0].end);
  });

  it('should create event on select', () => {
    const schedule = shallow(<Schedule {...props} />);
    const onSelectSlot = calendarProps(schedule).onSelectSlot || reportError("onSelectSlot");
    onSelectSlot({
      start: props.events[0].start,
      end: props.events[0].end,
      slots: [],
      action: "select"
    });
    expect(props.addEvent).toBeCalled();
    expect(props.addEvent).toBeCalledWith(props.events[0].start, props.events[0].end);
  });

  it('should create event of proper length if it span a day', () => {
    const schedule = shallow(<Schedule {...props} />);
    const onSelectSlot = calendarProps(schedule).onSelectSlot || reportError("onSelectSlot");
    onSelectSlot({
      start: props.events[1].start,
      end: props.events[1].start, // Not an error, selecting a single day yields start and end the same
      slots: [],
      action: "click"
    });
    expect(props.addEvent).toBeCalled();
    expect(props.addEvent).toBeCalledWith(props.events[1].start, props.events[1].end);
  });

  it('eventPropGetter should be prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const eventPropGetter = calendarProps(schedule).eventPropGetter;
    expect(eventPropGetter).toBe(props.eventStyle);
  });
  
  it('dayPropGetter should be prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const dayPropGetter = calendarProps(schedule).dayPropGetter;
    expect(dayPropGetter).toBe(props.dayStyle);
  });

  it('event component should defer to prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const components = calendarProps(schedule).components || reportError("components");
    const event = components.event as 
      React.FC<React.PropsWithChildren<EventProps<{ start: Date; end: Date; title: string }>>> 
      || reportError("components.event");
    const out = event(
      {
        event: props.events[0],
        ...props.events[0]
      });
    expect(props.eventComponent).toBeCalled();
    expect(props.eventComponent).toBeCalledWith({
      event: props.events[0],
      ...props.events[0]
    })
    expect(out).toEqual(props.eventComponent({
      event: props.events[0],
      ...props.events[0]
    }))
  });

  it('wrapper component should defer to prop', () => {
    const schedule = shallow(<Schedule {...props} />);
    const components = calendarProps(schedule).components || reportError("components");
    const wrapper = components.eventWrapper as 
      React.FC<React.PropsWithChildren<EventWrapperProps<{ start: Date; end: Date; title: string }>>> 
      || reportError("components.event");
    const wrapperProps: React.PropsWithChildren<EventWrapperProps<{ start: Date; end: Date; title: string }>> =
    {
      event: props.events[0],
      className: "wrapperClass",
      isRtl: false,
      getters: {},
      onClick: jest.fn(),
      onDoubleClick: jest.fn(),
      accessors: {},
      selected: false,
      label: "Label",
      continuesEarlier: false,
      continuesLater: true,
      style: {
        top: "33%",
        height: "67%",
        xOffset: 0
      }
    }
    const out = wrapper(wrapperProps);
    expect(props.wrapperStyle).toBeCalled();
    expect(props.wrapperStyle).toBeCalledWith(props.events[0]);
    expect(out).toMatchSnapshot();
  });
});

const props: Props<{ start: Date; end: Date; title: string }> = {
  startDate: new Date("2018-01-01T00:00"),
  endDate: new Date("2018-01-07T00:00"),
  events: [
    {
      title: "Event 1",
      start: new Date("2018-01-02T08:00"),
      end: new Date("2018-01-02T17:00")      
    },
    {
      title: "All Day Event ",
      start: new Date("2018-01-03T00:00"),
      end: new Date("2018-01-04T00:00")      
    },
    {
      title: "Multi-day Event",
      start: new Date("2018-01-0520:00"),
      end: new Date("2018-01-06T06:00")      
    }
  ],
  showAllDayCell: false,
  startAccessor: jest.fn(e => e.start),
  endAccessor: jest.fn(e => e.end),
  titleAccessor: jest.fn(e => e.title),
  addEvent: jest.fn(),
  eventStyle: jest.fn(() => ({ style: { color: "red" }, className: "class" })),
  wrapperStyle: jest.fn(() => ({ style: { color: "red" }, className: "class", props: { prop: true } })),
  dayStyle: jest.fn(() => ({ style: { color: "red" }, className: "class" })),
  popoverHeader: jest.fn(() => "Title"),
  popoverBody: jest.fn(() => "Body"),
  eventComponent: jest.fn(e => e.title)
}