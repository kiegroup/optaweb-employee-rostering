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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;
import static org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder.getCurrentSpotRosterView;

@Templated
public class SpotRosterPage implements Page {

    @Inject
    @DataField("solve")
    private HTMLButtonElement solve;

    @Inject
    @DataField("viewport")
    private ViewportView<LocalDateTime> viewportView;

    @Inject
    @Named("span")
    @DataField("hard-score")
    private HTMLElement hardScore;

    @Inject
    @Named("span")
    @DataField("soft-score")
    private HTMLElement softScore;

    @Inject
    private SpotRosterViewportFactory spotRosterViewportFactory;

    @Inject
    private TenantStore tenantStore;

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public Promise<Void> refresh() {
        return fetchSpotRosterView().then(spotRosterView -> {
            final Optional<HardSoftScore> score = Optional.ofNullable(spotRosterView.getScore());
            hardScore.textContent = score.map(HardSoftScore::getHardScore).map(Object::toString).orElse("");
            softScore.textContent = score.map(HardSoftScore::getSoftScore).map(Object::toString).orElse("");
            return initViewportView(spotRosterView);
        });
    }

    private Promise<SpotRosterView> fetchSpotRosterView() {
        return new Promise<>((resolve, reject) -> {
            getCurrentSpotRosterView(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<Void> initViewportView(final SpotRosterView spotRosterView) {
        viewportView.setViewport(spotRosterViewportFactory.getViewport(spotRosterView));
        return PromiseUtils.resolve();
    }

    private Promise<Void> solveRoster() {
        return new Promise<>((resolve, reject) -> {
            RosterRestServiceBuilder.solveRoster(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    @EventHandler("solve")
    public void onSolveClicked(final ClickEvent ignore) {
        solveRoster().then(i -> {
            repeat(this::refresh, 30000, 1000);
            return PromiseUtils.resolve();
        });
    }

    private void repeat(final Runnable task,
                        final int total,
                        final int step) {

        final long start = System.currentTimeMillis();
        Scheduler.get().scheduleFixedDelay(() -> {
            task.run();
            return System.currentTimeMillis() - start <= total;
        }, step);
    }
}
