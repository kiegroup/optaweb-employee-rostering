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

import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Alerts, Props } from './Alerts';
import { mockTranslate } from 'setupTests';
import moment from 'moment';

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
    expect(mockTranslate).toHaveBeenNthCalledWith(1, "alerts.infoMessage.title");
    expect(mockTranslate).toHaveBeenNthCalledWith(2, "alerts.successMessage.title");
    expect(mockTranslate).toHaveBeenNthCalledWith(3, "alerts.dangerMessage.title");

    expect(toJson(alertsElement)).toMatchSnapshot();
  });
});

const noAlerts: Props = {
  alerts: [],
  removeAlert: jest.fn()
}

const date = new Date(); // setupTests set the date to a mock date for us
const someAlerts: Props = {
  alerts: [
    {
      id: 0,
      createdAt: date,
      i18nKey: "infoMessage",
      variant: "info",
      params: {}
    },
    {
      id: 1,
      createdAt: moment(date).add(4, "seconds").toDate(),
      i18nKey: "successMessage",
      variant: "success",
      params: {}
    },
    {
      id: 2,
      createdAt: moment(date).add(11, "seconds").toDate(),
      i18nKey: "dangerMessage",
      variant: "danger",
      params: {}
    }
  ],
  removeAlert: jest.fn()
}