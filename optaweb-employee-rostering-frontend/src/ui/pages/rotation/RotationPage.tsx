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
  EmptyStateIcon, Title, EmptyStateBody, Split, SplitItem, Bullseye,
} from '@patternfly/react-core';
import { EventProps } from 'react-big-calendar';
import { modulo } from 'util/MathUtils';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { alert } from 'store/alert';
import { RosterState } from 'domain/RosterState';

import { ShiftTemplate } from 'domain/ShiftTemplate';
import { shiftTemplateSelectors, shiftTemplateOperations } from 'store/rotation';
import { WithTranslation, withTranslation, useTranslation, Trans } from 'react-i18next';
import { EditIcon, TrashIcon, CubesIcon, BlueprintIcon, CogIcon } from '@patternfly/react-icons';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { UrlProps, getPropsFromUrl, setPropsInUrl } from 'util/BookmarkableUtils';
import EditShiftTemplateModal from './EditShiftTemplateModal';
import { EmployeeStubList, Stub } from './EmployeeStub';
import { EditTimeBucketModal } from './EditTimeBucketModal';

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
  stubList: Stub[];
  isEditingTimeBuckets: boolean;
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
      stubList: [],
      isEditingTimeBuckets: false,
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

    return (
      <>
        <Title size='2xl'>Rotation</Title>
        <TypeaheadSelectInput
            aria-label="Select Spot"
            emptyText={t('selectSpot')}
            optionToStringMap={spot => spot.name}
            options={this.props.spotList}
            value={shownSpot}
            onChange={(s) => {
              setPropsInUrl(this.props, { shownSpot: s? s.name : undefined });
            }}
            noClearButton
        />
        <Split>
          <SplitItem>
            <EmployeeStubList
              selectedEmployee={null}
              stubList={this.state.stubList}
              onStubSelect={() => {}}
              onUpdateStubList={stubList => this.setState({ stubList })}
            />
          </SplitItem>
          <SplitItem isFilled />
          <SplitItem>
            <Button
              variant='secondary'
              onClick={() => this.setState({
                isEditingTimeBuckets: true,
              })}
            >
              <Bullseye>
                <CogIcon
                  style={{
                    marginRight: '5px',
                  }}/>
                Time Window
              </Bullseye>
            </Button>
          </SplitItem>
        </Split>
        <EditTimeBucketModal
          isOpen={this.state.isEditingTimeBuckets}
           timeBuckets={[]} 
           onUpdateTimeBucketList={bucketList => {}}
           onClose={() => this.setState({
             isEditingTimeBuckets: false,
           })}
        />
      </>
    );
  }
}

export default withTranslation('RotationPage')(
  connect(mapStateToProps, mapDispatchToProps)(withRouter(RotationPage)),
);
