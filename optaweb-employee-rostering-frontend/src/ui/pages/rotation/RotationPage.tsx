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
import { Spot } from 'domain/Spot';
import { AppState } from 'store/types';
import { spotSelectors } from 'store/spot';
import { connect } from 'react-redux';
import moment from 'moment';
import {
  Level, LevelItem, Button, Text, Pagination, ButtonVariant, EmptyState, EmptyStateVariant,
  EmptyStateIcon, Title, EmptyStateBody,
} from '@patternfly/react-core';
import { EventProps } from 'react-big-calendar';
import { modulo } from 'util/MathUtils';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import { RosterState } from 'domain/RosterState';

import { ShiftTemplate } from 'domain/ShiftTemplate';
import { shiftTemplateSelectors, shiftTemplateOperations } from 'store/rotation';
import { WithTranslation, withTranslation, useTranslation, Trans } from 'react-i18next';
import { EditIcon, TrashIcon, CubesIcon, BlueprintIcon } from '@patternfly/react-icons';
import Schedule from 'ui/components/calendar/Schedule';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { UrlProps, getPropsFromUrl, setPropsInUrl } from 'util/BookmarkableUtils';
import EditShiftTemplateModal from './EditShiftTemplateModal';

interface StateProps {
  tenantId: number;
  isLoading: boolean;
  spotList: Spot[];
  spotIdToShiftTemplateListMap: Map<number, ShiftTemplate[]>;
  rosterState: RosterState | null;
}

const mapStateToProps = (state: AppState): StateProps => ({
  tenantId: state.tenantData.currentTenantId,
  isLoading: state.shiftTemplateList.isLoading,
  spotList: spotSelectors.getSpotList(state),
  spotIdToShiftTemplateListMap: shiftTemplateSelectors.getShiftTemplateList(state)
    .reduce((prev, curr) => {
      const old = prev.get(curr.spot.id as number) as ShiftTemplate[];
      const templatesToAdd: ShiftTemplate[] = (state.rosterState.rosterState !== null
        && moment.duration(curr.durationBetweenRotationStartAndTemplateStart)
          .add(curr.shiftTemplateDuration).asDays() >= state.rosterState.rosterState.rotationLength)
        ? [curr, {
          ...curr,
          durationBetweenRotationStartAndTemplateStart:
           moment.duration(-state.rosterState.rosterState.rotationLength, 'days')
             .add(curr.durationBetweenRotationStartAndTemplateStart),
        }] : [curr];
      return prev.set(curr.spot.id as number, old.concat(templatesToAdd));
    },
    spotSelectors.getSpotList(state).reduce((prev, curr) => prev.set(curr.id as number, []),
      new Map<number, ShiftTemplate[]>())),
  rosterState: state.rosterState.rosterState,
});

export interface DispatchProps {
  addShiftTemplate: typeof shiftTemplateOperations.addShiftTemplate;
  removeShiftTemplate: typeof shiftTemplateOperations.removeShiftTemplate;
  updateShiftTemplate: typeof shiftTemplateOperations.updateShiftTemplate;
  showInfoMessage: typeof alert.showInfoMessage;
}

const mapDispatchToProps: DispatchProps = {
  addShiftTemplate: shiftTemplateOperations.addShiftTemplate,
  removeShiftTemplate: shiftTemplateOperations.removeShiftTemplate,
  updateShiftTemplate: shiftTemplateOperations.updateShiftTemplate,
  showInfoMessage: alert.showInfoMessage,
};

export type Props = RouteComponentProps & StateProps & DispatchProps;
interface State {
  isCreatingOrEditingShiftTemplate: boolean;
  selectedShiftTemplate?: ShiftTemplate;
}

// export const baseDate = moment('2018-01-01T00:00').locale('en').startOf('week').toDate();

const ShiftTemplatePopoverHeader: React.FC<{
  shiftTemplate: ShiftTemplate;
  rotationLength: number;
  onEdit: (shift: ShiftTemplate) => void;
  onCopy: (shift: ShiftTemplate) => void;
  onDelete: (shift: ShiftTemplate) => void;
}> = (props) => {
  const { t } = useTranslation('RotationPage');
  const durationBetweenRotationStartAndEnd = moment
    .duration(props.shiftTemplate.durationBetweenRotationStartAndTemplateStart)
    .add(props.shiftTemplate.shiftTemplateDuration);
  return (
    <span>
      <Text>
        {t('shiftTemplate', {
          spot: props.shiftTemplate.spot.name,
          rotationEmployee: props.shiftTemplate.rotationEmployee ? props.shiftTemplate.rotationEmployee.name
            : t('Unassigned'),
          dayStart: Math.floor(modulo(
            props.shiftTemplate.durationBetweenRotationStartAndTemplateStart.asDays(),
            props.rotationLength,
          )) + 1,
          startTime: moment('2018-01-01')
            .add(props.shiftTemplate.durationBetweenRotationStartAndTemplateStart).format('LT'),
          dayEnd: Math.floor(modulo(durationBetweenRotationStartAndEnd.asDays(),
            props.rotationLength)) + 1,
          endTime: moment('2018-01-01')
            .add(durationBetweenRotationStartAndEnd).format('LT'),
        })}

      </Text>
      <Button
        onClick={() => props.onEdit(props.shiftTemplate)}
        variant={ButtonVariant.link}
      >
        <EditIcon />
      </Button>
      <Button
        onClick={() => props.onCopy(props.shiftTemplate)}
        variant={ButtonVariant.link}
      >
        <BlueprintIcon />
      </Button>
      <Button
        onClick={() => props.onDelete(props.shiftTemplate)}
        variant={ButtonVariant.link}
      >
        <TrashIcon />
      </Button>
    </span>
  );
};

const ShiftTemplatePopoverBody: React.FC = () => <></>;

const ShiftTemplateEvent: React.FC<EventProps<ShiftTemplate>> = props => (
  <span
    style={{
      display: 'flex',
      height: '100%',
      width: '100%',
    }}
  >
    {props.title}
  </span>
);

export type RotationPageUrlProps = UrlProps<'weekNumber' | 'shownSpot'>;
export class RotationPage extends React.Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);
    this.addShiftTemplate = this.addShiftTemplate.bind(this);
    this.updateShiftTemplate = this.updateShiftTemplate.bind(this);
    this.deleteShiftTemplate = this.deleteShiftTemplate.bind(this);

    this.state = {
      isCreatingOrEditingShiftTemplate: false,
    };
  }

  addShiftTemplate(addedShiftTemplate: ShiftTemplate) {
    this.props.addShiftTemplate(addedShiftTemplate);
  }

  updateShiftTemplate(updatedShiftTemplate: ShiftTemplate) {
    this.props.updateShiftTemplate(updatedShiftTemplate);
  }


  deleteShiftTemplate(deletedShiftTemplate: ShiftTemplate) {
    this.props.removeShiftTemplate(deletedShiftTemplate);
  }

  render() {
    const urlProps = getPropsFromUrl<RotationPageUrlProps>(this.props, {
      weekNumber: '0',
      shownSpot: (this.props.spotList.length > 0) ? this.props.spotList[0].name : null,
    });
    const baseDate = moment('2018-01-01T00:00').startOf('week').toDate();
    const { t } = this.props;
    if (this.props.rosterState === null || this.props.isLoading || this.props.spotList.length <= 0
      || urlProps.shownSpot === null || !this.props.tReady) {
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
                aria-label="Spots Page"
                variant="primary"
                onClick={() => this.props.history.push(`/${this.props.tenantId}/wards`)}
              />,
            ]}
          />
        </EmptyState>
      );
    }

    const weekNumber = parseInt(urlProps.weekNumber as string, 10);
    const shownSpot = this.props.spotList.find(s => s.name === urlProps.shownSpot) as Spot;

    const startDate = moment(baseDate).add(weekNumber, 'weeks').toDate();
    const endDate = moment(startDate).add(1, 'week').toDate();
    const events: { shiftTemplate: ShiftTemplate; start: Date; end: Date }[] = [];

    (this.props.spotIdToShiftTemplateListMap.get(shownSpot.id as number) as ShiftTemplate[])
      .forEach((st) => {
        const startHours = st.shiftTemplateDuration.hours();
        const startMinutes = st.shiftTemplateDuration.minutes();
        const durationBetweenRotationStartAndTemplateStart = moment
          .duration(st.durationBetweenRotationStartAndTemplateStart);

        if (startHours === 0 && startMinutes === 0) {
          durationBetweenRotationStartAndTemplateStart.add(1, 'ms');
        }


        const durationBetweenRotationStartAndEnd = moment
          .duration(st.durationBetweenRotationStartAndTemplateStart).add(st.shiftTemplateDuration);
        const endHours = durationBetweenRotationStartAndEnd.hours();
        const endMinutes = durationBetweenRotationStartAndEnd.minutes();
        const shiftTemplateDuration = moment.duration(st.shiftTemplateDuration);
        if (endHours === 0 && endMinutes === 0) {
          shiftTemplateDuration.subtract(1, 'ms');
        }
        events.push({
          shiftTemplate: st,
          start: moment(baseDate).add(durationBetweenRotationStartAndTemplateStart).toDate(),
          end: moment(baseDate)
            .add(durationBetweenRotationStartAndTemplateStart)
            .add(shiftTemplateDuration).toDate(),
        });
      });

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
              emptyText={t('selectSpot')}
              optionToStringMap={spot => spot.name}
              options={this.props.spotList}
              value={shownSpot}
              onChange={(newSpot) => {
                if (newSpot !== undefined) {
                  setPropsInUrl(this.props, { shownSpot: newSpot.name });
                }
              }}
              noClearButton
            />
            <Pagination
              itemCount={Math.ceil(this.props.rosterState.rotationLength)}
              page={weekNumber + 1}
              onSetPage={(e, page) => {
                setPropsInUrl(this.props, { weekNumber: `${page - 1}` });
              }}
              perPage={7}
              perPageOptions={[]}
              titles={{
                items: t('days'),
                page: t('week'),
                itemsPerPage: t('weekNum'),
                perPageSuffix: t('weekNum'),
                toFirstPage: t('gotoFirstWeek'),
                toPreviousPage: t('gotoPreviousWeek'),
                toLastPage: t('gotoLastWeek'),
                toNextPage: t('gotoNextWeek'),
                optionsToggle: t('select'),
                currPage: t('currentWeek'),
                paginationTitle: t('weekSelect'),
              }}
            />
          </LevelItem>
          <LevelItem style={{ display: 'flex' }}>
            <Button
              style={{ margin: '5px' }}
              aria-label="Create Shift Template"
              onClick={() => {
                this.setState({
                  selectedShiftTemplate: undefined,
                  isCreatingOrEditingShiftTemplate: true,
                });
              }}
            >
              {t('createShiftTemplate')}
            </Button>
          </LevelItem>
        </Level>
        <EditShiftTemplateModal
          aria-label="Edit Shift Template"
          shiftTemplate={this.state.selectedShiftTemplate}
          isOpen={this.state.isCreatingOrEditingShiftTemplate}
          onSave={(shiftTemplate) => {
            if (this.state.selectedShiftTemplate !== undefined) {
              this.props.updateShiftTemplate(shiftTemplate);
            } else {
              this.props.addShiftTemplate(shiftTemplate);
            }
            this.setState({ selectedShiftTemplate: undefined, isCreatingOrEditingShiftTemplate: false });
          }}
          onDelete={(shiftTemplate) => {
            this.props.removeShiftTemplate(shiftTemplate);
            this.setState({ isCreatingOrEditingShiftTemplate: false });
          }}
          onClose={() => {
            this.setState({
              selectedShiftTemplate: undefined,
              isCreatingOrEditingShiftTemplate: false,
            });
          }}
        />
        <Schedule<{ shiftTemplate: ShiftTemplate; start: Date; end: Date }>
          key={shownSpot.id}
          startDate={startDate}
          endDate={endDate}
          dateFormat={date => this.props.t('rotationDay',
            {
              day: moment.duration(moment(date).diff(baseDate)).asDays() + 1,
            })
          }
          events={events}
          titleAccessor={e => (e.shiftTemplate.rotationEmployee
            ? e.shiftTemplate.rotationEmployee.name : t('unassigned'))}
          startAccessor={e => e.start}
          endAccessor={e => e.end}
          onAddEvent={
            (start, end) => {
              this.addShiftTemplate({
                tenantId: shownSpot.tenantId,
                durationBetweenRotationStartAndTemplateStart: moment.duration(moment(start)
                  .diff(baseDate)),
                shiftTemplateDuration: moment.duration(moment(end).diff(baseDate))
                  .subtract(moment(start).diff(baseDate)),
                spot: shownSpot,
                rotationEmployee: null,
                requiredSkillSet: [],
              });
            }
          }
          onUpdateEvent={
            (event, start, end) => {
              this.updateShiftTemplate({
                ...event.shiftTemplate,
                durationBetweenRotationStartAndTemplateStart: moment.duration(moment(start)
                  .diff(baseDate)),
                shiftTemplateDuration: moment.duration(moment(end).diff(baseDate))
                  .subtract(moment(start).diff(baseDate)),
              });
            }
          }
          eventStyle={() => ({})}
          dayStyle={() => ({})}
          wrapperStyle={() => ({})}
          popoverHeader={st => ShiftTemplatePopoverHeader({
            shiftTemplate: st.shiftTemplate,
            rotationLength: (this.props.rosterState as RosterState).rotationLength,
            onEdit: shiftTemplate => this.setState({
              selectedShiftTemplate: shiftTemplate,
              isCreatingOrEditingShiftTemplate: true,
            }),
            onCopy: shiftTemplate => this.props.addShiftTemplate({
              ...shiftTemplate,
              rotationEmployee: null,
              id: undefined,
              version: undefined,
            }),
            onDelete: shiftTemplate => this.props.removeShiftTemplate(shiftTemplate),
          })
          }
          popoverBody={() => ShiftTemplatePopoverBody({})}
          eventComponent={params => ShiftTemplateEvent({
            ...params,
            event: params.event.shiftTemplate,
          })}
        />
      </>
    );
  }
}

export default withTranslation('RotationPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(RotationPage)),
);
