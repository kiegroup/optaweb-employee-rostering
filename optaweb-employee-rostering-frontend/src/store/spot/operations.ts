
import { alert } from 'store/alert';
import { Spot } from 'domain/Spot';
import { AddAlertAction } from 'store/alert/types';
import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import {
  SetSpotListLoadingAction, AddSpotAction, RemoveSpotAction, UpdateSpotAction,
  RefreshSpotListAction,
} from './types';

export const addSpot: ThunkCommandFactory<Spot, AddAlertAction | AddSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  return client.post<Spot>(`/tenant/${tenantId}/spot/add`, spot).then((newSpot) => {
    dispatch(alert.showSuccessMessage('addSpot', { name: newSpot.name }));
    dispatch(actions.addSpot(newSpot));
  });
};

export const removeSpot:
ThunkCommandFactory<Spot, AddAlertAction | RemoveSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  const spotId = spot.id;
  return client.delete<boolean>(`/tenant/${tenantId}/spot/${spotId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeSpot', { name: spot.name }));
      dispatch(actions.removeSpot(spot));
    } else {
      dispatch(alert.showErrorMessage('removeSpotError', { name: spot.name }));
    }
  });
};

export const updateSpot:
ThunkCommandFactory<Spot, AddAlertAction | UpdateSpotAction> = spot => (dispatch, state, client) => {
  const { tenantId } = spot;
  return client.post<Spot>(`/tenant/${tenantId}/spot/update`, spot).then((updatedSpot) => {
    dispatch(alert.showSuccessMessage('updateSpot', { id: updatedSpot.id }));
    dispatch(actions.updateSpot(updatedSpot));
  });
};

export const refreshSpotList:
ThunkCommandFactory<void, SetSpotListLoadingAction | RefreshSpotListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsSpotListLoading(true));
  return client.get<Spot[]>(`/tenant/${tenantId}/spot/`).then((spotList) => {
    dispatch(actions.refreshSpotList(spotList));
    dispatch(actions.setIsSpotListLoading(false));
  });
};
