import { ServerSideExceptionInfo, BasicObject } from 'types';
import * as actions from './actions';
import { AlertInfo, AddAlertAction, RemoveAlertAction, AlertComponent } from './types';

export function showInfoMessage(i18nKey: string, params?: BasicObject, components?: AlertComponent[],
  componentProps?: BasicObject[]): AddAlertAction {
  return showMessage('info', i18nKey, params, components, componentProps);
}

export function showSuccessMessage(i18nKey: string, params?: BasicObject, components?: AlertComponent[],
  componentProps?: BasicObject[]): AddAlertAction {
  return showMessage('success', i18nKey, params, components, componentProps);
}

export function showServerError(exceptionInfo: ServerSideExceptionInfo & BasicObject): AddAlertAction {
  return addAlert({
    i18nKey: 'exception',
    variant: 'danger',
    params: {
      message: exceptionInfo.exceptionMessage,
    },
    components: [AlertComponent.SERVER_SIDE_EXCEPTION_DIALOG],
    componentProps: [exceptionInfo],
  });
}

export function showServerErrorMessage(message: string): AddAlertAction {
  return showErrorMessage('generic', { message });
}

export function showErrorMessage(i18nKey: string, params?: BasicObject, components?: AlertComponent[],
  componentProps?: BasicObject[]): AddAlertAction {
  return showMessage('danger', i18nKey, params, components, componentProps);
}

export function showMessage(variant: 'success' | 'danger' | 'warning' | 'info', i18nKey: string,
  params?: BasicObject, components?: AlertComponent[], componentProps?: BasicObject[]): AddAlertAction {
  return addAlert({
    i18nKey,
    variant,
    params: params || {},
    components: components || [],
    componentProps: componentProps || [],
  });
}

export function addAlert(alert: AlertInfo): AddAlertAction {
  return actions.addAlert({
    ...alert,
    createdAt: new Date(),
  });
}

export function removeAlert(alert: AlertInfo): RemoveAlertAction {
  return actions.removeAlert(alert.id as number);
}
