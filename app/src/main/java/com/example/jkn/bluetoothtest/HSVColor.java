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

    public static HSVColor randomHue() {
        Random rand = new Random();

        return new HSVColor(rand.nextInt(256), 255, 255);
    }

    @Override
    public String toString() {
        return String.valueOf(this.hue) +
                "/" +
                String.valueOf(this.saturation) +
                "/" +
                String.valueOf(this.value);
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
