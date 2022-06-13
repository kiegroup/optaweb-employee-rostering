
import { Action } from 'redux';
import { ThunkAction } from 'redux-thunk';
import { TenantData } from 'store/tenant/types';
import { SkillList } from 'store/skill/types';
import { SpotList } from 'store/spot/types';
import RestServiceClient from './rest/RestServiceClient';
import { ContractList } from './contract/types';
import { EmployeeList } from './employee/types';
import { CurrentRosterState, CurrentShiftRoster, CurrentAvailabilityRoster, CurrentSolverState } from './roster/types';
import { AlertList } from './alert/types';
import { TimeBucketList } from './rotation/types';

/**
 * ThunkCommand is a ThunkAction that has no result (it's typically something like
 * `Promise<ActionAfterDataFetched>`, but sending messages over WebSocket usually has no response
 * (with the exception of subscribe), so most of our operations are void).
 *
 * @template A Type of action(s) allowed to be dispatched.
 */
export type ThunkCommand<A extends Action> = ThunkAction<any, AppState, RestServiceClient, A>;

/**
 * Factory method that takes a value and creates an @type {Action}.
 *
 * @template V value type
 * @template A action type
 */
export type ActionFactory<V, A extends Action> = V extends void ?
  // https://stackoverflow.com/questions/55646272/conditional-method-parameters-based-on-generic-type
  () => A : // nullary
  V extends boolean?
    (value: boolean) => A : // boolean unary
    (value: V) => A; // unary

/**
 * Factory method that takes a value and creates a @type {ThunkCommand}.
 *
 * @template V value type
 * @template A action type
 */
export type ThunkCommandFactory<V, A extends Action> = V extends void ?
  () => ThunkCommand<A> : // nullary
  (value: V) => ThunkCommand<A>; // unary

export interface AppState {
  readonly tenantData: TenantData;
  readonly skillList: SkillList;
  readonly spotList: SpotList;
  readonly contractList: ContractList;
  readonly employeeList: EmployeeList;
  readonly timeBucketList: TimeBucketList;
  readonly rosterState: CurrentRosterState;
  readonly shiftRoster: CurrentShiftRoster;
  readonly availabilityRoster: CurrentAvailabilityRoster;
  readonly solverState: CurrentSolverState;
  readonly alerts: AlertList;
  readonly isConnected: boolean;
}
