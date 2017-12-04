package org.optaplanner.openshift.employeerostering.gwtui.client.canvas;

import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class ColorUtils {

    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5)) / 2.0;

    // TODO: Make a proper brighten method
    public static String brighten(String color) {
        return blend(color, "#E0E0E0", 0.5);
    }

    public static String blend(String color1, String color2, double rgb2amount) {
        int[] rgb1 = getRGB(color1);
        int[] rgb2 = getRGB(color2);
        double rgb1amount = 1 - rgb2amount;

        return getHexFromRGB(new int[]{(int) Math.round(rgb1[0] * rgb1amount + rgb2[0] * rgb2amount),
                (int) Math.round(rgb1[1] * rgb1amount + rgb2[1] * rgb2amount),
                (int) Math.round(rgb1[2] * rgb1amount + rgb2[2] * rgb2amount)});
    }

    public static String getColor(int num) {
        int[] color = getColorHelper(num);
        int hexNum = color[0] + (color[1] << 8) + (color[2] << 16);
        String hex = Integer.toHexString(hexNum);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return "#" + hex;
    }

    public static String getTextColor(String backgroundColor) {
        int[] rgb = getRGB(backgroundColor);
        double[] linearRGB = getLinearRGB(rgb);
        double L = 0.2126 * linearRGB[0] + 0.7152 * linearRGB[1] + 0.0722 * linearRGB[2];
        if (L > 0.179) {
            return "#000000";
        } else {
            return "#FFFFFF";
        }
    }

    public static double[] getLinearRGB(int[] rgb) {
        double[] linearRGB = new double[3];
        for (int i = 0; i < rgb.length; i++) {
            linearRGB[i] = rgb[i] / 255.0;
            if (linearRGB[i] <= 0.03928) {
                linearRGB[i] = linearRGB[i] / 12.92;
            } else {
                linearRGB[i] = Math.pow(((linearRGB[i] + 0.055) / 1.055), 2.4);
            }
        }
        return linearRGB;
    }

    public static int[] getRGB(String color) {
        if (color.startsWith("#")) {
            return getRGBFromHex(color);
        } else {
            return getRGBFromRGB(color);
        }
    }

    public static int[] getRGBFromHex(String hex) {
        String red = hex.substring(1, 3);
        String green = hex.substring(3, 5);
        String blue = hex.substring(5, 7);
        return new int[]{Integer.parseInt(red, 16), Integer.parseInt(green, 16), Integer.parseInt(blue, 16)};
    }

    public static int[] getRGBFromRGB(String rgb) {
        String[] values = rgb.substring(4).split(",");
        return new int[]{Integer.parseInt(values[0].trim()),
                Integer.parseInt(values[1].trim()),
                Integer.parseInt(values[2].substring(0, values[2].indexOf(')')).trim())};
    }

    public static String getHexFromRGB(int[] rgb) {
        int hexNum = rgb[0] + (rgb[1] << 8) + (rgb[2] << 16);
        String hex = Integer.toHexString(hexNum);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return "#" + hex;
    }

    public static int[] getColorHelper(int num) {
        int[] out = new int[3];
        out[0] = 0x59;
        out[1] = 0x26;
        out[2] = 0x7F;

        out[0] = (int) Math.round((out[0] * Math.pow(GOLDEN_RATIO, num)) % 256);
        out[1] = (int) Math.round((out[1] * Math.pow(GOLDEN_RATIO, num) + out[0]) % 256);
        out[2] = (int) Math.round((out[2] * Math.pow(GOLDEN_RATIO, num) + out[1]) % 256);

        return out;
    }

}
