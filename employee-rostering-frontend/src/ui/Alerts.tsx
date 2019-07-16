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
import { Alert, AlertActionCloseButton, Text, Title, Button, ButtonVariant, Modal } from '@patternfly/react-core';
import { ServerSideExceptionInfo } from 'types';
import './Alerts.css';

export interface AlertInfo {
  id?: number;
  timeoutId?: number;
  title: string;
  message: JSX.Element;
  variant: "success" | "danger" | "warning" | "info";
}

let alertRef: Alerts|null = null;

export function setAlertRef(newAlertRef: Alerts): void {
  alertRef = newAlertRef;
}

export function showInfoMessage(title: string, message: string) {
  showMessage("info", title, message);
}

export function showSuccessMessage(title: string, message: string) {
  showMessage("success", title, message);
}

export function showServerError(exceptionInfo: ServerSideExceptionInfo) {
  if (alertRef !== null) {
    alertRef.addAlert({
      title: "Server Error",
      variant: "danger",
      message: (
        <span>
          {exceptionInfo.exceptionMessage}
          <Button
            variant={ButtonVariant.link}
            onClick={() => alertRef? alertRef.setServerSideException(exceptionInfo) : null}
          >
            See Stack Trace.
          </Button>
        </span>
      )
    });
  }
}

export function showServerErrorMessage(message: string) {
  showErrorMessage("Server Error", message);
}

export function showErrorMessage(title: string, message: string) {
  showMessage("danger", title, message);
}

export function showMessage(variant: "success" | "danger" | "warning" | "info", title: string, message: string) {
  if (alertRef !== null) {
    alertRef.addAlert({
      title: title,
      variant: variant,
      message: <span>{message}</span>
    });
  }
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

interface Props {
  setRef: (alertRef: Alerts) => void;
}

interface State {
  alertCount: number;
  alerts: AlertInfo[];
  alertToClassNames: Map<number, string>;
  serverErrorInfo: ServerSideExceptionInfo|null;
}

export default class Alerts extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      alertCount: 0,
      alerts: [],
      serverErrorInfo: null,
      alertToClassNames: new Map()
    };
    this.addAlert = this.addAlert.bind(this);
    this.removeAlert = this.removeAlert.bind(this);
    this.setServerSideException = this.setServerSideException.bind(this);
    props.setRef(this);
  }

  addAlert(alert: AlertInfo) {
    const alertId = this.state.alertCount;
    const timeoutId = window.setTimeout(() => {
      this.setState(prevState => ({
        alertToClassNames: prevState.alertToClassNames.set(alertId, "fade-and-slide-out")
      }));
      window.setTimeout(() => this.removeAlert(alertId), 2000);
    }, 10000);
    this.setState(prevState => ({
      alerts: prevState.alerts.concat([{ ...alert, id: alertId, timeoutId }]),
      alertToClassNames: prevState.alertToClassNames.set(alertId, "fade-and-slide-in"),
      alertCount: prevState.alertCount + 1
    }));
  }
  
  removeAlert(alertId: number) {
    this.setState(prevState => {
      const alertToClassNameMap = new Map<number,string>(prevState.alertToClassNames);
      alertToClassNameMap.delete(alertId);
      return {
        alerts: prevState.alerts.filter(alert => alert.id !== alertId),
        alertToClassNames: alertToClassNameMap
      }
    });
  }

  setServerSideException(exception: ServerSideExceptionInfo) {
    this.setState({ serverErrorInfo: exception });
  }

  render() {
    return (
      <div style={{
        position: 'absolute',
        top: 0,
        right: 0,
        display: 'grid',
        gridAutoRows: 'auto',
        gridTemplateColumns: 'auto',
        paddingTop: '5px',
        gridRowGap: '5px',
        overflowY: 'auto',
        background: 'transparent',
        zIndex: 10000
      }}
      >
        {this.state.alerts.map(alert => (
          <Alert
            className={this.state.alertToClassNames.get(alert.id as number)}
            key={alert.id}
            title={alert.title}
            variant={alert.variant}
            onMouseEnter={() => window.clearTimeout(alert.timeoutId as number)}
            action={(
              <AlertActionCloseButton
                onClose={() => this.removeAlert(alert.id as number)} 
              />
            )}
          >
            {alert.message}
          </Alert>
        ))}
        <Modal
          title="Server Side Error"
          isOpen={this.state.serverErrorInfo !== null}
          onClose={() => this.setState({ serverErrorInfo: null })}
          actions={(
            <Button
              onClick={() => this.setState({ serverErrorInfo: null })}
            >
              Close
            </Button>
          )}
        >
          {createStackTrace(this.state.serverErrorInfo)}
        </Modal>
      </div>
    );
  }
}