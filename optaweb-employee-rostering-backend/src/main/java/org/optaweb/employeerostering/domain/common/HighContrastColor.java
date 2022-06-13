package org.optaweb.employeerostering.domain.common;

import java.util.Objects;

public enum HighContrastColor {
    // Patternfly Red
    DARK_RED("#7d1007"),
    RED("#a30000"),
    LIGHT_RED("#c9190b"),

    // Patternfly Blue
    DARK_BLUE("#2b9af3"),
    BLUE("#73bcf7"),
    LIGHT_BLUE("#bee1f4"),

    // Patternfly Yellow
    DARK_YELLOW("#f4c145"),
    YELLOW("#f6d173"),
    LIGHT_YELLOW("#f9e0a2"),

    // Patternfly Orange
    DARK_ORANGE("#ec7a08"),
    ORANGE("#ef9234"),
    LIGHT_ORANGE("#f4b678"),

    // Patternfly Green
    DARK_GREEN("#6ec664"),
    GREEN("#95d58e"),
    LIGHT_GREEN("#bde5b8"),

    // Patternfly Purple
    DARK_PURPLE("#a18fff"),
    PURPLE("#b2a3ff"),
    LIGHT_PURPLE("#cbc1ff"),

    // Patternfly Cyan
    DARK_CYAN("#a2d9d9"),
    CYAN("#73c5c5"),
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
