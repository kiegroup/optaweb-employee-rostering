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

package org.optaplanner.openshift.employeerostering.gwtui.client.util;

import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.slf4j.Logger;

@Dependent
public class TimingUtils {

    @Inject
    private Logger logger;

    @Inject
    private LoadingSpinner loadingSpinner;

    public <T> T time(final String label, final Supplier<T> r) {
        long start = System.currentTimeMillis();
        T ret = r.get();
        logger.info(label + " took " + (System.currentTimeMillis() - start) + "ms");
        return ret;
    }

    public void time(final String label, final Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        logger.info(label + " took " + (System.currentTimeMillis() - start) + "ms");
    }

    public void repeat(final Runnable task,
                       final int total,
                       final int step,
                       final String loadingTaskId) {

        final long start = System.currentTimeMillis();

        Scheduler.get().scheduleFixedDelay(() -> {

            task.run();

            final boolean shouldRunAgain = System.currentTimeMillis() - start <= total;

            if (!shouldRunAgain) {
                loadingSpinner.hideFor(loadingTaskId);
            }

            return shouldRunAgain;
        }, step);
    }
}
