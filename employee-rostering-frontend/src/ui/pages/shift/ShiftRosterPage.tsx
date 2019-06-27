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
import React from "react";
import Shift from "domain/Shift";
import Spot from "domain/Spot";
import { AppState } from "store/types";
import { spotSelectors } from "store/spot";
import { shiftSelectors } from "store/shift";
import { connect } from 'react-redux';
import GridLayout from 'react-grid-layout';
import WeekPicker from 'ui/components/WeekPicker';
import Schedule from 'ui/components/Schedule';
import moment from 'moment';
import { Level, LevelItem, Button, Pagination } from "@patternfly/react-core";

interface StateProps {
  spotList: Spot[];
  shiftList: Shift[];
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  spotList: spotSelectors.getSpotList(state),
  shiftList: shiftSelectors.getShiftList(state)
}); 
  
export interface DispatchProps {
}
  
const mapDispatchToProps: DispatchProps = {
};

export interface State {
  firstDayInWeek: Date;
}
  
export type Props = StateProps & DispatchProps;

export class ShiftSchedule extends Schedule<Shift> {
  getDataLane(title: string, row: number, gridCount: number): JSX.Element {
    const gridItemWidth = parseInt(this.getCSSVariable("--grid-unit-size"));
    const rowHeight = parseInt(this.getCSSVariable("--grid-row-size"));

    return (
      <div className="lane-container" key={title}>
        <div className="lane-title">{title}</div>
        <GridLayout
          className="lane-content"
          cols={gridCount}
          rowHeight={rowHeight}
          width={gridCount * gridItemWidth}
          margin={[5,5]}
          style={{
            width: gridCount * gridItemWidth + "px"
          }}
        >
          {
            this.props.rowData[row].map((data) => {
              const dataStartDate = this.props.dataGetStartDate(data);
              const dataEndDate = this.props.dataGetEndDate(data);
              
              const startPositionInGrid = moment(dataStartDate).diff(this.props.startDate, "minutes") / this.props.minDurationInMinutes;
              const endPositionInGrid = moment(dataEndDate).diff(this.props.startDate, "minutes") / this.props.minDurationInMinutes;

              return (
                <span
                  className="blob"
                  key={data.id}
                  data-grid={{
                    i: String(data.id),
                    x: startPositionInGrid,
                    y: 1,
                    w: endPositionInGrid - startPositionInGrid,
                    h: 1,
                    minH: 1,
                    maxH: 1
                  }}
                >
                  {this.props.dataToNameMap(data)}
                </span>
              );
            })
          }
        </GridLayout>
      </div>
    );
  }
}

export class ShiftRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      firstDayInWeek: new Date(),
    };
  }

  render() {
    if (this.props.shiftList.length === 0 || this.props.spotList.length === 0) {
      return (<div />);
    }

    const startDate = moment.min(this.props.shiftList.map(s => moment(s.startDateTime))).toDate();
    const endDate = moment(startDate).add(7, "days").toDate();
    return (
      <>
        <Level gutter="sm">
          <LevelItem>
            <WeekPicker
              value={this.state.firstDayInWeek}
              onChange={(fd, ld) => this.setState({ firstDayInWeek: fd })}
            />
          </LevelItem>
          <LevelItem style={{display: "flex"}}>
            <Button>Publish</Button>
            <Button>Schedule</Button>
            <Button>Refresh</Button>
            <Pagination itemCount={10} />
            <Button>Create Shift</Button>
          </LevelItem>
        </Level>
        <ShiftSchedule
          startDate={startDate}
          endDate={endDate}
          rowTitles={this.props.spotList.map(spot => spot.name)}
          rowData={this.props.spotList.map(spot => this.props.shiftList.filter(shift => shift.spot.id === spot.id && moment(shift.startDateTime).isBefore(endDate)))}
          dataToNameMap={s => (s.employee !== null)? s.employee.name : "Unassigned"}
          dataGetStartDate={s => s.startDateTime}
          dataGetEndDate={s => s.endDateTime}
          minDurationInMinutes={60 * 4}
        />
      </>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ShiftRosterPage);