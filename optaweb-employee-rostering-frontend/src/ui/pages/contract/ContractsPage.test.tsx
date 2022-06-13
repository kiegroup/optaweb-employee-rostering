import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Contract } from 'domain/Contract';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Map } from 'immutable';
import { mockRedux, mockTranslate } from 'setupTests';
import { DataTable, RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { contractOperations } from 'store/contract';
import { doNothing } from 'types';
import { TextInput } from '@patternfly/react-core';
import { ContractRow, ContractsPage, EditableContractRow } from './ContractsPage';

const noContractsStore = mockStore({
  contractList: {
    isLoading: false,
    contractMapById: Map(),
  },
}).store;

const twoContractsStore = mockStore({
  contractList: {
    isLoading: false,
    contractMapById: Map<number, Contract>()
      .set(0, {
        id: 0,
        version: 0,
        tenantId: 0,
        name: 'Contract 1',
        maximumMinutesPerDay: 1,
        maximumMinutesPerWeek: 2,
        maximumMinutesPerMonth: null,
        maximumMinutesPerYear: null,
      })
      .set(1,
        {
          id: 1,
          version: 0,
          tenantId: 0,
          name: 'Contract 2',
          maximumMinutesPerDay: null,
          maximumMinutesPerWeek: null,
          maximumMinutesPerMonth: 3,
          maximumMinutesPerYear: 4,
        }),
  },
}).store;

describe('Contracts page', () => {
  const addContract = (contract: Contract) => ['add', contract];
  const updateContract = (contract: Contract) => ['update', contract];
  const removeContract = (contract: Contract) => ['remove', contract];

  beforeEach(() => {
    jest.spyOn(contractOperations, 'addContract').mockImplementation(contract => addContract(contract) as any);
    jest.spyOn(contractOperations, 'updateContract').mockImplementation(contract => updateContract(contract) as any);
    jest.spyOn(contractOperations, 'removeContract').mockImplementation(contract => removeContract(contract) as any);
    jest.spyOn(twoContractsStore, 'dispatch').mockImplementation(doNothing);
  });

  it('should render correctly with no skills', () => {
    mockRedux(noContractsStore);
    const skillsPage = shallow(<ContractsPage {...getRouterProps('/0/skill', {})} />);
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few skills', () => {
    mockRedux(twoContractsStore);
    const skillsPage = shallow(
      <ContractsPage {...getRouterProps('/0/skill', {})} />,
    );
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('clicking on the edit button should show editor', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);
    viewer.find(RowViewButtons).simulate('edit');

    expect(viewer).toMatchSnapshot();
    viewer.find(EditableContractRow).simulate('close');
    expect(viewer).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('no name should be invalid', () => {
    const contract = {
      ...twoContractsStore.getState().contractList.contractMapById.get(0) as Contract,
      name: '',
    };
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const contract = {
      ...twoContractsStore.getState().contractList.contractMapById.get(0) as Contract,
      id: 3,
      name: 'Contract 1',
    };
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new contract should call add contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew onClose={jest.fn()} />);

    const name = 'New Contract Name';
    const maximumMinutesPerDay = 1;
    const maximumMinutesPerWeek = 2;
    const maximumMinutesPerMonth = 3;
    const maximumMinutesPerYear = 4;

    editor.find(`[columnName="${mockTranslate('name')}"]`).find(TextInput).simulate('change', name);
    editor.find(`[columnName="${mockTranslate('maxMinutesPerDay')}"]`)
      .find(TextInput).simulate('change', `${maximumMinutesPerDay}`);
    editor.find(`[columnName="${mockTranslate('maxMinutesPerWeek')}"]`)
      .find(TextInput).simulate('change', `${maximumMinutesPerWeek}`);
    editor.find(`[columnName="${mockTranslate('maxMinutesPerMonth')}"]`)
      .find(TextInput).simulate('change', `${maximumMinutesPerMonth}`);
    editor.find(`[columnName="${mockTranslate('maxMinutesPerYear')}"]`)
      .find(TextInput).simulate('change', `${maximumMinutesPerYear}`);

    const newContract = {
      ...contract,
      name,
      maximumMinutesPerDay,
      maximumMinutesPerWeek,
      maximumMinutesPerMonth,
      maximumMinutesPerYear,
    };

    editor.find(RowEditButtons).prop('onSave')();
    expect(contractOperations.addContract).toBeCalledWith(newContract);
    expect(twoContractsStore.dispatch).toBeCalledWith(addContract(newContract));
  });

  it('saving updated contract should call update contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const editor = shallow(<EditableContractRow contract={contract} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(contractOperations.updateContract).toBeCalledWith(contract);
    expect(twoContractsStore.dispatch).toBeCalledWith(updateContract(contract));
  });

  it('clicking on the edit button in the viewer should show the editor', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);

    // Clicking the edit button should show the editor
    viewer.find(RowViewButtons).prop('onEdit')();
    expect(viewer).toMatchSnapshot();

    // Clicking the close button should show the viwer
    viewer.find(EditableContractRow).prop('onClose')();
    expect(viewer).toMatchSnapshot();
  });

  it('deleting should call delete contract', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const viewer = shallow(<ContractRow {...contract} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(contractOperations.removeContract).toBeCalledWith(contract);
    expect(twoContractsStore.dispatch).toBeCalledWith(removeContract(contract));
  });

  it('DataTable rowWrapper should be ContractRow', () => {
    const contract = twoContractsStore.getState().contractList.contractMapById.get(0) as Contract;
    mockRedux(twoContractsStore);
    const contractsPage = shallow(<ContractsPage {...getRouterProps('/0/contract', {})} />);
    const rowWrapper = shallow(contractsPage.find(DataTable).prop('rowWrapper')(contract));
    expect(rowWrapper).toMatchSnapshot();
  });

  it('DataTable newRowWrapper should be EditableContractRow', () => {
    mockRedux(twoContractsStore);
    const contractsPage = shallow(<ContractsPage {...getRouterProps('/0/contract', {})} />);
    const removeRow = jest.fn();
    const newRowWrapper = shallow((contractsPage.find(DataTable).prop('newRowWrapper') as any)(removeRow));
    expect(newRowWrapper).toMatchSnapshot();
    newRowWrapper.find(RowEditButtons).prop('onClose')();
    expect(removeRow).toBeCalled();
  });
});
