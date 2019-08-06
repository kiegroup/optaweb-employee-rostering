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
import Shift from 'domain/Shift';
import ShiftView, { shiftToShiftView } from 'domain/ShiftView';
import moment from 'moment';
import { alert } from 'store/alert';
import { refreshShiftRoster, refreshAvailabilityRoster } from 'store/roster/operations';
import { getHardMediumSoftScoreFromString } from 'domain/HardMediumSoftScore';
import { objectWithout } from 'util/ImmutableCollectionOperations';

type KindaShiftView1 = Pick<ShiftView, Exclude<keyof ShiftView, "indictmentScore">> & { indictmentScore?: string };
type KindaShiftView2 = Pick<KindaShiftView1, Exclude<keyof KindaShiftView1, "startDateTime">> & { startDateTime: string };
type KindaShiftView3 = Pick<KindaShiftView2, Exclude<keyof KindaShiftView2, "endDateTime">> & { endDateTime: string };
export type KindaShiftView = KindaShiftView3;

export function shiftAdapter(shift: Shift): KindaShiftView {
  return {
    ...objectWithout(shiftToShiftView(shift), "indictmentScore", "startDateTime", "endDateTime",
      ...Object.keys(shift).filter(k => Array.isArray((shift as {[P: string]: any})[k])) as (keyof ShiftView)[]) as any,
    startDateTime: moment(shift.startDateTime).local().format("YYYY-MM-DDTHH:mm:ss"),
    endDateTime: moment(shift.endDateTime).local().format("YYYY-MM-DDTHH:mm:ss")
  };
}

export function kindaShiftViewAdapter(kindaShiftView: KindaShiftView): ShiftView {
  const kindaShiftViewClone: any = { ...kindaShiftView };
  kindaShiftViewClone.indictmentScore = getHardMediumSoftScoreFromString(kindaShiftView.indictmentScore as string);
  
  // Since property P is related to indictments iff it is an array,
  // We can convert all indictments by mapping all keys that are arrays
  for(const key in kindaShiftViewClone) {
    if (Array.isArray(kindaShiftViewClone[key])) {
      kindaShiftViewClone[key] = kindaShiftViewClone[key].map((s: any) => ({ ...s, score: getHardMediumSoftScoreFromString(s.score) }));
    }
  }

  return {
    ...kindaShiftViewClone,
    startDateTime: moment(kindaShiftView.startDateTime).toDate(),
    endDateTime: moment(kindaShiftView.endDateTime).toDate(),
  };
}

export const addShift: ThunkCommandFactory<Shift, any> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.post<KindaShiftView>(`/tenant/${tenantId}/shift/add`, shiftAdapter(shift)).then(newShift => {
      dispatch(alert.showSuccessMessage("addShift", { startDateTime: moment(newShift.startDateTime).format("LLL"), endDateTime: moment(newShift.endDateTime).format("LLL") }));
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    });
  };

export const removeShift: ThunkCommandFactory<Shift, any> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    const shiftId = shift.id;
    return client.delete<boolean>(`/tenant/${tenantId}/shift/${shiftId}`).then(isSuccess => {
      if (isSuccess) {
        dispatch(alert.showSuccessMessage("removeShift", { id: shift.id, startDateTime: moment(shift.startDateTime).format("LLL"), endDateTime: moment(shift.endDateTime).format("LLL") }));
        dispatch(refreshShiftRoster());
        dispatch(refreshAvailabilityRoster());
      }
      else {
        dispatch(alert.showErrorMessage("removeShiftError", { id: shift.id, startDateTime: moment(shift.startDateTime).format("LLL"), endDateTime: moment(shift.endDateTime).format("LLL") }));
      }
    });
  };

export const updateShift: ThunkCommandFactory<Shift, any> = shift =>
  (dispatch, state, client) => {
    const tenantId = shift.tenantId;
    return client.put<KindaShiftView>(`/tenant/${tenantId}/shift/update`, shiftAdapter(shift)).then(updatedShift => {
      dispatch(alert.showSuccessMessage("updateShift", { id: updatedShift.id }));
      dispatch(refreshShiftRoster());
      dispatch(refreshAvailabilityRoster());
    });
  };