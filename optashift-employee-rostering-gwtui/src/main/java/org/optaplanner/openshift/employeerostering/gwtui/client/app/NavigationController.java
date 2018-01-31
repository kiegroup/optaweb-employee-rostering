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

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;

@Dependent
public class NavigationController {

    @Inject
    private AppView view;

    @Inject
    private Pages pages;

    public void onPageChanged(final @Observes PageChange pageChange) {
        final Page page = pages.get(pageChange.getPageId());

        page.beforeOpen().then(i -> {
            view.goTo(page);
            return page.onOpen();
        }).then(i -> {
            pageChange.afterPageOpen.run();
            return PromiseUtils.resolve();
        });
    }

    public HTMLElement getAppElement() {
        return view.getElement();
    }

    public static class PageChange {

        private final Pages.Id pageId;
        private final Runnable afterPageOpen;

        public PageChange(final Pages.Id pageId) {
            this(pageId, () -> {
            });
        }

        public PageChange(final Pages.Id pageId,
                          final Runnable afterPageOpen) {

            this.pageId = pageId;
            this.afterPageOpen = afterPageOpen;
        }

        private Pages.Id getPageId() {
            return pageId;
        }
    }
}
