import React from 'react';
import { Shift } from 'domain/Shift';
import { Spot } from 'domain/Spot';
import { AppState } from 'store/types';
import { shiftOperations } from 'store/shift';
import { rosterOperations, rosterSelectors } from 'store/roster';
import { spotSelectors } from 'store/spot';
import { connect } from 'react-redux';
import WeekPicker from 'ui/components/WeekPicker';
import moment from 'moment';
import { Button, EmptyState, EmptyStateVariant, Title, EmptyStateBody, EmptyStateIcon } from '@patternfly/react-core';
import Color from 'color';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import { RosterState } from 'domain/RosterState';
import Schedule, { StyleSupplier } from 'ui/components/calendar/Schedule';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { CubesIcon } from '@patternfly/react-icons';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';
import 'ui/components/TypeaheadSelectInput.css';
import Actions from 'ui/components/Actions';
import { HardMediumSoftScore } from 'domain/HardMediumSoftScore';
import { ScoreDisplay } from 'ui/components/ScoreDisplay';
import { getPropsFromUrl, setPropsInUrl, UrlProps } from 'util/BookmarkableUtils';
import { IndictmentSummary } from 'domain/indictment/IndictmentSummary';
import ShiftEvent, { getShiftColor, ShiftPopupHeader, ShiftPopupBody } from './ShiftEvent';
import EditShiftModal from './EditShiftModal';
import ExportScheduleModal from './ExportScheduleModal';

interface StateProps {
  tenantId: number;
  isSolving: boolean;
  isLoading: boolean;
  allSpotList: Spot[];
  shownSpotList: Spot[];
  spotIdToShiftListMap: Map<number, Shift[]>;
  totalNumOfSpots: number;
  rosterState: RosterState | null;
  score: HardMediumSoftScore | null;
  indictmentSummary: IndictmentSummary | null;
}

export type ShiftRosterUrlProps = UrlProps<'spot'|'week'>;
// Snapshot of the last value to show when loading
let lastSpotIdToShiftListMap: Map<number, Shift[]> = new Map<number, Shift[]>();
let lastShownSpotList: Spot[] = [];

// eslint-disable-next-line no-return-assign
const mapStateToProps = (state: AppState): StateProps => ({
  tenantId: state.tenantData.currentTenantId,
  isSolving: state.solverState.solverStatus !== 'NOT_SOLVING',
  isLoading: rosterSelectors.isShiftRosterLoading(state),
  allSpotList: spotSelectors.getSpotList(state),
  // The use of "x = isLoading? x : getUpdatedData()" is a way to use old value if data is still loading
  shownSpotList: lastShownSpotList = rosterSelectors.isShiftRosterLoading(state) ? lastShownSpotList
    : rosterSelectors.getSpotListInShiftRoster(state),
  spotIdToShiftListMap: lastSpotIdToShiftListMap = rosterSelectors.getSpotListInShiftRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForSpot(state, curr)),
    // reducing an empty array returns the starting value
    rosterSelectors.isShiftRosterLoading(state) ? lastSpotIdToShiftListMap : new Map<number, Shift[]>()),
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState,
  score: state.shiftRoster.shiftRosterView ? state.shiftRoster.shiftRosterView.score : null,
  indictmentSummary: state.shiftRoster.shiftRosterView ? state.shiftRoster.shiftRosterView.indictmentSummary : null,
});

export interface DispatchProps {
  addShift: typeof shiftOperations.addShift;
  removeShift: typeof shiftOperations.removeShift;
  updateShift: typeof shiftOperations.updateShift;
  getShiftRosterFor: typeof rosterOperations.getShiftRosterFor;
  refreshShiftRoster: typeof rosterOperations.refreshShiftRoster;
  replanRoster: typeof rosterOperations.replanRoster;
  commitChanges: typeof rosterOperations.commitChanges;
  terminateSolvingRosterEarly: typeof rosterOperations.terminateSolvingRosterEarly;
  showInfoMessage: typeof alert.showInfoMessage;
}

const mapDispatchToProps: DispatchProps = {
  addShift: shiftOperations.addShift,
  removeShift: shiftOperations.removeShift,
  updateShift: shiftOperations.updateShift,
  getShiftRosterFor: rosterOperations.getShiftRosterFor,
  refreshShiftRoster: rosterOperations.refreshShiftRoster,
  replanRoster: rosterOperations.replanRoster,
  commitChanges: rosterOperations.commitChanges,
  terminateSolvingRosterEarly: rosterOperations.terminateSolvingRosterEarly,
  showInfoMessage: alert.showInfoMessage,
};

export type Props = RouteComponentProps & StateProps & DispatchProps & WithTranslation;
interface State {
  isCreatingOrEditingShift: boolean;
  isExportingSchedule: boolean;
  selectedShift?: Shift;
  firstLoad: boolean;
}

export class ShiftRosterPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.onUpdateShiftRoster = this.onUpdateShiftRoster.bind(this);
    this.addShift = this.addShift.bind(this);
    this.deleteShift = this.deleteShift.bind(this);
    this.updateShift = this.updateShift.bind(this);
    this.getShiftStyle = this.getShiftStyle.bind(this);
    this.getDayStyle = this.getDayStyle.bind(this);
    this.state = {
      isCreatingOrEditingShift: false,
      isExportingSchedule: false,
      firstLoad: true,
    };
  }

  onUpdateShiftRoster(urlProps: ShiftRosterUrlProps) {
    if (this.props.rosterState) {
      const spot = this.props.allSpotList.find(s => s.name === urlProps.spot)
          || (this.props.allSpotList[0] /* can be undefined */);
      const startDate = moment(urlProps.week || new Date()).startOf('week').toDate();
      const endDate = moment(startDate).endOf('week').toDate();

      if (spot) {
        this.props.getShiftRosterFor({
          fromDate: startDate,
          toDate: endDate,
          spotList: [spot],
        });
        this.setState({ firstLoad: false });
        setPropsInUrl(this.props, { ...urlProps, spot: spot.name });
      }
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

  getShiftStyle: StyleSupplier<Shift> = (shift) => {
    const color = getShiftColor(shift);

    if (this.props.rosterState !== null
      && moment(shift.startDateTime).isBefore(this.props.rosterState.firstDraftDate)) {
      // Published
      return {
        style: {
          border: '1px solid',
          backgroundColor: Color(color).hex(),
        },
      };
    }

    // Draft
    return {
      style: {
        backgroundColor: color,
        opacity: 0.3,
        border: '1px dashed',
      },
    };
  }

  getDayStyle: StyleSupplier<Date> = (date) => {
    if (this.props.rosterState !== null && moment(date).isBefore(moment().startOf('day'))) {
      return {
        className: 'historic-day',
        style: {
          backgroundColor: '#d3d7cf',
        },
      };
    }

    if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      return {
        className: 'published-day',
        style: {
          backgroundColor: 'var(--pf-global--BackgroundColor--300)',
        },
      };
    }

    return {
      className: 'draft-day',
      style: {
        backgroundColor: 'var(--pf-global--BackgroundColor--100)',
      },
    };
  }

  componentDidMount() {
    const urlProps = getPropsFromUrl<ShiftRosterUrlProps>(this.props, {
      spot: null,
      week: null,
    });
    this.onUpdateShiftRoster(urlProps);
  }

  componentDidUpdate(prevProps: Props) {
    const urlProps = getPropsFromUrl<ShiftRosterUrlProps>(this.props, {
      spot: null,
      week: null,
    });
    if (this.state.firstLoad || prevProps.tenantId !== this.props.tenantId || urlProps.spot === null) {
      this.onUpdateShiftRoster(urlProps);
    }
  }

  render() {
    const { t } = this.props;
    const urlProps = getPropsFromUrl<ShiftRosterUrlProps>(this.props, {
      spot: null,
      week: null,
    });
    const changedTenant = this.props.shownSpotList.length === 0
      || this.props.tenantId !== (this.props.shownSpotList[0]).tenantId;

    if (this.props.shownSpotList.length === 0 || this.state.firstLoad
        || changedTenant || this.props.rosterState === null) {
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={CubesIcon} />
          <Trans
            t={t}
            i18nKey="noSpots"
            components={[
              <Title headingLevel="h5" size="lg" key={0} />,
              <EmptyStateBody key={1} />,
              <Button
                key={2}
                aria-label="Wards Page"
                variant="primary"
                onClick={() => this.props.history.push(`/${this.props.tenantId}/wards`)}
              />,
            ]}
          />
        </EmptyState>
      );
    }

    const startDate = moment(urlProps.week || new Date()).startOf('week').toDate();
    const endDate = moment(startDate).endOf('week').toDate();
    const shownSpot = this.props.allSpotList.find(s => s.name === urlProps.spot)
                      || (this.props.shownSpotList[0]);
    const score: HardMediumSoftScore = this.props.score || { hardScore: 0, mediumScore: 0, softScore: 0 };
    const indictmentSummary: IndictmentSummary = this.props.indictmentSummary
       || { constraintToCountMap: {}, constraintToScoreImpactMap: {} };
    const actions = [
      { name: t('commitChanges'), action: this.props.commitChanges, isDisabled: this.props.isSolving },
      { name: this.props.isSolving ? t('terminateEarly') : t('reschedule'),
        action: this.props.isSolving ? this.props.terminateSolvingRosterEarly : this.props.replanRoster },
      { name: t('refresh'),
        action: () => {
          this.props.refreshShiftRoster();
          this.props.showInfoMessage('shiftRosterRefresh');
        } },
      { name: t('createShift'),
        action: () => {
          this.setState({
            selectedShift: undefined,
            isCreatingOrEditingShift: true,
          });
        } },
      {
        name: t('exportToExcel'),
        action: () => {
          this.setState({
            isExportingSchedule: true,
          });
        },
        isDisabled: this.props.isSolving,
      },
    ];

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
            aria-label="Select Spot"
            emptyText={t('selectSpot')}
            optionToStringMap={spot => spot.name}
            options={this.props.allSpotList}
            value={shownSpot}
            onChange={(s) => {
              this.onUpdateShiftRoster({
                ...urlProps,
                spot: s ? s.name : null,
              });
            }}
            noClearButton
          />
          <WeekPicker
            aria-label="Select Week to View"
            value={startDate}
            minDate={this.props.rosterState ? this.props.rosterState.lastHistoricDate : undefined}
            maxDate={this.props.rosterState ? this.props.rosterState.firstDraftDate : undefined}
            onChange={(weekStart) => {
              this.onUpdateShiftRoster({
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
        <EditShiftModal
          aria-label="Edit Shift"
          isOpen={this.state.isCreatingOrEditingShift}
          shift={this.state.selectedShift}
          onDelete={(shift) => {
            this.deleteShift(shift);
            this.setState({ isCreatingOrEditingShift: false });
          }
          }
          onSave={(shift) => {
            if (this.state.selectedShift !== undefined) {
              this.updateShift(shift);
            } else {
              this.addShift(shift);
            }
            this.setState({ isCreatingOrEditingShift: false });
          }}
          onClose={() => this.setState({ isCreatingOrEditingShift: false })}
        />
        <ExportScheduleModal
          isOpen={this.state.isExportingSchedule}
          onClose={() => {
            this.setState({ isExportingSchedule: false });
          }}
          defaultFromDate={startDate}
          defaultToDate={endDate}
        />
        <Schedule<Shift>
          key={(this.props.shownSpotList[0]).id}
          startDate={startDate}
          endDate={endDate}
          events={this.props.spotIdToShiftListMap.get(shownSpot.id as number) || []}
          titleAccessor={shift => (shift.employee ? shift.employee.name : t('unassigned'))}
          startAccessor={shift => moment(shift.startDateTime).toDate()}
          endAccessor={shift => moment(shift.endDateTime).toDate()}
          onAddEvent={
            (start, end) => {
              this.addShift({
                tenantId: shownSpot.tenantId,
                startDateTime: start,
                endDateTime: end,
                spot: shownSpot,
                employee: null,
                rotationEmployee: null,
                pinnedByUser: false,
                originalEmployee: null,
                requiredSkillSet: [],
              });
            }
          }
          onUpdateEvent={
            (shift, start, end) => {
              this.updateShift({
                ...shift,
                startDateTime: start,
                endDateTime: end,
              });
            }
          }
          eventStyle={this.getShiftStyle}
          dayStyle={this.getDayStyle}
          wrapperStyle={() => ({})}
          popoverHeader={shift => ShiftPopupHeader({
            shift,
            onEdit: editedShift => this.setState({
              isCreatingOrEditingShift: true,
              selectedShift: editedShift,
            }),
            onCopy: copiedShift => this.addShift({
              ...copiedShift,
              employee: null,
              id: undefined,
              version: undefined,
            }),
            onDelete: deletedShift => this.deleteShift(deletedShift),
          })}
          popoverBody={shift => ShiftPopupBody(shift)}
          eventComponent={props => ShiftEvent(props)}
        />
      </>
    );
  }
}

export default withTranslation('ShiftRosterPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(ShiftRosterPage)),
);
