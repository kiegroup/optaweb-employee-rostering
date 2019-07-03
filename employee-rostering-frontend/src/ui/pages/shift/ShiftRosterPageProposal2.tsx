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
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import { Level, LevelItem, Button, Pagination } from "@patternfly/react-core";
import { PaginationData } from "types";
import { Calendar, momentLocalizer } from 'react-big-calendar'
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop'
import './BigCalendarSchedule.css';

const DragAndDropCalendar = withDragAndDrop(Calendar);

interface StateProps {
  isLoading: boolean;
  spotList: Spot[];
  spotIdToShiftListMap: Map<number, Shift[]>;
  startDate: Date | null;
  endDate: Date | null;
  paginationData: PaginationData;
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
  paginationData: state.shiftRoster.pagination,
  totalNumOfSpots: spotSelectors.getSpotList(state).length
}); 
  
export interface DispatchProps {
  getShiftRoster: typeof rosterOperations.getShiftRoster;
}
  
const mapDispatchToProps: DispatchProps = {
  getShiftRoster: rosterOperations.getShiftRoster
};
  
export type Props = StateProps & DispatchProps;

export class ShiftRosterPage extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    this.onDateChange = this.onDateChange.bind(this);
    this.onSetPage = this.onSetPage.bind(this);
    this.onPerPageSelect = this.onPerPageSelect.bind(this);
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getShiftRoster({
      fromDate: startDate,
      toDate: endDate,
      pagination: this.props.paginationData
    });
  }

  onSetPage(event: any, page: number) {
    this.props.getShiftRoster({
      fromDate: this.props.startDate as Date,
      toDate: this.props.endDate as Date,
      pagination: {
        ...this.props.paginationData,
        pageNumber: page - 1
      }
    });
  }

  onPerPageSelect(event: any, perPage: number) {
    this.props.getShiftRoster({
      fromDate: this.props.startDate as Date,
      toDate: this.props.endDate as Date,
      pagination: {
        ...this.props.paginationData,
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
    const shifts = Array.from(this.props.spotIdToShiftListMap.values())
      .reduce((prev, curr) => prev.concat(curr));
    const localizer = momentLocalizer(moment);

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
              page={this.props.paginationData.pageNumber + 1}
              perPage={this.props.paginationData.itemsPerPage}
              onSetPage={this.onSetPage}
              onPerPageSelect={this.onPerPageSelect}
            />
            <Button>Create Shift</Button>
          </LevelItem>
        </Level>
        <div style={{
          height: "85%"
        }}
        >
          <DragAndDropCalendar
            date={startDate}
            length={moment.duration(moment(startDate).to(moment(endDate))).asDays()}
            localizer={localizer}
            events={shifts.map(shift => ({
              ...shift,
              start: shift.startDateTime,
              end: shift.endDateTime
            }))}
            titleAccessor={shift => shift.employee? shift.employee.name : "Unassigned"}
            allDayAccessor={shift => false}
            resources={this.props.spotList}
            resourceIdAccessor="id"
            resourceTitleAccessor="name"
            resourceAccessor={shift => shift.spot.id}
            startAccessor={shift => moment(shift.startDateTime).toDate()}
            endAccessor={shift => moment(shift.endDateTime).toDate()}
            toolbar={false}
            view="week"
            views={["week"]}
            selectable
            showMultiDayTimes
          />
        </div>
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