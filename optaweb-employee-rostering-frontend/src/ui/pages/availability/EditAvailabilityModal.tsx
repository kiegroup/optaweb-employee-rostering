import React from 'react';
import { connect } from 'react-redux';

import { Button, InputGroup, Label, Form, Modal, ButtonVariant } from '@patternfly/react-core';
import DatePicker from 'react-datepicker';

import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { Employee } from 'domain/Employee';
import { AppState } from 'store/types';
import { employeeSelectors } from 'store/employee';

import 'react-datepicker/dist/react-datepicker.css';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { withTranslation, WithTranslation } from 'react-i18next';

interface Props {
  tenantId: number;
  availability?: EmployeeAvailability;
  isOpen: boolean;
  employeeList: Employee[];
  onSave: (availability: EmployeeAvailability) => void;
  onDelete: (availability: EmployeeAvailability) => void;
  onClose: () => void;
}

const mapStateToProps = (state: AppState, ownProps: {
  availability?: EmployeeAvailability;
  isOpen: boolean;
  onSave: (availability: EmployeeAvailability) => void;
  onDelete: (availability: EmployeeAvailability) => void;
  onClose: () => void;
}): Props => ({
  ...ownProps,
  tenantId: state.tenantData.currentTenantId,
  employeeList: employeeSelectors.getEmployeeList(state),
});

interface State {
  resetCount: number;
  editedValue: Partial<EmployeeAvailability>;
}

export class EditAvailabilityModal extends React.Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);

    this.onSave = this.onSave.bind(this);
    this.state = {
      resetCount: 0,
      editedValue: { ...this.props.availability },
    };
  }

  componentDidUpdate(prevProps: Props, prevState: State) {
    if (this.props.availability === undefined && prevProps.availability !== undefined) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ resetCount: prevState.resetCount + 1,
        editedValue: {
          tenantId: this.props.tenantId,
        } });
    } else if (this.props.availability !== undefined
      && (this.props.availability.id !== prevState.editedValue.id
        || this.props.availability.version !== prevState.editedValue.version)) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ resetCount: prevState.resetCount + 1, editedValue: this.props.availability });
    }
  }

  onSave() {
    const availability = this.state.editedValue;
    if (availability.employee !== undefined && availability.startDateTime !== undefined
      && availability.endDateTime !== undefined && availability.state !== undefined) {
      this.props.onSave({ ...availability, tenantId: this.props.tenantId } as EmployeeAvailability);
    }
  }

  render() {
    const dateFormat = 'MMMM dd, hh:mm a';
    const { t, tReady } = this.props;
    if (!tReady) {
      return (<></>);
    }
    return (
      <Modal
        title={this.props.availability ? t('editAvailability') : t('createAvailability')}
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
          ].concat(this.props.availability ? [
            <Button
              aria-label="Delete"
              variant={ButtonVariant.danger}
              key={1}
              onClick={() => this.props.onDelete(this.props.availability as EmployeeAvailability)}
            >
              {t('delete')}
            </Button>,
          ] : []).concat([
            <Button aria-label="Save" key={2} onClick={this.onSave}>{t('save')}</Button>,
          ])
        }
        variant="small"
      >
        <Form id="modal-element" key={this.state.resetCount} onSubmit={e => e.preventDefault()}>
          <InputGroup>
            <Label>{t('availabilityStart')}</Label>
            <DatePicker
              aria-label="Availability Start"
              selected={this.state.editedValue.startDateTime}
              onChange={date => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, startDateTime: (date !== null) ? date : undefined },
              }))}
              dateFormat={dateFormat}
              showTimeSelect
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('availabilityEnd')}</Label>
            <DatePicker
              aria-label="Availability End"
              selected={this.state.editedValue.endDateTime}
              onChange={date => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, endDateTime: (date !== null) ? date : undefined },
              }))}
              dateFormat={dateFormat}
              showTimeSelect
            />
          </InputGroup>
          <InputGroup>
            <Label>Employee</Label>
            <TypeaheadSelectInput
              aria-label="Employee"
              emptyText={t('selectEmployee')}
              value={this.state.editedValue.employee}
              options={this.props.employeeList}
              optionToStringMap={employee => employee.name}
              onChange={employee => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, employee },
              }))}
            />
          </InputGroup>
          <InputGroup>
            <Label>{t('type')}</Label>
            <TypeaheadSelectInput
              aria-label="Type"
              emptyText="Select Type..."
              value={this.state.editedValue.state}
              options={['UNAVAILABLE', 'DESIRED', 'UNDESIRED'] as ('UNAVAILABLE'|'DESIRED'|'UNDESIRED')[]}
              optionToStringMap={state => this.props.t(`EmployeeAvailabilityState.${state}`)}
              onChange={state => this.setState(prevState => ({
                editedValue: { ...prevState.editedValue, state },
              }))}
            />
          </InputGroup>
        </Form>
      </Modal>
    );
  }
}

// eslint-disable-next-line no-undef
export default withTranslation('EditAvailabilityModal')(
  connect(mapStateToProps)(EditAvailabilityModal),
);
