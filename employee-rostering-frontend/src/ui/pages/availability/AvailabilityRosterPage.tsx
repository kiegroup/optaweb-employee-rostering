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
import EditShiftModal from '../shift/EditShiftModal';
import Color from 'color';
import TypeaheadSelectInput from "ui/components/TypeaheadSelectInput";
import { alert } from "store/alert";
import RosterState from "domain/RosterState";
import ShiftEvent, { getShiftColor } from "../shift/ShiftEvent";

import 'react-big-calendar/lib/css/react-big-calendar.css';
import '../shift/ReactBigCalendarOverrides.css';
import EmployeeAvailability from "domain/EmployeeAvailability";
import { employeeSelectors } from "store/employee";
import Employee from "domain/Employee";
import { availabilityOperations } from "store/availability";

interface StateProps {
  isSolving: boolean;
  isLoading: boolean;
  allEmployeeList: Employee[];
  shownEmployeeList: Employee[];
  employeeIdToShiftListMap: Map<number, Shift[]>;
  employeeIdToAvailabilityListMap: Map<number, EmployeeAvailability[]>;
  startDate: Date | null;
  endDate: Date | null;
  totalNumOfSpots: number;
  rosterState: RosterState | null;
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  isSolving: state.solverState.isSolving,
  isLoading: state.availabilityRoster.isLoading,
  allEmployeeList: employeeSelectors.getEmployeeList(state),
  shownEmployeeList: rosterSelectors.getEmployeeListInAvailabilityRoster(state),
  employeeIdToShiftListMap: rosterSelectors.getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForEmployee(state, curr)),
    new Map<number, Shift[]>()),
  employeeIdToAvailabilityListMap: rosterSelectors.getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getAvailabilityListForEmployee(state, curr)),
    new Map<number, EmployeeAvailability[]>()),
  startDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.startDate).toDate() : null,
  endDate: (state.shiftRoster.shiftRosterView)? moment(state.shiftRoster.shiftRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState
}); 
  
export interface DispatchProps {
  addEmployeeAvailability: typeof availabilityOperations.addEmployeeAvailability;
  removeEmployeeAvailability: typeof availabilityOperations.removeEmployeeAvailability;
  updateEmployeeAvailability: typeof availabilityOperations.updateEmployeeAvailability;
  getAvailabilityRosterFor: typeof rosterOperations.getAvailabilityRosterFor;
  refreshAvailabilityRoster: typeof rosterOperations.refreshAvailabilityRoster;
  solveRoster: typeof rosterOperations.solveRoster;
  publishRoster: typeof rosterOperations.publish;
  terminateSolvingRosterEarly: typeof rosterOperations.terminateSolvingRosterEarly;
  showInfoMessage: typeof alert.showInfoMessage;
}
  
const mapDispatchToProps: DispatchProps = {
  addEmployeeAvailability: availabilityOperations.addEmployeeAvailability,
  removeEmployeeAvailability: availabilityOperations.removeEmployeeAvailability,
  updateEmployeeAvailability: availabilityOperations.updateEmployeeAvailability,
  getAvailabilityRosterFor: rosterOperations.getAvailabilityRosterFor,
  refreshAvailabilityRoster: rosterOperations.refreshAvailabilityRoster,
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

interface ShiftOrAvailability {
  type: "Shift"|"Availability";
  start: Date;
  end: Date;
  reference: Shift|EmployeeAvailability;
}

function isShift(shiftOrAvailability: Shift|EmployeeAvailability): shiftOrAvailability is Shift {
  return "spot" in shiftOrAvailability;
}

function isAvailability(shiftOrAvailability: Shift|EmployeeAvailability): shiftOrAvailability is EmployeeAvailability {
  return !isShift(shiftOrAvailability);
}

export function EventWrapper(props: PropsWithChildren<{
  event: ShiftOrAvailability;
  style: React.CSSProperties;
}>): JSX.Element {
  const gridRowStart = parseInt(props.style.top as string) + 1;
  const gridRowEnd = parseInt(props.style.height as string) + gridRowStart;
  let className = "rbc-event";
  let zIndex = 0;
  if (isAvailability(props.event.reference)) {
    className = "availability";
  }
  else {
    zIndex = 1;
  }
  return (
    <div
      className={className}
      style={{
        gridRowStart,
        gridRowEnd,
        backgroundColor: "transparent",
        border: "none",
        zIndex
      }}
    >
      {props.children}
    </div>
  );
}

const AvailabilityEvent: React.FC<EmployeeAvailability> = (ea: EmployeeAvailability) => {
  return (
    <span
      data-tip
      data-for={String(ea.id)}
      style={{
        display: "flex",
        height: "100%",
        width: "100%"
      }}
    >
      {ea.state}
    </span>
  );
}

export class AvailabilityRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onDateChange = this.onDateChange.bind(this);
    this.onUpdateEmployeeList = this.onUpdateEmployeeList.bind(this);
    this.getEventStyle = this.getEventStyle.bind(this);
    this.getDayStyle = this.getDayStyle.bind(this);
    this.state = {
      isCreatingOrEditingShift: false
    };
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getAvailabilityRosterFor({
      fromDate: startDate,
      toDate: endDate,
      employeeList: this.props.shownEmployeeList
    });
  }

  onUpdateEmployeeList(employee: Employee|undefined) {
    if (employee) {
      this.props.getAvailabilityRosterFor({
        fromDate: this.props.startDate as Date,
        toDate: this.props.endDate as Date,
        employeeList: [employee]
      });
    }
  }

  getEventStyle(soa: ShiftOrAvailability): { style: React.CSSProperties } {
    const style: React.CSSProperties = {};
    if (isAvailability(soa.reference)) {
      switch (soa.reference.state) {
        case "DESIRED": {
          style.backgroundColor = "green";
          break;
        }
        case "UNDESIRED": {
          style.backgroundColor = "yellow";
          break;
        }
        case "UNAVAILABLE": {
          style.backgroundColor = "red";
          break;
        }
      }
      return { style };
    }
    else {
      return {
        style: {
          border: "1px dashed"
        } 
      };
    }
  }

  getDayStyle(date: Date): { style: React.CSSProperties } {
    if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      return {
        style: {
          backgroundColor: "var(--pf-global--BackgroundColor--300)"
        }
      }
    }
    else {
      return {
        style: {
          backgroundColor: "var(--pf-global--BackgroundColor--100)"
        }
      }
    }
  }

  render() {
    if (this.props.isLoading || this.props.shownEmployeeList.length <= 0) {
      return <div />;
    }

    const startDate = this.props.startDate as Date;
    const endDate = this.props.endDate as Date;
    const localizer = momentLocalizer(moment);
    const employee = this.props.shownEmployeeList[0];
    const events: ShiftOrAvailability[] = [];

    if (this.props.employeeIdToAvailabilityListMap.get(employee.id as number) !== undefined) {
      (this.props.employeeIdToAvailabilityListMap.get(employee.id as number) as EmployeeAvailability[]).forEach(ea => {
        let start = ea.startDateTime;
        let end = ea.endDateTime;
        if (start.getHours() === 0 && start.getMinutes() === 0) {
          start = moment(ea.startDateTime).add(1, "ms").toDate();
        }
        if (end.getHours() === 0 && end.getMinutes() === 0) {
          end = moment(ea.endDateTime).subtract(1, "ms").toDate();
        }
        events.push({
          type: "Availability",
          start,
          end,
          reference: ea
        })
      });
    }

    if (this.props.employeeIdToShiftListMap.get(employee.id as number) !== undefined) {
      (this.props.employeeIdToShiftListMap.get(employee.id as number) as Shift[]).forEach(shift => {
        events.push({
          type: "Shift",
          start: shift.startDateTime,
          end: shift.endDateTime,
          reference: shift
        })
      });
    }
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
              options={this.props.allEmployeeList}
              defaultValue={this.props.shownEmployeeList[0]}
              onChange={this.onUpdateEmployeeList}
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
                this.props.refreshAvailabilityRoster();
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
              this.setState({ isCreatingOrEditingShift: false });
            }
            }
            onSave={shift => {
              this.setState({ isCreatingOrEditingShift: false });
            }}
            onClose={() => this.setState({ isCreatingOrEditingShift: false })}
          />
          <Title size="md">{employee.name}</Title>
          <div style={{
            height: "calc(100% - 20px)"
          }}
          >
            <Calendar
              key={employee.id}
              date={startDate}
              length={moment.duration(moment(startDate).to(moment(endDate))).asDays()}
              localizer={localizer}
              events={events}
              titleAccessor={soa => isShift(soa.reference)? soa.reference.spot.name : soa.reference.state}
              allDayAccessor={soa => false}
              startAccessor={soa => moment(soa.start).toDate()}
              endAccessor={soa => moment(soa.end).toDate()}
              toolbar={false}
              view="week"
              views={["week"]}
              onSelectSlot={(slotInfo: { start: string|Date; end: string|Date; action: "select"|"click"|"doubleClick" }) => {
                if (slotInfo.action === "select") {
                  this.props.addEmployeeAvailability({
                    tenantId: employee.tenantId,
                    startDateTime: moment(slotInfo.start).toDate(),
                    endDateTime: moment(slotInfo.end).toDate(),
                    employee: employee,
                    state: "UNAVAILABLE"
                  });
                }
              }
              }
              onView={() => {}}
              onNavigate={() => {}}
              timeslots={4}
              eventPropGetter={this.getEventStyle}
              dayPropGetter={this.getDayStyle}
              selectable
              showMultiDayTimes
              components={{
                eventWrapper: (params) => EventWrapper(params as any),
                event: (props) => isShift(props.event.reference)? ShiftEvent(
                  {
                    ...props.event.reference,
                    title: props.event.reference.spot.name,
                    event: props.event.reference,
                    onEdit: () => {
                    },
                    onDelete: () => {
                    }
                  }) : AvailabilityEvent(props.event.reference)
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
})(AvailabilityRosterPage);