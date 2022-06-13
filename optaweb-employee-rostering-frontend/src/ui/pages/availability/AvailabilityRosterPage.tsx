import React from 'react';
import { Shift } from 'domain/Shift';
import { AppState } from 'store/types';
import { rosterOperations, rosterSelectors } from 'store/roster';
import { spotSelectors } from 'store/spot';
import { connect } from 'react-redux';
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import { Button, EmptyState, EmptyStateVariant, Title, EmptyStateIcon, EmptyStateBody } from '@patternfly/react-core';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import { RosterState } from 'domain/RosterState';

import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { employeeSelectors } from 'store/employee';
import { Employee } from 'domain/Employee';
import { availabilityOperations } from 'store/availability';
import { shiftOperations } from 'store/shift';
import Schedule, { StyleSupplier } from 'ui/components/calendar/Schedule';
import { CubesIcon } from '@patternfly/react-icons';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';
import Actions from 'ui/components/Actions';
import { HardMediumSoftScore } from 'domain/HardMediumSoftScore';
import { ScoreDisplay } from 'ui/components/ScoreDisplay';
import { UrlProps, setPropsInUrl, getPropsFromUrl } from 'util/BookmarkableUtils';
import { IndictmentSummary } from 'domain/indictment/IndictmentSummary';
import AvailabilityEvent, { AvailabilityPopoverHeader, AvailabilityPopoverBody } from './AvailabilityEvent';
import EditAvailabilityModal from './EditAvailabilityModal';
import ShiftEvent, { ShiftPopupHeader, ShiftPopupBody } from '../shift/ShiftEvent';
import EditShiftModal from '../shift/EditShiftModal';

interface StateProps {
  tenantId: number;
  isSolving: boolean;
  isLoading: boolean;
  allEmployeeList: Employee[];
  shownEmployeeList: Employee[];
  employeeIdToShiftListMap: Map<number, Shift[]>;
  employeeIdToAvailabilityListMap: Map<number, EmployeeAvailability[]>;
  totalNumOfSpots: number;
  rosterState: RosterState | null;
  score: HardMediumSoftScore | null;
  indictmentSummary: IndictmentSummary | null;
}

// Snapshot of the last value to show when loading
let lastEmployeeIdToShiftListMap: Map<number, Shift[]> = new Map<number, Shift[]>();
let lastEmployeeIdToAvailabilityListMap:
Map<number, EmployeeAvailability[]> = new Map<number, EmployeeAvailability[]>();
let lastShownEmployeeList: Employee[] = [];

// We use assignments to update the memoize values
// eslint-disable-next-line no-return-assign
const mapStateToProps = (state: AppState): StateProps => ({
  tenantId: state.tenantData.currentTenantId,
  isSolving: state.solverState.solverStatus !== 'NOT_SOLVING',
  isLoading: rosterSelectors.isAvailabilityRosterLoading(state),
  allEmployeeList: employeeSelectors.getEmployeeList(state),
  shownEmployeeList: lastShownEmployeeList = rosterSelectors.isAvailabilityRosterLoading(state)
    ? lastShownEmployeeList : rosterSelectors.getEmployeeListInAvailabilityRoster(state),
  employeeIdToShiftListMap: lastEmployeeIdToShiftListMap = rosterSelectors
    .getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForEmployee(state, curr)),
    rosterSelectors.isAvailabilityRosterLoading(state) ? lastEmployeeIdToShiftListMap : new Map<number, Shift[]>()),
  employeeIdToAvailabilityListMap: lastEmployeeIdToAvailabilityListMap = rosterSelectors
    .getEmployeeListInAvailabilityRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getAvailabilityListForEmployee(state, curr)),
    rosterSelectors.isAvailabilityRosterLoading(state) ? lastEmployeeIdToAvailabilityListMap
      : new Map<number, EmployeeAvailability[]>()),
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState,
  score: state.availabilityRoster.availabilityRosterView ? state.availabilityRoster.availabilityRosterView.score : null,
  indictmentSummary: state.availabilityRoster.availabilityRosterView
    ? state.availabilityRoster.availabilityRosterView.indictmentSummary : null,
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
  removeShift: shiftOperations.removeShift,
};

export type Props = RouteComponentProps & StateProps & DispatchProps & WithTranslation;
interface State {
  selectedAvailability?: EmployeeAvailability;
  isCreatingOrEditingAvailability: boolean;
  isCreatingOrEditingShift: boolean;
  selectedShift?: Shift;
  firstLoad: boolean;
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
    && end.getHours() === 0 && end.getMinutes() === 0;
}

export function isAllDayAvailability(ea: EmployeeAvailability) {
  return isDay(ea.startDateTime, ea.endDateTime);
}

export type AvailabilityRosterUrlProps = UrlProps<'employee'|'week'>;
export class AvailabilityRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onUpdateAvailabilityRoster = this.onUpdateAvailabilityRoster.bind(this);
    this.getEventStyle = this.getEventStyle.bind(this);
    this.getDayStyle = this.getDayStyle.bind(this);
    this.state = {
      isCreatingOrEditingShift: false,
      isCreatingOrEditingAvailability: false,
      firstLoad: true,
    };
  }

  onUpdateAvailabilityRoster(urlProps: AvailabilityRosterUrlProps) {
    if (this.props.rosterState !== null) {
      const startDate = moment(urlProps.week || moment(this.props.rosterState.firstDraftDate)).startOf('week').toDate();
      const endDate = moment(startDate).endOf('week').toDate();
      const employee = this.props.allEmployeeList
        .find(e => e.name === urlProps.employee) || this.props.allEmployeeList[0];
      if (employee) {
        this.props.getAvailabilityRosterFor({
          fromDate: startDate,
          toDate: endDate,
          employeeList: [employee],
        });
        this.setState({ firstLoad: false });
        setPropsInUrl(this.props, { ...urlProps, employee: employee.name });
      }
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
        default:
          throw new Error(`Unexpected availability state: ${soa.reference.state}`);
      }
    }

    if (this.props.rosterState !== null && moment(soa.start).isBefore(this.props.rosterState.firstDraftDate)) {
      style.border = '1px solid';
    } else {
      style.border = '1px dashed';
    }

    return { style };
  }

  getDayStyle: (availabilities: EmployeeAvailability[]) => StyleSupplier<Date> = availabilities => (date) => {
    let className = '';
    const style: React.CSSProperties = {};
    const dayAvailability = availabilities.find(ea => !moment(ea.startDateTime).isAfter(date)
      && moment(date).isBefore(ea.endDateTime));
    if (dayAvailability !== undefined) {
      switch (dayAvailability.state) {
        case 'DESIRED': {
          className = 'desired';
          break;
        }
        case 'UNDESIRED': {
          className = 'undesired';
          break;
        }
        case 'UNAVAILABLE': {
          className = 'unavailable';
          break;
        }
        default:
          throw new Error(`Unexpected availability state: ${dayAvailability.state}`);
      }
    }
    if (this.props.rosterState !== null && moment(date).isBefore(moment(moment().startOf('day')))) {
      if (!className) {
        style.backgroundColor = '#d3d7cf';
      }
      className += ' historic-day';
    } else if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      if (!className) {
        style.backgroundColor = 'var(--pf-global--BackgroundColor--300)';
      }
      className += ' published-day';
    } else {
      if (!className) {
        style.backgroundColor = 'var(--pf-global--BackgroundColor--100)';
      }
      className += ' draft-day';
    }

    return { className: className.trim(), style };
  }

  componentDidMount() {
    const urlProps = getPropsFromUrl<AvailabilityRosterUrlProps>(this.props, {
      employee: null,
      week: null,
    });
    this.onUpdateAvailabilityRoster(urlProps);
  }

  componentDidUpdate(prevProps: Props) {
    const urlProps = getPropsFromUrl<AvailabilityRosterUrlProps>(this.props, {
      employee: null,
      week: null,
    });
    if (this.state.firstLoad || prevProps.tenantId !== this.props.tenantId || urlProps.employee === null) {
      this.onUpdateAvailabilityRoster(urlProps);
    }
  }


  render() {
    const { t, tReady } = this.props;
    if (!tReady) {
      return (<></>);
    }
    const urlProps = getPropsFromUrl<AvailabilityRosterUrlProps>(this.props, {
      employee: null,
      week: null,
    });
    const changedTenant = this.props.shownEmployeeList.length === 0
      || (urlProps.employee !== null
      && this.props.tenantId !== (this.props.shownEmployeeList[0]).tenantId);

    if (this.props.shownEmployeeList.length === 0 || changedTenant || this.props.rosterState === null) {
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={CubesIcon} />
          <Trans
            t={t}
            i18nKey="noEmployees"
            components={[
              <Title key={0} headingLevel="h5" size="lg" />,
              <EmptyStateBody key={1} />,
              <Button
                key={2}
                aria-label="Employees Page"
                variant="primary"
                onClick={() => this.props.history.push(`/${this.props.tenantId}/employees`)}
              />,
            ]}
          />
        </EmptyState>
      );
    }

    const startDate = moment(urlProps.week || moment(this.props.rosterState.firstDraftDate)).startOf('week').toDate();
    const endDate = moment(startDate).endOf('week').toDate();
    const shownEmployee = this.props.allEmployeeList.find(e => e.name === urlProps.employee)
      || this.props.shownEmployeeList[0];
    const score: HardMediumSoftScore = this.props.score || { hardScore: 0, mediumScore: 0, softScore: 0 };
    const indictmentSummary: IndictmentSummary = this.props.indictmentSummary || { constraintToCountMap: {},
      constraintToScoreImpactMap: {} };
    const events: ShiftOrAvailability[] = [];
    const actions = [
      { name: t('publish'), action: this.props.publishRoster, isDisabled: this.props.isSolving },
      { name: this.props.isSolving ? t('terminateEarly') : t('schedule'),
        action: this.props.isSolving ? this.props.terminateSolvingRosterEarly : this.props.solveRoster },
      { name: t('refresh'),
        action: () => {
          this.props.refreshAvailabilityRoster();
          this.props.showInfoMessage('availabilityRosterRefresh');
        } },
      { name: t('createAvailability'),
        action: () => {
          if (!this.state.isCreatingOrEditingShift) {
            this.setState({
              selectedAvailability: undefined,
              isCreatingOrEditingAvailability: true,
            });
          }
        } },
    ];

    if (this.props.employeeIdToAvailabilityListMap.get(shownEmployee.id as number) !== undefined) {
      (this.props.employeeIdToAvailabilityListMap.get(shownEmployee.id as number) as EmployeeAvailability[])
        .forEach((ea) => {
          events.push({
            type: 'Availability',
            start: ea.startDateTime,
            end: ea.endDateTime,
            reference: ea,
          });
        });
    }

    if (this.props.employeeIdToShiftListMap.get(shownEmployee.id as number) !== undefined) {
      (this.props.employeeIdToShiftListMap.get(shownEmployee.id as number) as Shift[]).forEach((shift) => {
        events.push({
          type: 'Shift',
          start: shift.startDateTime,
          end: shift.endDateTime,
          reference: shift,
        });
      });
    }
    return (
      <>
        <span
          style={{
            display: 'grid',
            height: '60px',
            padding: '5px 5px 5px 5px',
            gridTemplateColumns: 'auto auto auto 1fr',
            backgroundColor: 'var(--pf-global--BackgroundColor--100)',
          }}
        >
          <TypeaheadSelectInput
            aria-label="Select Employee"
            emptyText={t('selectEmployee')}
            optionToStringMap={employee => employee.name}
            options={this.props.allEmployeeList}
            value={shownEmployee}
            onChange={(e) => {
              this.onUpdateAvailabilityRoster({
                ...urlProps,
                employee: e ? e.name : null,
              });
            }}
            noClearButton
          />
          <WeekPicker
            aria-label="Select Week to View"
            value={startDate}
            onChange={(weekStart) => {
              this.onUpdateAvailabilityRoster({
                ...urlProps,
                week: moment(weekStart).format('YYYY-MM-DD'),
              });
            }}
          />
          <ScoreDisplay score={score} indictmentSummary={indictmentSummary} isSolving={this.props.isSolving} />
          <Actions
            actions={actions}
          />
        </span>
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
          onAddEvent={
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
          onUpdateEvent={
            (event, start, end) => {
              if (isAvailability(event.reference)) {
                this.props.updateEmployeeAvailability({
                  ...event.reference,
                  startDateTime: start,
                  endDateTime: end,
                });
              } else {
                this.props.showInfoMessage('editShiftsInShiftRoster');
              }
            }
          }
          eventStyle={this.getEventStyle}
          dayStyle={this.getDayStyle(
            (this.props.employeeIdToAvailabilityListMap
              .get(shownEmployee.id as number) || [])
              .filter(isAllDayAvailability),
          )}
          wrapperStyle={event => ({
            // eslint-disable-next-line no-nested-ternary
            className: (isAvailability(event.reference))
              ? (isAllDayAvailability(event.reference)
                ? 'availability-allday-wrapper' : 'availability-wrapper')
              : undefined,
            style: {
              zIndex: (isShift(event.reference)) ? 1 : 0,
            },
          })}
          popoverHeader={
            soa => ((isShift(soa.reference)) ? ShiftPopupHeader({
              shift: soa.reference,
              onEdit: () => {
                if (!this.state.isCreatingOrEditingAvailability) {
                  this.setState({
                    selectedShift: soa.reference as Shift,
                    isCreatingOrEditingShift: true,
                  });
                }
              },
              onDelete: () => {
                this.props.updateShift({
                  ...soa.reference as Shift,
                  employee: null,
                });
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
            soa => ((isShift(soa.reference)) ? ShiftPopupBody(soa.reference) : AvailabilityPopoverBody({}))
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

export default withTranslation('AvailabilityRosterPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(AvailabilityRosterPage)),
);
