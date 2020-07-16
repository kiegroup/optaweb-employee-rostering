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

import java.util.Objects;

public enum HighContrastColor {
    // Patternfly Red
    DARK_RED("#7D1007"),
    RED("#A30000"),
    LIGHT_RED("#C9190B"),
        
    // Patternfly Blue
    DARK_BLUE("#2B9AF3"),
    BLUE("#73BCF7"),
    LIGHT_BLUE("#BEE1F4"),
        
    // Patternfly Yellow
    DARK_YELLOW("#F4C145"),
    YELLOW("#F6D173"),
    LIGHT_YELLOW("#F9E0A2"),
        
    // Patternfly Orange
    DARK_ORANGE("#EC7A08"),
    ORANGE("#EF9234"),
    LIGHT_ORANGE("#F4B678"),
        
    // Patternfly Green
    DARK_GREEN("#6EC664"),
    GREEN("#95D58E"),
    LIGHT_GREEN("#BDE5B8"),
        
    // Patternfly Purple
    DARK_PURPLE("#A18FFF"),
    PURPLE("#B2A3FF"),
    LIGHT_PURPLE("#CBC1FF"),
        
    // Patternfly Cyan
    DARK_CYAN("#A2D9D9"), 
    CYAN("#73C5C5"),
    LIGHT_CYAN("#009596");
    
    String colorInHex;
    
    private HighContrastColor(String colorInHex) {
        this.colorInHex = colorInHex;
    }
    
    /**
     * Generates a color for the given Object. The color
     * is chosen based on the Object hash code.
     * 
     * @param obj the Object to generate a color for
     * @return A String representing the color generated in hex
     */
    public static String generateColorFromHashcode(Object obj) {
        HighContrastColor[] colorChoices = HighContrastColor.values();
        return colorChoices[Math.abs(Objects.hashCode(obj) % (colorChoices.length))].colorInHex;
    }
}
