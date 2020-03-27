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
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { Sorter } from 'types';
import { Spot } from 'domain/Spot';
import { act } from 'react-dom/test-utils';
import { useTranslation } from 'react-i18next';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { SpotsPage, Props } from './SpotsPage';

describe('Spots page', () => {
  it('should render correctly with no spots', () => {
    const spotsPage = shallow(<SpotsPage {...noSpots} />);
    expect(toJson(spotsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few spots', () => {
    const spotsPage = shallow(<SpotsPage {...twoSpots} />);
    expect(toJson(spotsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = twoSpots.tableData[1];
    const viewer = shallow(spotsPage.renderViewer(spot));
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = twoSpots.tableData[1];
    const editor = shallow(spotsPage.renderEditor(spot));
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('should update properties on change', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const setProperty = jest.fn();
    const editor = spotsPage.editDataRow(spotsPage.getInitialStateForNewRow(), setProperty);
    const nameCol = shallow(editor[0]);
    nameCol.simulate('change', 'Test');
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('name', 'Test');

    setProperty.mockClear();
    const requiredSkillSetCol = mount(editor[1]);
    act(() => {
      requiredSkillSetCol.find(MultiTypeaheadSelectInput).props().onChange([twoSpots.skillList[0]]);
    });
    expect(setProperty).toBeCalled();
    expect(setProperty).toBeCalledWith('requiredSkillSet', [twoSpots.skillList[0]]);
  });

  it('should call addSpot on addData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = { name: 'Spot', requiredSkillSet: [], tenantId: 0, covidWard: false };
    spotsPage.addData(spot);
    expect(twoSpots.addSpot).toBeCalled();
    expect(twoSpots.addSpot).toBeCalledWith(spot);
  });

  it('should call updateSpot on updateData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = { name: 'Spot', requiredSkillSet: [], tenantId: 0, id: 1, version: 0, covidWard: false };
    spotsPage.updateData(spot);
    expect(twoSpots.updateSpot).toBeCalled();
    expect(twoSpots.updateSpot).toBeCalledWith(spot);
  });

  it('should call removeSpot on removeData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = { name: 'Spot', requiredSkillSet: [], tenantId: 0, id: 1, version: 0, covidWard: false };
    spotsPage.removeData(spot);
    expect(twoSpots.removeSpot).toBeCalled();
    expect(twoSpots.removeSpot).toBeCalledWith(spot);
  });

  it('should return a filter that match by name and skill', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const filter = spotsPage.getFilter();

    expect(twoSpots.tableData.filter(filter('1'))).toEqual([twoSpots.tableData[0], twoSpots.tableData[1]]);
    expect(twoSpots.tableData.filter(filter('Spot 1'))).toEqual([twoSpots.tableData[0]]);
    expect(twoSpots.tableData.filter(filter('2'))).toEqual([twoSpots.tableData[1]]);
    expect(twoSpots.tableData.filter(filter('Skill'))).toEqual([twoSpots.tableData[1]]);
  });

  it('should return a sorter that sort by name', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const sorter = spotsPage.getSorters()[0] as Sorter<Spot>;
    const list = [twoSpots.tableData[1], twoSpots.tableData[0]];
    expect(list.sort(sorter)).toEqual(twoSpots.tableData);
    expect(spotsPage.getSorters()[1]).toBeNull();
  });

  it('should treat incompleted data as incomplete', () => {
    const spotsPage = new SpotsPage(twoSpots);

    const noName = { tenantId: 0, requiredSkillSet: [] };
    const result1 = spotsPage.isDataComplete(noName);
    expect(result1).toEqual(false);

    const noRequiredSkillSet = { tenantId: 0, name: 'Name' };
    const result2 = spotsPage.isDataComplete(noRequiredSkillSet);
    expect(result2).toEqual(false);

    const completed = { tenantId: 0, name: 'Name', requiredSkillSet: [], covidWard: false };
    const result3 = spotsPage.isDataComplete(completed);
    expect(result3).toEqual(true);
  });

  it('should treat empty name as invalid', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const components = { tenantId: 0, name: '', requiredSkillSet: [], covidWard: false };
    const result = spotsPage.isValid(components);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const components = { tenantId: 0, name: 'Spot', requiredSkillSet: [], covidWard: false };
    const result = spotsPage.isValid(components);
    expect(result).toEqual(true);
  });
});

const noSpots: Props = {
  ...useTranslation('SpotsPage'),
  tReady: true,
  tenantId: 0,
  title: 'Spots',
  columnTitles: ['Name'],
  tableData: [],
  skillList: [],
  addSpot: jest.fn(),
  updateSpot: jest.fn(),
  removeSpot: jest.fn(),
  ...getRouterProps('/spots', {}),
};

const twoSpots: Props = {
  ...useTranslation('SpotsPage'),
  tReady: true,
  tenantId: 0,
  title: 'Spots',
  columnTitles: ['Name'],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: 'Spot 1',
    requiredSkillSet: [],
    covidWard: false,
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: 'Spot 2',
    requiredSkillSet: [{ tenantId: 0, name: 'Skill 1' }, { tenantId: 0, name: 'Skill 2' }],
    covidWard: true,
  }],
  skillList: [{ tenantId: 0, name: 'Skill 1' }, { tenantId: 0, name: 'Skill 2' }],
  addSpot: jest.fn(),
  updateSpot: jest.fn(),
  removeSpot: jest.fn(),
  ...getRouterProps('/spots', {}),
};
