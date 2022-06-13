import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { mockTranslate } from 'setupTests';
import { AlertComponent } from 'store/alert/types';
import moment from 'moment';
import { ServerSideExceptionInfo, BasicObject } from 'types';
import { Alerts, Props, mapToComponent } from './Alerts';

describe('Alerts', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly without alerts', () => {
    const alertsElement = shallow(<Alerts {...noAlerts} />);
    expect(toJson(alertsElement)).toMatchSnapshot();
  });

  it('should render correctly with alerts', () => {
    const alertsElement = shallow(<Alerts {...someAlerts} />);
    expect(mockTranslate).toBeCalledTimes(3);
    expect(mockTranslate).toHaveBeenNthCalledWith(1, 'infoMessage.title');
    expect(mockTranslate).toHaveBeenNthCalledWith(2, 'successMessage.title');
    expect(mockTranslate).toHaveBeenNthCalledWith(3, 'dangerMessage.title');

    expect(toJson(alertsElement)).toMatchSnapshot();
  });

  it('should render correctly with alerts with components', () => {
    const alertsElement = shallow(<Alerts {...someAlertsWithComponents} />);
    expect(mockTranslate).toBeCalledTimes(1);
    expect(mockTranslate).toHaveBeenNthCalledWith(1, 'exception.title');

    expect(toJson(alertsElement)).toMatchSnapshot();
  });

  it('should render the correct component for AlertComponent.SERVER_SIDE_EXCEPTION_DIALOG', () => {
    const serverSideException: ServerSideExceptionInfo & BasicObject = {
      i18nKey: 'error1',
      exceptionMessage: 'message1',
      exceptionClass: 'Error1',
      messageParameters: ['hi'],
      stackTrace: ['1.1', '1.2', '1.3'],
      exceptionCause: {
        i18nKey: 'error2',
        exceptionMessage: 'message2',
        exceptionClass: 'Error2',
        messageParameters: [],
        stackTrace: ['2.1', '2.2', '2.3'],
        exceptionCause: null,
      },
    };

    const component = mapToComponent(AlertComponent.SERVER_SIDE_EXCEPTION_DIALOG, serverSideException);
    expect(component).toMatchSnapshot();
  });
});

const noAlerts: Props = {
  alerts: [],
  removeAlert: jest.fn(),
};

const date = new Date(); // setupTests set the date to a mock date for us
const someAlerts: Props = {
  alerts: [
    {
      id: 0,
      createdAt: date,
      i18nKey: 'infoMessage',
      variant: 'info',
      params: {},
      components: [],
      componentProps: [],
    },
    {
      id: 1,
      createdAt: moment(date).add(4, 'seconds').toDate(),
      i18nKey: 'successMessage',
      variant: 'success',
      params: {},
      components: [],
      componentProps: [],
    },
    {
      id: 2,
      createdAt: moment(date).add(11, 'seconds').toDate(),
      i18nKey: 'dangerMessage',
      variant: 'danger',
      params: {},
      components: [],
      componentProps: [],
    },
  ],
  removeAlert: jest.fn(),
};

const someAlertsWithComponents: Props = {
  alerts: [
    {
      id: 0,
      createdAt: date,
      i18nKey: 'exception',
      variant: 'danger',
      params: {},
      components: [AlertComponent.SERVER_SIDE_EXCEPTION_DIALOG],
      componentProps: [{
        i18nKey: 'error1',
        exceptionMessage: 'message1',
        exceptionClass: 'Error1',
        messageParameters: ['hi'],
        stackTrace: ['1.1', '1.2', '1.3'],
        exceptionCause: {
          i18nKey: 'error2',
          exceptionMessage: 'message2',
          exceptionClass: 'Error2',
          messageParameters: [],
          stackTrace: ['2.1', '2.2', '2.3'],
          exceptionCause: null,
        },
      }],
    },
  ],
  removeAlert: jest.fn(),
};
