import * as React from 'react';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { useTranslation } from 'react-i18next';
import { Split, SplitItem, Button, Level, LevelItem, Text, ButtonVariant } from '@patternfly/react-core';
import { EditIcon, TrashIcon, OkIcon, WarningTriangleIcon, ErrorCircleOIcon } from '@patternfly/react-icons';
import moment from 'moment';

export interface AvailabilityEventProps {
  availability: EmployeeAvailability;
  onEdit: (ea: EmployeeAvailability) => void;
  onDelete: (ea: EmployeeAvailability) => void;
  updateEmployeeAvailability: (ea: EmployeeAvailability) => void;
  removeEmployeeAvailability: (ea: EmployeeAvailability) => void;
}

const AvailabilityPopoverHeader: React.FC<AvailabilityEventProps> = props => (
  <span>
    <Text>
      {
        `${props.availability.employee.name}, ${
          moment(props.availability.startDateTime).format('LT')}-${
          moment(props.availability.endDateTime).format('LT')}`
      }
    </Text>
    <Button
      aria-label="Edit"
      onClick={() => props.onEdit(props.availability)}
      variant={ButtonVariant.link}
    >
      <EditIcon />
    </Button>
    <Button
      aria-label="Delete"
      onClick={() => props.onDelete(props.availability)}
      variant={ButtonVariant.link}
    >
      <TrashIcon />
    </Button>
  </span>
);

const AvailabilityPopoverBody: React.FC = () => <></>;


const AvailabilityEvent: React.FC<AvailabilityEventProps> = (props: AvailabilityEventProps) => {
  const { t } = useTranslation();
  return (
    <span className="availability-event">
      <Split>
        <SplitItem isFilled={false}>{t(`EmployeeAvailabilityState.${props.availability.state}`)}</SplitItem>
        <SplitItem isFilled />
        <SplitItem isFilled={false}>
          <Button
            aria-label="Delete"
            onClick={() => props.removeEmployeeAvailability(props.availability)}
            variant="danger"
          >
            <TrashIcon />
          </Button>
        </SplitItem>
      </Split>
      <Level hasGutter>
        <LevelItem>
          <Button
            aria-label="Desired"
            title={t('EmployeeAvailabilityState.DESIRED')}
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: 'DESIRED',
            })}
            style={{
              backgroundColor: 'green',
              margin: '1px',
              width: 'min-content',
            }}
            variant="tertiary"
          >
            <OkIcon />
          </Button>
          <Button
            aria-label="Undesired"
            title={t('EmployeeAvailabilityState.UNDESIRED')}
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: 'UNDESIRED',
            })}
            style={{
              backgroundColor: 'yellow',
              margin: '1px',
              width: 'min-content',
            }}
            variant="tertiary"
          >
            <WarningTriangleIcon />
          </Button>
          <Button
            aria-label="Unavailable"
            title={t('EmployeeAvailabilityState.UNAVAILABLE')}
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: 'UNAVAILABLE',
            })}
            style={{
              backgroundColor: 'red',
              margin: '1px',
              width: 'min-content',
            }}
            variant="tertiary"
          >
            <ErrorCircleOIcon />
          </Button>
        </LevelItem>
      </Level>
    </span>
  );
};

export { AvailabilityPopoverBody, AvailabilityPopoverHeader };

export default AvailabilityEvent;
