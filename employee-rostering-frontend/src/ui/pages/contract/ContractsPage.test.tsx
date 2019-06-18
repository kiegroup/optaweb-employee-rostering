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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
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

  it('should treat empty name as invalid', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = {...twoContracts.tableData[1], name: ""};
    const result = contractsPage.isValid(contract);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const contractsPage = new ContractsPage(twoContracts);
    const contract = {...twoContracts.tableData[1], name: "Contract"};
    const result = contractsPage.isValid(contract);
    expect(result).toEqual(true);
  });
});

const noContracts: Props = {
  tenantId: 0,
  title: "Contracts",
  columnTitles: ["Name", "Max Hours Per Day", "Max Hours Per Week", "Max Hours Per Month", "Max Hours Per Year"],
  tableData: [],
  addContract: jest.fn(),
  updateContract: jest.fn(),
  removeContract: jest.fn()
};

const twoContracts: Props = {
  tenantId: 0,
  title: "Contracts",
  columnTitles: ["Name", "Max Hours Per Day", "Max Hours Per Week", "Max Hours Per Month", "Max Hours Per Year"],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: "Contract 1",
    maximumMinutesPerDay: null,
    maximumMinutesPerWeek: null,
    maximumMinutesPerMonth: null,
    maximumMinutesPerYear: null
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: "Contract 2",
    maximumMinutesPerDay: 1,
    maximumMinutesPerWeek: null,
    maximumMinutesPerMonth: 10,
    maximumMinutesPerYear: 120
  }],
  addContract: jest.fn(),
  updateContract: jest.fn(),
  removeContract: jest.fn()
};