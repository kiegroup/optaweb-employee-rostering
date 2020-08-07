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
import { shallow } from 'enzyme';
import DatePicker from 'react-datepicker';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import moment from 'moment-timezone';
import 'moment/locale/en-ca';

import MockDate from 'mockdate';
import { Button, Text } from '@patternfly/react-core';
import { HistoryIcon, CalendarIcon } from '@patternfly/react-icons';
import WeekPicker, { DateRangeInputElement } from './WeekPicker';

describe('WeekPicker component', () => {
  beforeAll(() => {
    moment.locale('en');
  });

  it('should render correctly', () => {
    const weekPicker = shallow(<WeekPicker value={moment('2019-07-03').toDate()} onChange={jest.fn()} />);
    weekPicker.setState({ isOpen: true });
    expect(toJson(weekPicker)).toMatchSnapshot();
  });

  it('should pass previous week to onChange when previous week is clicked', () => {
    const onChange = jest.fn();
    const weekPicker = shallow(<WeekPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    weekPicker.find('[aria-label="Previous Week"]').simulate('click');
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-06-23').toDate(), moment('2019-06-23').endOf('week').toDate());
  });

  it('should pass next week to onChange when next week is clicked', () => {
    const onChange = jest.fn();
    const weekPicker = shallow(<WeekPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    weekPicker.find('[aria-label="Next Week"]').simulate('click');
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-07-07').toDate(), moment('2019-07-07').endOf('week').toDate());
  });

  it('should go to the week containing a day when the day is clicked', () => {
    const onChange = jest.fn();
    const weekPicker = shallow(<WeekPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    weekPicker.find(DatePicker).simulate('change', moment('2019-07-30').toDate());
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-07-28').toDate(), moment('2019-07-28').endOf('week').toDate());
  });
});

describe('WeekPicker custom input component', () => {
  beforeAll(() => {
    moment.locale('en');
  });

  it('custom input should render correctly', () => {
    const solvingStartTime = moment('2019-07-23').toDate();
    MockDate.set(solvingStartTime);
    const goToCurrentWeek = jest.fn();
    const date = moment('2019-07-03').toDate();
    const datePickerRef = { current: { setOpen: jest.fn() } };
    const customInput = shallow(
      <DateRangeInputElement
        value={date}
        datePickerRef={datePickerRef as any as React.RefObject<DatePicker>}
        goToCurrentWeek={goToCurrentWeek}
      />,
    );

    expect(customInput).toMatchSnapshot();
  });

  it('should go to current week when the reset button is clicked', () => {
    const solvingStartTime = moment('2019-07-23').toDate();
    MockDate.set(solvingStartTime);
    const goToCurrentWeek = jest.fn();
    const date = moment('2019-07-03').toDate();
    const datePickerRef = { current: { setOpen: jest.fn() } };
    const customInput = shallow(
      <DateRangeInputElement
        value={date}
        datePickerRef={datePickerRef as any as React.RefObject<DatePicker>}
        goToCurrentWeek={goToCurrentWeek}
      />,
    );

    customInput.find(Button).filterWhere(p => p.contains(<HistoryIcon />)).simulate('click');
    expect(datePickerRef.current.setOpen).not.toBeCalled();
    expect(goToCurrentWeek).toBeCalled();
  });

  it('should open the week selector if the input or calendar icon is clicked', () => {
    const solvingStartTime = moment('2019-07-23').toDate();
    MockDate.set(solvingStartTime);
    const goToCurrentWeek = jest.fn();
    const date = moment('2019-07-03').toDate();
    const datePickerRef = { current: { setOpen: jest.fn() } };
    const customInput = shallow(
      <DateRangeInputElement
        value={date}
        datePickerRef={datePickerRef as any as React.RefObject<DatePicker>}
        goToCurrentWeek={goToCurrentWeek}
      />,
    );

    customInput.find(Button).filterWhere(p => p.contains(<CalendarIcon />)).simulate('click');
    expect(datePickerRef.current.setOpen).toBeCalled();
    expect(goToCurrentWeek).not.toBeCalled();

    datePickerRef.current.setOpen.mockClear();

    customInput.find(Text).simulate('click');
    expect(datePickerRef.current.setOpen).toBeCalled();
    expect(goToCurrentWeek).not.toBeCalled();
  });
});
