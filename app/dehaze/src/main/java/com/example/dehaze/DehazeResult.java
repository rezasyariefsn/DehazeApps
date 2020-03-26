package com.example.dehaze;

public class DehazeResult {
    private final int height;
    private final int width;
    private final int[] result;
    private final int[] depth;

    public DehazeResult(int height, int width, int[] result, int[] depth) {
        this.height = height;
        this.width = width;
        this.result = result;
        this.depth = depth;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int[] getResult() {
        return result;
    }

    public int[] getDepth() {
        return depth;
    }
}
