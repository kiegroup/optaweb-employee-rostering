
import { alert } from 'store/alert';
import { createIdMapFromList, mapDomainObjectToView } from 'util/ImmutableCollectionOperations';
import { onGet, onPost, onDelete, onUploadFile } from 'store/rest/RestTestUtils';
import { Employee } from 'domain/Employee';
import * as skillActions from 'store/skill/actions';
import * as contractActions from 'store/contract/actions';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import reducer, { employeeSelectors, employeeOperations } from './index';

const state: Partial<AppState> = {
  employeeList: {
    isLoading: false,
    employeeMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Employee 1',
        skillProficiencySet: [3],
        contract: 1,
        shortId: 'e1',
        color: '#FFFFFF',
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Employee 2',
        skillProficiencySet: [],
        contract: 1,
        shortId: 'e2',
        color: '#FFFFFF',
      },
    ]),
  },
  contractList: {
    isLoading: false,
    contractMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 50,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: 10,
        maximumMinutesPerYear: null,
      },
    ]),
  },
  skillList: {
    isLoading: false,
    skillMapById: createIdMapFromList([
      {
        tenantId: 0,
        id: 3,
        version: 0,
        name: 'Skill 3',
      },
    ]),
  },
};

describe('Employee operations', () => {
  const mockEmployee: Employee = {
    tenantId: 0,
    id: 0,
    version: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  };
  it('should dispatch actions and call client on refresh employee list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockEmployeeList: Employee[] = [mockEmployee];

    onGet(`/tenant/${tenantId}/employee/`, mockEmployeeList);
    await store.dispatch(employeeOperations.refreshEmployeeList());
    expect(store.getActions()).toEqual([
      actions.setIsEmployeeListLoading(true),
      actions.refreshEmployeeList(mockEmployeeList),
      actions.setIsEmployeeListLoading(false),
    ]);
    expect(client.get).toHaveBeenCalledTimes(1);
    expect(client.get).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/`);
  });

  it('should dispatch actions and call client on success delete employee', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;

    const employeeToDelete = mockEmployee;
    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, true);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeEmployee', { name: employeeToDelete.name }),
      actions.removeEmployee(employeeToDelete),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);
  });

  it('should dispatch actions and call client on failed delete employee', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const employeeToDelete = mockEmployee;

    onDelete(`/tenant/${tenantId}/employee/${employeeToDelete.id}`, false);
    await store.dispatch(employeeOperations.removeEmployee(employeeToDelete));
    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeEmployeeError', { name: employeeToDelete.name }),
    ]);
    expect(client.delete).toHaveBeenCalledTimes(1);
    expect(client.delete).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/${employeeToDelete.id}`);
  });

  it('should dispatch actions and call client on add employee', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const employeeToAdd: Employee = { ...mockEmployee, id: undefined, version: undefined };
    const employeeWithUpdatedId: Employee = { ...employeeToAdd, id: 4, version: 0 };

    onPost(`/tenant/${tenantId}/employee/add`, employeeToAdd, employeeWithUpdatedId);
    await store.dispatch(employeeOperations.addEmployee(employeeToAdd));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addEmployee', { name: employeeToAdd.name }),
      actions.addEmployee(employeeWithUpdatedId),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/add`, employeeToAdd);
  });

  it('should dispatch actions and call client on update employee', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const employeeToUpdate: Employee = mockEmployee;
    const employeeWithUpdatedVersion: Employee = { ...mockEmployee, version: 1 };
    onPost(`/tenant/${tenantId}/employee/update`, employeeToUpdate, employeeWithUpdatedVersion);
    await store.dispatch(employeeOperations.updateEmployee(employeeToUpdate));
    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('updateEmployee', { id: employeeToUpdate.id }),
      actions.updateEmployee(employeeWithUpdatedVersion),
    ]);
    expect(client.post).toHaveBeenCalledTimes(1);
    expect(client.post).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/update`, employeeToUpdate);
  });

  it('should dispatch actions and call client on upload employee list', async () => {
    const { store, client } = mockStore(state);
    const tenantId = store.getState().tenantData.currentTenantId;
    const mockEmployeeList: Employee[] = [mockEmployee];

    const fileMock = 'myFile' as unknown as File;
    onUploadFile(`/tenant/${tenantId}/employee/import`, fileMock, mockEmployeeList);
    onGet(`/tenant/${tenantId}/skill/`, []);
    onGet(`/tenant/${tenantId}/contract/`, []);

    await store.dispatch(employeeOperations.uploadEmployeeList(fileMock));
    expect(store.getActions()).toEqual([
      actions.setIsEmployeeListLoading(true),
      skillActions.setIsSkillListLoading(true),
      contractActions.setIsContractListLoading(true),
      alert.showSuccessMessage('importSuccessful'),
      skillActions.refreshSkillList([]),
      skillActions.setIsSkillListLoading(false),
      contractActions.refreshContractList([]),
      contractActions.setIsContractListLoading(false),
      actions.refreshEmployeeList(mockEmployeeList),
      actions.setIsEmployeeListLoading(false),

    ]);
    expect(client.uploadFile).toHaveBeenCalledTimes(1);
    expect(client.uploadFile).toHaveBeenCalledWith(`/tenant/${tenantId}/employee/import`, fileMock);
    expect(client.get).toBeCalledTimes(2);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/skill/`);
    expect(client.get).toBeCalledWith(`/tenant/${tenantId}/contract/`);
  });
});

describe('Employee reducers', () => {
  const addedEmployee: Employee = {
    tenantId: 0,
    id: 4,
    version: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 2,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  };
  const updatedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: 'Updated Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  };
  const deletedEmployee: Employee = {
    tenantId: 0,
    id: 1,
    version: 0,
    name: 'Employee 1',
    skillProficiencySet: [],
    contract: {
      tenantId: 0,
      id: 1,
      name: 'Contract',
      maximumMinutesPerDay: null,
      maximumMinutesPerWeek: null,
      maximumMinutesPerMonth: null,
      maximumMinutesPerYear: null,
    },
    shortId: 'e1',
    color: '#FFFFFF',
  };
  const { store } = mockStore(state);
  const storeState = store.getState();
  it('set is loading', () => {
    expect(
      reducer(state.employeeList, actions.setIsEmployeeListLoading(true)),
    ).toEqual({ ...state.employeeList,
      isLoading: true });
  });
  it('add employee', () => {
    expect(
      reducer(state.employeeList, actions.addEmployee(addedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: storeState.employeeList.employeeMapById
        .set(addedEmployee.id as number, mapDomainObjectToView(addedEmployee)) });
  });
  it('remove employee', () => {
    expect(
      reducer(state.employeeList, actions.removeEmployee(deletedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: storeState.employeeList.employeeMapById.delete(deletedEmployee.id as number) });
  });
  it('update employee', () => {
    expect(
      reducer(state.employeeList, actions.updateEmployee(updatedEmployee)),
    ).toEqual({ ...state.employeeList,
      employeeMapById: storeState.employeeList.employeeMapById
        .set(updatedEmployee.id as number, mapDomainObjectToView(updatedEmployee)) });
  });
  it('refresh employee list', () => {
    expect(
      reducer(state.employeeList, actions.refreshEmployeeList([addedEmployee])),
    ).toEqual({ ...state.employeeList,
      employeeMapById: createIdMapFromList([addedEmployee]) });
  });
});

describe('Employee selectors', () => {
  const { store } = mockStore(state);
  const storeState = store.getState();
  it('should throw an error if employee list, contract list or spot list is loading', () => {
    expect(() => employeeSelectors.getEmployeeById({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    }, 1234)).toThrow();
    expect(() => employeeSelectors.getEmployeeById({
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    }, 1234)).toThrow();
    expect(() => employeeSelectors.getEmployeeById({
      ...storeState,
      spotList: { ...storeState.spotList, isLoading: true },
    }, 1234)).toThrow();
  });

  it('should get a employee by id', () => {
    const employee = employeeSelectors.getEmployeeById(storeState, 1);
    expect(employee).toEqual({
      tenantId: 0,
      id: 1,
      version: 0,
      name: 'Employee 1',
      skillProficiencySet: [
        {
          tenantId: 0,
          id: 3,
          version: 0,
          name: 'Skill 3',
        },
      ],
      contract: {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 50,
        maximumMinutesPerWeek: null,
        maximumMinutesPerMonth: 10,
        maximumMinutesPerYear: null,
      },
      shortId: 'e1',
      color: '#FFFFFF',
    });
  });

  it('should return an empty list if employee list, skill list or contract list is loading', () => {
    let employeeList = employeeSelectors.getEmployeeList({
      ...storeState,
      skillList: { ...storeState.skillList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
    employeeList = employeeSelectors.getEmployeeList({
      ...storeState,
      contractList: { ...storeState.contractList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
    employeeList = employeeSelectors.getEmployeeList({
      ...storeState,
      employeeList: { ...storeState.employeeList, isLoading: true },
    });
    expect(employeeList).toEqual([]);
  });

  it('should return a list of all employee', () => {
    const employeeList = employeeSelectors.getEmployeeList(storeState);
    expect(employeeList).toEqual(expect.arrayContaining([
      {
        tenantId: 0,
        id: 1,
        version: 0,
        name: 'Employee 1',
        skillProficiencySet: [
          {
            tenantId: 0,
            id: 3,
            version: 0,
            name: 'Skill 3',
          },
        ],
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract 1',
          maximumMinutesPerDay: 50,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 10,
          maximumMinutesPerYear: null,
        },
        shortId: 'e1',
        color: '#FFFFFF',
      },
      {
        tenantId: 0,
        id: 2,
        version: 0,
        name: 'Employee 2',
        skillProficiencySet: [],
        contract: {
          tenantId: 0,
          id: 1,
          version: 0,
          name: 'Contract 1',
          maximumMinutesPerDay: 50,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 10,
          maximumMinutesPerYear: null,
        },
        shortId: 'e2',
        color: '#FFFFFF',
      },
    ]));
    expect(employeeList.length).toEqual(2);
  });
});
