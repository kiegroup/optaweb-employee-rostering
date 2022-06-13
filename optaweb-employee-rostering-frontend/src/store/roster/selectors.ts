import { Shift } from 'domain/Shift';
import { Spot } from 'domain/Spot';
import { ShiftRosterView } from 'domain/ShiftRosterView';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { Employee } from 'domain/Employee';
import { AvailabilityRosterView } from 'domain/AvailabilityRosterView';
import { spotSelectors } from 'store/spot';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { skillSelectors } from 'store/skill';
import { RosterState } from 'domain/RosterState';
import { employeeSelectors } from '../employee';
import { AppState } from '../types';

export function isShiftRosterLoading(state: AppState) {
  return state.spotList.isLoading || state.employeeList.isLoading || state.skillList.isLoading
    || state.contractList.isLoading || state.shiftRoster.isLoading
    || state.rosterState.isLoading;
}

export function isAvailabilityRosterLoading(state: AppState) {
  return state.spotList.isLoading || state.employeeList.isLoading || state.skillList.isLoading
    || state.contractList.isLoading || state.availabilityRoster.isLoading
    || state.rosterState.isLoading;
}

export function getRosterState(state: AppState): RosterState | null {
  if (state.rosterState.isLoading) {
    return null;
  }

  return state.rosterState.rosterState;
}

export const getSpotListInShiftRoster = (state: AppState): Spot[] => {
  if (isShiftRosterLoading(state)) {
    return [];
  }
  return (state.shiftRoster.shiftRosterView as ShiftRosterView).spotList
    .filter(spot => spotSelectors.getSpotList(state).find(x => x.id === spot.id) !== undefined);
};

export const getShiftListForSpot = (state: AppState, spot: Spot): Shift[] => {
  if (isShiftRosterLoading(state)) {
    throw Error('Shift Roster is loading');
  }
  if (getSpotListInShiftRoster(state).find(s => s.id === spot.id) !== undefined
  && (spot.id as number) in (state.shiftRoster.shiftRosterView as ShiftRosterView)
    .spotIdToShiftViewListMap) {
    return ((state.shiftRoster.shiftRosterView as ShiftRosterView)
      .spotIdToShiftViewListMap[spot.id as number]).map(sv => ({
      ...objectWithout(sv, 'spotId', 'rotationEmployeeId', 'employeeId',
        'originalEmployeeId', 'requiredSkillSetIdList'),
      spot,
      requiredSkillSet: (sv.requiredSkillSetIdList.map(id => skillSelectors.getSkillById(state, id))),
      originalEmployee: (sv.originalEmployeeId !== null)
        ? employeeSelectors.getEmployeeById(state, sv.originalEmployeeId) : null,
      rotationEmployee: (sv.rotationEmployeeId !== null)
        ? employeeSelectors.getEmployeeById(state, sv.rotationEmployeeId) : null,
      employee: (sv.employeeId !== null) ? employeeSelectors.getEmployeeById(state, sv.employeeId) : null,
    })).sort((a, b) => (a.id as number) - (b.id as number));
  }

  return [];
};

export const getEmployeeListInAvailabilityRoster = (state: AppState): Employee[] => {
  if (isAvailabilityRosterLoading(state)) {
    return [];
  }
  return (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView).employeeList
    .filter(employee => employeeSelectors.getEmployeeList(state).find(x => x.id === employee.id) !== undefined);
};

export const getShiftListForEmployee = (state: AppState, employee: Employee): Shift[] => {
  if (isAvailabilityRosterLoading(state)) {
    throw Error('Availability Roster is loading');
  }
  if (getEmployeeListInAvailabilityRoster(state).find(e => e.id === employee.id) !== undefined
    && (employee.id as number) in (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
      .employeeIdToShiftViewListMap) {
    return ((state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
      .employeeIdToShiftViewListMap[employee.id as number]).map(sv => ({
      ...objectWithout(sv, 'spotId', 'rotationEmployeeId', 'employeeId',
        'originalEmployeeId', 'requiredSkillSetIdList'),
      spot: spotSelectors.getSpotById(state, sv.spotId),
      requiredSkillSet: (sv.requiredSkillSetIdList.map(id => skillSelectors.getSkillById(state, id))),
      originalEmployee: (sv.originalEmployeeId !== null)
        ? employeeSelectors.getEmployeeById(state, sv.originalEmployeeId) : null,
      employee,
      rotationEmployee: (sv.rotationEmployeeId !== null)
        ? employeeSelectors.getEmployeeById(state, sv.rotationEmployeeId) : null,
    })).sort((a, b) => (a.id as number) - (b.id as number));
  }

  return [];
};

export const getAvailabilityListForEmployee = (state: AppState, employee: Employee): EmployeeAvailability[] => {
  if (isAvailabilityRosterLoading(state)) {
    throw Error('Availability Roster is loading');
  }
  if (getEmployeeListInAvailabilityRoster(state).find(e => e.id === employee.id) !== undefined
  && (employee.id as number) in (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
    .employeeIdToAvailabilityViewListMap) {
    return ((state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
      .employeeIdToAvailabilityViewListMap[employee.id as number]).map(ea => ({
      ...objectWithout(ea, 'employeeId'),
      employee: employeeSelectors.getEmployeeById(state, ea.employeeId),
    }));
  }

  return [];
};
