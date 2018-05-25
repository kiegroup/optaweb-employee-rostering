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

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.HistoryManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;

@Templated
@ApplicationScoped
public class HeaderView implements IsElement {

    @Inject
    @DataField("rest-api")
    private HTMLAnchorElement restApi;

    @Inject
    @DataField("admin")
    private HTMLAnchorElement admin;

    @Inject
    @DataField("menu")
    private MenuView menu;

    @Inject
    @DataField("tenant-selector")
    private TenantSelectorView tenantSelectorView;

    @Inject
    @DataField("container")
    private HTMLDivElement container;

    @Inject
    @DataField("header")
    @Named("nav")
    private HTMLElement header;

    @Inject
    private Elemental2DomUtil domUtils;

    @Inject
    private HistoryManager historyManager;

    @Inject
    private Event<PageChange> pageChangeEvent;

    public void addStickyElement(org.jboss.errai.common.client.api.elemental2.IsElement element) {
        container.appendChild(element.getElement());
    }

    @EventHandler("admin")
    private void gotoAdminPage(@ForEvent("click") MouseEvent e) {
        historyManager.updateHistory(Pages.Id.ADMIN, Collections.emptyMap());
        pageChangeEvent.fire(new PageChange(Pages.Id.ADMIN));
    }

    public void removeStickyElements() {
        domUtils.removeAllElementChildren(container);
        container.appendChild(header);
    }

}
