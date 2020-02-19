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
import React from 'react';
// @ts-ignore
import DatePicker from '@wojtekmaj/react-daterange-picker';
import moment from 'moment';
import { HistoryIcon } from '@patternfly/react-icons';
import './IntervalPicker.css';
import { Button, ButtonVariant, InputGroup } from '@patternfly/react-core';

export interface IntervalPickerProps {
  value: Date;
  interval: 'day' | 'week' | 'month';
  onChange: (intervalStartDate: Date, intervalEndDate: Date) => void;
  onIntervalChange: (interval: 'day' | 'week' | 'month') => void;
}

export interface IntervalPickerState {
  isOpen: boolean;
  interval: 'day' | 'week' | 'month';
}

function getDaysInInterval(dateInInterval: Date, interval: 'day' | 'week' | 'month'): Date[] {
  const dateMoment = moment(dateInInterval);
  return [
    dateMoment.startOf(interval).toDate(),
    dateMoment.endOf(interval).toDate(),
  ];
}

export default class IntervalPicker extends React.Component<IntervalPickerProps, IntervalPickerState> {
  constructor(props: IntervalPickerProps) {
    super(props);
    this.state = {
      isOpen: false,
      interval: props.interval,
    };
  }

  goToCurrentInterval() {
    this.goToIntervalContaining(new Date());
  }

  goToIntervalContaining(date: Date) {
    const [firstDay, lastDay] = getDaysInInterval(date, this.state.interval);
    this.props.onChange(firstDay, lastDay);
    this.setState({ isOpen: false });
  }

  switchIntervalTo(interval: 'day' | 'week' | 'month') {
    this.setState({ interval });
    const [firstDay, lastDay] = getDaysInInterval(this.props.value, interval);
    this.props.onChange(firstDay, lastDay);
    this.props.onIntervalChange(interval);
  }

  render() {
    const locale = moment.locale();
    return (
      <div className="interval-picker-container">
        <Button
          aria-label="Previous Interval"
          variant={ButtonVariant.plain}
          onClick={
            () => this.goToIntervalContaining(moment(this.props.value).subtract(1, this.state.interval).toDate())
          }
        >
          <svg
            fill="currentColor"
            height="1em"
            width="1em"
            viewBox="0 0 256 512"
            aria-hidden="true"
            role="img"
            style={{ verticalAlign: '-0.125em' }}
          >
            <path
              d={
                'M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 '
                + '256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 '
                + '0l-136-136c-9.5-9.4-9.5-24.6-.1-34z'
              }
              transform=""
            />
          </svg>
        </Button>
        <DatePicker
          className="interval-picker"
          locale={
            /* moment intreprets "en" as "en-US", this intreprets "en" as "en-GB" */
            locale === 'en' ? 'en-US' : locale
          }
          value={getDaysInInterval(this.props.value, this.state.interval)}
          onChange={() => this.goToCurrentInterval()}
          onClickDay={(value: Date) => this.goToIntervalContaining(value)}
          onCalendarOpen={() => this.setState({ isOpen: true })}
          onCalendarClose={() => this.setState({ isOpen: false })}
          isOpen={this.state.isOpen}
          clearIcon={<HistoryIcon />}
          required
        />
        <Button
          aria-label="Next Interval"
          variant={ButtonVariant.plain}
          onClick={() => this.goToIntervalContaining(moment(this.props.value).add(1, this.state.interval).toDate())}
        >
          <svg
            fill="currentColor"
            height="1em"
            width="1em"
            viewBox="0 0 256 512"
            aria-hidden="true"
            role="img"
            style={{ verticalAlign: '-0.125em' }}
          >
            <path
              d={
                'M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 '
                + '0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 '
                + '0l136 136c9.5 9.4 9.5 24.6.1 34z'
              }
              transform=""
            />
          </svg>
        </Button>
        <InputGroup className="interval-picker-toggle">
          <Button
            aria-label="Day"
            variant={this.state.interval === 'day' ? 'primary' : 'tertiary'}
            onClick={() => this.switchIntervalTo('day')}
          >
            Day
          </Button>
          <Button
            aria-label="Week"
            variant={this.state.interval === 'week' ? 'primary' : 'tertiary'}
            onClick={() => this.switchIntervalTo('week')}
          >
            Week
          </Button>
          <Button
            aria-label="Month"
            variant={this.state.interval === 'month' ? 'primary' : 'tertiary'}
            onClick={() => this.switchIntervalTo('month')}
          >
            Month
          </Button>
        </InputGroup>
      </div>
    );
  }
}
