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
      <Title size="md">
        {`${exceptionInfo.exceptionClass}: ${exceptionInfo.exceptionMessage}`}
      </Title>
      {exceptionInfo.stackTrace.map(line => (
        <Text key={line}>{line}</Text>
      ))}
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

  return (
    <>
      {t(props.i18nKey, props.messageParameters)}
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
