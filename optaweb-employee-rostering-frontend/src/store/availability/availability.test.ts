
import { alert } from 'store/alert';
import * as rosterOperations from 'store/roster/operations';
import { onPost, onPut, onDelete } from 'store/rest/RestTestUtils';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import moment from 'moment';
import { Contract } from 'domain/Contract';
import { serializeLocalDateTime } from 'store/rest/DataSerialization';
import { availabilityAdapter, kindaAvailabilityViewAdapter, KindaEmployeeAvailabilityView } from './operations';
import { availabilityOperations } from './index';
import { AppState } from '../types';
import { mockStore } from '../mockStore';

const state: Partial<AppState> = {
  // Empty as we can just use default state for this test
};

const contract: Contract = {
  tenantId: 0,
  id: 100,
  version: 0,
  name: 'Contract',
  maximumMinutesPerDay: null,
  maximumMinutesPerMonth: null,
  maximumMinutesPerWeek: null,
  maximumMinutesPerYear: null,
};

describe('Availability operations', () => {
  it('should dispatch actions and call client on addAvailability', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshAvailabilityRoster = jest.spyOn(rosterOperations, 'refreshAvailabilityRoster');

    const addedAvailability: EmployeeAvailability = {
      tenantId,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employee: {
        tenantId: 0,
        id: 20,
        name: 'Employee',
        skillProficiencySet: [],
        contract,
        shortId: 'e',
        color: '#FFFFFF',
      },
      state: 'DESIRED',
    };

    onPost(`/tenant/${tenantId}/employee/availability/add`, availabilityAdapter(addedAvailability),
      availabilityAdapter(addedAvailability));

    await store.dispatch(availabilityOperations.addEmployeeAvailability(addedAvailability));

    expect(client.post).toBeCalled();
    expect(client.post).toBeCalledWith(`/tenant/${tenantId}/employee/availability/add`,
      availabilityAdapter(addedAvailability));
    expect(mockRefreshAvailabilityRoster).toBeCalled();
  });

  it('should dispatch actions and call client on a successful delete Availability', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshAvailabilityRoster = jest.spyOn(rosterOperations, 'refreshAvailabilityRoster');

    const deletedAvailability: EmployeeAvailability = {
      tenantId,
      id: 10,
      version: 0,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employee: {
        tenantId: 0,
        id: 20,
        name: 'Employee',
        skillProficiencySet: [],
        contract,
        shortId: 'e',
        color: '#FFFFFF',
      },
      state: 'DESIRED',
    };

    onDelete(`/tenant/${tenantId}/employee/availability/${deletedAvailability.id}`, true);

    await store.dispatch(availabilityOperations.removeEmployeeAvailability(deletedAvailability));

    expect(client.delete).toBeCalled();
    expect(client.delete).toBeCalledWith(`/tenant/${tenantId}/employee/availability/${deletedAvailability.id}`);
    expect(mockRefreshAvailabilityRoster).toBeCalled();
  });

  it('should call client but not dispatch actions on a failed delete Availability', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshAvailabilityRoster = jest.spyOn(rosterOperations, 'refreshAvailabilityRoster');
    const mockShowErrorMessage = jest.spyOn(alert, 'showErrorMessage');

    const deletedAvailability: EmployeeAvailability = {
      tenantId,
      id: 10,
      version: 0,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employee: {
        tenantId: 0,
        id: 20,
        name: 'Employee',
        skillProficiencySet: [],
        contract,
        shortId: 'e',
        color: '#FFFFFF',
      },
      state: 'DESIRED',
    };

    onDelete(`/tenant/${tenantId}/employee/availability/${deletedAvailability.id}`, false);

    await store.dispatch(availabilityOperations.removeEmployeeAvailability(deletedAvailability));

    expect(client.delete).toBeCalled();
    expect(client.delete).toBeCalledWith(`/tenant/${tenantId}/employee/availability/${deletedAvailability.id}`);
    expect(mockRefreshAvailabilityRoster).not.toBeCalled();

    expect(mockShowErrorMessage).toBeCalled();
    expect(mockShowErrorMessage).toBeCalledWith('removeAvailabilityError', {
      employeeName: 'Employee',
      startDateTime: moment(availabilityStartTime).format('LLL'),
      endDateTime: moment(availabilityEndTime).format('LLL'),
    });
  });

  it('should dispatch actions and call client on updateAvailability', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();

    const mockRefreshAvailabilityRoster = jest.spyOn(rosterOperations, 'refreshAvailabilityRoster');

    const updatedAvailability: EmployeeAvailability = {
      tenantId,
      id: 10,
      version: 0,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employee: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        skillProficiencySet: [],
        contract,
        shortId: 'e',
        color: '#FFFFFF',
      },
      state: 'DESIRED',
    };

    const updatedAvailabilityWithUpdatedVersion: EmployeeAvailability = {
      ...updatedAvailability,
      version: 1,
    };

    onPut(`/tenant/${tenantId}/employee/availability/update`, availabilityAdapter(updatedAvailability),
      availabilityAdapter(updatedAvailabilityWithUpdatedVersion));

    await store.dispatch(availabilityOperations.updateEmployeeAvailability(updatedAvailability));

    expect(client.put).toBeCalled();
    expect(client.put).toBeCalledWith(`/tenant/${tenantId}/employee/availability/update`,
      availabilityAdapter(updatedAvailability));
    expect(mockRefreshAvailabilityRoster).toBeCalled();
  });
});

describe('Availability adapters', () => {
  it('availabilityAdapter should convert a Availability to a KindaAvailabilityView', () => {
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();
    const availability: EmployeeAvailability = {
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employee: {
        tenantId: 0,
        id: 20,
        name: 'Spot',
        skillProficiencySet: [],
        contract,
        shortId: 'e',
        color: '#FFFFFF',
      },
      state: 'DESIRED',
    };

    expect(availabilityAdapter(availability)).toEqual({
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: serializeLocalDateTime(availabilityStartTime),
      endDateTime: serializeLocalDateTime(availabilityEndTime),
      employeeId: 20,
      state: 'DESIRED',
    });
  });

  it('kindaAvailabilityAdapter should convert a KindaEmployeeAvailabilityView to an EmployeeAvailabilityView', () => {
    const availabilityStartTime = moment('2018-01-01T09:00').toDate();
    const availabilityEndTime = moment('2018-01-01T12:00').toDate();
    const kindaAvailabilityView: KindaEmployeeAvailabilityView = {
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: serializeLocalDateTime(availabilityStartTime),
      endDateTime: serializeLocalDateTime(availabilityEndTime),
      employeeId: 10,
      state: 'DESIRED',
    };

    expect(kindaAvailabilityViewAdapter(kindaAvailabilityView)).toEqual({
      tenantId: 0,
      id: 11,
      version: 0,
      startDateTime: availabilityStartTime,
      endDateTime: availabilityEndTime,
      employeeId: 10,
      state: 'DESIRED',
    });
  });
});
