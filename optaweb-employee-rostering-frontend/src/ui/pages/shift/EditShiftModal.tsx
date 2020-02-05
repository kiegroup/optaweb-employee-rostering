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

import { Button, Switch, InputGroup, Label, Form, Modal, ButtonVariant } from '@patternfly/react-core';
import DatePicker from 'react-datepicker';

import { Shift } from 'domain/Shift';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
import { employeeSelectors } from 'store/employee';
import { spotSelectors } from 'store/spot';
import { withTranslation, WithTranslation } from 'react-i18next';

import 'react-datepicker/dist/react-datepicker.css';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';

interface Props {
  tenantId: number;
  shift?: Shift;
  isOpen: boolean;
  employeeList: Employee[];
  spotList: Spot[];
  onSave: (shift: Shift) => void;
  onDelete: (shift: Shift) => void;
  onClose: () => void;
}

const mapStateToProps = (state: AppState, ownProps: {
  shift?: Shift;
  isOpen: boolean;
  onSave: (shift: Shift) => void;
  onDelete: (shift: Shift) => void;
  onClose: () => void;
}): Props => ({
  ...ownProps,
  tenantId: state.tenantData.currentTenantId,
  employeeList: employeeSelectors.getEmployeeList(state),
  spotList: spotSelectors.getSpotList(state),
});

interface State {
  resetCount: number;
  editedValue: Partial<Shift>;
}

export class EditShiftModal extends React.Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);

    this.onSave = this.onSave.bind(this);
    this.state = {
      resetCount: 0,
      editedValue: { ...this.props.shift },
    };
  }

  componentDidUpdate(prevProps: Props, prevState: State) {
    if (this.props.shift === undefined && prevProps.shift !== undefined) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ resetCount: prevState.resetCount + 1,
        editedValue: {
          tenantId: this.props.tenantId,
          employee: null,
          rotationEmployee: null,
          pinnedByUser: false,
        } });
    } else if (this.props.shift !== undefined
      && (this.props.shift.id !== prevState.editedValue.id
        || this.props.shift.version !== prevState.editedValue.version)) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ resetCount: prevState.resetCount + 1, editedValue: this.props.shift });
    }
  }

  onSave() {
    const shift = this.state.editedValue;
    if (shift.spot !== undefined && shift.startDateTime !== undefined
        && shift.endDateTime !== undefined && shift.employee !== undefined
        && shift.pinnedByUser !== undefined && shift.rotationEmployee !== undefined) {
      this.props.onSave({ ...shift, tenantId: this.props.tenantId } as Shift);
    }
  }

  render() {
    const { t } = this.props;
    const dateFormat = 'MMMM dd, hh:mm a';

    return (
      <Modal
        title={this.props.shift ? t('editShift') : t('createShift')}
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
          ].concat(this.props.shift ? [
            <Button
              aria-label="Delete"
              variant={ButtonVariant.danger}
              key={1}
              onClick={() => this.props.onDelete(this.props.shift as Shift)}
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
            <Label>{t('shiftStart')}</Label>
            <DatePicker
              aria-label="Shift Start"
              selected={this.state.editedValue.startDateTime}
              onChange={date => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, startDateTime: (date !== null) ? date : undefined },
              }))}
              dateFormat={dateFormat}
              showTimeSelect
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('shiftEnd')}</Label>
            <DatePicker
              aria-label="Shift End"
              selected={this.state.editedValue.endDateTime}
              onChange={date => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, endDateTime: (date !== null) ? date : undefined },
              }))}
              dateFormat={dateFormat}
              showTimeSelect
            />
          </InputGroup>
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
            <Label>{t('employee')}</Label>
            <TypeaheadSelectInput
              aria-label="Employee"
              emptyText={t('unassigned')}
              value={(this.state.editedValue.employee !== null)
                ? this.state.editedValue.employee : undefined}
              options={this.props.employeeList}
              optionToStringMap={employee => (employee ? employee.name : t('unassigned'))}
              onChange={employee => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, employee: (employee !== undefined) ? employee : null },
              }))}
              optional
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('rotationEmployee')}</Label>
            <TypeaheadSelectInput
              aria-label="Rotation Employee"
              emptyText={t('none')}
              value={(this.state.editedValue.rotationEmployee !== null)
                ? this.state.editedValue.rotationEmployee : undefined}
              options={this.props.employeeList}
              optionToStringMap={employee => (employee ? employee.name : t('none'))}
              onChange={employee => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, rotationEmployee: (employee !== undefined) ? employee : null },
              }))}
              optional
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('isPinned')}</Label>
            <Switch
              aria-label="Is Pinned"
              isChecked={this.state.editedValue.pinnedByUser}
              onChange={isPinned => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, pinnedByUser: isPinned },
              }))}
            />
          </InputGroup>
        </Form>
      </Modal>
    );
  }
}

export default withTranslation('EditShiftModal')(
  connect(mapStateToProps)(EditShiftModal),
);
