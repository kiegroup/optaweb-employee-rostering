
import * as tenantOperations from 'store/tenant/operations';
import { onPost } from 'store/rest/RestTestUtils';
import { alert } from 'store/alert';
import { doNothing } from 'types';
import { List } from 'immutable';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as adminOperations from './operations';

describe('Contract operations', () => {
  const mockRefreshTenantList = jest.spyOn(tenantOperations, 'refreshTenantList');

  beforeAll(() => {
    mockRefreshTenantList.mockImplementation(() => doNothing);
  });

  afterAll(() => {
    mockRefreshTenantList.mockRestore();
  });

  it('should dispatch actions and call client on reset application', async () => {
    const { store, client } = mockStore(state);

    onPost('/admin/reset', {}, {});
    await store.dispatch(adminOperations.resetApplication());
    expect(store.getActions()).toEqual([
      alert.showInfoMessage('resetApplicationSuccessful'),
    ]);

    expect(mockRefreshTenantList).toBeCalledTimes(1);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith('/admin/reset', {});
  });
});

const state: Partial<AppState> = {
  tenantData: {
    currentTenantId: 0,
    tenantList: List(),
    timezoneList: ['America/Toronto'],
  },
};
