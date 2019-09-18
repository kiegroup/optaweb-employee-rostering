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
import Shift from 'domain/Shift';
import { AppState } from 'store/types';
import { rosterOperations, rosterSelectors } from 'store/roster';
import { spotSelectors } from 'store/spot';
import { connect } from 'react-redux';
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import {
  Level, LevelItem, Button, EmptyState, EmptyStateVariant, Title, EmptyStateIcon, EmptyStateBody,
} from '@patternfly/react-core';
import EditShiftModal from '../shift/EditShiftModal';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import RosterState from 'domain/RosterState';
import ShiftEvent, { ShiftPopupHeader, ShiftPopupBody } from '../shift/ShiftEvent';

import EmployeeAvailability from 'domain/EmployeeAvailability';
import { employeeSelectors } from 'store/employee';
import Employee from 'domain/Employee';
import { availabilityOperations } from 'store/availability';
import { shiftOperations } from 'store/shift';
import EditAvailabilityModal from './EditAvailabilityModal';
import AvailabilityEvent, { AvailabilityPopoverHeader, AvailabilityPopoverBody } from './AvailabilityEvent';
import Schedule, { StyleSupplier } from "ui/components/calendar/Schedule";
import { CubesIcon } from '@patternfly/react-icons';
import {
  withRouter, RouteComponentProps,
} from 'react-router-dom';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';

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

// Snapshot of the last value to show when loading
let lastEmployeeIdToShiftListMap: Map<number, Shift[]> = new Map<number, Shift[]>();
let lastEmployeeIdToAvailabilityListMap:
Map<number, EmployeeAvailability[]> = new Map<number, EmployeeAvailability[]>();
let lastShownEmployeeList: Employee[] = [];

const mapStateToProps = (state: AppState): StateProps => ({
  isSolving: state.solverState.isSolving,
  isLoading: rosterSelectors.isLoading(state),
  allEmployeeList: employeeSelectors.getEmployeeList(state),
  shownEmployeeList: lastShownEmployeeList = rosterSelectors.isLoading(state)
    ? lastShownEmployeeList : rosterSelectors.getEmployeeListInAvailabilityRoster(state),
  employeeIdToShiftListMap: lastEmployeeIdToShiftListMap = rosterSelectors
    .getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForEmployee(state, curr)),
    rosterSelectors.isLoading(state) ? lastEmployeeIdToShiftListMap : new Map<number, Shift[]>()),
  employeeIdToAvailabilityListMap: lastEmployeeIdToAvailabilityListMap = rosterSelectors
    .getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getAvailabilityListForEmployee(state, curr)),
    rosterSelectors.isLoading(state) ? lastEmployeeIdToAvailabilityListMap
      : new Map<number, EmployeeAvailability[]>()),
  startDate: (state.availabilityRoster.availabilityRosterView)
    ? moment(state.availabilityRoster.availabilityRosterView.startDate).toDate() : null,
  endDate: (state.availabilityRoster.availabilityRosterView)
    ? moment(state.availabilityRoster.availabilityRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState,
});

export interface DispatchProps {
  addEmployeeAvailability: typeof availabilityOperations.addEmployeeAvailability;
  removeEmployeeAvailability: typeof availabilityOperations.removeEmployeeAvailability;
  updateEmployeeAvailability: typeof availabilityOperations.updateEmployeeAvailability;
  getAvailabilityRosterFor: typeof rosterOperations.getAvailabilityRosterFor;
  refreshAvailabilityRoster: typeof rosterOperations.refreshAvailabilityRoster;
  getInitialAvailabilityRoster: typeof rosterOperations.getInitialAvailabilityRoster;
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
  getInitialAvailabilityRoster: rosterOperations.getInitialAvailabilityRoster,
  solveRoster: rosterOperations.solveRoster,
  publishRoster: rosterOperations.publish,
  terminateSolvingRosterEarly: rosterOperations.terminateSolvingRosterEarly,
  showInfoMessage: alert.showInfoMessage,
  addShift: shiftOperations.addShift,
  updateShift: shiftOperations.updateShift,
  removeShift: shiftOperations.removeShift,
}

export type Props = RouteComponentProps & StateProps & DispatchProps & WithTranslation;
interface State {
  selectedAvailability?: EmployeeAvailability;
  isCreatingOrEditingAvailability: boolean;
  isCreatingOrEditingShift: boolean;
  selectedShift?: Shift;
}

export interface ShiftOrAvailability {
  type: 'Shift'|'Availability';
  start: Date;
  end: Date;
  reference: Shift|EmployeeAvailability;
}

export function isShift(shiftOrAvailability: Shift|EmployeeAvailability): shiftOrAvailability is Shift {
  return 'spot' in shiftOrAvailability;
}

export function isAvailability(shiftOrAvailability: Shift|EmployeeAvailability):
shiftOrAvailability is EmployeeAvailability {
  return !isShift(shiftOrAvailability);
}

export function isDay(start: Date, end: Date) {
  return start.getHours() === 0 && start.getMinutes() === 0
    && end.getHours() === 0 && end.getMinutes() === 0
}

export function isAllDayAvailability(ea: EmployeeAvailability) {
  return isDay(ea.startDateTime, ea.endDateTime);
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
      isCreatingOrEditingAvailability: false,
    };
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getAvailabilityRosterFor({
      fromDate: startDate,
      toDate: endDate,
      employeeList: this.props.shownEmployeeList,
    });
  }

  onUpdateEmployeeList(employee: Employee|undefined) {
    if (employee) {
      this.props.getAvailabilityRosterFor({
        fromDate: this.props.startDate as Date,
        toDate: this.props.endDate as Date,
        employeeList: [employee],
      });
    }
  }

  getEventStyle: StyleSupplier<ShiftOrAvailability> = (soa) => {
    const style: React.CSSProperties = {};
    if (isAvailability(soa.reference)) {
      switch (soa.reference.state) {
        case 'DESIRED': {
          style.backgroundColor = 'green';
          break;
        }
        case 'UNDESIRED': {
          style.backgroundColor = 'yellow';
          break;
        }
        case 'UNAVAILABLE': {
          style.backgroundColor = 'red';
          break;
        }
      }
    }

    if (this.props.rosterState !== null && moment(soa.start).isBefore(this.props.rosterState.firstDraftDate)) {
      style.border = '1px solid';
    } else {
      style.border = '1px dashed';
    }

    return { style };
  }

  getDayStyle: (availabilities: EmployeeAvailability[]) => StyleSupplier<Date> = (availabilities) =>
    (date) => {
      let className = "";
      const style: React.CSSProperties = {};
      const dayAvailability = availabilities.find(ea => 
        !moment(ea.startDateTime).isAfter(date) && moment(date).isBefore(ea.endDateTime))
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
    const { t, tReady } = this.props;
    if (!tReady) {
      return (<></>);
    }
    if (this.props.shownEmployeeList.length <= 0) {
      if (!this.props.isLoading && this.props.allEmployeeList.length > 0) {
        this.props.getInitialAvailabilityRoster();
      }
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={CubesIcon} />
          <Trans
            i18nKey="noEmployeesAvailability"
            components={[
              <Title key={0} headingLevel="h5" size="lg" />,
              <EmptyStateBody key={1} />,
              <Button
                key={2}
                aria-label="Employees Page"
                variant="primary"
                onClick={() => this.props.history.push('/employees')}
              />
            ]}
          />
        </EmptyState>
      );
    }

    const startDate = this.props.startDate as Date;
    const endDate = this.props.endDate as Date;
    const shownEmployee = this.props.shownEmployeeList[0];
    const events: ShiftOrAvailability[] = [];

    if (this.props.employeeIdToAvailabilityListMap.get(shownEmployee.id as number) !== undefined) {
      (this.props.employeeIdToAvailabilityListMap.get(shownEmployee.id as number) as EmployeeAvailability[])
        .forEach((ea) => {
          events.push({
            type: 'Availability',
            start: ea.startDateTime,
            end: ea.endDateTime,
            reference: ea,
          })
        });
    }

    if (this.props.employeeIdToShiftListMap.get(shownEmployee.id as number) !== undefined) {
      (this.props.employeeIdToShiftListMap.get(shownEmployee.id as number) as Shift[]).forEach((shift) => {
        events.push({
          type: 'Shift',
          start: shift.startDateTime,
          end: shift.endDateTime,
          reference: shift,
        })
      });
    }
    return (
      <>
        <Level
          gutter="sm"
          style={{
            height: '60px',
            padding: '5px 5px 5px 5px',
            backgroundColor: 'var(--pf-global--BackgroundColor--100)',
          }}
        >
          <LevelItem style={{ display: 'flex' }}>
            <TypeaheadSelectInput
              aria-label="Select Employee"
              emptyText={t("selectEmployee")}
              optionToStringMap={employee => employee.name}
              options={this.props.allEmployeeList}
              value={this.props.shownEmployeeList[0]}
              onChange={this.onUpdateEmployeeList}
            />
            <WeekPicker
              aria-label="Select Week to View"
              value={this.props.startDate as Date}
              onChange={this.onDateChange}
            />
          </LevelItem>
          <LevelItem style={{ display: 'flex' }}>
            <Button
              style={{ margin: '5px' }}
              aria-label="Publish"
              onClick={this.props.publishRoster}
            >
              {t("publish")}
            </Button>
            {(!this.props.isSolving
              && (
                <Button
                  style={{ margin: '5px' }}
                  aria-label="Solve"
                  onClick={this.props.solveRoster}
                >
                  {t("schedule")}
                </Button>
              )) || (
              <Button
                style={{ margin: '5px' }}
                aria-label="Terminate Early"
                onClick={this.props.terminateSolvingRosterEarly}
              >
                {t("terminateEarly")}
              </Button>
            )
            }
            <Button
              style={{ margin: '5px' }}
              aria-label="Refresh"
              onClick={() => {
                this.props.refreshAvailabilityRoster();
                this.props.showInfoMessage('availabilityRosterRefresh');
              }
              }
            >
              {t("refresh")}
            </Button>
            <Button
              style={{ margin: '5px' }}
              aria-label="Create Availability"
              onClick={() => {
                if (!this.state.isCreatingOrEditingShift) {
                  this.setState({
                    selectedAvailability: undefined,
                    isCreatingOrEditingAvailability: true,
                  })
                }
              }}
            >
              {t("createAvailability")}
            </Button>
          </LevelItem>
        </Level>
        <EditAvailabilityModal
          availability={this.state.selectedAvailability}
          isOpen={this.state.isCreatingOrEditingAvailability}
          onSave={(availability) => {
            if (this.state.selectedAvailability !== undefined) {
              this.props.updateEmployeeAvailability(availability);
            } else {
              this.props.addEmployeeAvailability(availability);
            }
            this.setState({ selectedAvailability: undefined, isCreatingOrEditingAvailability: false });
          }}
          onDelete={(availability) => {
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
          onSave={(shift) => {
            if (this.state.selectedShift !== undefined) {
              this.props.updateShift(shift);
            } else {
              this.props.addShift(shift);
            }
            this.setState({ selectedShift: undefined, isCreatingOrEditingShift: false });
          }}
          onClose={() => this.setState({ selectedShift: undefined, isCreatingOrEditingShift: false })}
        />
        <Schedule<ShiftOrAvailability>
          showAllDayCell
          key={shownEmployee.id}
          startDate={startDate}
          endDate={endDate}
          events={events}
          titleAccessor={soa => (isShift(soa.reference) ? soa.reference.spot.name : soa.reference.state)}
          startAccessor={soa => soa.start}
          endAccessor={soa => soa.end}
          addEvent={
            (start, end) => {
              this.props.addEmployeeAvailability({
                tenantId: shownEmployee.tenantId,
                startDateTime: start,
                endDateTime: end,
                employee: shownEmployee,
                state: 'UNAVAILABLE',
              });
            }
          }
          eventStyle={this.getEventStyle}
          dayStyle={this.getDayStyle(
            (this.props.employeeIdToAvailabilityListMap
              .get(shownEmployee.id as number) as EmployeeAvailability[])
              .filter(isAllDayAvailability))}
          wrapperStyle={event => ({
            className: (isAvailability(event.reference))
              ? (isAllDayAvailability(event.reference)
                ? 'availability-allday-wrapper' : 'availability-wrapper')
              : undefined,
            style: {
              zIndex: (isShift(event.reference))? 1 : 0
            }
          })}
          popoverHeader={
            soa => ((isShift(soa.reference)) ? ShiftPopupHeader({
              shift: soa.reference,
              onEdit: () => {
                if (!this.state.isCreatingOrEditingAvailability) {
                  this.setState({
                    selectedShift: soa.reference as Shift,
                    isCreatingOrEditingShift: true,
                  })
                }
              },
              onDelete: () => {
                this.props.updateShift({
                  ...soa.reference as Shift,
                  employee: null,
                })
              },
            }) : AvailabilityPopoverHeader({
              availability: soa.reference,
              onEdit: ea => this.setState({
                isCreatingOrEditingAvailability: true,
                selectedAvailability: ea,
              }),
              onDelete: ea => this.props.removeEmployeeAvailability(ea),
              updateEmployeeAvailability: this.props.updateEmployeeAvailability,
              removeEmployeeAvailability: this.props.removeEmployeeAvailability,
            }))
          }
          popoverBody={
            soa => ((isShift(soa.reference)) ? ShiftPopupBody(soa.reference) : AvailabilityPopoverBody)
          }
          eventComponent={props => (isShift(props.event.reference) ? ShiftEvent(
            {
              ...props.event.reference,
              title: props.event.reference.spot.name,
              event: props.event.reference,
            },
          ) : AvailabilityEvent({
            availability: props.event.reference,
            onEdit: ea => this.setState({
              isCreatingOrEditingAvailability: true,
              selectedAvailability: ea,
            }),
            onDelete: ea => this.props.removeEmployeeAvailability(ea),
            updateEmployeeAvailability: this.props.updateEmployeeAvailability,
            removeEmployeeAvailability: this.props.removeEmployeeAvailability,
          }))
          }
        />
      </>
    );
  }
}

export default withTranslation()(connect(mapStateToProps, mapDispatchToProps)(withRouter(AvailabilityRosterPage)));
