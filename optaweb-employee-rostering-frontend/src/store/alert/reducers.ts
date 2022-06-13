
import { List } from 'immutable';
import { ActionType, AlertList, AlertAction } from './types';

export const initialState: AlertList = {
  alertList: List(),
  idGeneratorIndex: 0,
};

const alertReducer = (state = initialState, action: AlertAction): AlertList => {
  switch (action.type) {
    case ActionType.ADD_ALERT: {
      const alertWithId = { ...action.alertInfo, id: state.idGeneratorIndex };
      const nextIndex = state.idGeneratorIndex + 1;
      return { ...state, idGeneratorIndex: nextIndex, alertList: state.alertList.push(alertWithId) };
    }
    case ActionType.REMOVE_ALERT: {
      return { ...state, alertList: state.alertList.filterNot(alert => alert.id === action.id) };
    }
    default:
      return state;
  }
};

export default alertReducer;
