/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import OptionalInput from 'ui/components/OptionalInput';
import { Sorter } from 'types';
import { Contract } from 'domain/Contract';
import { useTranslation } from 'react-i18next';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { ContractsPage, Props } from './ContractsPage';

describe('Contracts page', () => {
  it('should render correctly with no contracts', () => {
    const contractsPage = shallow(<ContractsPage {...noContracts} />);
    expect(toJson(contractsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few contracts', () => {
    const contractsPage = shallow(<ContractsPage {...twoContracts} />);
    expect(toJson(contractsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = twoContracts.tableData[1];
    const viewer = shallow(contractsPage.renderViewer(contract));
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = twoContracts.tableData[1];
    const editor = shallow(contractsPage.renderEditor(contract));
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('should update properties on change', () => {
    const spotsPage = new ContractsPage(twoContracts);
    const setProperty = jest.fn();
    const editor = spotsPage.editDataRow(spotsPage.getInitialStateForNewRow(), setProperty);
    const nameCol = shallow(editor[0]);
    nameCol.simulate('change', 'Test');
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('name', 'Test');

    setProperty.mockClear();
    const maxMinutesPerDayCol = mount(editor[1]).find(OptionalInput);
    expect(maxMinutesPerDayCol.props().valueToString(10)).toEqual('10');
    expect(maxMinutesPerDayCol.props().valueMapper('10')).toEqual(10);
    expect(maxMinutesPerDayCol.props().isValid('10')).toEqual(true);
    expect(maxMinutesPerDayCol.props().isValid('ab10')).toEqual(false);
    expect(maxMinutesPerDayCol.props().isValid('-10')).toEqual(false);
    maxMinutesPerDayCol.find(OptionalInput).props().onChange(10);
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('maximumMinutesPerDay', 10);

    setProperty.mockClear();
    const maxMinutesPerWeekCol = mount(editor[2]).find(OptionalInput);
    expect(maxMinutesPerWeekCol.props().valueToString(10)).toEqual('10');
    expect(maxMinutesPerWeekCol.props().valueMapper('10')).toEqual(10);
    expect(maxMinutesPerWeekCol.props().isValid('10')).toEqual(true);
    expect(maxMinutesPerWeekCol.props().isValid('ab10')).toEqual(false);
    expect(maxMinutesPerWeekCol.props().isValid('-10')).toEqual(false);
    maxMinutesPerWeekCol.props().onChange(10);
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('maximumMinutesPerWeek', 10);

    setProperty.mockClear();
    const maxMinutesPerMonthCol = mount(editor[3]).find(OptionalInput);
    expect(maxMinutesPerMonthCol.props().valueToString(10)).toEqual('10');
    expect(maxMinutesPerMonthCol.props().valueMapper('10')).toEqual(10);
    expect(maxMinutesPerMonthCol.props().isValid('10')).toEqual(true);
    expect(maxMinutesPerMonthCol.props().isValid('ab10')).toEqual(false);
    expect(maxMinutesPerMonthCol.props().isValid('-10')).toEqual(false);
    maxMinutesPerMonthCol.props().onChange(10);
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('maximumMinutesPerMonth', 10);

    setProperty.mockClear();
    const maxMinutesPerYearCol = mount(editor[4]).find(OptionalInput);
    expect(maxMinutesPerYearCol.props().valueToString(10)).toEqual('10');
    expect(maxMinutesPerYearCol.props().valueMapper('10')).toEqual(10);
    expect(maxMinutesPerYearCol.props().isValid('10')).toEqual(true);
    expect(maxMinutesPerYearCol.props().isValid('ab10')).toEqual(false);
    expect(maxMinutesPerYearCol.props().isValid('-10')).toEqual(false);
    maxMinutesPerYearCol.props().onChange(10);
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('maximumMinutesPerYear', 10);
  });

  it('should call addContract on addData', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = twoContracts.tableData[1];
    contractsPage.addData(contract);
    expect(twoContracts.addContract).toBeCalled();
    expect(twoContracts.addContract).toBeCalledWith(contract);
  });

  it('should call updateContract on updateData', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = twoContracts.tableData[1];
    contractsPage.updateData(contract);
    expect(twoContracts.updateContract).toBeCalled();
    expect(twoContracts.updateContract).toBeCalledWith(contract);
  });

  it('should call removeContract on removeData', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = twoContracts.tableData[1];
    contractsPage.removeData(contract);
    expect(twoContracts.removeContract).toBeCalled();
    expect(twoContracts.removeContract).toBeCalledWith(contract);
  });

  it('should return a filter that match by name', () => {
    const contractPage = new ContractsPage(twoContracts);
    const filter = contractPage.getFilter();

    expect(twoContracts.tableData.filter(filter('1'))).toEqual([twoContracts.tableData[0]]);
    expect(twoContracts.tableData.filter(filter('2'))).toEqual([twoContracts.tableData[1]]);
  });

  it('should return a sorter that sort by name', () => {
    const contractPage = new ContractsPage(twoContracts);
    const sorter = contractPage.getSorters()[0] as Sorter<Contract>;
    const list = [twoContracts.tableData[1], twoContracts.tableData[0]];
    expect(list.sort(sorter)).toEqual(twoContracts.tableData);
  });

  it('should treat incompleted data as incomplete', () => {
    const contractsPage = new ContractsPage(twoContracts);

    const noName = { ...twoContracts.tableData[1], name: undefined };
    const result1 = contractsPage.isDataComplete(noName);
    expect(result1).toEqual(false);

    const noMaxHoursPerDay = { ...twoContracts.tableData[1], maximumMinutesPerDay: undefined };
    const result2 = contractsPage.isDataComplete(noMaxHoursPerDay);
    expect(result2).toEqual(false);

    const noMaxHoursPerWeek = { ...twoContracts.tableData[1], maximumMinutesPerWeek: undefined };
    const result3 = contractsPage.isDataComplete(noMaxHoursPerWeek);
    expect(result3).toEqual(false);

    const noMaxHoursPerMonth = { ...twoContracts.tableData[1], maximumMinutesPerMonth: undefined };
    const result4 = contractsPage.isDataComplete(noMaxHoursPerMonth);
    expect(result4).toEqual(false);

    const noMaxHoursPerYear = { ...twoContracts.tableData[1], maximumMinutesPerYear: undefined };
    const result5 = contractsPage.isDataComplete(noMaxHoursPerYear);
    expect(result5).toEqual(false);

    const completed = { ...twoContracts.tableData[1] };
    const result6 = contractsPage.isDataComplete(completed);
    expect(result6).toEqual(true);
  });

  it('should treat empty name as invalid', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = { ...twoContracts.tableData[1], name: '' };
    const result = contractsPage.isValid(contract);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = { ...twoContracts.tableData[1], name: 'Contract' };
    const result = contractsPage.isValid(contract);
    expect(result).toEqual(true);
  });
});

const noContracts: Props = {
  ...useTranslation('ContractsPage'),
  tReady: true,
  tenantId: 0,
  title: 'Contracts',
  columnTitles: ['Name', 'Max Hours Per Day', 'Max Hours Per Week', 'Max Hours Per Month', 'Max Hours Per Year'],
  tableData: [],
  addContract: jest.fn(),
  updateContract: jest.fn(),
  removeContract: jest.fn(),
  ...getRouterProps('/contacts', {}),
};

const twoContracts: Props = {
  ...useTranslation('ContractsPage'),
  tReady: true,
  tenantId: 0,
  title: 'Contracts',
  columnTitles: ['Name', 'Max Hours Per Day', 'Max Hours Per Week', 'Max Hours Per Month', 'Max Hours Per Year'],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: 'Contract 1',
    maximumMinutesPerDay: null,
    maximumMinutesPerWeek: null,
    maximumMinutesPerMonth: null,
    maximumMinutesPerYear: null,
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: 'Contract 2',
    maximumMinutesPerDay: 1,
    maximumMinutesPerWeek: 20,
    maximumMinutesPerMonth: 10,
    maximumMinutesPerYear: 120,
  }],
  addContract: jest.fn(),
  updateContract: jest.fn(),
  removeContract: jest.fn(),
  ...getRouterProps('/contacts', {}),
};
