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
import { rosterOperations, rosterSelectors } from "store/roster";
import { spotSelectors } from "store/spot";
import { connect } from 'react-redux';
import GridLayout from 'react-grid-layout';
import WeekPicker from 'ui/components/WeekPicker';
import Schedule from 'ui/components/Schedule';
import moment from 'moment';
import { Level, LevelItem, Button, Pagination } from "@patternfly/react-core";
import ShiftEvent, { getShiftColor } from "./ShiftEvent";
import { PaginationData } from "types";
import { shiftOperations } from "store/shift";

interface StateProps {
  isLoading: boolean;
  spotList: Spot[];
  spotIdToShiftListMap: Map<number, Shift[]>;
  startDate: Date | null;
  endDate: Date | null;
  totalNumOfSpots: number;
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  isLoading: state.shiftRoster.isLoading,
  spotList: rosterSelectors.getSpotListInShiftRoster(state),
  spotIdToShiftListMap: rosterSelectors.getSpotListInShiftRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForSpot(state, curr)),
    new Map<number, Shift[]>()),
  startDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.startDate).toDate() : null,
  endDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length
}); 
  
export interface DispatchProps {
  getShiftRoster: typeof rosterOperations.getShiftRoster;
  updateShift: typeof shiftOperations.updateShift;
}
  
const mapDispatchToProps: DispatchProps = {
  getShiftRoster: rosterOperations.getShiftRoster,
  updateShift: shiftOperations.updateShift
};
  
export type Props = StateProps & DispatchProps;

export class ShiftSchedule extends Schedule<Shift> {
  getDataLane(title: string, row: number, gridCount: number): JSX.Element {
    const gridItemWidth = parseInt(this.getCSSVariable("--grid-unit-size"));
    const rowHeight = parseInt(this.getCSSVariable("--grid-row-size"));
    const updateGridItem: GridLayout.ItemCallback = (layout, oldItem, newItem) => {
      const startDateTime = this.getDateFromPositionInGrid(newItem.x);
      const endDateTime = this.getDateFromPositionInGrid(newItem.x + newItem.w);
      const shift = this.props.rowData[row].find(s => String(s.id) === newItem.i) as Shift;
      if (shift.startDateTime.getTime() === startDateTime.getTime() && shift.endDateTime.getTime() === endDateTime.getTime()) {
        return;
      }
      this.props.updateData({
        ...shift,
        startDateTime: startDateTime,
        endDateTime: endDateTime
      });
    }

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
          onDragStop={updateGridItem}
          onResizeStop={updateGridItem}
        >
          {
            this.props.rowData[row].map((data) => {
              const dataStartDate = this.props.dataGetStartDate(data);
              const dataEndDate = this.props.dataGetEndDate(data);
              
              const startPositionInGrid = this.getPositionInGrid(gridCount, dataStartDate);
              const endPositionInGrid =this.getPositionInGrid(gridCount, dataEndDate);

              return (
                <span
                  className="blob"
                  key={data.id}
                  style={{
                    backgroundColor: getShiftColor(data)
                  }}
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
                  <ShiftEvent
                    event={data}
                    title={this.props.dataToNameMap(data)}
                    onEdit={() => {}}
                    onDelete={() => {}}
                  />
                </span>
              );
            })
          }
        </GridLayout>
      </div>
    );
  }
}

interface State {
  paginationData: PaginationData;
}

export class ShiftRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onDateChange = this.onDateChange.bind(this);
    this.onSetPage = this.onSetPage.bind(this);
    this.onPerPageSelect = this.onPerPageSelect.bind(this);

    this.state = {
      paginationData: {
        pageNumber: 0,
        itemsPerPage: 10
      }
    };
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getShiftRoster({
      fromDate: startDate,
      toDate: endDate,
      pagination: this.state.paginationData
    });
  }

  onSetPage(event: any, page: number) {
    this.setState(prevState => ({ paginationData: { ...prevState.paginationData, pageNumber: page - 1 }}))
    this.props.getShiftRoster({
      fromDate: this.props.startDate as Date,
      toDate: this.props.endDate as Date,
      pagination: {
        ...this.state.paginationData,
        pageNumber: page - 1
      }
    });
  }

  onPerPageSelect(event: any, perPage: number) {
    this.setState(prevState => ({ paginationData: { ...prevState.paginationData, itemsPerPage: perPage }}))
    this.props.getShiftRoster({
      fromDate: this.props.startDate as Date,
      toDate: this.props.endDate as Date,
      pagination: {
        ...this.state.paginationData,
        itemsPerPage: perPage
      }
    });
  }

  render() {
    if (this.props.isLoading || this.props.spotList.length === 0) {
      return <div />;
    }

    const startDate = this.props.startDate as Date;
    const endDate = this.props.endDate as Date;

    return (
      <>
        <Level
          gutter="sm"
          style={{
            padding: "5px 5px 5px 5px",
            backgroundColor: "var(--pf-global--BackgroundColor--100)"
          }}
        >
          <LevelItem>
            <WeekPicker
              value={this.props.startDate as Date}
              onChange={this.onDateChange}
            />
          </LevelItem>
          <LevelItem style={{display: "flex"}}>
            <Button>Publish</Button>
            <Button>Schedule</Button>
            <Button>Refresh</Button>
            <Pagination
              itemCount={this.props.totalNumOfSpots}
              page={this.state.paginationData.pageNumber + 1}
              perPage={this.state.paginationData.itemsPerPage}
              onSetPage={this.onSetPage}
              onPerPageSelect={this.onPerPageSelect}
            />
            <Button>Create Shift</Button>
          </LevelItem>
        </Level>
        <ShiftSchedule
          startDate={startDate}
          endDate={endDate}
          rowTitles={this.props.spotList.map(spot => spot.name)}
          rowData={this.props.spotList.map(spot => this.props.spotIdToShiftListMap.get(spot.id as number) as Shift[])}
          dataToNameMap={s => (s.employee !== null)? s.employee.name : "Unassigned"}
          dataGetStartDate={s => s.startDateTime}
          dataGetEndDate={s => s.endDateTime}
          minDurationInMinutes={30}
          hourDividersInDay={8}
          updateData={this.props.updateShift}
        />
      </>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps, null, {
  areStatesEqual: (next, prev) => {
    if (next.shiftRoster.isLoading) {
      return true;
    }
    else {
      return next === prev;
    }
  }
})(ShiftRosterPage);