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

function selectValue(selectPlaceholder, value) {
  cy.get(`input[aria-label="${selectPlaceholder}"]`)
    .click({ force: true })
    .type(value, { force: true });

  cy.get('[class*="-menu"]')
    .contains(value)
    .click();
}

function changeToTenant(tenant) {
  cy.get('.pf-c-dropdown > button').click();
  cy.contains(tenant).click();
}

function gotoPage(page) {
  cy.get('#nav-toggle').click();
  cy.get(`[href*="/${page}"]`).click();
  cy.get('#nav-toggle').click();
}

function dragCreateShift(day, from, to) {
  cy.contains('2:00 AM').then((firstTimeEle) => {
    cy.contains('4:00 AM').then((secondTimeEle) => {
      // Apparently the difference between two labels is double the length of one timeslot
      const twoHourYDiff = (secondTimeEle[0].offsetTop - firstTimeEle[0].offsetTop) / 2;
      const reference = Cypress.moment('2018-01-01T00:00');
      const fromDate = Cypress.moment(`2018-01-01T${from}`);
      const toDate = Cypress.moment(`2018-01-01T${to}`);

      // Add 10px, since the timeslots are slightly misaligned due to CSS
      const startLocation = (Cypress.moment.duration(fromDate.diff(reference)).asHours() / 2) * twoHourYDiff + 10;
      const endLocation = (Cypress.moment.duration(toDate.diff(reference)).asHours() / 2) * twoHourYDiff + 10;
      cy.get('.rbc-events-container').eq(day).trigger('mousedown', { force: true, x: 100, y: startLocation });
      cy.get('.rbc-events-container').eq(day).trigger('mousemove', { force: true, x: 100, y: endLocation });
      cy.get('.rbc-events-container').eq(day).trigger('mouseup', { force: true, x: 100, y: endLocation });
    });
  });
}

function closeAlerts() {
  cy.get('.pf-c-alert__action > button', { timeout: 120000 }).click({ multiple: true, force: true });
}

describe('A new tenant can be created, who can have their own employees, spots, skills and shifts', () => {
  before(() => {
    cy.server();
    cy.visit('/');
  });

  beforeEach(() => {
    cy.wait(3000);// Wait for all data from last test/start to finish loading
    cy.get('[data-cy=settings]').click();
    cy.get('[data-cy=reset-application]').click();
    cy.get('[data-cy=confirm]').click();
    closeAlerts();
  });

  it('basic tenant workflow', () => {
    // Create a tenant
    cy.get('[data-cy=settings]').click();
    cy.get('[data-cy=add-tenant]').click();
    cy.get('[data-cy=name]').type('Test Tenant');
    cy.get('[data-cy=schedule-start-date]').type('2018-01-01');
    cy.get('[data-cy=draft-length]').type('7');
    cy.get('[data-cy=publish-notice]').type('7');
    // TODO: publish length is disabled/not supported by backend, uncomment when supported
    // (if ever)
    // cy.get('[data-cy=publish-length]').type("7");
    cy.get('[data-cy=rotation-length]').type('7');
    selectValue('Select a timezone...', 'UTC');
    cy.get('[data-cy=save]').click();
    closeAlerts();

    changeToTenant('Test Tenant');

    // Create a new skill
    gotoPage('skills');
    cy.get('button').contains('Add').click();
    cy.get('[aria-label="Name"]').type('New Skill');
    cy.get('[aria-label="Save"]').click();
    closeAlerts();

    // Create a spot
    gotoPage('wards');
    cy.get('button').contains('Add').click();
    cy.get('[aria-label="Name"]').type('Required Skill Spot');
    selectValue('Select required skills...', 'New Skill');
    cy.get('[aria-label="Save"]').click();
    closeAlerts();

    // Create a contract
    gotoPage('contracts');
    cy.get('button').contains('Add').click();
    cy.get('[aria-label="Name"]').type('New Contract');
    cy.get('[aria-label="Save"]').click();
    closeAlerts();

    // Create a employee
    gotoPage('employees');
    cy.get('button').contains('Add').click();
    cy.get('[aria-label="Name"]').type('Employee with Skills');
    selectValue('Select a contract...', 'New Contract');
    selectValue('Select skill proficiencies...', 'New Skill');
    cy.get('[aria-label="Save"]').click();
    closeAlerts();

    // Create a shifts
    gotoPage('shift');

    // Sometimes a shift is not created, so wait to make the test stable
    cy.wait(1500);
    dragCreateShift(2, '00:00', '06:00');

    // Schedule for 5 seconds
    cy.get('[aria-label="Actions"]').click();
    cy.contains('Schedule').click();
    cy.wait(5000);
    closeAlerts();
    cy.get('[aria-label="Actions"]').click();
    cy.contains('Terminate Early').click();
    closeAlerts();

    // Verify the shift is assigned to our created employee
    cy.contains('Employee with Skills').should('exist');
  });
});
