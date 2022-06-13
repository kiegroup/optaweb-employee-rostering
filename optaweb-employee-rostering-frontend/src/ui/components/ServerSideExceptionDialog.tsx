import * as React from 'react';
import { ServerSideExceptionInfo } from 'types';
import { Text, Title, Button, ButtonVariant, Modal } from '@patternfly/react-core';
import { useTranslation } from 'react-i18next';

function createStackTrace(exceptionInfo: ServerSideExceptionInfo|null): JSX.Element {
  if (exceptionInfo === null) {
    return (<></>);
  }

  return (
    <>
      <Title headingLevel="h1" size="md">
        {`${exceptionInfo.exceptionClass}: ${exceptionInfo.exceptionMessage}`}
      </Title>
      <Text
        style={{
          whiteSpace: 'pre-wrap',
        }}
      >
        {exceptionInfo.stackTrace.join('\n')}
      </Text>
      {
        exceptionInfo.exceptionCause ? (
          <>
            {/* Not i18n translated as this is intended for the developer */}
            <Text>Caused By</Text>
            {createStackTrace(exceptionInfo.exceptionCause)}
          </>
        ) : (<></>)
      }
    </>
  );
}

export const ServerSideExceptionDialog: React.FC<React.PropsWithChildren<ServerSideExceptionInfo>> = (props) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const dialogBody = createStackTrace(props);
  const { t } = useTranslation('ServerSideException');
  const messageParameters = (props.i18nKey === 'ServerSideException.entityConstraintViolation') ? {
    entityClass: props.messageParameters[0],
    violations: props.messageParameters.slice(1),
  } : props.messageParameters;

  return (
    <>
      {t(props.i18nKey, { ...messageParameters, joinArrays: '\n' })}
      <Button
        aria-label="Show Stack Trace"
        variant={ButtonVariant.link}
        onClick={() => setIsOpen(true)}
      >
        {props.children}
      </Button>
      <Modal
        title={t('serverSideError')}
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        actions={(
          <Button
            aria-label="Close"
            onClick={() => setIsOpen(false)}
          >
            {t('close')}
          </Button>
        )}
      >
        {dialogBody}
      </Modal>
    </>
  );
};
