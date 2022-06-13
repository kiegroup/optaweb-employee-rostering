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
  const [fromDate, setFromDate] = React.useState<Date | null>(props.defaultFromDate);
  const [toDate, setToDate] = React.useState<Date | null>(props.defaultToDate);
  const [exportedSpots, setExportedSpots] = React.useState<Spot[]>(props.spotList);

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

  const spotSet = (exportedSpots.length > 0) ? exportedSpots.map(s => `${s.id}`).join(',') : null;

  let exportUrl = '_blank';
  if (spotSet && toDate && fromDate) {
    exportUrl = `${process.env.REACT_APP_BACKEND_URL}/rest/tenant/${props.tenantId}/roster/shiftRosterView/excel?`
                    + `startDate=${moment(fromDate).format('YYYY-MM-DD')}&`
                    + `endDate=${moment(toDate).format('YYYY-MM-DD')}&spotList=${spotSet}`;
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
      variant="small"
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
            onChange={newExportedSpots => setExportedSpots(newExportedSpots)}
          />
        </InputGroup>
      </Form>
    </Modal>
  );
};

export default connect(mapStateToProps)(ExportScheduleModal);
