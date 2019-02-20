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
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.optaweb.employeerostering.gwtui.client.common.MomentJs.Moment;

class BootstrapDateTimePicker extends JQuery {

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native BootstrapDateTimePicker get(HTMLElement element);

    @JsMethod
    public native void datetimepicker(BootstrapDateTimePickerOptions options);

    @JsMethod
    public native Date val();

    @JsMethod
    public native void val(String date);

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerIcons {

        String today;
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerData {

        @JsMethod
        public native void date(String date);

        @JsMethod
        public native void date(Date date);

        @JsMethod
        public native void date(Moment moment);
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerOptions {

        String format;
        String toolbarPlacement;
        boolean inline;
        boolean sideBySide;
        boolean allowInputToggle;
        boolean showTodayButton;
        BootstrapDateTimePickerIcons icons;
    }
}
