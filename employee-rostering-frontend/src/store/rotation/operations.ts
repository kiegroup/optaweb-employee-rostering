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

import { ShiftTemplate } from 'domain/ShiftTemplate';
import { alert } from 'store/alert';
import { AddAlertAction } from 'store/alert/types';
import {
  shiftTemplateToShiftTemplateView,
  shiftTemplateViewToDomainObjectView,
  ShiftTemplateView,
} from 'store/rotation/ShiftTemplateView';
import {
  SetShiftTemplateListLoadingAction, AddShiftTemplateAction, RemoveShiftTemplateAction,
  UpdateShiftTemplateAction, RefreshShiftTemplateListAction,
} from './types';
import * as actions from './actions';
import { ThunkCommandFactory } from '../types';

export const addShiftTemplate: ThunkCommandFactory<ShiftTemplate, AddAlertAction |
AddShiftTemplateAction> = shiftTemplate => (dispatch, state, client) => {
  const { tenantId } = shiftTemplate;
  const view = shiftTemplateToShiftTemplateView(shiftTemplate);
  return client.post<ShiftTemplateView>(`/tenant/${tenantId}/rotation/add`, view).then((newShiftTemplate) => {
    dispatch(actions.addShiftTemplate(shiftTemplateViewToDomainObjectView(newShiftTemplate)));
  });
};

export const removeShiftTemplate: ThunkCommandFactory<ShiftTemplate, AddAlertAction |
RemoveShiftTemplateAction> = shiftTemplate => (dispatch, state, client) => {
  const { tenantId } = shiftTemplate;
  const shiftTemplateId = shiftTemplate.id;
  return client.delete<boolean>(`/tenant/${tenantId}/rotation/${shiftTemplateId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(actions.removeShiftTemplate(
        shiftTemplateViewToDomainObjectView(shiftTemplateToShiftTemplateView(shiftTemplate)),
      ));
    } else {
      dispatch(alert.showErrorMessage('removeShiftTemplateError', { id: shiftTemplateId }));
    }
  });
};

export const updateShiftTemplate: ThunkCommandFactory<ShiftTemplate, AddAlertAction |
UpdateShiftTemplateAction> = shiftTemplate => (dispatch, state, client) => {
  const { tenantId } = shiftTemplate;
  const view = shiftTemplateToShiftTemplateView(shiftTemplate);
  return client.put<ShiftTemplateView>(`/tenant/${tenantId}/rotation/update`, view).then((updatedShiftTemplate) => {
    dispatch(actions.updateShiftTemplate(shiftTemplateViewToDomainObjectView(updatedShiftTemplate)));
  });
};

export const refreshShiftTemplateList: ThunkCommandFactory<void, SetShiftTemplateListLoadingAction |
RefreshShiftTemplateListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsShiftTemplateListLoading(true));
  return client.get<ShiftTemplateView[]>(`/tenant/${tenantId}/rotation/`).then((shiftTemplateList) => {
    dispatch(actions.refreshShiftTemplateList(shiftTemplateList.map(shiftTemplateViewToDomainObjectView)));
    dispatch(actions.setIsShiftTemplateListLoading(false));
  });
};
