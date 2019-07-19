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
import Spot from 'domain/Spot';
import Employee from 'domain/Employee';
import Shift from 'domain/Shift';
import { ShiftRosterPage } from './ShiftRosterPage';
import RosterState from 'domain/RosterState';
import moment from 'moment-timezone';
import "moment/locale/en-ca";

describe('Shift Roster Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly when loaded', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when loading', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isLoading
      allSpotList={[]}
      shownSpotList={[]}
      spotIdToShiftListMap={new Map()}
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when solving', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isSolving
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when creating a new shift via button', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find('Button[aria-label="Create Shift"]').simulate("click");
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: "Spot",
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: "Skill"
    }
  ]
}

const employee: Employee = {
  tenantId: 0,
  id: 4,
  version: 0,
  name: "Employee 1",
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: "Basic Contract",
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000
  },
  skillProficiencySet: [{
    tenantId: 0,
    id: 6,
    version: 0,
    name: "Not Required Skill"
  }]
}

const shift: Shift = {
  tenantId: 0,
  id: 1,
  version: 0, 
  startDateTime: moment("2018-07-01T09:00").toDate(),
  endDateTime: moment("2018-07-01T17:00").toDate(),
  spot: spot,
  employee: employee,
  rotationEmployee: {
    ...employee,
    id: 7,
    name: "Rotation Employee"
  },
  pinnedByUser: false,
  indictmentScore: { hardScore: 0, mediumScore: 0, softScore: 0 },
  requiredSkillViolationList: [],
  unavailableEmployeeViolationList: [],
  shiftEmployeeConflictList: [],
  desiredTimeslotForEmployeeRewardList: [],
  undesiredTimeslotForEmployeePenaltyList: [],
  rotationViolationPenaltyList: [],
  unassignedShiftPenaltyList: [],
  contractMinutesViolationPenaltyList: []
};

const startDate = moment("2018-07-01T09:00").startOf('week').toDate();
const endDate = moment("2018-07-01T09:00").endOf('week').toDate()

const rosterState: RosterState = {
  tenant: {
    id: 0,
    version: 0,
    name: "Tenant"
  },
  publishNotice: 14,
  publishLength: 7,
  firstDraftDate: new Date("2018-07-01"),
  draftLength: 7,
  unplannedRotationOffset: 0,
  rotationLength: 7,
  lastHistoricDate: new Date("2018-07-01"),
  timeZone: "EST"
};

const baseProps = {
  isSolving: false,
  isLoading: false,
  allSpotList: [spot],
  shownSpotList: [spot],
  spotIdToShiftListMap: new Map<number, Shift[]>([
    [2, [shift]]
  ]),
  startDate: startDate,
  endDate: endDate,
  totalNumOfSpots: 1,
  rosterState: rosterState,
  addShift: jest.fn(),
  removeShift: jest.fn(),
  updateShift: jest.fn(),
  getShiftRosterFor: jest.fn(),
  refreshShiftRoster: jest.fn(),
  solveRoster: jest.fn(),
  publishRoster:jest.fn(),
  terminateSolvingRosterEarly: jest.fn()
}