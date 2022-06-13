import React from 'react';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import { HistoryIcon, CalendarIcon } from '@patternfly/react-icons';
import './WeekPicker.css';
import { Button, ButtonVariant, Text } from '@patternfly/react-core';

export interface WeekPickerProps {
  value: Date;
  minDate?: Date;
  maxDate?: Date;
  onChange: (weekStartDate: Date, weekEndDate: Date) => void;
}

function getFirstDayInWeek(dateInWeek: Date): Date {
  return moment(dateInWeek).startOf('week').toDate();
}

function getLastDayInWeek(dateInWeek: Date): Date {
  return moment(dateInWeek).endOf('week').toDate();
}

export interface DatePickerCustomInputProps {
  value?: Date;
  divRef?: React.LegacyRef<HTMLDivElement>;
  datePickerRef: React.RefObject<DatePicker>;
  goToCurrentWeek: () => void;
}
export const DateRangeInputElement: React.FC<DatePickerCustomInputProps> = (props) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const onClick = () => {
    if (props.datePickerRef.current) {
      props.datePickerRef.current.setOpen(!isOpen);
      setIsOpen(!isOpen);
    }
  };
  return (
    <div
      ref={props.divRef}
      className="pf-c-form-control"
      style={{
        display: 'grid',
        height: 'auto',
        gridTemplateColumns: '1fr auto auto',
        alignItems: 'center',
      }}
    >
      <Text
        onClick={onClick}
      >
        {
          `${moment(getFirstDayInWeek(props.value as Date)).format('L')} - ${
            moment(getLastDayInWeek(props.value as Date)).format('L')}`
        }
      </Text>
      <Button
        style={{
          color: 'black',
        }}
        onClick={() => props.goToCurrentWeek()}
        variant="link"
      >
        <HistoryIcon />
      </Button>
      <Button
        style={{
          color: 'black',
        }}
        onClick={onClick}
        variant="link"
      >
        <CalendarIcon />
      </Button>
    </div>
  );
};

export interface WeekPickerState {
  datePickerRef: React.RefObject<DatePicker>;
}
export default class WeekPicker extends React.Component<WeekPickerProps, WeekPickerState> {
  constructor(props: WeekPickerProps) {
    super(props);
    this.goToCurrentWeek = this.goToCurrentWeek.bind(this);
    this.goToWeekContaining = this.goToWeekContaining.bind(this);
    this.state = { datePickerRef: React.createRef() };
  }

  goToCurrentWeek() {
    this.goToWeekContaining(new Date());
  }

  goToWeekContaining(date: Date) {
    if (this.props.minDate && getFirstDayInWeek(date) < getFirstDayInWeek(this.props.minDate)) {
      this.goToWeekContaining(this.props.minDate);
      return;
    }
    if (this.props.maxDate && getLastDayInWeek(date) > getLastDayInWeek(this.props.maxDate)) {
      this.goToWeekContaining(this.props.maxDate);
      return;
    }
    this.props.onChange(getFirstDayInWeek(date), getLastDayInWeek(date));
  }

  render() {
    const MyDateRangeInputElement = React.forwardRef<HTMLDivElement, DatePickerCustomInputProps>(
      (dateRangeInputProps, ref) => (
        <DateRangeInputElement {...dateRangeInputProps} divRef={ref} />
      ),
    );

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
          ref={this.state.datePickerRef}
          selected={getFirstDayInWeek(this.props.value)}
          startDate={getFirstDayInWeek(this.props.value)}
          endDate={getLastDayInWeek(this.props.value)}
          onChange={(date) => {
            if (date !== null) {
              this.goToWeekContaining(date);
            } else {
              this.goToCurrentWeek();
            }
          }}
          minDate={this.props.minDate}
          maxDate={this.props.maxDate}
          customInput={(
            <MyDateRangeInputElement
              datePickerRef={this.state.datePickerRef}
              goToCurrentWeek={this.goToCurrentWeek}
            />
          )}
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
