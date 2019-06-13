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

  it('should return a spot with the tenantId but no name, requiredSkillSet id or version on createNewDataInstance', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = spotsPage.createNewDataInstance();
    expect(spot.tenantId).toEqual(twoSpots.tenantId);
    expect(spot.name).toEqual("");
    expect(spot.requiredSkillSet).toEqual([]);
    expect(spot.id).toBeUndefined();
    expect(spot.version).toBeUndefined();
  })

  it('should call addSpot on addData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = {name: "Spot", requiredSkillSet: [], tenantId: 0};
    spotsPage.addData(spot);
    expect(twoSpots.addSpot).toBeCalled();
    expect(twoSpots.addSpot).toBeCalledWith(spot);
  });

  it('should call updateSpot on updateData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = {name: "Spot", requiredSkillSet: [], tenantId: 0, id: 1, version: 0};
    spotsPage.updateData(spot);
    expect(twoSpots.updateSpot).toBeCalled();
    expect(twoSpots.updateSpot).toBeCalledWith(spot);
  });

  it('should call removeSpot on removeData', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const spot = {name: "Spot", requiredSkillSet: [], tenantId: 0, id: 1, version: 0};
    spotsPage.removeData(spot);
    expect(twoSpots.removeSpot).toBeCalled();
    expect(twoSpots.removeSpot).toBeCalledWith(spot);
  });

  it('should treat empty name as invalid', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const components = {tenantId: 0, name: "", requiredSkillSet: []};
    const result = spotsPage.isValid(components);
    expect(result).toEqual(false);
  });

  it('should treat non-empty name as valid', () => {
    const spotsPage = new SpotsPage(twoSpots);
    const components = {tenantId: 0, name: "Spot", requiredSkillSet: []};
    const result = spotsPage.isValid(components);
    expect(result).toEqual(true);
  });
});

const noSpots: Props = {
  tenantId: 0,
  title: "Spots",
  columnTitles: ["Name"],
  tableData: [],
  skillList: [],
  addSpot: jest.fn(),
  updateSpot: jest.fn(),
  removeSpot: jest.fn()
};

const twoSpots: Props = {
  tenantId: 0,
  title: "Spots",
  columnTitles: ["Name"],
  tableData: [{
    id: 0,
    version: 0,
    tenantId: 0,
    name: "Spot 1",
    requiredSkillSet: []
  },
  {
    id: 1,
    version: 0,
    tenantId: 0,
    name: "Spot 2",
    requiredSkillSet: [{tenantId: 0, name: "Skill 1"}, {tenantId: 0, name: "Skill 2"}]
  }],
  skillList: [{tenantId: 0, name: "Skill 1"}, {tenantId: 0, name: "Skill 2"}],
  addSpot: jest.fn(),
  updateSpot: jest.fn(),
  removeSpot: jest.fn()
};