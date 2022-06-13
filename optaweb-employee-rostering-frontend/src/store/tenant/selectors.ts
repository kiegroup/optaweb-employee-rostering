import { AppState } from 'store/types';

export const getTenantId = (state: AppState) => state.tenantData.currentTenantId;

export const getTenantList = (state: AppState) => state.tenantData.tenantList;
