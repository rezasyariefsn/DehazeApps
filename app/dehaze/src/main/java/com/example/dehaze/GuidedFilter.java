package com.example.dehaze;


import static com.example.dehaze.ColorUtils.getBlue;
import static com.example.dehaze.ColorUtils.getGreen;
import static com.example.dehaze.ColorUtils.getRed;
import static com.example.dehaze.ColorUtils.maxChannel;
import static com.example.dehaze.ColorUtils.minChannel;

public class GuidedFilter {
    private static final int GUIDED_FILTER_WINDOW_RADIUS = 40;
    private static final float EPS = 1e-3f;
    private static final int CHANNELS = 3;
    private static final int R = 0;
    private static final int G = 1;
    private static final int B = 2;

    private float[][][] normalize(int[] pixels, int height, int width) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                min = Math.min(min, minChannel(pixels[y * width + x]));
                max = Math.max(max, maxChannel(pixels[y * width + x]));
            }
        }
        float[][][] result = new float[height][width][CHANNELS];
        float length = Math.max(1, max - min);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                result[y][x][R] = (getRed(pixels[y * width + x]) - min) / length;
                result[y][x][G] = (getGreen(pixels[y * width + x]) - min) / length;
                result[y][x][B] = (getBlue(pixels[y * width + x]) - min) / length;
            }
        }
        return result;
    }

    private float[][] mean(float[][] source, int height, int width) {
        float[][] buf = new float[height][width];
        for (int y = 0; y < height; ++y) {
            buf[y][0] = source[y][0];
            for (int x = 1; x < width; ++x)
                buf[y][x] = buf[y][x - 1] + source[y][x];
        }
        for (int y = 1; y < height; ++y) {
            for (int x = 0; x < width; ++x)
                buf[y][x] = buf[y - 1][x] + buf[y][x];
        }
        float[][] result = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int leftExclusively = Math.max(-1, x - GUIDED_FILTER_WINDOW_RADIUS - 1);
                int rightInclusively = Math.min(width - 1, x + GUIDED_FILTER_WINDOW_RADIUS);
                int topExclusively = Math.max(-1, y - GUIDED_FILTER_WINDOW_RADIUS - 1);
                int bottomInclusively = Math.min(height - 1, y + GUIDED_FILTER_WINDOW_RADIUS);
                float sum = buf[bottomInclusively][rightInclusively];
                if (leftExclusively >= 0)
                    sum -= buf[bottomInclusively][leftExclusively];
                if (topExclusively >= 0)
                    sum -= buf[topExclusively][rightInclusively];
                if (leftExclusively >= 0 && topExclusively >= 0)
                    sum += buf[topExclusively][leftExclusively];
                int windowSize = (bottomInclusively - topExclusively) *
                        (rightInclusively - leftExclusively);
                result[y][x] = sum / windowSize;
            }
        }
        return result;
    }

    private float[][][] rotateDimensions(float[][][] source) {
        int height = source.length;
        int width = source[0].length;
        int depth = source[0][1].length;
        float[][][] result = new float[depth][height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                for (int z = 0; z < depth; ++z) {
                    result[z][y][x] = source[y][x][z];
                }
            }
        }
        return result;
    }

    private float[][] perElProduct(float[][] a, float[][] b, int height, int width) {
        float[][] result = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                result[y][x] = a[y][x] * b[y][x];
            }
        }
        return result;
    }

    private float[][] subtract(float[][] a, float[][] b, int height, int width) {
        float[][] result = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                result[y][x] = a[y][x] - b[y][x];
            }
        }
        return result;
    }

    private float[][] sum(float[][] a, float[][] b, int height, int width) {
        float[][] result = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                result[y][x] = a[y][x] + b[y][x];
            }
        }
        return result;
    }

    private float[][] inv3x3(float[][] m) { // todo optimize for symmetric matrix
        // computes the inverse of a matrix m
        float det = m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2]) -
                m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);

        float invdet = 1 / det;

        float[][] minv = new float[3][3];
        minv[0][0] = (m[1][1] * m[2][2] - m[2][1] * m[1][2]) * invdet;
        minv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invdet;
        minv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invdet;
        minv[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invdet;
        minv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invdet;
        minv[1][2] = (m[1][0] * m[0][2] - m[0][0] * m[1][2]) * invdet;
        minv[2][0] = (m[1][0] * m[2][1] - m[2][0] * m[1][1]) * invdet;
        minv[2][1] = (m[2][0] * m[0][1] - m[0][0] * m[2][1]) * invdet;
        minv[2][2] = (m[0][0] * m[1][1] - m[1][0] * m[0][1]) * invdet;
        return minv;
    }

    public float[][] filter(int[] rawGuidance, int height, int width, float[][] guided) {
        float[][][] guidance = normalize(rawGuidance, height, width);
        float[][][] guidanceMeans = new float[CHANNELS][][];
        float[][][] rotatedGuidance = rotateDimensions(guidance);
        for (int c = 0; c < CHANNELS; ++c) {
            guidanceMeans[c] = mean(rotatedGuidance[c], height, width);
        }
        float[][] guidedMean = mean(guided, height, width);
        float[][][] productMeans = new float[CHANNELS][][];
        for (int c = 0; c < CHANNELS; ++c) {
            productMeans[c] = mean(perElProduct(rotatedGuidance[c], guided, height, width), height, width);
        }
        float[][][] productCovariance = new float[CHANNELS][][];
        for (int c = 0; c < CHANNELS; ++c) {
            productCovariance[c] = subtract(productMeans[c], perElProduct(guidanceMeans[c], guidedMean, height, width), height, width);
        }

        float[][][][] var = new float[CHANNELS][CHANNELS][][];
        for (int i = 0; i < CHANNELS; ++i) {
            for (int j = i; j < CHANNELS; ++j) {
                var[i][j] = subtract(
                        mean(perElProduct(rotatedGuidance[i], rotatedGuidance[j], height, width), height, width),
                        perElProduct(guidanceMeans[i], guidanceMeans[j], height, width),
                        height, width
                );
            }
        }

        float[][][] a = new float[CHANNELS][height][width];
        float[][] sigma = new float[3][3];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                for (int i = 0; i < CHANNELS; ++i)
                    for (int j = i; j < CHANNELS; ++j)
                        sigma[i][j] = sigma[j][i] = var[i][j][y][x] + (i == j ? EPS : 0);
                float[][] invSigma = inv3x3(sigma);
                float covR = productCovariance[R][y][x];
                float covG = productCovariance[G][y][x];
                float covB = productCovariance[B][y][x];
                for (int i = 0; i < CHANNELS; ++i)
                    a[i][y][x] = covR * invSigma[0][i] + covG * invSigma[1][i] + covB * invSigma[2][i];
            }
        }
        float[][] b = subtract(subtract(subtract(
                guidedMean,
                perElProduct(a[R], guidanceMeans[R], height, width), height, width),
                perElProduct(a[G], guidanceMeans[G], height, width), height, width),
                perElProduct(a[B], guidanceMeans[B], height, width), height, width);
        return sum(sum(sum(
                perElProduct(mean(a[R], height, width), rotatedGuidance[R], height, width),
                perElProduct(mean(a[G], height, width), rotatedGuidance[G], height, width), height, width),
                perElProduct(mean(a[B], height, width), rotatedGuidance[B], height, width), height, width),
                mean(b, height, width), height, width);
    }
}
