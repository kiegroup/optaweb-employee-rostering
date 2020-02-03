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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import { ServerSideExceptionDialog } from './ServerSideExceptionDialog';

describe('ServerSideExceptionDialog', () => {
  it('should render an ServerSideException Alert correctly', () => {
    const serverSideException: ServerSideExceptionInfo = {
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

    const serverSideExceptionDialog = shallow(
      <ServerSideExceptionDialog {...serverSideException}>Show Stack Trace</ServerSideExceptionDialog>,
    );
    expect(toJson(serverSideExceptionDialog)).toMatchSnapshot();
  });

  it('should render an ServerSideException Modal correctly', () => {
    const serverSideException: ServerSideExceptionInfo = {
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

    const serverSideExceptionDialog = shallow(
      <ServerSideExceptionDialog {...serverSideException}>Show Stack Trace</ServerSideExceptionDialog>,
    );
    serverSideExceptionDialog.find('[aria-label="Show Stack Trace"]').simulate('click');
    expect(toJson(serverSideExceptionDialog)).toMatchSnapshot();
  });
});
