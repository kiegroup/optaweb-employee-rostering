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
import { shiftOperations } from "store/shift"; 
import { rosterOperations, rosterSelectors } from "store/roster";
import { spotSelectors } from "store/spot";
import { connect } from 'react-redux';
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import { Level, LevelItem, Button, Title } from "@patternfly/react-core";
import { Calendar, momentLocalizer } from 'react-big-calendar'
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop'
import EditShiftModal from './EditShiftModal';
import './BigCalendarSchedule.css';

import 'react-big-calendar/lib/addons/dragAndDrop/styles.scss'
import TypeaheadSelectInput from "ui/components/TypeaheadSelectInput";
import { updateShift } from "store/shift/operations";

const DragAndDropCalendar = withDragAndDrop(Calendar);

interface StateProps {
  isLoading: boolean;
  allSpotList: Spot[];
  shownSpotList: Spot[];
  spotIdToShiftListMap: Map<number, Shift[]>;
  startDate: Date | null;
  endDate: Date | null;
  totalNumOfSpots: number;
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  isLoading: state.shiftRoster.isLoading,
  allSpotList: spotSelectors.getSpotList(state),
  shownSpotList: rosterSelectors.getSpotListInShiftRoster(state),
  spotIdToShiftListMap: rosterSelectors.getSpotListInShiftRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForSpot(state, curr)),
    new Map<number, Shift[]>()),
  startDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.startDate).toDate() : null,
  endDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length
}); 
  
export interface DispatchProps {
  addShift: typeof shiftOperations.addShift;
  removeShift: typeof shiftOperations.removeShift;
  updateShift: typeof shiftOperations.updateShift;
  getShiftRosterFor: typeof rosterOperations.getShiftRosterFor;
}
  
const mapDispatchToProps: DispatchProps = {
  addShift: shiftOperations.addShift,
  removeShift: shiftOperations.removeShift,
  updateShift: shiftOperations.updateShift,
  getShiftRosterFor: rosterOperations.getShiftRosterFor
};
  
export type Props = StateProps & DispatchProps;
interface State {
  isCreatingOrEditingShift: boolean;
  selectedShift?: Shift;
}

export class ShiftRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onDateChange = this.onDateChange.bind(this);
    this.onUpdateSpotList = this.onUpdateSpotList.bind(this);
    this.addShift = this.addShift.bind(this);
    this.updateShift = this.updateShift.bind(this);
    this.state = {
      isCreatingOrEditingShift: false
    };
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getShiftRosterFor({
      fromDate: startDate,
      toDate: endDate,
      spotList: this.props.shownSpotList
    });
  }

  onUpdateSpotList(spot: Spot|undefined) {
    if (spot) {
      this.props.getShiftRosterFor({
        fromDate: this.props.startDate as Date,
        toDate: this.props.endDate as Date,
        spotList: [spot]
      });
    }
  }

  addShift(addedShift: Shift) {
    this.props.addShift(addedShift);
  }

  updateShift(updatedShift: Shift) {
    this.props.updateShift(updatedShift);
  }

  render() {
    if (this.props.isLoading || this.props.shownSpotList.length <= 0) {
      return <div />;
    }

    const startDate = this.props.startDate as Date;
    const endDate = this.props.endDate as Date;
    const localizer = momentLocalizer(moment);
    const spot = this.props.shownSpotList[0];

    return (
      <>
        <Level
          gutter="sm"
          style={{
            height: "60px",
            padding: "5px 5px 5px 5px",
            backgroundColor: "var(--pf-global--BackgroundColor--100)"
          }}
        >
          <LevelItem style={{display: "flex"}}>
            <WeekPicker
              value={this.props.startDate as Date}
              onChange={this.onDateChange}
            />
            <TypeaheadSelectInput
              emptyText="Select Spots"
              optionToStringMap={spot => spot.name}
              options={this.props.allSpotList}
              defaultValue={this.props.shownSpotList[0]}
              onChange={this.onUpdateSpotList}
            />
          </LevelItem>
          <LevelItem style={{display: "flex"}}>
            <Button>Publish</Button>
            <Button>Schedule</Button>
            <Button>Refresh</Button>
            <Button>Create Shift</Button>
          </LevelItem>
        </Level>
        <div style={{
          height: "calc(100% - 60px)"
        }}
        >
          <EditShiftModal
            isOpen={this.state.isCreatingOrEditingShift}
            shift={this.state.selectedShift}
            onSave={shift => {
              this.updateShift(shift);
              this.setState({ isCreatingOrEditingShift: false });
            }}
            onClose={() => this.setState({ isCreatingOrEditingShift: false })}
          />
          <Title size="md">{spot.name}</Title>
          <div style={{
            height: "calc(100% - 20px)"
          }}
          >
            <DragAndDropCalendar
              key={spot.id}
              date={startDate}
              length={moment.duration(moment(startDate).to(moment(endDate))).asDays()}
              localizer={localizer}
              events={(this.props.spotIdToShiftListMap.get(spot.id as number) as Shift[]) as any[]}
              titleAccessor={shift => shift.employee? shift.employee.name : "Unassigned"}
              allDayAccessor={shift => false}
              startAccessor={shift => moment(shift.startDateTime).toDate()}
              endAccessor={shift => moment(shift.endDateTime).toDate()}
              toolbar={false}
              view="week"
              views={["week"]}
              onSelectSlot={(slotInfo: { start: string|Date; end: string|Date; action: "select"|"click"|"doubleClick" }) => {
                if (slotInfo.action === "select") {
                  this.addShift({
                    tenantId: spot.tenantId,
                    startDateTime: moment(slotInfo.start).toDate(),
                    endDateTime: moment(slotInfo.end).toDate(),
                    spot: spot,
                    employee: null,
                    rotationEmployee: null,
                    pinnedByUser: false
                  });
                }
              }
              }
              onEventDrop={(args: {event: Shift; start: string|Date; end: string|Date}) =>
                this.updateShift({...args.event,
                  startDateTime: moment(args.start).toDate(),
                  endDateTime: moment(args.end).toDate()
                })
              }
              onEventResize={(args: {event: Shift; start: string|Date; end: string|Date}) =>
                this.updateShift({...args.event,
                  startDateTime: moment(args.start).toDate(),
                  endDateTime: moment(args.end).toDate()
                })
              }
              onView={() => {}}
              onNavigate={() => {}} 
              onSelectEvent={shift => {
                this.setState({
                  selectedShift: shift,
                  isCreatingOrEditingShift: true
                })
              }}
              timeslots={4}
              selectable
              resizable
              showMultiDayTimes
            />
          </div>
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