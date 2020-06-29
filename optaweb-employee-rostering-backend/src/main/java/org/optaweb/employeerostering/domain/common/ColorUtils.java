/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.domain.common;

public class ColorUtils {
    
    private static final String[] COLOR_CHOICES = new String[] {
        // Patternfly Red
        "#7D1007", "#A30000", "#C9190B",
        // Patternfly Blue
        "#2B9AF3", "#73BCF7", "#BEE1F4",
        // Patternfly Yellow
        "#F4C145", "#F6D173", "#F9E0A2",
        // Patternfly Orange
        "#EC7A08", "#EF9234", "#F4B678",
        // Patternfly Green
        "#6EC664", "#95D58E", "#BDE5B8",
        // Patternfly Purple
        "#A18FFF", "#B2A3FF", "#CBC1FF",
        // Patternfly Cyan
        "#A2D9D9", "#73C5C5", "#009596",
    };
    
    private ColorUtils() {}
    
    public static String getRandomColor(String name) {
        return COLOR_CHOICES[Math.abs(name.hashCode() % (COLOR_CHOICES.length))];
    }
}
