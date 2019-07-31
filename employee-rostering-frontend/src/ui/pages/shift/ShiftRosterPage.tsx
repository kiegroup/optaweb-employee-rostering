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
import React, { PropsWithChildren } from "react";
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
import EditShiftModal from './EditShiftModal';
import Color from 'color';
import TypeaheadSelectInput from "ui/components/TypeaheadSelectInput";
import { alert } from "store/alert";
import RosterState from "domain/RosterState";
import ShiftEvent, { getShiftColor } from "./ShiftEvent";

import 'react-big-calendar/lib/css/react-big-calendar.css';
import './ReactBigCalendarOverrides.css';
import { rebeccapurple } from "color-name";

interface StateProps {
  isSolving: boolean;
  isLoading: boolean;
  allSpotList: Spot[];
  shownSpotList: Spot[];
  spotIdToShiftListMap: Map<number, Shift[]>;
  startDate: Date | null;
  endDate: Date | null;
  totalNumOfSpots: number;
  rosterState: RosterState | null;
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  isSolving: state.solverState.isSolving,
  isLoading: state.shiftRoster.isLoading,
  allSpotList: spotSelectors.getSpotList(state),
  shownSpotList: rosterSelectors.getSpotListInShiftRoster(state),
  spotIdToShiftListMap: rosterSelectors.getSpotListInShiftRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForSpot(state, curr)),
    new Map<number, Shift[]>()),
  startDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.startDate).toDate() : null,
  endDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState
}); 
  
export interface DispatchProps {
  addShift: typeof shiftOperations.addShift;
  removeShift: typeof shiftOperations.removeShift;
  updateShift: typeof shiftOperations.updateShift;
  getShiftRosterFor: typeof rosterOperations.getShiftRosterFor;
  refreshShiftRoster: typeof rosterOperations.refreshShiftRoster;
  solveRoster: typeof rosterOperations.solveRoster;
  publishRoster: typeof rosterOperations.publish;
  terminateSolvingRosterEarly: typeof rosterOperations.terminateSolvingRosterEarly;
  showInfoMessage: typeof alert.showInfoMessage;
}
  
const mapDispatchToProps: DispatchProps = {
  addShift: shiftOperations.addShift,
  removeShift: shiftOperations.removeShift,
  updateShift: shiftOperations.updateShift,
  getShiftRosterFor: rosterOperations.getShiftRosterFor,
  refreshShiftRoster: rosterOperations.refreshShiftRoster,
  solveRoster: rosterOperations.solveRoster,
  publishRoster: rosterOperations.publish,
  terminateSolvingRosterEarly: rosterOperations.terminateSolvingRosterEarly,
  showInfoMessage: alert.showInfoMessage
}
  
export type Props = StateProps & DispatchProps;
interface State {
  isCreatingOrEditingShift: boolean;
  selectedShift?: Shift;
}

export function EventWrapper(props: PropsWithChildren<{
  event: Shift;
  style: React.CSSProperties;
}>): JSX.Element {
  const gridRowStart = parseInt(props.style.top as string) + 1;
  const gridRowEnd = parseInt(props.style.height as string) + gridRowStart;
  let className = "rbc-event";

  if (moment(props.event.endDateTime).get("date") !== moment(props.event.startDateTime).get("date")) {
    if (gridRowStart === 1) {
      className = className + " continues-from-previous-day";
    }
    if (gridRowEnd === 100) {
      className = className + " continues-next-day";
    }
  }

  return (
    <div
      className={className}
      style={{
        gridRowStart: gridRowStart,
        gridRowEnd: gridRowEnd,
        backgroundColor: "transparent",
        border: "none"
      }}
    >
      {props.children}
    </div>
  );
}

export class ShiftRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onDateChange = this.onDateChange.bind(this);
    this.onUpdateSpotList = this.onUpdateSpotList.bind(this);
    this.addShift = this.addShift.bind(this);
    this.deleteShift = this.deleteShift.bind(this);
    this.updateShift = this.updateShift.bind(this);
    this.getShiftStyle = this.getShiftStyle.bind(this);
    this.getDayStyle = this.getDayStyle.bind(this);
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


  deleteShift(deletedShift: Shift) {
    this.props.removeShift(deletedShift);
  }

  getShiftStyle(shift: Shift): { style: React.CSSProperties } {
    const color = getShiftColor(shift);
                
    if (this.props.rosterState !== null && moment(shift.startDateTime).isBefore(this.props.rosterState.firstDraftDate)) {
      // Published
      return {
        style: {
          border: "1px solid",
          backgroundColor: Color(color).saturate(-0.5).hex()
        }
      };
    }
    else {
      // Draft
      return {
        style: {
          backgroundColor: color,
          border: "1px dashed"
        } 
      };
    }
  }

  getDayStyle(date: Date): { className: string; style: React.CSSProperties } {
    if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      return {
        className: "published-day",
        style: {
          backgroundColor: "var(--pf-global--BackgroundColor--300)"
        }
      }
    }
    else {
      return {
        className: "draft-day",
        style: {
          backgroundColor: "var(--pf-global--BackgroundColor--100)"
        }
      }
    }
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
              aria-label="Select Week to View"
              value={this.props.startDate as Date}
              onChange={this.onDateChange}
            />
            <TypeaheadSelectInput
              aria-label="Select Spot"
              emptyText="Select Spot"
              optionToStringMap={spot => spot.name}
              options={this.props.allSpotList}
              defaultValue={this.props.shownSpotList[0]}
              onChange={this.onUpdateSpotList}
            />
          </LevelItem>
          <LevelItem style={{display: "flex"}}>
            <Button
              style={{margin: "5px"}}
              aria-label="Publish"
              onClick={this.props.publishRoster}
            >
              Publish
            </Button>
            {(!this.props.isSolving &&
              (
                <Button
                  style={{margin: "5px"}}
                  aria-label="Solve"
                  onClick={this.props.solveRoster}
                >
                  Schedule
                </Button>
              )) || (
              <Button
                style={{margin: "5px"}}
                aria-label="Terminate Early"
                onClick={this.props.terminateSolvingRosterEarly}
              >
                Terminate Early
              </Button>
            )
            }
            <Button
              style={{margin: "5px"}}
              aria-label="Refresh"
              onClick={() => {
                this.props.refreshShiftRoster();
                this.props.showInfoMessage("shiftRosterRefresh");
              }
              }
            >
              Refresh
            </Button>
            <Button
              style={{margin: "5px"}}
              aria-label="Create Shift"
              onClick={() => {
                this.setState({
                  selectedShift: undefined,
                  isCreatingOrEditingShift: true
                })
              }}
            >
              Create Shift
            </Button>
          </LevelItem>
        </Level>
        <div style={{
          height: "calc(100% - 60px)"
        }}
        >
          <EditShiftModal
            aria-label="Edit Shift"
            isOpen={this.state.isCreatingOrEditingShift}
            shift={this.state.selectedShift}
            onDelete={(shift) => {
              this.deleteShift(shift);
              this.setState({ isCreatingOrEditingShift: false });
            }
            }
            onSave={shift => {
              if (this.state.selectedShift !== undefined) {
                this.updateShift(shift);
              }
              else {
                this.addShift(shift);
              }
              this.setState({ isCreatingOrEditingShift: false });
            }}
            onClose={() => this.setState({ isCreatingOrEditingShift: false })}
          />
          <Title size="md">{spot.name}</Title>
          <div style={{
            height: "calc(100% - 20px)"
          }}
          >
            <Calendar
              className="rbc-no-allday-cell"
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
              onView={() => {}}
              onNavigate={() => {}}
              timeslots={4}
              eventPropGetter={this.getShiftStyle}
              dayPropGetter={this.getDayStyle}
              selectable
              showMultiDayTimes
              components={{
                eventWrapper: (params) => EventWrapper(params as any),
                event: (props) => ShiftEvent(
                  {
                    ...props,
                    onEdit: () => {
                      this.setState({
                        selectedShift: props.event,
                        isCreatingOrEditingShift: true
                      })
                    },
                    onDelete: () => {
                      this.deleteShift(props.event)
                    }
                  })
              }}
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