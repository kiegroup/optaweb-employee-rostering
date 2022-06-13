import { Page, PageSection, PageSidebar } from '@patternfly/react-core';
import React, { useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import './App.css';
import { useMediaQuery } from 'react-responsive';
import Background from './components/Background';
import Header from './header/Header';
import Alerts from './Alerts';
import {
  AdminPage, ShiftRosterPage, AvailabilityRosterPage, SkillsPage, SpotsPage, ContractsPage,
  EmployeesPage, RotationPage, CurrentShiftRosterPage,
} from './pages';
import Navigation from './header/Navigation';
import { ConnectionStatus } from './components/ConnectionStatus';

const App: React.FC = () => {
  const [isNavExpanded, setNavExpanded] = useState(false);
  const smallerThanLaptop = useMediaQuery({ maxWidth: 1399 });
  if (!smallerThanLaptop && isNavExpanded) {
    setNavExpanded(false);
  }
  return (
    <Page
      header={<Header onNavToggle={() => setNavExpanded(!isNavExpanded)} />}
      sidebar={<PageSidebar isNavOpen={isNavExpanded} nav={<Navigation variant="default" />} />}
    >
      <Background />
      <ConnectionStatus />
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
            path="/:tenantId/skills"
            exact
            component={SkillsPage}
          />
          <Route
            path="/:tenantId/spots"
            exact
            component={SpotsPage}
          />
          <Route
            path="/:tenantId/contracts"
            exact
            component={ContractsPage}
          />
          <Route
            path="/:tenantId/employees"
            exact
            component={EmployeesPage}
          />
          <Route
            path="/:tenantId/shift"
            exact
            component={ShiftRosterPage}
          />
          <Route
            path="/:tenantId/adjust"
            exact
            component={CurrentShiftRosterPage}
          />
          <Route
            path="/:tenantId/availability"
            exact
            component={AvailabilityRosterPage}
          />
          <Route
            path="/:tenantId/rotation"
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
  );
};

export default App;
