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

package org.optaweb.employeerostering.gwtui.client.header;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
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
import org.optaweb.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaweb.employeerostering.gwtui.client.pages.Pages;
import org.optaweb.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;

import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.AVAILABILITY_ROSTER;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.CONTRACTS;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEES;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.ROTATION;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.SHIFT_ROSTER;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;
import static org.optaweb.employeerostering.gwtui.client.pages.Pages.Id.SPOTS;

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
    @DataField("contracts")
    private HTMLAnchorElement contracts;

    @Inject
    @DataField("availability-roster")
    private HTMLAnchorElement availabilityRoster;

    @Inject
    @DataField("shift-roster")
    private HTMLAnchorElement shiftRoster;

    @Inject
    @DataField("rotation")
    private HTMLAnchorElement rotation;

    @Inject
    private Event<PageChange> pageChangeEvent;

    @PostConstruct
    private void initMenu() {
        pageChangeEvent.fire(new PageChange(Pages.Id.SHIFT_ROSTER));
        setInactive(skills, spots, employees, contracts, rotation, shiftRoster, availabilityRoster, shiftRoster);
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

    @EventHandler("contracts")
    public void contracts(final @ForEvent("click") MouseEvent e) {
        goTo(CONTRACTS, e);
    }

    @EventHandler("availability-roster")
    public void availabilityRoster(final @ForEvent("click") MouseEvent e) {
        goTo(AVAILABILITY_ROSTER, e);
    }

    @EventHandler("shift-roster")
    public void shiftRoster(final @ForEvent("click") MouseEvent e) {
        goTo(SHIFT_ROSTER, e);
    }

    @EventHandler("rotation")
    public void rotations(final @ForEvent("click") MouseEvent e) {
        goTo(ROTATION, e);
    }

    private void goTo(final Pages.Id pageId,
                      final @ForEvent("click") MouseEvent event) {
        if (!isDisabled(Js.cast(event.target))) {
            pageChangeEvent.fire(new PageChange(pageId));
            handleActiveLink(Js.cast(event.target));
        } else {
            ErrorPopup.show("There are no Tenants currently. Add one in the Admin page first.");
        }
    }

    public void onTenantsReady(final @Observes TenantStore.TenantsReady tenantsReady) {
        setEnabled(skills, spots, employees, contracts, rotation, shiftRoster, availabilityRoster);
    }

    public void onNoTenants(final @Observes TenantStore.NoTenants noTenants) {
        setDisabled(skills, spots, employees, contracts, rotation, shiftRoster, availabilityRoster);
    }

    public void handleActiveLink(final HTMLElement target) {
        setInactive(skills, spots, employees, contracts, availabilityRoster, rotation, shiftRoster);
        setActive(target);
    }

    public void onPageChangeEvent(@Observes final PageChange pageChangeEvent) {
        setInactive(skills, spots, employees, contracts, availabilityRoster, rotation, shiftRoster);
        switch (pageChangeEvent.getPageId()) {
            case AVAILABILITY_ROSTER:
                setActive(availabilityRoster);
                break;
            case EMPLOYEES:
                setActive(employees);
                break;
            case ROTATION:
                setActive(rotation);
                break;
            case SHIFT_ROSTER:
                setActive(shiftRoster);
                break;
            case SKILLS:
                setActive(skills);
                break;
            case SPOTS:
                setActive(spots);
                break;
            default:
                break;
        }
    }

    private void setActive(final HTMLElement element) {
        ((HTMLElement) element.parentNode).classList.add("active");
    }

    private void setInactive(final HTMLElement... elements) {
        Arrays.asList(elements).forEach(e -> ((HTMLElement) e.parentNode).classList.remove("active"));
    }

    private void setEnabled(final HTMLElement... elements) {
        Arrays.asList(elements).forEach(e -> e.setAttribute("data-is-disabled", "false"));
    }

    private void setDisabled(final HTMLElement... elements) {
        Arrays.asList(elements).forEach(e -> e.setAttribute("data-is-disabled", "true"));
    }

    private boolean isDisabled(final HTMLElement element) {
        return element.getAttribute("data-is-disabled").equals("true");
    }
}
