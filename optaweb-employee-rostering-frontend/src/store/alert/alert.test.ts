import { ServerSideExceptionInfo, BasicObject } from 'types';
import { List } from 'immutable';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { alert } from './index';
import { AlertInfo, AlertComponent } from './types';

const state: Partial<AppState> = {
  alerts: {
    alertList: List([{
      id: 0,
      createdAt: new Date(),
      i18nKey: 'alert1',
      variant: 'info' as 'info',
      params: {},
      components: [],
      componentProps: [],
    }]),
    idGeneratorIndex: 1,
  },
};

describe('Alert operations', () => {
  it('should dispatch actions on showSuccessMessage', async () => {
    const { store } = mockStore(state);
    const i18nKey = 'test';

    store.dispatch(alert.showSuccessMessage(i18nKey));
    expect(store.getActions()).toEqual([alert.showMessage('success', i18nKey)]);
  });

  it('should dispatch actions on showInfoMessage', async () => {
    const { store } = mockStore(state);
    const i18nKey = 'test';

    store.dispatch(alert.showInfoMessage(i18nKey));
    expect(store.getActions()).toEqual([alert.showMessage('info', i18nKey)]);
  });

  it('should dispatch actions on showErrorMessage', async () => {
    const { store } = mockStore(state);
    const i18nKey = 'test';
    const params = { errorMsg: 'Hi' };

    store.dispatch(alert.showErrorMessage(i18nKey, params));
    expect(store.getActions()).toEqual([alert.showMessage('danger', i18nKey, params)]);
  });


  it('should dispatch actions on showServerError', async () => {
    const { store } = mockStore(state);
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

    store.dispatch(alert.showServerError(serverSideException));
    expect(store.getActions()).toEqual([
      alert.showMessage('danger', 'exception',
        { message: 'message1' },
        [AlertComponent.SERVER_SIDE_EXCEPTION_DIALOG],
        [serverSideException]),
    ]);
  });

  it('should dispatch actions on showServerErrorMessage', async () => {
    const { store } = mockStore(state);
    const i18nKey = 'generic';
    const params = { message: 'Hi' };

    store.dispatch(alert.showServerErrorMessage(params.message));
    expect(store.getActions()).toEqual([alert.showMessage('danger', i18nKey, params)]);
  });

  it('should dispatch actions on showMessage', async () => {
    const { store } = mockStore(state);
    const variant = 'success';
    const i18nKey = 'generic';

    store.dispatch(alert.showMessage(variant, i18nKey));
    expect(store.getActions()).toEqual([
      alert.addAlert({
        variant, i18nKey, params: {}, components: [], componentProps: [],
      }),
    ]);
  });

  it('should dispatch actions on addAlert', async () => {
    const { store } = mockStore(state);
    const alertInfo: AlertInfo = {
      i18nKey: 'key',
      variant: 'info',
      params: { name: 'ha' },
      components: [],
      componentProps: [],
    };

    store.dispatch(alert.addAlert(alertInfo));
    expect(store.getActions()).toEqual([
      actions.addAlert({ ...alertInfo, createdAt: new Date() }),
    ]);
  });

  it('should dispatch actions on removeAlert', async () => {
    const { store } = mockStore(state);
    const alertInfo: AlertInfo = {
      id: 1,
      i18nKey: 'key',
      variant: 'info',
      params: { name: 'ha' },
      components: [],
      componentProps: [],
    };

    store.dispatch(alert.removeAlert(alertInfo));
    expect(store.getActions()).toEqual([
      actions.removeAlert(1),
    ]);
  });
});

describe('Alert reducers', () => {
  const addedAlert: AlertInfo = {
    createdAt: new Date(),
    i18nKey: 'alert2',
    variant: 'success',
    params: {},
    components: [],
    componentProps: [],
  };
  const removedAlertId = 0;
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('add an alert', () => {
    expect(
      reducer(state.alerts, actions.addAlert(addedAlert)),
    ).toEqual({ idGeneratorIndex: 2, alertList: storeState.alerts.alertList.push({ ...addedAlert, id: 1 }) });
  });

  it('remove an alert', () => {
    expect(
      reducer(state.alerts, actions.removeAlert(removedAlertId)),
    ).toEqual({ ...state.alerts,
      alertList: storeState.alerts.alertList
        .filterNot(a => a.id === removedAlertId) });
  });
});
