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
