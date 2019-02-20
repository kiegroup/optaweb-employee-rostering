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

package org.optaweb.employeerostering.gwtui.client.common;

import elemental2.core.Date;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class MomentJs {

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(Date date, String format);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(String date, String format);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(Date date);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(String date);

    @JsType(isNative = true, name = "Moment")
    public static class Moment {

        public native String format(String format);

        public native Moment day(double num);

        public native Date toDate();
    }
}
