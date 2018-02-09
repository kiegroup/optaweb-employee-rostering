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

import elemental2.dom.DomGlobal;

public class TimingUtils {

    public static <T> T time(final String label, final Supplier<T> r) {
        long start = System.currentTimeMillis();
        T ret = r.get();
        DomGlobal.console.info(label + " took " + (System.currentTimeMillis() - start) + "ms");
        return ret;
    }

    public static void time(final String label, final Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        DomGlobal.console.info(label + " took " + (System.currentTimeMillis() - start) + "ms");
    }
}
