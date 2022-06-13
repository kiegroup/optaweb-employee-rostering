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
