/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import AlertsElement, * as alerts from './Alerts';
import { Button, ButtonVariant } from '@patternfly/react-core';
import { ServerSideExceptionInfo } from 'types';

describe('Alerts', () => {
  it('showMessage should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    alerts.showMessage("success", "title", "message");
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "title",
      variant: "success",
      message: <span>message</span>
    });
  });

  it('showInfoMessage should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    alerts.showInfoMessage("title", "message");
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "title",
      variant: "info",
      message: <span>message</span>
    });
  });

  it('showErrorMessage should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    alerts.showErrorMessage("title", "message");
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "title",
      variant: "danger",
      message: <span>message</span>
    });
  });

  it('showSuccessMessage should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    alerts.showSuccessMessage("title", "message");
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "title",
      variant: "success",
      message: <span>message</span>
    });
  });

  it('showServerErrorMessage should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    alerts.showServerErrorMessage("message");
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "Server Error",
      variant: "danger",
      message: <span>message</span>
    });
  });

  it('showServerError should work correctly', () => {
    const mockAlertsElement: AlertsElement = {
      addAlert: jest.fn(),
      setServerSideException: jest.fn()
    } as any as AlertsElement; 
    alerts.setAlertRef(mockAlertsElement);
    
    const serverErrorInfo: ServerSideExceptionInfo = {
      i18nKey: "i18nKey1",
      exceptionClass: "Exception1",
      exceptionMessage: "Server Error 1",
      messageParameters: [],
      stackTrace: ["Line 1.1", "Line 1.2", "Line 1.3"],
      exceptionCause: {
        i18nKey: "i18nKey2",
        exceptionClass: "Exception2",
        exceptionMessage: "Server Error 2",
        messageParameters: [],
        stackTrace: ["Line 2.1", "Line 2.2", "Line 2.3"],
        exceptionCause: null
      }
    };

    alerts.showServerError(serverErrorInfo);
    expect(mockAlertsElement.addAlert).toBeCalled();
    expect(mockAlertsElement.addAlert).toBeCalledWith({
      title: "Server Error",
      variant: "danger",
      message: (
        <span>
          Server Error 1
          <Button
            variant={ButtonVariant.link}
            onClick={expect.any(Function)}
          >
            See Stack Trace.
          </Button>
        </span>
      )
    });

    const errorMessage = (mockAlertsElement.addAlert as jest.Mock).mock.calls[0][0].message;
    shallow(errorMessage).find(Button).simulate('click')
    expect(mockAlertsElement.setServerSideException).toBeCalled();
    expect(mockAlertsElement.setServerSideException).toBeCalledWith(serverErrorInfo);
  });

  it('Alerts constructor should work correctly', () => {
    const mockSetRef = jest.fn();
    const alertsElement = new AlertsElement({ setRef: mockSetRef });

    expect(mockSetRef).toBeCalled();
    expect(mockSetRef).toBeCalledWith(alertsElement);
    expect(alertsElement.state).toEqual({
      alertCount: 0,
      alerts: [],
      serverErrorInfo: null,
      alertToClassNames: new Map()
    });
  });

  it('addAlert should work correctly', () => {
    jest.useFakeTimers();
    const alertsElement = mount(<AlertsElement setRef={jest.fn()} />);
    const alert: alerts.AlertInfo = {
      title: "title",
      variant: "info",
      message: <span>message</span>
    };
    (alertsElement.instance() as AlertsElement).addAlert(alert);
    expect((alertsElement.instance() as AlertsElement).state).toEqual({
      alertCount: 1,
      alertToClassNames: new Map([[
        0, "fade-and-slide-in"
      ]]),
      serverErrorInfo: null,
      alerts: [{
        ...alert,
        id: 0,
        timeoutId: expect.any(Number)
      }]
    });

    jest.runOnlyPendingTimers();
    expect((alertsElement.instance() as AlertsElement).state).toEqual({
      alertCount: 1,
      alertToClassNames: new Map([[
        0, "fade-and-slide-out"
      ]]),
      serverErrorInfo: null,
      alerts: [{
        ...alert,
        id: 0,
        timeoutId: expect.any(Number)
      }]
    });

    jest.runOnlyPendingTimers();
    expect((alertsElement.instance() as AlertsElement).state).toEqual({
      alertCount: 1,
      alertToClassNames: new Map(),
      serverErrorInfo: null,
      alerts: []
    });
  });

  it('removeAlert should work correctly', () => {
    jest.useFakeTimers();
    const alertsElement = mount(<AlertsElement setRef={jest.fn()} />);
    const alert1: alerts.AlertInfo = {
      title: "title 1",
      variant: "info",
      message: <span>message 1</span>
    };
    const alert2: alerts.AlertInfo = {
      title: "title 2",
      variant: "success",
      message: <span>message 2</span>
    };
    const alert3: alerts.AlertInfo = {
      title: "title 3",
      variant: "danger",
      message: <span>message 3</span>
    };

    (alertsElement.instance() as AlertsElement).addAlert(alert1);
    (alertsElement.instance() as AlertsElement).addAlert(alert2);
    (alertsElement.instance() as AlertsElement).addAlert(alert3);

    (alertsElement.instance() as AlertsElement).removeAlert(1);
    expect((alertsElement.instance() as AlertsElement).state).toEqual({
      alertCount: 3,
      alertToClassNames: new Map([
        [0, "fade-and-slide-in"],
        [2, "fade-and-slide-in"]
      ]),
      serverErrorInfo: null,
      alerts: [
        { ...alert1, id: 0, timeoutId: expect.any(Number) },
        { ...alert3, id: 2, timeoutId: expect.any(Number) }
      ]
    });
  });

  it('setServerSideException should work correctly', () => {
    const alertsElement = mount(<AlertsElement setRef={jest.fn()} />);
    const serverSideException: ServerSideExceptionInfo = {
      i18nKey: "i18nKey1",
      exceptionClass: "Exception1",
      exceptionMessage: "Server Error 1",
      messageParameters: [],
      stackTrace: ["Line 1.1", "Line 1.2", "Line 1.3"],
      exceptionCause: {
        i18nKey: "i18nKey2",
        exceptionClass: "Exception2",
        exceptionMessage: "Server Error 2",
        messageParameters: [],
        stackTrace: ["Line 2.1", "Line 2.2", "Line 2.3"],
        exceptionCause: null
      }
    };
    (alertsElement.instance() as AlertsElement).setServerSideException(serverSideException);
    expect((alertsElement.instance() as AlertsElement).state).toEqual({
      alertCount: 0,
      alertToClassNames: new Map(),
      serverErrorInfo: serverSideException,
      alerts: []
    });
  });

  it('should render correctly without alerts', () => {
    const alertsElement = shallow(<AlertsElement setRef={jest.fn()} />);
    expect(toJson(alertsElement)).toMatchSnapshot();
  });

  it('should render correctly with alerts', () => {
    const alertsElement = shallow(<AlertsElement setRef={alerts.setAlertRef} />);
    alerts.showInfoMessage("Info", "Message 1");
    alerts.showSuccessMessage("Success", "Message 2");
    alerts.showErrorMessage("Error", "Message 3");

    expect(toJson(alertsElement)).toMatchSnapshot();
  });

  it('should render correctly with a server side exception', () => {
    const alertsElement = shallow(<AlertsElement setRef={alerts.setAlertRef} />);
    alerts.showInfoMessage("Info", "Message 1");
    alerts.showSuccessMessage("Success", "Message 2");
    (alertsElement.instance() as AlertsElement).setServerSideException({
      i18nKey: "i18nKey1",
      exceptionClass: "Exception1",
      exceptionMessage: "Server Error 1",
      messageParameters: [],
      stackTrace: ["Line 1.1", "Line 1.2", "Line 1.3"],
      exceptionCause: {
        i18nKey: "i18nKey2",
        exceptionClass: "Exception2",
        exceptionMessage: "Server Error 2",
        messageParameters: [],
        stackTrace: ["Line 2.1", "Line 2.2", "Line 2.3"],
        exceptionCause: null
      }
    });
    
    expect(toJson(alertsElement)).toMatchSnapshot();
  });
});