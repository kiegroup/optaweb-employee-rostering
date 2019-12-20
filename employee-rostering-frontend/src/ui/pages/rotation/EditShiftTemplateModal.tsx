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
import { connect } from 'react-redux';

import { Button, InputGroup, Label, Form, Modal, ButtonVariant, TextInput } from '@patternfly/react-core';

import { ShiftTemplate } from 'domain/ShiftTemplate';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
import { spotSelectors } from 'store/spot';
import { employeeSelectors } from 'store/employee';
import { modulo } from 'util/MathUtils';

import 'react-datepicker/dist/react-datepicker.css';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { withTranslation, WithTranslation } from 'react-i18next';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import moment from 'moment';
import { RosterState } from 'domain/RosterState';

export interface Props {
  tenantId: number;
  shiftTemplate?: ShiftTemplate;
  isOpen: boolean;
  spotList: Spot[];
  employeeList: Employee[];
  rotationLength: number | null;
  onSave: (availability: ShiftTemplate) => void;
  onDelete: (availability: ShiftTemplate) => void;
  onClose: () => void;
}

const mapStateToProps = (state: AppState, ownProps: {
  shiftTemplate?: ShiftTemplate;
  isOpen: boolean;
  onSave: (shiftTemplate: ShiftTemplate) => void;
  onDelete: (shiftTemplate: ShiftTemplate) => void;
  onClose: () => void;
}): Props => ({
  ...ownProps,
  tenantId: state.tenantData.currentTenantId,
  spotList: spotSelectors.getSpotList(state),
  rotationLength: state.rosterState.isLoading ? null : (state.rosterState.rosterState as RosterState).rotationLength,
  employeeList: employeeSelectors.getEmployeeList(state),
});

export type ShiftTemplateData = Pick<ShiftTemplate, Exclude<keyof ShiftTemplate,
'durationBetweenRotationStartAndTemplateStart' | 'shiftTemplateDuration'>> & {
  startDayOffset: number;
  startTime: {
    hours: number;
    minutes: number;
  };
  endDayOffset: number;
  endTime: {
    hours: number;
    minutes: number;
  };
}

interface State {
  resetCount: number;
  editedValue: Partial<ShiftTemplateData>;
}

export function shiftTemplateDataToShiftTemplate(data: ShiftTemplateData,
  rotationLength: number): ShiftTemplate {
  return {
    ...objectWithout(data, 'startDayOffset', 'startTime', 'endDayOffset', 'endTime'),
    durationBetweenRotationStartAndTemplateStart:
      moment.duration(data.startDayOffset, 'days').add(data.startTime.hours, 'hours')
        .add(data.startTime.minutes, 'minutes'),
    shiftTemplateDuration: (data.endDayOffset >= data.startDayOffset)
      ? moment.duration(data.endDayOffset, 'days').add(data.endTime.hours, 'hours')
        .add(data.endTime.minutes, 'minutes').subtract(moment.duration(data.startDayOffset, 'days')
          .add(data.startTime.hours, 'hours').add(data.startTime.minutes, 'minutes'))
      : moment.duration(rotationLength, 'days').subtract(moment.duration(data.startDayOffset, 'days')
        .add(data.startTime.hours, 'hours').add(data.startTime.minutes, 'minutes'))
        .add(data.endDayOffset, 'days').add(data.endTime.hours, 'hours')
        .add(data.endTime.minutes, 'minutes'),
  };
}

export function shiftTemplateToShiftTemplateData(shiftTemplate: ShiftTemplate,
  rotationLength: number): ShiftTemplateData {
  const durationBetweenRotationStartAndEnd = moment
    .duration(shiftTemplate.durationBetweenRotationStartAndTemplateStart).add(shiftTemplate.shiftTemplateDuration);
  return {
    ...objectWithout(shiftTemplate, 'durationBetweenRotationStartAndTemplateStart', 'shiftTemplateDuration'),
    startDayOffset: modulo(Math.floor(shiftTemplate.durationBetweenRotationStartAndTemplateStart.asDays()),
      rotationLength),
    startTime: {
      hours: shiftTemplate.durationBetweenRotationStartAndTemplateStart.hours(),
      minutes: shiftTemplate.durationBetweenRotationStartAndTemplateStart.minutes(),
    },
    endDayOffset: modulo(Math.floor(durationBetweenRotationStartAndEnd.asDays()), rotationLength),
    endTime: {
      hours: durationBetweenRotationStartAndEnd.hours(),
      minutes: durationBetweenRotationStartAndEnd.minutes(),
    },
  };
}

export class EditShiftTemplateModal extends React.Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);

    this.onSave = this.onSave.bind(this);
    this.state = (this.props.shiftTemplate && this.props.rotationLength) ? {
      resetCount: 0,
      editedValue: {
        ...shiftTemplateToShiftTemplateData(this.props.shiftTemplate, this.props.rotationLength),
      },
    } : { resetCount: 0, editedValue: {} };
  }

  componentDidUpdate(prevProps: Props, prevState: State) {
    if (this.props.shiftTemplate === undefined && prevProps.shiftTemplate !== undefined) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ resetCount: prevState.resetCount + 1,
        editedValue: {
          tenantId: this.props.tenantId,
        } });
    } else if (this.props.shiftTemplate !== undefined
      && this.props.rotationLength !== null
      && (this.props.shiftTemplate.id !== prevState.editedValue.id
        || this.props.shiftTemplate.version !== prevState.editedValue.version)) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        resetCount: prevState.resetCount + 1,
        editedValue: shiftTemplateToShiftTemplateData(this.props.shiftTemplate, this.props.rotationLength),
      });
    }
  }

  onSave() {
    const shiftTemplateData = this.state.editedValue;
    if (!shiftTemplateData.rotationEmployee) {
      shiftTemplateData.rotationEmployee = null;
    }

    if (shiftTemplateData.spot !== undefined && shiftTemplateData.startDayOffset !== undefined
      && shiftTemplateData.startTime !== undefined && shiftTemplateData.endDayOffset !== undefined
      && shiftTemplateData.endTime !== undefined) {
      this.props.onSave({
        tenantId: this.props.tenantId,
        ...shiftTemplateDataToShiftTemplate(shiftTemplateData as ShiftTemplateData,
          this.props.rotationLength as number),
      } as ShiftTemplate);
    }
  }

  render() {
    const { t } = this.props;
    return (
      <Modal
        title={this.props.shiftTemplate ? t('editShiftTemplate') : t('createShiftTemplate')}
        onClose={this.props.onClose}
        isOpen={this.props.isOpen}
        actions={
          [
            <Button
              aria-label="Close Modal"
              variant={ButtonVariant.tertiary}
              key={0}
              onClick={this.props.onClose}
            >
              {t('close')}
            </Button>,
          ].concat(this.props.shiftTemplate ? [
            <Button
              aria-label="Delete"
              variant={ButtonVariant.danger}
              key={1}
              onClick={() => this.props.onDelete(this.props.shiftTemplate as ShiftTemplate)}
            >
              {t('delete')}
            </Button>,
          ] : []).concat([
            <Button aria-label="Save" key={2} onClick={this.onSave}>{t('save')}</Button>,
          ])
        }
        isSmall
      >
        <Form id="modal-element" key={this.state.resetCount} onSubmit={e => e.preventDefault()}>
          <InputGroup>
            <Label>{t('spot')}</Label>
            <TypeaheadSelectInput
              aria-label="Spot"
              emptyText={t('selectSpot')}
              value={this.state.editedValue.spot}
              options={this.props.spotList}
              optionToStringMap={spot => spot.name}
              onChange={spot => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, spot },
              }))}
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('startDayOffset')}</Label>
            <TextInput
              aria-label="Start Day Offset"
              type="number"
              defaultValue={(this.state.editedValue.startDayOffset !== undefined) ? String(
                this.state.editedValue.startDayOffset + 1,
              ) : undefined}
              min={1}
              max={this.props.rotationLength ? this.props.rotationLength : undefined}
              onChange={(v) => {
                this.setState(old => ({
                  editedValue: {
                    ...old.editedValue,
                    startDayOffset: v ? parseInt(v, 10) - 1 : undefined,
                  },
                }));
              }}
            />
            <Label>{t('startTime')}</Label>
            <TextInput
              aria-label="Start Time"
              type="time"
              defaultValue={this.state.editedValue.startTime
                ? moment('2018-01-01T00:00').add(this.state.editedValue.startTime.hours, 'hours')
                  .add(this.state.editedValue.startTime.minutes, 'minutes').format('HH:mm')
                : undefined}
              onChange={(v) => {
                const parts = v.split(':');
                this.setState(old => ({
                  editedValue: {
                    ...old.editedValue,
                    startTime: {
                      hours: parseInt(parts[0], 10),
                      minutes: parseInt(parts[1], 10),
                    },
                  },
                }));
              }}
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('endDayOffset')}</Label>
            <TextInput
              aria-label="End Day Offset"
              type="number"
              defaultValue={(this.state.editedValue.endDayOffset !== undefined) ? String(
                this.state.editedValue.endDayOffset + 1,
              ) : undefined}
              min={1}
              max={this.props.rotationLength ? this.props.rotationLength : undefined}
              onChange={(v) => {
                this.setState(old => ({
                  editedValue: {
                    ...old.editedValue,
                    endDayOffset: v ? parseInt(v, 10) - 1 : undefined,
                  },
                }));
              }}
            />
            <Label>{t('endTime')}</Label>
            <TextInput
              aria-label="End Time"
              defaultValue={this.state.editedValue.endTime
                ? moment('2018-01-01T00:00').add(this.state.editedValue.endTime.hours, 'hours')
                  .add(this.state.editedValue.endTime.minutes, 'minutes').format('HH:mm')
                : undefined}
              type="time"
              onChange={(v) => {
                const parts = v.split(':');
                this.setState(old => ({
                  editedValue: {
                    ...old.editedValue,
                    endTime: {
                      hours: parseInt(parts[0], 10),
                      minutes: parseInt(parts[1], 10),
                    },
                  },
                }));
              }}
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('rotationEmployee')}</Label>
            <TypeaheadSelectInput
              aria-label="Employee"
              emptyText={t('unassigned')}
              value={this.state.editedValue.rotationEmployee ? this.state.editedValue.rotationEmployee : undefined}
              options={[undefined, ...this.props.employeeList]}
              optionToStringMap={employee => (employee ? employee.name : t('unassigned'))}
              onChange={employee => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, rotationEmployee: employee || null },
              }))}
              optional
            />
          </InputGroup>
        </Form>
      </Modal>
    );
  }
}

export default withTranslation('EditShiftTemplateModal')(
  connect(mapStateToProps)(EditShiftTemplateModal),
);
