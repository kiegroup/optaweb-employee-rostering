
import { Action } from 'redux';
import { BasicObject } from 'types';
import { List } from 'immutable';

export enum ActionType {
  ADD_ALERT = 'ADD_ALERT',
  REMOVE_ALERT = 'REMOVE_ALERT'
}

export interface AddAlertAction extends Action<ActionType.ADD_ALERT> {
  readonly alertInfo: AlertInfo;
}

export interface RemoveAlertAction extends Action<ActionType.REMOVE_ALERT> {
  readonly id: number;
}

export type AlertAction = AddAlertAction | RemoveAlertAction;

export enum AlertComponent {
  SERVER_SIDE_EXCEPTION_DIALOG = 'SERVER_SIDE_EXCEPTION_DIALOG'
}

export interface AlertInfo {
  id?: number;
  createdAt?: Date;
  i18nKey: string;
  variant: 'success' | 'danger' | 'warning' | 'info';
  params: BasicObject;
  components: AlertComponent[];
  componentProps: BasicObject[];
}

export interface AlertList {
  readonly alertList: List<AlertInfo>;
  readonly idGeneratorIndex: number;
}
