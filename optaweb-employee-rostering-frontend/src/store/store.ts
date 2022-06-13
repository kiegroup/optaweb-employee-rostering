
import { applyMiddleware, combineReducers, createStore, Store } from 'redux';
// it's possible to disable the extension in production
// by importing from redux-devtools-extension/developmentOnly
import { composeWithDevTools } from 'redux-devtools-extension';
import { createLogger } from 'redux-logger';
import thunk from 'redux-thunk';
import axios from 'axios';
import RestServiceClient from './rest';
import { AppState } from './types';
import tenantReducer, { connectionReducer } from './tenant';
import skillReducer from './skill/reducers';
import spotReducer from './spot/reducers';
import contractReducer from './contract/reducers';
import employeeReducer from './employee/reducers';
import timeBucketReducer from './rotation/reducers';
import alertReducer from './alert/reducers';
import {
  rosterStateReducer, shiftRosterViewReducer, availabilityRosterReducer,
  solverReducer,
} from './roster/reducers';

export interface StoreConfig {
  readonly restBaseURL: string;
}

export function configureStore(
  { restBaseURL }: StoreConfig,
  preloadedState?: Partial<AppState>,
): Store<AppState> {
  const restServiceClient = new RestServiceClient(restBaseURL, axios);

  const middlewares = [thunk.withExtraArgument(restServiceClient), createLogger()];
  const middlewareEnhancer = applyMiddleware(...middlewares);

  const enhancers = [middlewareEnhancer];
  const composedEnhancers = composeWithDevTools(...enhancers);

  // map reducers to state slices
  const rootReducer = combineReducers<AppState>({
    tenantData: tenantReducer,
    skillList: skillReducer,
    spotList: spotReducer,
    contractList: contractReducer,
    employeeList: employeeReducer,
    timeBucketList: timeBucketReducer,
    rosterState: rosterStateReducer,
    shiftRoster: shiftRosterViewReducer,
    availabilityRoster: availabilityRosterReducer,
    solverState: solverReducer,
    alerts: alertReducer,
    isConnected: connectionReducer,
  });

  /* if (process.env.NODE_ENV !== 'production' && module.hot) {
    module.hot.accept('./reducers', () => store.replaceReducer(rootReducer));
  } */

  const store = createStore(
    rootReducer,
    preloadedState,
    composedEnhancers,
  );

  restServiceClient.setDispatch(store.dispatch);
  return store;
}
