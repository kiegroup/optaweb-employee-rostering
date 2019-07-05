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

import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import Shift from 'domain/Shift';
import ShiftView, { shiftToShiftView } from 'domain/ShiftView';
import { SetShiftListLoadingAction, AddShiftAction, RemoveShiftAction, UpdateShiftAction, RefreshShiftListAction } from './types';
import moment from 'moment';
import Spot from 'domain/Spot';
import Employee from 'domain/Employee';
import DomainObject from 'domain/DomainObject';
import { refreshShiftRoster } from 'store/roster/operations';

interface KindaShift extends DomainObject {
  startDateTime: string;
  endDateTime: string;
  spot: Spot;
  rotationEmployee: Employee | null;
  employee: Employee | null;
  pinnedByUser: boolean;
}

interface KindaShiftView extends DomainObject {
  startDateTime: string;
  endDateTime: string;
  spotId: number;
  rotationEmployeeId: number | null;
  employeeId: number | null;
  pinnedByUser: boolean;
}

function shiftAdapter(shift: Shift): KindaShiftView {
  return {
    ...shiftToShiftView(shift),
    startDateTime: moment(shift.startDateTime).local().format("YYYY-MM-DDTHH:mm:ss"),
    endDateTime: moment(shift.endDateTime).local().format("YYYY-MM-DDTHH:mm:ss")
  };
}

function kindaShiftViewAdapter(kindaShiftView: KindaShiftView): ShiftView {
  return {
    ...kindaShiftView,
    startDateTime: moment(kindaShiftView.startDateTime).toDate(),
    endDateTime: moment(kindaShiftView.endDateTime).toDate()
  };
}

export const addShift: ThunkCommandFactory<Shift, AddShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.post<KindaShiftView>(`/tenant/${tenantId}/shift/add`, shiftAdapter(shift)).then(newShift => {
      dispatch(actions.addShift(kindaShiftViewAdapter(newShift)))
      dispatch(refreshShiftRoster());
    });
  };

export const removeShift: ThunkCommandFactory<Shift, RemoveShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    const shiftId = shift.id;
    return client.delete<boolean>(`/tenant/${tenantId}/shift/${shiftId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(actions.removeShift(shiftToShiftView(shift)));
        dispatch(refreshShiftRoster());
      }
    });
  };

export const updateShift: ThunkCommandFactory<Shift, UpdateShiftAction> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.put<KindaShiftView>(`/tenant/${tenantId}/shift/update`, shiftAdapter(shift)).then(updatedShift => {
      dispatch(actions.updateShift(kindaShiftViewAdapter(updatedShift)));
      dispatch(refreshShiftRoster());
    });
  };

export const refreshShiftList: ThunkCommandFactory<void, SetShiftListLoadingAction | RefreshShiftListAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setIsShiftListLoading(true));
    return client.get<KindaShiftView[]>(`/tenant/${tenantId}/shift/`).then(shiftList => {
      dispatch(actions.refreshShiftList(shiftList.map(kindaShiftViewAdapter)));
      dispatch(actions.setIsShiftListLoading(false));
      dispatch(refreshShiftRoster());
    });
  };
