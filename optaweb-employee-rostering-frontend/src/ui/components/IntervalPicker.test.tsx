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
// @ts-ignore
import DatePicker from '@wojtekmaj/react-daterange-picker';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import moment from 'moment-timezone';
import 'moment/locale/en-ca';

import MockDate from 'mockdate';
import IntervalPicker from './IntervalPicker';

describe('IntervalPicker component', () => {
  beforeAll(() => {
    moment.locale('en');
  });

  it('should render correctly when closed', () => {
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={jest.fn()} />);
    expect(toJson(intervalPicker)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={jest.fn()} />);
    intervalPicker.setState({ isOpen: true });
    expect(toJson(intervalPicker)).toMatchSnapshot();
  });

  it('should pass previous interval to onChange when previous interval is clicked', () => {
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    intervalPicker.find('[aria-label="Previous Interval"]').simulate('click');
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-06-23').toDate(), moment('2019-06-23').endOf('week').toDate());
  });

  it('should pass next interval to onChange when next interval is clicked', () => {
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    intervalPicker.find('[aria-label="Next Interval"]').simulate('click');
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-07-07').toDate(), moment('2019-07-07').endOf('week').toDate());
  });

  it('should go to current week when the reset button is clicked', () => {
    const solvingStartTime = moment('2019-07-23').toDate();
    MockDate.set(solvingStartTime);
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    ((intervalPicker.find(DatePicker).props() as any).onChange as Function)({} as React.FormEvent);
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-07-21').toDate(), moment('2019-07-21').endOf('week').toDate());
  });

  it('should go to the week containing a day when the day is clicked', () => {
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    ((intervalPicker.find(DatePicker).props() as any).onClickDay as Function)(moment('2019-07-30').toDate());
    expect(onChange).toBeCalled();
    expect(onChange)
      .toBeCalledWith(moment('2019-07-28').toDate(), moment('2019-07-28').endOf('week').toDate());
  });

  it('should open when opened', () => {
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    ((intervalPicker.find(DatePicker).props() as any).onCalendarOpen as Function)();
    expect(intervalPicker.instance().state).toEqual({
      isOpen: true,
    });
  });

  it('should closed when closed', () => {
    const onChange = jest.fn();
    const intervalPicker = shallow(<IntervalPicker value={moment('2019-07-03').toDate()} onChange={onChange} />);
    intervalPicker.setState({ isOpen: true });
    ((intervalPicker.find(DatePicker).props() as any).onCalendarClose as Function)();
    expect(intervalPicker.instance().state).toEqual({
      isOpen: false,
    });
  });
});
