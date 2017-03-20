package com.example.jkn.bluetoothtest.colorpicker;

import android.graphics.Color;

import java.util.List;

/**
 * Created by Jan Klostermann on 01.07.2016.
 */
public class ColorName {

    private String name;
    private String hexColor;
    private int color;

    public ColorName() {

    }

    public ColorName(String name, String hexColor, int color) {
        this.name = name;
        this.hexColor = hexColor;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static ColorName findClosestColorName(List<ColorName> colorNameList, int colorToName) {

        double nearestColorDistance = Double.MAX_VALUE;
        ColorName nearestColorName = null;

        for (ColorName colorName : colorNameList) {
            int color = colorName.getColor();
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            int r1 = Color.red(colorToName);
            int g1 = Color.green(colorToName);
            int b1 = Color.blue(colorToName);

            double difference = Math.sqrt(Math.pow(r - r1, 2) + Math.pow(g - g1, 2) + Math.pow(b - b1, 2));

            if (difference == 0) {
                nearestColorName = colorName;
                break;
            }

            if (difference < nearestColorDistance) {
                nearestColorDistance = difference;
                nearestColorName = colorName;
            }
        }

        return nearestColorName;
    }

    public static double calculateLuminosity(int color) {

        double r = Color.red(color);
        double g = Color.green(color);
        double b = Color.blue(color);

        double[] c = new double[] {r, g, b};

        for (int i = 0; i < c.length; i++) {
            c[i] = c[i] / 255.0;
            if (c[i] <= 0.03928)
                c[i] = c[i] / 12.92;
            else
                c[i] = Math.pow((c[i] + 0.055) / 1.055, 2.4);
        }

        double luminosity = 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];

        return luminosity;
    }

    public static String colorToHexString(int color) {
        String hexColor = String.format("#%06X", (0xFFFFFF & color));
        return hexColor;
    }
}
