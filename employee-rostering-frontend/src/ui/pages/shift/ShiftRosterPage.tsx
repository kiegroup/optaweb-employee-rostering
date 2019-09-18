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
import {
  Level, LevelItem, Button, EmptyState, EmptyStateVariant, Title, EmptyStateBody, EmptyStateIcon,
} from '@patternfly/react-core';
import EditShiftModal from './EditShiftModal';
import Color from 'color';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import RosterState from 'domain/RosterState';
import ShiftEvent, { getShiftColor, ShiftPopupHeader, ShiftPopupBody } from './ShiftEvent';
import Schedule, { StyleSupplier } from 'ui/components/calendar/Schedule';
import {
  withRouter, RouteComponentProps,
} from 'react-router-dom'
import { CubesIcon } from '@patternfly/react-icons';
import { withTranslation, WithTranslation, Trans } from 'react-i18next';

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

// Snapshot of the last value to show when loading
let lastSpotIdToShiftListMap: Map<number, Shift[]> = new Map<number, Shift[]>();
let lastShownSpotList: Spot[] = [];

const mapStateToProps = (state: AppState): StateProps => ({
  isSolving: state.solverState.isSolving,
  isLoading: rosterSelectors.isLoading(state),
  allSpotList: spotSelectors.getSpotList(state),
  // The use of "x = isLoading? x : getUpdatedData()" is a way to use old value if data is still loading
  shownSpotList: lastShownSpotList = rosterSelectors.isLoading(state) ? lastShownSpotList
    : rosterSelectors.getSpotListInShiftRoster(state),
  spotIdToShiftListMap: lastSpotIdToShiftListMap = rosterSelectors.getSpotListInShiftRoster(state)
    .reduce((prev, curr) => prev.set(curr.id as number,
      rosterSelectors.getShiftListForSpot(state, curr)),
    // reducing an empty array returns the starting value
    rosterSelectors.isLoading(state) ? lastSpotIdToShiftListMap : new Map<number, Shift[]>()),
  startDate: (state.shiftRoster.shiftRosterView) ? moment(state.shiftRoster.shiftRosterView.startDate).toDate() : null,
  endDate: (state.shiftRoster.shiftRosterView) ? moment(state.shiftRoster.shiftRosterView.endDate).toDate() : null,
  totalNumOfSpots: spotSelectors.getSpotList(state).length,
  rosterState: state.rosterState.rosterState,
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
  getInitialShiftRoster: typeof rosterOperations.getInitialShiftRoster;
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
  showInfoMessage: alert.showInfoMessage,
  getInitialShiftRoster: rosterOperations.getInitialShiftRoster,
}

export type Props = RouteComponentProps & StateProps & DispatchProps & WithTranslation;
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
    this.deleteShift = this.deleteShift.bind(this);
    this.updateShift = this.updateShift.bind(this);
    this.getShiftStyle = this.getShiftStyle.bind(this);
    this.getDayStyle = this.getDayStyle.bind(this);
    this.state = {
      isCreatingOrEditingShift: false,
    };
  }

  onDateChange(startDate: Date, endDate: Date) {
    this.props.getShiftRosterFor({
      fromDate: startDate,
      toDate: endDate,
      spotList: this.props.shownSpotList,
    });
  }

  onUpdateSpotList(spot: Spot|undefined) {
    if (spot) {
      this.props.getShiftRosterFor({
        fromDate: this.props.startDate as Date,
        toDate: this.props.endDate as Date,
        spotList: [spot],
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

  getShiftStyle: StyleSupplier<Shift> = (shift) => {
    const color = getShiftColor(shift);

    if (this.props.rosterState !== null
      && moment(shift.startDateTime).isBefore(this.props.rosterState.firstDraftDate)) {
      // Published
      return {
        style: {
          border: '1px solid',
          backgroundColor: Color(color).saturate(-0.5).hex(),
        },
      };
    }

    // Draft
    return {
      style: {
        backgroundColor: color,
        border: '1px dashed',
      },
    };
  }

  getDayStyle: StyleSupplier<Date> = (date) => {
    if (this.props.rosterState !== null && moment(date).isBefore(this.props.rosterState.firstDraftDate)) {
      return {
        className: 'published-day',
        style: {
          backgroundColor: 'var(--pf-global--BackgroundColor--300)',
        },
      }
    }

    return {
      className: 'draft-day',
      style: {
        backgroundColor: 'var(--pf-global--BackgroundColor--100)',
      },
    }
  }

  render() {
    const { t } = this.props;
    if (this.props.shownSpotList.length <= 0) {
      if (!this.props.isLoading && this.props.allSpotList.length > 0) {
        this.props.getInitialShiftRoster();
      }
      return (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={CubesIcon} />
          <Trans
            i18nKey="noSpotsShift"
            components={[
              <Title headingLevel="h5" size="lg" key={0} />,
              <EmptyStateBody key={1} />,
              <Button
                key={2}
                aria-label="Spots Page"
                variant="primary"
                onClick={() => this.props.history.push('/spots')}
              />
            ]}
          />
        </EmptyState>
      );
    }

    const startDate = this.props.startDate as Date;
    const endDate = this.props.endDate as Date;
    const shownSpot = this.props.shownSpotList[0];
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
              aria-label="Select Spot"
              emptyText={t("selectSpot")}
              optionToStringMap={spot => spot.name}
              options={this.props.allSpotList}
              value={this.props.shownSpotList[0]}
              onChange={this.onUpdateSpotList}
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
                this.props.refreshShiftRoster();
                this.props.showInfoMessage('shiftRosterRefresh');
              }
              }
            >
              {t("refresh")}
            </Button>
            <Button
              style={{ margin: '5px' }}
              aria-label="Create Shift"
              onClick={() => {
                this.setState({
                  selectedShift: undefined,
                  isCreatingOrEditingShift: true,
                })
              }}
            >
              {t("createShift")}
            </Button>
          </LevelItem>
        </Level>
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
        <Schedule<Shift>
          key={shownSpot.id}
          startDate={startDate}
          endDate={endDate}
          events={this.props.spotIdToShiftListMap.get(shownSpot.id as number) as Shift[]}
          titleAccessor={shift => (shift.employee ? shift.employee.name : t("unassigned"))}
          startAccessor={shift => moment(shift.startDateTime).toDate()}
          endAccessor={shift => moment(shift.endDateTime).toDate()}
          addEvent={
            (start, end) => {
              this.addShift({
                tenantId: shownSpot.tenantId,
                startDateTime: start,
                endDateTime: end,
                spot: shownSpot,
                employee: null,
                rotationEmployee: null,
                pinnedByUser: false,
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
            onDelete: deletedShift => this.deleteShift(deletedShift),
          })}
          popoverBody={shift => ShiftPopupBody(shift)}
          eventComponent={props => ShiftEvent(props)}
        />
      </>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation()(withRouter(ShiftRosterPage)));
