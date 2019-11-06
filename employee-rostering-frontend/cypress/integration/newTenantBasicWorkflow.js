
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

function selectValue(selectPlaceholder, selectValue) {
  cy.get(`[placeholder="${selectPlaceholder}"]`).clear();
  cy.get(`[placeholder="${selectPlaceholder}"]`).type(selectValue);
  cy.get("button").contains(selectValue).last().click();
}

function changeToTenant(tenant) {
  cy.get('.pf-c-dropdown > button').click();
  cy.contains(tenant).click();
}

function gotoPage(page) {
  cy.get("#nav-toggle").click();
  cy.get(`[href="/${page}"]`).click();
  cy.get("#nav-toggle").click();
}

function dragCreateShift(day, from, to) {
  cy.contains("2:00 AM").then(firstTimeEle => {
    cy.contains("4:00 AM").then(secondTimeEle => {
      // Apparently the difference between two labels is double the length of one timeslot
      var twoHourYDiff = (secondTimeEle[0].offsetTop - firstTimeEle[0].offsetTop) / 2;
      var reference = Cypress.moment(`2018-01-01T00:00`)
      var fromDate = Cypress.moment(`2018-01-01T${from}`);
      var toDate = Cypress.moment(`2018-01-01T${to}`);
      
      // Add 10px, since the timeslots are slightly misaligned due to CSS
      var startLocation = (Cypress.moment.duration(fromDate.diff(reference)).asHours() / 2) * twoHourYDiff + 10;
      var endLocation = (Cypress.moment.duration(toDate.diff(reference)).asHours() / 2) * twoHourYDiff + 10;
      cy.get(".rbc-events-container").eq(day).trigger('mousedown', { force: true, x: 100, y: startLocation });
      cy.get(".rbc-events-container").eq(day).trigger('mousemove', { force: true, x: 100, y: endLocation });
      cy.get(".rbc-events-container").eq(day).trigger('mouseup', { force: true, x: 100, y: endLocation });
    });
  });
}

function closeAlerts() {
    cy.get(".pf-c-alert__action > button").click({ multiple: true, force: true });
}

describe('A new tenant can be created, who can have their own employees, spots, skills and shifts', () => {
    before(() => {
      cy.server();
      cy.visit('/');
    });
    
    beforeEach(() => {
       cy.get('[data-cy=settings]').click();
       cy.get('[data-cy=reset-application]').click();
       closeAlerts();
    });
    
    it('basic tenant workflow', () => {
        // Create a tenant
        cy.get('[data-cy=settings]').click();
        cy.get('[data-cy=add-tenant]').click();
        cy.get('[data-cy=name]').type("Test Tenant");
        cy.get('[data-cy=schedule-start-date]').type("2018-01-01");
        cy.get('[data-cy=draft-length]').type("7");
        cy.get('[data-cy=publish-notice]').type("7");
        // TODO: publish length is disabled/not supported by backend, uncomment when supported
        // (if ever)
        // cy.get('[data-cy=publish-length]').type("7");
        cy.get('[data-cy=rotation-length]').type("7");
        selectValue("Select a timezone...", "UTC");
        cy.get('[data-cy=save]').click();
        closeAlerts();
        
        changeToTenant("Test Tenant");
        
        // Create a new skill
        gotoPage("skills");
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("New Skill");
        cy.get('[aria-label="Save"]').click();
        closeAlerts();
        
        // Create two spots
        gotoPage("spots");
        
        // First spot doesn't require any skills
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("No Skill Spot");
        cy.get('[aria-label="Save"]').click();
        closeAlerts();
        
        // Second spot requires our skill
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("Required Skill Spot");
        selectValue("Select required skills...", "New Skill");
        cy.get('[aria-label="Save"]').click();
        closeAlerts();
        
        // Create a contract
        gotoPage("contracts");
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("New Contract");
        cy.get('[aria-label="Save"]').click();
        closeAlerts();
        
        // Create two employees
        gotoPage("employees");
        
        // First employee doesn't have any skills
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("No Skills Employee");
        selectValue("Select a contract...", "New Contract");
        cy.get('[aria-label="Save"]').click();
        
        // Second employee has our skill
        cy.get("button").contains("Add").click();
        cy.get('[aria-label="Name"]').type("Employee with Skills");
        selectValue("Select a contract...", "New Contract");
        selectValue("Select skill proficiencies...", "New Skill");
        cy.get('[aria-label="Save"]').click();
        closeAlerts();
        
        // Create some shifts
        gotoPage("shift");
        
        // First shift is on Monday, and is for the spot that doesn't require any skills
        selectValue("Select a Spot...", "No Skill Spot");
        cy.wait(1000);
        dragCreateShift(1, "09:00", "17:00");
        
        // Second shift is in the early morning on Tuesday, and is for the spot that requires our skill
        selectValue("Select a Spot...", "Required Skill Spot");
        cy.wait(1000);
        dragCreateShift(2, "00:00", "06:00");
        
        // Schedule for 5 seconds
        cy.get('[aria-label="Actions"]').click();
        cy.contains("Schedule").click();
        cy.wait(5000);
        closeAlerts();
        cy.get('[aria-label="Actions"]').click();
        cy.contains("Terminate Early").click();
        closeAlerts();
        
        // The second shift should have the employee with skills
        cy.contains("Employee with Skills").should('exist');
        
        // The first shift should have the employee without skills
        selectValue("Select a Spot...", "No Skill Spot");
        cy.wait(1000);
        cy.contains("No Skills Employee").should('exist');
    });
});