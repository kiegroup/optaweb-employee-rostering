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
import { AppState } from "store/types";
import { rosterOperations, rosterSelectors } from "store/roster";
import { spotSelectors } from "store/spot";
import { connect } from 'react-redux';
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import { Level, LevelItem, Button, Title, Split, SplitItem } from "@patternfly/react-core";
import { Calendar, momentLocalizer } from 'react-big-calendar'
import EditShiftModal from '../shift/EditShiftModal';
import TypeaheadSelectInput from "ui/components/TypeaheadSelectInput";
import { alert } from "store/alert";
import RosterState from "domain/RosterState";
import ShiftEvent from "../shift/ShiftEvent";

import 'react-big-calendar/lib/css/react-big-calendar.css';
import '../shift/ReactBigCalendarOverrides.css';
import EmployeeAvailability from "domain/EmployeeAvailability";
import { employeeSelectors } from "store/employee";
import Employee from "domain/Employee";
import { availabilityOperations } from "store/availability";
import { OkIcon, WarningTriangleIcon, ErrorCircleOIcon, TrashIcon } from "@patternfly/react-icons";
import { shiftOperations } from "store/shift";
import { useTranslation } from "react-i18next";
import EditAvailabilityModal from "./EditAvailabilityModal";

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
  startDate: (state.availabilityRoster.availabilityRosterView)? moment(state.availabilityRoster.availabilityRosterView.startDate).toDate() : null,
  endDate: (state.availabilityRoster.availabilityRosterView)? moment(state.availabilityRoster.availabilityRosterView.endDate).toDate() : null,
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
  addShift: typeof shiftOperations.addShift;
  updateShift: typeof shiftOperations.updateShift;
  removeShift: typeof shiftOperations.removeShift;
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
  showInfoMessage: alert.showInfoMessage,
  addShift: shiftOperations.addShift,
  updateShift: shiftOperations.updateShift,
  removeShift: shiftOperations.removeShift
}
  
export type Props = StateProps & DispatchProps;
interface State {
  selectedAvailability?: EmployeeAvailability;
  isCreatingOrEditingAvailability: boolean;
  isCreatingOrEditingShift: boolean;
  selectedShift?: Shift;
}

export interface ShiftOrAvailability {
  type: "Shift"|"Availability";
  start: Date;
  end: Date;
  reference: Shift|EmployeeAvailability;
}

export function isShift(shiftOrAvailability: Shift|EmployeeAvailability): shiftOrAvailability is Shift {
  return "spot" in shiftOrAvailability;
}

export function isAvailability(shiftOrAvailability: Shift|EmployeeAvailability): shiftOrAvailability is EmployeeAvailability {
  return !isShift(shiftOrAvailability);
}

export function isDay(start: Date, end: Date) {
  return start.getHours() === 0 && start.getMinutes() === 0 &&
    end.getHours() === 0 && end.getMinutes() === 0
}

export function isAllDayAvailability(ea: EmployeeAvailability) {
  return isDay(ea.startDateTime, ea.endDateTime);
}

export function EventWrapper(props: PropsWithChildren<{
  event: ShiftOrAvailability;
  style: React.CSSProperties;
}>): JSX.Element {
  if (!props.style) {
    return (
      <div
        className="availability-allday-wrapper"
      >
        {props.children}
      </div>
    );
  }

  const gridRowStart = parseInt(props.style.top as string) + 1;
  const gridRowEnd = parseInt(props.style.height as string) + gridRowStart;
  let className = "rbc-event";
  let zIndex = 0;

  if (isAvailability(props.event.reference)) {
    className = "availability-wrapper";
  }
  else {
    zIndex = 1;
  }

  if (moment(props.event.end).get("date") !== moment(props.event.start).get("date")) {
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

interface AvailabilityEventProps {
  availability: EmployeeAvailability;
  updateEmployeeAvailability: (ea: EmployeeAvailability) => void;
  removeEmployeeAvailability: (ea: EmployeeAvailability) => void;
}

const AvailabilityEvent: React.FC<AvailabilityEventProps> = (props: AvailabilityEventProps) => {
  const { t } = useTranslation();
  return (
    <span
      data-tip
      data-for={String(props.availability.id)}
      className="availability-event"

    >
      <Split>
        <SplitItem isFilled={false}>{t("EmployeeAvailabilityState." + props.availability.state)}</SplitItem>
        <SplitItem isFilled />
        <SplitItem isFilled={false}>
          <Button
            onClick={() => props.removeEmployeeAvailability(props.availability)}
            variant="danger"
          >
            <TrashIcon />
          </Button>
        </SplitItem>
      </Split>
      <Level gutter="sm">
        <LevelItem>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "DESIRED"
            })}
            style={{
              backgroundColor: "green",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <OkIcon />
          </Button>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "UNDESIRED"
            })}
            style={{
              backgroundColor: "yellow",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <WarningTriangleIcon />
          </Button>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "UNAVAILABLE"
            })}
            style={{
              backgroundColor: "red",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <ErrorCircleOIcon />
          </Button>
        </LevelItem>
      </Level>
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
      isCreatingOrEditingShift: false,
      isCreatingOrEditingAvailability: false
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
    }

    if (this.props.rosterState !== null && moment(soa.start).isBefore(this.props.rosterState.firstDraftDate)) {
      style.border = "1px solid";
    }
    else {
      style.border = "1px dashed";
    }

    return { style };
  }

  getDayStyle(date: Date, availabilities: EmployeeAvailability[]): { className: string; style: React.CSSProperties } {
    let className = "";
    const style: React.CSSProperties = {};
    const dayAvailability = availabilities.find(ea => !moment(ea.startDateTime).isAfter(date) && moment(date).isBefore(ea.endDateTime))
    if (dayAvailability !== undefined) {
      switch (dayAvailability.state) {
        case "DESIRED": {
          className = "desired";
          break;
        }
        case "UNDESIRED": {
          className = "undesired";
          break;
        }
        case "UNAVAILABLE": {
          className = "unavailable";
          break;
        }
      }
    }
    if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      if (!className) {
        style.backgroundColor = "var(--pf-global--BackgroundColor--300)";
      }
      className = className + " published-day";
    }
    else {
      if (!className) {
        style.backgroundColor = "var(--pf-global--BackgroundColor--100)"
      }
      className = className + " draft-day";
    }

    return { className: className.trim() , style };
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
              aria-label="Select Employee"
              emptyText="Select Employee"
              optionToStringMap={employee => employee.name}
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
                this.props.showInfoMessage("availabilityRosterRefresh");
              }
              }
            >
              Refresh
            </Button>
            <Button
              style={{margin: "5px"}}
              aria-label="Create Availability"
              onClick={() => {
                if (!this.state.isCreatingOrEditingShift) {
                  this.setState({
                    selectedAvailability: undefined,
                    isCreatingOrEditingAvailability: true
                  })
                }
              }}
            >
              Create Availability
            </Button>
          </LevelItem>
        </Level>
        <div style={{
          height: "calc(100% - 60px)"
        }}
        >
          <EditAvailabilityModal
            availability={this.state.selectedAvailability}
            isOpen={this.state.isCreatingOrEditingAvailability}
            onSave={availability => {
              if (this.state.selectedAvailability !== undefined) {
                this.props.updateEmployeeAvailability(availability);
              }
              else {
                this.props.addEmployeeAvailability(availability);
              }
              this.setState({ selectedAvailability: undefined, isCreatingOrEditingAvailability: false });
            }}
            onDelete={availability => {
              this.props.removeEmployeeAvailability(availability);
              this.setState({ isCreatingOrEditingAvailability: false });
            }}
            onClose={() => this.setState({ selectedAvailability: undefined, isCreatingOrEditingAvailability: false })}
          />
          <EditShiftModal
            aria-label="Edit Shift"
            isOpen={this.state.isCreatingOrEditingShift}
            shift={this.state.selectedShift}
            onDelete={(shift) => {
              this.props.removeShift(shift);
              this.setState({ selectedShift: undefined, isCreatingOrEditingShift: false });
            }
            }
            onSave={shift => {
              if (this.state.selectedShift !== undefined) {
                this.props.updateShift(shift);
              }
              else {
                this.props.addShift(shift);
              }
              this.setState({ selectedShift: undefined, isCreatingOrEditingShift: false });
            }}
            onClose={() => this.setState({ selectedShift: undefined, isCreatingOrEditingShift: false })}
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
              allDayAccessor={soa => isAvailability(soa.reference)? isAllDayAvailability(soa.reference) : false}
              startAccessor={soa => moment(soa.start).toDate()}
              endAccessor={soa => moment(soa.end).toDate()}
              toolbar={false}
              view="week"
              views={["week"]}
              onSelectSlot={(slotInfo: { start: string|Date; end: string|Date; action: "select"|"click"|"doubleClick" }) => {
                if (slotInfo.action === "select" || (slotInfo.action === "click" && isDay(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate()))) {
                  if (isDay(moment(slotInfo.start).toDate(), moment(slotInfo.end).toDate())) {
                    this.props.addEmployeeAvailability({
                      tenantId: employee.tenantId,
                      startDateTime: moment(slotInfo.start).toDate(),
                      endDateTime: moment(slotInfo.end).add(1, "day").toDate(),
                      employee: employee,
                      state: "UNAVAILABLE"
                    });
                  }
                  else {
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
              }
              onView={() => {}}
              onNavigate={() => {}}
              timeslots={4}
              eventPropGetter={this.getEventStyle}
              dayPropGetter={day => this.getDayStyle(day, (this.props.employeeIdToAvailabilityListMap.get(employee.id as number) as EmployeeAvailability[]).filter(isAllDayAvailability))}
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
                      if (!this.state.isCreatingOrEditingAvailability) {
                        this.setState({
                          selectedShift: props.event.reference as Shift,
                          isCreatingOrEditingShift: true
                        })
                      }
                    },
                    onDelete: () => {
                      this.props.updateShift({
                        ...props.event.reference as Shift,
                        employee: null
                      })
                    }
                  }) : AvailabilityEvent({
                  availability: props.event.reference,
                  updateEmployeeAvailability: this.props.updateEmployeeAvailability,
                  removeEmployeeAvailability: this.props.removeEmployeeAvailability
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
    if (rosterSelectors.isLoading(next)) {
      return true;
    }
    else {
      return next === prev;
    }
  }
})(AvailabilityRosterPage);