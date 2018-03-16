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

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;

import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEES;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEE_ROSTER;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.ROTATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOTS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOT_ROSTER;

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
    @DataField("employee-roster")
    private HTMLAnchorElement employeeRoster;

    @Inject
    @DataField("spot-roster")
    private HTMLAnchorElement spotRoster;

    @Inject
    @DataField("rotation")
    private HTMLAnchorElement rotation;

    @Inject
    private Event<PageChange> pageChangeEvent;

    @PostConstruct
    private void initMenu() {
        pageChangeEvent.fire(new PageChange(Pages.Id.SKILLS));
        setInactive(skills, spots, employees, rotation, spotRoster, employeeRoster);
        setActive(skills);
    }

    @EventHandler("skills")
    public void skills(final @ForEvent("click") MouseEvent e) {
        goTo(SKILLS, e);
    }

    @EventHandler("spots")
    public void spots(final @ForEvent("click") MouseEvent e) {
        goTo(SPOTS, e);
    }

    @EventHandler("employees")
    public void employees(final @ForEvent("click") MouseEvent e) {
        goTo(EMPLOYEES, e);
    }

    @EventHandler("employee-roster")
    public void employeeRoster(final @ForEvent("click") MouseEvent e) {
        goTo(EMPLOYEE_ROSTER, e);
    }

    @EventHandler("spot-roster")
    public void spotRoster(final @ForEvent("click") MouseEvent e) {
        goTo(SPOT_ROSTER, e);
    }

    @EventHandler("rotation")
    public void rotations(final @ForEvent("click") MouseEvent e) {
        goTo(ROTATION, e);
    }

    private void goTo(final Pages.Id pageId,
                      final @ForEvent("click") MouseEvent event) {

        pageChangeEvent.fire(new PageChange(pageId));
        handleActiveLink(Js.cast(event.target));
    }

    private void handleActiveLink(final HTMLElement target) {
        setInactive(skills, spots, employees, employeeRoster, rotation, spotRoster);
        setActive(target);
    }

    private void setActive(final HTMLElement element) {
        ((HTMLElement) element.parentNode).classList.add("active");
    }

    private void setInactive(final HTMLElement... elements) {
        Arrays.asList(elements).forEach(e -> ((HTMLElement) e.parentNode).classList.remove("active"));
    }
}
