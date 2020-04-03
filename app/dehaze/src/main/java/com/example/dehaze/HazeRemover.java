package com.example.dehaze;

import android.graphics.Bitmap;

import static com.example.dehaze.ColorUtils.*;

public class HazeRemover {
    private static final float TRANSMISSION_THRESHOLD = 0.2f;
    private static final int MAX_ATMOSPHERE = 220;
    private static final int DARK_CHANNEL_WINDOW_RADIUS = 7;
    private static final float OMEGA = 0.95f;

    private final GuidedFilter guidedFilter;

    private final float[][] fBuffer1;
    private final float[][] fBuffer2;
    private final float[][] fBuffer3;
    private final float[][] fBuffer4;

    public HazeRemover(GuidedFilter guidedFilter, int maxHeight, int maxWidth) {
        this.guidedFilter = guidedFilter;
        this.fBuffer1 = new float[maxHeight][maxWidth];
        this.fBuffer2 = new float[maxHeight][maxWidth];
        this.fBuffer3 = new float[maxHeight][maxWidth];
        this.fBuffer4 = new float[maxHeight][maxWidth];
    }

    private static void calcDarkChannel(float[][] srcDest, float[][] buffer, int height, int width) {
        FloatMinQueue queue = new FloatMinQueue(Math.max(height, width));
        for (int y = 0; y < height; ++y) {
            queue.clear();
            for (int x = 0; x < DARK_CHANNEL_WINDOW_RADIUS; ++x) {
                float darkness = srcDest[y][x];
                queue.push(darkness);
            }
            for (int x = 0; x < width; ++x) {
                if (x - DARK_CHANNEL_WINDOW_RADIUS > 0) {
                    float obsolete = srcDest[y][x - DARK_CHANNEL_WINDOW_RADIUS - 1];
                    queue.pop(obsolete);
                }
                if (x + DARK_CHANNEL_WINDOW_RADIUS < width) {
                    float darkness = srcDest[y][x + DARK_CHANNEL_WINDOW_RADIUS];
                    queue.push(darkness);
                }
                buffer[y][x] = queue.min();
            }
        }
        for (int x = 0; x < width; ++x) {
            queue.clear();
            for (int y = 0; y < DARK_CHANNEL_WINDOW_RADIUS; ++y) {
                float darkness = buffer[y][x];
                queue.push(darkness);
            }
            for (int y = 0; y < height; ++y) {
                if (y - DARK_CHANNEL_WINDOW_RADIUS > 0) {
                    float obsolete = buffer[y - DARK_CHANNEL_WINDOW_RADIUS - 1][x];
                    queue.pop(obsolete);
                }
                if (y + DARK_CHANNEL_WINDOW_RADIUS < height) {
                    float darkness = buffer[y + DARK_CHANNEL_WINDOW_RADIUS][x];
                    queue.push(darkness);
                }
                srcDest[y][x] = queue.min();
            }
        }
    }

    private int getAtmosphere(int[] source, int height, int width, float[][] darkChannel) {
        float max = Float.MIN_VALUE; // todo replace with 5 percentile
        int maxX = -1, maxY = -1;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (darkChannel[y][x] > max) {
                    max = darkChannel[y][x];
                    maxX = x;
                    maxY = y;
                }
            }
        }
        return source[maxY * width + maxX];
    }

    private static void getTransmission(int[] source, float[][] destination, float[][] buffer, int height, int width, int atmosphere) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float darkness = Float.MAX_VALUE;
                darkness = Math.min(darkness, (float) getRed(source[y * width + x]) / getRed(atmosphere));
                darkness = Math.min(darkness, (float) getGreen(source[y * width + x]) / getGreen(atmosphere));
                darkness = Math.min(darkness, (float) getBlue(source[y * width + x]) / getBlue(atmosphere));
                destination[y][x] = darkness;
            }
        }
        calcDarkChannel(destination, buffer, height, width);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                destination[y][x] = 1 - OMEGA * destination[y][x];
            }
        }
    }

    private void calcRadiance(int[] srcDest, int atmosphere, float[][] transmission, int height, int width) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int r = toChannel((getRed(srcDest[y * width + x]) - getRed(atmosphere)) / transmission[y][x] + getRed(atmosphere));
                int g = toChannel((getGreen(srcDest[y * width + x]) - getGreen(atmosphere)) / transmission[y][x] + getGreen(atmosphere));
                int b = toChannel((getBlue(srcDest[y * width + x]) - getBlue(atmosphere)) / transmission[y][x] + getBlue(atmosphere));
                srcDest[y * width + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    //Depth Map
    private int[] toHeatmap(float[][] depth, int height, int width) {
        int[] result = new int[height * width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int channel = toChannel(depth[y][x] * 0xFF);
                // todo make red! coz red goaz fasta!!!
                result[y * width + x] = 0xFFFF0000 | (channel << 8);
            }
        }
        return result;
    }

    public DehazeResult dehaze(int[] pixels, int height, int width) {
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer1[y][x] = minChannel(pixels[y * width + x]);
        calcDarkChannel(fBuffer1, fBuffer2, height, width);
        int atmosphere = getAtmosphere(pixels, height, width, fBuffer1);
        // todo bound atmosphere
        getTransmission(pixels, fBuffer1, fBuffer2, height, width, atmosphere);
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer1[y][x] = Math.max(fBuffer1[y][x], TRANSMISSION_THRESHOLD); // todo threshold transmission remove?
        float[][] refinedTransmission = guidedFilter.filter(pixels, height, width, fBuffer1);
        calcRadiance(pixels, atmosphere, refinedTransmission, height, width);
        return new DehazeResult(
                height,
                width,
                pixels,
                toHeatmap(refinedTransmission, height, width)
        );
    }

    // FIXME Diwang nambahin custom dehaze method, supaya bisa diatur2 pakai seekbar. parameter: threshold
    public DehazeResult dehaze(int[] pixels, int height, int width, float threshold) {
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer1[y][x] = minChannel(pixels[y * width + x]);
        calcDarkChannel(fBuffer1, fBuffer2, height, width);
        int atmosphere = getAtmosphere(pixels, height, width, fBuffer1);
        // todo bound atmosphere
        getTransmission(pixels, fBuffer1, fBuffer2, height, width, atmosphere);
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer1[y][x] = Math.max(fBuffer1[y][x], threshold); // todo threshold transmission remove?
        float[][] refinedTransmission = guidedFilter.filter(pixels, height, width, fBuffer1);
        calcRadiance(pixels, atmosphere, refinedTransmission, height, width);
        return new DehazeResult(
                height,
                width,
                pixels,
                toHeatmap(refinedTransmission, height, width)
        );
    }

//    public Bitmap getBuffer1 (){
//        return Bitmap.createBitmap()
//    }


    public DehazeResult dehazeProcess1(int[] pixels, int height, int width, float threshold) {
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer3[y][x] = minChannel(pixels[y * width + x]);
        calcDarkChannel(fBuffer3, fBuffer2, height, width);
//        int atmosphere = getAtmosphere(pixels, height, width, fBuffer1);
//        // todo bound atmosphere
//        getTransmission(pixels, fBuffer1, fBuffer2, height, width, atmosphere);
//        for (int y = 0; y < height; ++y)
//            for (int x = 0; x < width; ++x)
//                fBuffer1[y][x] = Math.max(fBuffer1[y][x], threshold); // todo threshold transmission remove?
//        float[][] refinedTransmission = guidedFilter.filter(pixels, height, width, fBuffer1);
//        calcRadiance(pixels, atmosphere, refinedTransmission, height, width);
        return new DehazeResult(
                height,
                width,
                pixels,
                toHeatmap(fBuffer3, height, width)
        );
    }

    public DehazeResult dehazeProcess2(int[] pixels, int height, int width, float threshold) {
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                fBuffer4[y][x] = minChannel(pixels[y * width + x]);
        calcDarkChannel(fBuffer4, fBuffer2, height, width);
        int atmosphere = getAtmosphere(pixels, height, width, fBuffer4);
        // todo bound atmosphere
        getTransmission(pixels, fBuffer4, fBuffer2, height, width, atmosphere);
//        for (int y = 0; y < height; ++y)
//            for (int x = 0; x < width; ++x)
//                fBuffer1[y][x] = Math.max(fBuffer1[y][x], threshold); // todo threshold transmission remove?
//        calcRadiance(pixels, atmosphere, refinedTransmission, height, width);
        float[][] refinedTransmission = guidedFilter.filter(pixels, height, width, fBuffer1);
        return new DehazeResult(
                height,
                width,
                pixels,
                toHeatmap(fBuffer4, height, width)
        );
    }

    private static class FloatMinQueue {

        private final float[] queue;
        private int head, tail;

        FloatMinQueue(int maxSize) {
            this.queue = new float[maxSize];
        }

        void push(float value) {
            while (tail > head && queue[tail - 1] > value)
                tail--;
            queue[tail++] = value;
        }

        void pop(float value) {
            if (tail > head && queue[head] == value)
                head++;
        }

        float min() {
            return queue[head];
        }

        void clear() {
            head = tail = 0;
        }
    }
}


