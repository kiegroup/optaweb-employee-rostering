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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.HistoryManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;

import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.AVAILABILITY_ROSTER;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.EMPLOYEES;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.ROTATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SHIFT_ROSTER;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOTS;

@Templated
@Singleton
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

    @Inject
    private TenantStore tenantStore;

    @Inject
    private HistoryManager historyManager;

    private Map<Pages.Id, HTMLAnchorElement> pageIdToAnchor;

    @PostConstruct
    private void initMenu() {
        pageIdToAnchor = new HashMap<>();
        pageIdToAnchor.put(SKILLS, skills);
        pageIdToAnchor.put(SPOTS, spots);
        pageIdToAnchor.put(EMPLOYEES, employees);
        pageIdToAnchor.put(ROTATION, rotation);
        pageIdToAnchor.put(AVAILABILITY_ROSTER, availabilityRoster);
        pageIdToAnchor.put(SHIFT_ROSTER, shiftRoster);
        pageChangeEvent.fire(new PageChange(Pages.Id.SHIFT_ROSTER));
        setAllInactive();
        setActive(shiftRoster);
    }

    @EventHandler("skills")
    public void skills(final @ForEvent("click") MouseEvent e) {
        goTo(SKILLS);
    }

    @EventHandler("spots")
    public void spots(final @ForEvent("click") MouseEvent e) {
        goTo(SPOTS);
    }

    @EventHandler("employees")
    public void employees(final @ForEvent("click") MouseEvent e) {
        goTo(EMPLOYEES);
    }

    @EventHandler("availability-roster")
    public void availabilityRoster(final @ForEvent("click") MouseEvent e) {
        goTo(AVAILABILITY_ROSTER);
    }

    @EventHandler("shift-roster")
    public void shiftRoster(final @ForEvent("click") MouseEvent e) {
        goTo(SHIFT_ROSTER);
    }

    @EventHandler("rotation")
    public void rotations(final @ForEvent("click") MouseEvent e) {
        goTo(ROTATION);
    }

    public void goTo(final Pages.Id pageId) {
        Map<String, String> params = new HashMap<>();
        params.put("tenantId", tenantStore.getCurrentTenantId().toString());
        historyManager.updateHistory(pageId, params);
        pageChangeEvent.fire(new PageChange(pageId));
    }

    public void handleActiveLink(@Observes PageChange pageChange) {
        setAllInactive();
        if (pageIdToAnchor.containsKey(pageChange.getPageId())) {
            setActive(pageIdToAnchor.get(pageChange.getPageId()));
        }
    }

    private void setActive(final HTMLElement element) {
        ((HTMLElement) element.parentNode).classList.add("active");
    }

    private void setAllInactive(final HTMLElement... elements) {
        pageIdToAnchor.values().forEach(e -> ((HTMLElement) e.parentNode).classList.remove("active"));
    }
}
