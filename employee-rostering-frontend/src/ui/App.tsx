/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import { Page, PageSection } from '@patternfly/react-core';
import * as React from 'react';
import { Route, Switch } from 'react-router-dom';
import './App.css';
import Background from './components/Background';
import Header from './header/Header';
import Alerts from './Alerts';
import { AdminPage, ShiftRosterPage, AvailabilityRosterPage, SkillsPage, SpotsPage, ContractsPage, 
  EmployeesPage, RotationPage } from './pages';

const App: React.FC = () => {
  return (
    <>
      <Page header={<Header />}>
        <Background />
        <PageSection
          style={{
            position: 'relative',
            display: 'flex',
            flexDirection: 'column',
            overflowY: 'auto',
            height: '100%',
          }}
        >
          <Alerts />
          <Switch>
            <Route
              path="/skills"
              exact
              component={SkillsPage}
            />
            <Route
              path="/spots"
              exact
              component={SpotsPage}
            />
            <Route
              path="/contracts"
              exact
              component={ContractsPage}
            />
            <Route
              path="/employees"
              exact
              component={EmployeesPage}
            />
            <Route
              path="/shift"
              exact
              component={ShiftRosterPage} 
            />
            <Route
              path="/availability"
              exact
              component={AvailabilityRosterPage}
            />
            <Route
              path="/rotation"
              exact
              component={RotationPage}
            />
            <Route
              path="/admin"
              exact
              component={AdminPage}
            />
          </Switch>
        </PageSection>
      </Page>
    </>
  );
};

export default App;
