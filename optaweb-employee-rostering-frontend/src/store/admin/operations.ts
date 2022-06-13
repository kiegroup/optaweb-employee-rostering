
import { Action } from 'redux';
import { refreshTenantList } from 'store/tenant/operations';
import { alert } from 'store/alert';
import { ThunkCommandFactory } from '../types';

export const resetApplication:
ThunkCommandFactory<void, Action<any>> = () => (dispatch, state, client) => {
  client.post<void>('/admin/reset', {}).then(() => {
    dispatch(alert.showInfoMessage('resetApplicationSuccessful'));
    dispatch(refreshTenantList());
  });
};
