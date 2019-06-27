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
import moment from 'moment';
import './Schedule.css';
import DomainObject from 'domain/DomainObject';
import GridLayout from 'react-grid-layout';

export interface ScheduleProps<T extends DomainObject> {
  startDate: Date;
  endDate: Date;
  minDurationInMinutes: number;
  rowTitles: string[];
  rowData: T[][];
  dataGetStartDate: (data: T) => Date;
  dataGetEndDate: (data: T) => Date;
  dataToNameMap: (data: T) => string;
}

type CSSVariable = "--grid-header-column-width" | "--grid-soft-line-interval" | "--grid-hard-line-interval" | "--grid-unit-size" | "--grid-row-size"

export interface ScheduleState {
  cssVariables: React.CSSProperties;
}

export default abstract class Schedule<T extends DomainObject> extends React.Component<ScheduleProps<T>, ScheduleState> {
  
  windowResizeListener: (e: UIEvent) => void;

  constructor(props: ScheduleProps<T>) {
    super(props);
    this.getDateTimeLane = this.getDateTimeLane.bind(this);
    this.getDataLane = this.getDataLane.bind(this);
    this.getCSSVariable = this.getCSSVariable.bind(this);
    const gridCount = moment(this.props.endDate.getTime()).diff(this.props.startDate.getTime(), "minutes") / this.props.minDurationInMinutes;
    const gridUnitSize = Math.max(((window.innerWidth - 200) / gridCount), 45);
    this.state = {
      cssVariables: {
        "--grid-unit-size": gridUnitSize + "px",
        "--grid-row-size": "50px",
        "--grid-soft-line-interval": gridUnitSize + "px",
        "--grid-hard-line-interval": gridUnitSize * (moment.duration(1, "day").asMinutes() / this.props.minDurationInMinutes) + "px"
      } as React.CSSProperties
    };

    this.windowResizeListener = (e) => {
      const currentGridCount = moment(this.props.endDate.getTime()).diff(this.props.startDate.getTime(), "minutes") / this.props.minDurationInMinutes;
      const gridUnitSize =  Math.max(((e.target as Window).outerWidth - 200) / currentGridCount, 45);
      this.setCSSVariable("--grid-unit-size", gridUnitSize + "px");
      this.setCSSVariable("--grid-soft-line-interval", gridUnitSize + "px");
      this.setCSSVariable("--grid-hard-line-interval", gridUnitSize * (moment.duration(1, "day").asMinutes() / this.props.minDurationInMinutes) + "px");
      this.forceUpdate();
    }
  }

  componentDidMount() {
    window.addEventListener("resize", this.windowResizeListener);
  }

  componentWillUnmount() {
    window.removeEventListener("resize", this.windowResizeListener);
  }

  setCSSVariable(variable: CSSVariable, value: string): void {
    this.setState((prevState) => ({
      cssVariables: {...prevState.cssVariables, [variable]: value}
    }));
  }

  getCSSVariable(variable: CSSVariable): string {
    return (this.state.cssVariables as any)[variable];
  }

  getGridPosition(start: number, end: number): React.CSSProperties {
    const snapStartPositionInGrid = Math.floor(start);
    const startPositionRemainder = start - snapStartPositionInGrid;

    const snapEndPositionInGrid = Math.floor(end);
    const endPositionRemainder = end - snapEndPositionInGrid;

    return {
      gridColumnStart: snapStartPositionInGrid + 1,
      marginLeft: "calc(" + startPositionRemainder + "* var(--grid-unit-size))",
      gridColumnEnd: snapEndPositionInGrid + 1,
      marginRight: "calc(" + endPositionRemainder + "* var(--grid-unit-size))",
    };
  }

  getDateTimeLane(durationInDays: number, gridCount: number): JSX.Element {
    const gridUnitsPerDay = (60 * 24) / this.props.minDurationInMinutes;
    return (
      <div className="date-time-lane">
        <div className="date-time-corner" />
        <div className="date-time-container">
          {
            Array.from(new Array(durationInDays)).map((v, index) => {
              const date = moment(this.props.startDate)
                .add(index, "days");
              return (
                <div
                  key={date.format("L")}
                  className="date-tick"
                  style={this.getGridPosition(index * gridUnitsPerDay, (index + 1) * gridUnitsPerDay)}
                >
                  {date.format("L")}
                </div>
              );
            })
          }
          {
            Array.from(new Array(durationInDays)).map((v, index) => {
              return (
                <div
                  key={moment(this.props.startDate).add(index, "days").toString()}
                  className="date-change-tick"
                  style={this.getGridPosition(index * gridUnitsPerDay, (index + 1) * gridUnitsPerDay)}
                />
              );
            })
          }
          {
            Array.from(new Array(gridCount)).map((v, index) => {
              const date = moment(this.props.startDate)
                .add(this.props.minDurationInMinutes * index, "minutes");
              return (
                <div
                  key={date.format("LLLL")}
                  className="time-tick"
                  style={this.getGridPosition(index, index)}
                >
                  {date.format("LT")}
                </div>
              );
            })
          }
        </div>
      </div>
    );
  }

  abstract getDataLane(title: string, row: number, gridCount: number): JSX.Element;

  render() {
    const gridCount = moment(this.props.endDate.getTime()).diff(this.props.startDate.getTime(), "minutes") / this.props.minDurationInMinutes;
    const durationInDays =  moment(this.props.endDate.getTime()).diff(this.props.startDate.getTime(), "days"); 
    return (
      <div className="schedule" style={this.state.cssVariables}>
        {this.getDateTimeLane(durationInDays, gridCount)}
        {this.props.rowTitles.map((title, row) => this.getDataLane(title, row, gridCount))}
      </div>
    );
  }
}