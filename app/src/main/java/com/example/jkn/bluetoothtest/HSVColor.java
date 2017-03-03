package com.example.jkn.bluetoothtest;

import java.util.Random;

/**
 * Created by jkn on 03.03.17.
 */

public class HSVColor {

    private int hue;
    private int saturation;
    private int value;

    public HSVColor(int hue, int saturation, int value) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }

    public static HSVColor random() {
        Random rand = new Random();

        return new HSVColor(rand.nextInt(256), rand.nextInt(256), 255);
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
