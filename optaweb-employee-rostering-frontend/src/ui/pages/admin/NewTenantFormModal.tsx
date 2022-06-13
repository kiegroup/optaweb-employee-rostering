import * as React from 'react';
import { Modal, Button, ButtonVariant, InputGroup, Label, TextInput, Form } from '@patternfly/react-core';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { RosterState } from 'domain/RosterState';
import { tenantOperations } from 'store/tenant';
import { connect } from 'react-redux';
import { AppState } from 'store/types';
import moment from 'moment';
import { useTranslation } from 'react-i18next';

interface StateProps {
  timezoneList: string[];
}

const mapStateToProps = (state: AppState, props: OwnProps) => ({
  ...props,
  timezoneList: state.tenantData.timezoneList,
});

interface DispatchProps {
  addTenant: typeof tenantOperations.addTenant;
  refreshSupportedTimezones: typeof tenantOperations.refreshSupportedTimezones;
}

const mapDispatchToProps: DispatchProps = {
  addTenant: tenantOperations.addTenant,
  refreshSupportedTimezones: tenantOperations.refreshSupportedTimezones,
};

interface OwnProps {
  isOpen: boolean;
  onClose: () => void;
}

export type Props = StateProps & DispatchProps & OwnProps;

export function isFormCompleted(rs: Partial<RosterState>): rs is RosterState {
  return rs.draftLength !== undefined && rs.firstDraftDate !== undefined
    && rs.lastHistoricDate !== undefined && rs.publishLength !== undefined
    && rs.publishNotice !== undefined && rs.rotationLength !== undefined
    && rs.rotationLength >= 2 && rs.tenant !== undefined && rs.timeZone !== undefined
    && rs.unplannedRotationOffset !== undefined;
}

export const NewTenantFormModal: React.FC<Props> = (props) => {
  const { t } = useTranslation('NewTenantFormModal');
  const { refreshSupportedTimezones } = props;
  React.useEffect(() => {
    refreshSupportedTimezones();
  }, [refreshSupportedTimezones]);

  const [formData, setFormData] = React.useState<Partial<RosterState>>({
    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    publishLength: 7,
    unplannedRotationOffset: 0,
  });

  const setProperty = (properties: Partial<RosterState>) => {
    setFormData({ ...formData, ...properties });
  };

  return (
    <Modal
      title={t('createTenant')}
      onClose={props.onClose}
      isOpen={props.isOpen}
      actions={
        [(
          <Button
            aria-label="Close Modal"
            variant={ButtonVariant.tertiary}
            key={0}
            onClick={props.onClose}
          >
            {t('close')}
          </Button>
        ),
        (
          <Button
            isDisabled={!isFormCompleted(formData)}
            aria-label="Save"
            data-cy="save"
            key={2}
            onClick={() => {
              if (isFormCompleted(formData)) {
                props.addTenant(formData);
                props.onClose();
              }
            }}
          >
            {t('save')}
          </Button>
        ),
        ]
      }
      variant="small"
    >
      <Form onSubmit={e => e.preventDefault()}>
        <InputGroup>
          <Label>{t('name')}</Label>
          <TextInput
            aria-label="Name"
            data-cy="name"
            onChange={name => setProperty({
              tenant: {
                name,
              },
            })}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('scheduleStartDate')}</Label>
          <TextInput
            type="date"
            aria-label="Schedule Start Date"
            data-cy="schedule-start-date"
            onChange={date => setProperty({
              lastHistoricDate: moment(date).subtract(1, 'day').toDate(),
              firstDraftDate: moment(date).toDate(),
            })}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('draftLength')}</Label>
          <TextInput
            type="number"
            aria-label="Draft Length"
            data-cy="draft-length"
            onChange={length => setProperty({ draftLength: parseInt(length, 10) })}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('publishNotice')}</Label>
          <TextInput
            type="number"
            aria-label="Publish Notice"
            data-cy="publish-notice"
            onChange={notice => setProperty({ publishNotice: parseInt(notice, 10) })}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('publishLength')}</Label>
          <TextInput
            defaultValue="7"
            type="number"
            onChange={length => setProperty({ publishLength: parseInt(length, 10) })}
            aria-label="Publish Length"
            data-cy="publish-length"
            isDisabled
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('rotationLength')}</Label>
          <TextInput
            type="number"
            aria-label="Rotation Length"
            onChange={length => setProperty({ rotationLength: parseInt(length, 10) })}
            data-cy="rotation-length"
            min={2}
          />
        </InputGroup>
        <InputGroup
          style={{
            display: 'grid',
            gridTemplateColumns: 'auto 1fr',
          }}
        >
          <Label>{t('timezone')}</Label>
          <TypeaheadSelectInput
            aria-label="Timezone"
            emptyText={t('selectATimezone')}
            value={formData.timeZone}
            options={props.timezoneList}
            optionToStringMap={s => s}
            onChange={tz => setProperty({ timeZone: tz })}
            autoSize={false}
          />
        </InputGroup>
      </Form>
    </Modal>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(NewTenantFormModal);
