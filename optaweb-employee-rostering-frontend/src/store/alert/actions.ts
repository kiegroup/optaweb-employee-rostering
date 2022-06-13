
import { ActionFactory } from '../types';
import { ActionType, AlertInfo, AddAlertAction, RemoveAlertAction } from './types';

export const addAlert: ActionFactory<AlertInfo, AddAlertAction> = newAlert => ({
  type: ActionType.ADD_ALERT,
  alertInfo: newAlert,
});

export const removeAlert: ActionFactory<number, RemoveAlertAction> = id => ({
  type: ActionType.REMOVE_ALERT,
  id,
});
