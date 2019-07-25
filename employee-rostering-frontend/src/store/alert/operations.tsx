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
import { Text, Title, Button, ButtonVariant, Modal } from '@patternfly/react-core';
import { ServerSideExceptionInfo } from 'types';
import * as actions from './actions';
import { AlertInfo, AddAlertAction, RemoveAlertAction } from './types';
import { useTranslation } from 'react-i18next';

interface ObjectStringMap { [K: string]: any }

export function showInfoMessage(i18nKey: string, params?: ObjectStringMap, components?: JSX.Element[]): AddAlertAction {
  return showMessage("info", i18nKey, params, components);
}

export function showSuccessMessage(i18nKey: string, params?: ObjectStringMap, components?: JSX.Element[]): AddAlertAction {
  return showMessage("success", i18nKey, params, components);
}

const ServerSideExceptionDialog: React.FC<React.PropsWithChildren<ServerSideExceptionInfo>> = (props) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const dialogBody = createStackTrace(props);
  const { t } = useTranslation();

  return (
    <>
      {t(props.i18nKey, props.messageParameters)}
      <Button
        variant={ButtonVariant.link}
        onClick={() => setIsOpen(true)}
      >
        {props.children}
      </Button>
      <Modal
        title="Server Side Error"
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        actions={(
          <Button
            onClick={() => setIsOpen(false)}
          >
            Close
          </Button>
        )}
      >
        {dialogBody}
      </Modal>
    </>
  );
};

export function showServerError(exceptionInfo: ServerSideExceptionInfo): AddAlertAction {
  return addAlert({
    i18nKey: "exception",
    variant: "danger",
    params: {
      message: exceptionInfo.exceptionMessage,
    },
    components: [<ServerSideExceptionDialog {...exceptionInfo} key="0" />]
  });
}

export function showServerErrorMessage(message: string): AddAlertAction {
  return showErrorMessage("generic", { message: message });
}

export function showErrorMessage(i18nKey: string, params?: ObjectStringMap, components?: JSX.Element[]): AddAlertAction {
  return showMessage("danger", i18nKey, params, components);
}

export function showMessage(variant: "success" | "danger" | "warning" | "info", i18nKey: string, params?: ObjectStringMap, components?: JSX.Element[]): AddAlertAction {
  return addAlert({
    i18nKey: i18nKey,
    variant: variant,
    params: params? params: {},
    components: components
  });
}

function createStackTrace(exceptionInfo: ServerSideExceptionInfo|null): JSX.Element {
  if (exceptionInfo === null) {
    return (<></>);
  }
  else {
    return (
      <>
        <Title size="md">
          {exceptionInfo.exceptionClass + ": " + exceptionInfo.exceptionMessage}
        </Title>
        {exceptionInfo.stackTrace.map(line => (
          <Text key={line}>{line}</Text>
        ))}
        {
          exceptionInfo.exceptionCause? (
            <>
              <Text>Caused By</Text>
              {createStackTrace(exceptionInfo.exceptionCause)}
            </>
          ): (<></>)
        }
      </>
    );
  }
}

export function addAlert(alert: AlertInfo): AddAlertAction {
  return actions.addAlert({
    ...alert,
    createdAt: new Date()
  });
}

export function removeAlert(alert: AlertInfo): RemoveAlertAction {
  return actions.removeAlert(alert.id as number);
}