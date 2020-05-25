/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import { Modal, Button, ButtonVariant, Form, InputGroup, Label } from '@patternfly/react-core';
import DatePicker from 'react-datepicker';
import { useTranslation } from 'react-i18next';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { Spot } from 'domain/Spot';
import { AppState } from 'store/types';
import { spotSelectors } from 'store/spot';
import { connect } from 'react-redux';
import moment from 'moment';

interface StateProps {
  tenantId: number;
  spotList: Spot[];
}

interface OwnProps {
  isOpen: boolean;
  onClose: () => void;
  defaultFromDate: Date;
  defaultToDate: Date;
}

const mapStateToProps = (state: AppState, ownProps: OwnProps): StateProps & OwnProps => ({
  ...ownProps,
  tenantId: state.tenantData.currentTenantId,
  spotList: spotSelectors.getSpotList(state),
});

export const ExportScheduleModal: React.FC<StateProps & OwnProps> = (props) => {
  const { t } = useTranslation('ExportScheduleModal');
  const [fromDate, setFromDate] = React.useState<Date | null>(null);
  const [toDate, setToDate] = React.useState<Date | null>(null);
  const [exportedSpots, setExportedSpots] = React.useState<Spot[]>([]);

  // Work around since useEffect use shallowEquality, and the same date created at different times are not equal
  const defaultFromDateTime = props.defaultFromDate.getTime();
  const defaultToDateTime = props.defaultToDate.getTime();

  React.useEffect(() => {
    if (props.isOpen) {
      setFromDate(new Date(defaultFromDateTime));
      setToDate(new Date(defaultToDateTime));
      setExportedSpots(props.spotList);
    }
  }, [props.isOpen, defaultFromDateTime, defaultToDateTime, props.spotList]);

  const spotSet = (exportedSpots.length > 0) ? exportedSpots.map(s => `${s.id}`)
    .reduce((prev, next) => `${prev},${next}`) : null;

  let exportUrl = '_blank';
  if (spotSet && toDate && fromDate) {
    exportUrl = `${process.env.REACT_APP_BACKEND_URL}/rest/tenant/${props.tenantId}/roster/shiftRosterView/excel?`
                    + `startDate=${moment(fromDate as Date).format('YYYY-MM-DD')}&`
                    + `endDate=${moment(toDate as Date).format('YYYY-MM-DD')}&spotList=${spotSet}`;
  }
  const exportSchedule = () => {
    props.onClose();
  };

  return (
    <Modal
      title={t('exportSchedule')}
      isOpen={props.isOpen}
      onClose={props.onClose}
      actions={
        [
          <Button
            aria-label="Close Modal"
            variant={ButtonVariant.tertiary}
            key={0}
            onClick={props.onClose}
          >
            {t('close')}
          </Button>,
          <a
            href={exportUrl}
            className="pf-c-button pf-m-primary"
            download
            onClick={exportSchedule}
          >
            {t('export')}
          </a>,
        ]
      }
      isSmall
    >
      <Form id="modal-element" onSubmit={e => e.preventDefault()}>
        <InputGroup>
          <Label>{t('fromDate')}</Label>
          <DatePicker
            aria-label={t('fromDate')}
            selected={fromDate}
            onChange={setFromDate}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('toDate')}</Label>
          <DatePicker
            aria-label={t('toDate')}
            selected={toDate}
            onChange={setToDate}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('forSpots')}</Label>
          <MultiTypeaheadSelectInput
            aria-label={t('forSpots')}
            emptyText={t('selectSpots')}
            value={exportedSpots}
            options={props.spotList}
            optionToStringMap={spot => spot.name}
            onChange={setExportedSpots}
          />
        </InputGroup>
      </Form>
    </Modal>
  );
};

export default connect(mapStateToProps)(ExportScheduleModal);
