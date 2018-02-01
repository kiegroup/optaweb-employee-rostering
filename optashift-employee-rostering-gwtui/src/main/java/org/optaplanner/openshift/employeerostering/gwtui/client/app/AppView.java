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

package org.optaplanner.openshift.employeerostering.gwtui.client.app;

import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import jsinterop.base.Js;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.footer.FooterView;
import org.optaplanner.openshift.employeerostering.gwtui.client.header.HeaderView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;

@Templated
public class AppView implements IsElement {

    @Inject
    @DataField("header")
    private HeaderView header;

    @Inject
    @DataField("content")
    private HTMLDivElement content;

    @Inject
    @DataField("footer")
    private FooterView footer;

    public void goTo(final Page page) {
        content.innerHTML = "";
        content.appendChild(page.getElement());
    }
}
