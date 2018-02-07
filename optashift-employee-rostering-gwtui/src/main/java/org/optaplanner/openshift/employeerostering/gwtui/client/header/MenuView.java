/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.header;

import java.util.Arrays;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;

import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.DOMAIN_GRID_DEMO;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEES;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEE_ROSTER;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.ROTATIONS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOTS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOT_ROSTER;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.TEST_GRID_1;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.TEST_GRID_2;

@Templated
public class MenuView implements IsElement {

    @Inject
    @DataField("skills")
    private HTMLAnchorElement skills;

    @Inject
    @DataField("spots")
    private HTMLAnchorElement spots;

    @Inject
    @DataField("employees")
    private HTMLAnchorElement employees;

    @Inject
    @DataField("rotations")
    private HTMLAnchorElement rotations;

    @Inject
    @DataField("spot-roster")
    private HTMLAnchorElement spotRoster;

    @Inject
    @DataField("employee-roster")
    private HTMLAnchorElement employeeRoster;

    @Inject
    @DataField("test-grid-1")
    private HTMLAnchorElement testGrid1;

    @Inject
    @DataField("test-grid-2")
    private HTMLAnchorElement testGrid2;

    @Inject
    @DataField("domain-grid-demo")
    private HTMLAnchorElement domainGridDemo;

    @Inject
    private Event<PageChange> pageChangeEvent;

    @EventHandler("skills")
    public void skills(final ClickEvent e) {
        goTo(SKILLS, e);
    }

    @EventHandler("spots")
    public void spots(final ClickEvent e) {
        goTo(SPOTS, e);
    }

    @EventHandler("employees")
    public void employees(final ClickEvent e) {
        goTo(EMPLOYEES, e);
    }

    @EventHandler("rotations")
    public void rotations(final ClickEvent e) {
        goTo(ROTATIONS, e);
    }

    @EventHandler("spot-roster")
    public void spotRoster(final ClickEvent e) {
        goTo(SPOT_ROSTER, e);
    }

    @EventHandler("employee-roster")
    public void employeeRoster(final ClickEvent e) {
        goTo(EMPLOYEE_ROSTER, e);
    }

    @EventHandler("test-grid-1")
    public void testGrid1(final ClickEvent e) {
        goTo(TEST_GRID_1, e);
    }

    @EventHandler("test-grid-2")
    public void testGrid2(final ClickEvent e) {
        goTo(TEST_GRID_2, e);
    }

    @EventHandler("domain-grid-demo")
    public void domainGriDdemo(final ClickEvent e) {
        goTo(DOMAIN_GRID_DEMO, e);
    }

    private void goTo(final Pages.Id pageId,
                      final ClickEvent event) {

        pageChangeEvent.fire(new PageChange(pageId));
        handleActiveLink(Js.cast(event.getNativeEvent()));
    }

    private void handleActiveLink(final MouseEvent event) {
        setInactive(skills, spots, employees, rotations, spotRoster, employeeRoster, testGrid1, testGrid2, domainGridDemo);
        setActive(Js.cast(event.target));
    }

    private void setActive(final HTMLElement element) {
        ((HTMLElement) element.parentNode).classList.add("active");
    }

    private void setInactive(final HTMLElement... elements) {
        Arrays.asList(elements).forEach(e -> ((HTMLElement) e.parentNode).classList.remove("active"));
    }
}
