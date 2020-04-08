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
import './WeekPicker.css';
import { Button, ButtonVariant } from '@patternfly/react-core';

export interface WeekPickerProps {
  value: Date;
  minDate?: Date;
  maxDate?: Date;
  onChange: (weekStartDate: Date, weekEndDate: Date) => void;
}

export interface WeekPickerState {
  isOpen: boolean;
}

function getFirstDayInWeek(dateInWeek: Date): Date {
  return moment(dateInWeek).startOf('week').toDate();
}

function getLastDayInWeek(dateInWeek: Date): Date {
  return moment(dateInWeek).endOf('week').toDate();
}

export default class WeekPicker extends React.Component<WeekPickerProps, WeekPickerState> {
  constructor(props: WeekPickerProps) {
    super(props);
    this.state = { isOpen: false };
  }

  goToCurrentWeek() {
    this.goToWeekContaining(new Date());
  }

  goToWeekContaining(date: Date) {
    if (this.props.minDate && getFirstDayInWeek(date) < getFirstDayInWeek(this.props.minDate)) {
      this.goToWeekContaining(this.props.minDate);
    } else if (this.props.maxDate && getLastDayInWeek(date) > getLastDayInWeek(this.props.maxDate)) {
      this.goToWeekContaining(this.props.maxDate);
    }
    this.props.onChange(getFirstDayInWeek(date), getLastDayInWeek(date));
    this.setState({ isOpen: false });
  }

  render() {
    const locale = moment.locale();
    return (
      <div className="week-picker-container">
        <Button
          aria-label="Previous Week"
          variant={ButtonVariant.plain}
          isDisabled={this.props.minDate && this.props.value <= getFirstDayInWeek(this.props.minDate)}
          onClick={() => this.goToWeekContaining(moment(this.props.value).subtract(1, 'w').toDate())}
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
          className="week-picker"
          locale={
            /* moment intreprets "en" as "en-US", this intreprets "en" as "en-GB" */
            locale === 'en' ? 'en-US' : locale
          }
          value={[getFirstDayInWeek(this.props.value), getLastDayInWeek(this.props.value)]}
          onChange={() => this.goToCurrentWeek()}
          onClickDay={(value: Date) => this.goToWeekContaining(value)}
          onCalendarOpen={() => this.setState({ isOpen: true })}
          onCalendarClose={() => this.setState({ isOpen: false })}
          minDate={this.props.minDate}
          maxDate={this.props.maxDate}
          isOpen={this.state.isOpen}
          clearIcon={<HistoryIcon />}
          required
        />
        <Button
          aria-label="Next Week"
          variant={ButtonVariant.plain}
          isDisabled={this.props.maxDate && this.props.value >= getFirstDayInWeek(this.props.maxDate)}
          onClick={() => this.goToWeekContaining(moment(this.props.value).add(1, 'w').toDate())}
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
      </div>
    );
  }
}
