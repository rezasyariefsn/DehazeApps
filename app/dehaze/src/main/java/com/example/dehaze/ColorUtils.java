package com.example.dehaze;

public class ColorUtils {
    static int getRed(int color) {
        return (color >>> 16) & 0xFF;
    }

    static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    static int getBlue(int color) {
        return color & 0xFF;
    }

    static int minChannel(int color) {
        return Math.min(getRed(color), Math.min(getGreen(color), getBlue(color)));
    }

    static int maxChannel(int color) {
        return Math.max(getRed(color), Math.max(getGreen(color), getBlue(color)));
    }

    static int toChannel(float value) {
        return Math.min(255, Math.max(0, Math.round(value)));
    }
}
