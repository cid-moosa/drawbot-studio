package com.dullbits.drawbot.image;

import java.awt.image.BufferedImage;

/**
 * 2D spatial convolution filtering matching Image_Tools.pde.
 * Implements subtle unsharp masking, scaling/normalizing kernels, and convolution passes.
 */
public final class ConvolutionEngine {

    private static final float[][] BASE_UNSHARP = {
        { -0.00391f, -0.01563f, -0.02344f, -0.01563f, -0.00391f },
        { -0.01563f, -0.06250f, -0.09375f, -0.06250f, -0.01563f },
        { -0.02344f, -0.09375f,  1.85980f, -0.09375f, -0.02344f },
        { -0.01563f, -0.06250f, -0.09375f, -0.06250f, -0.01563f },
        { -0.00391f, -0.01563f, -0.02344f, -0.01563f, -0.00391f }
    };

    private static final float[][] BLUR_3X3 = {
        { 1f, 1f, 1f },
        { 1f, 1f, 1f },
        { 1f, 1f, 1f }
    };

    private static final float[][] SHARPEN_3X3 = {
        {  0f, -1f,  0f },
        { -1f,  5f, -1f },
        {  0f, -1f,  0f }
    };

    private ConvolutionEngine() {}

    /**
     * Applies subtle unsharp mask convolution with amount-scaled kernel.
     */
    public static BufferedImage unsharpen(BufferedImage src, int amount) {
        float[][] matrix = (amount > 1) ? scaleMatrix(BASE_UNSHARP, amount) : copyMatrix(BASE_UNSHARP);
        matrix = normalizeMatrix(matrix);
        return convolve(src, matrix, 1.0f, 0.0f);
    }

    public static BufferedImage blur(BufferedImage src) {
        return convolve(src, normalizeMatrix(BLUR_3X3), 1.0f, 0.0f);
    }

    public static BufferedImage sharpen(BufferedImage src) {
        return convolve(src, SHARPEN_3X3, 1.0f, 0.0f);
    }

    /**
     * Applies 2D spatial convolution kernel with factor scaling and bias offset.
     */
    public static BufferedImage convolve(BufferedImage src, float[][] matrix, float factor, float bias) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[] srcPixels = ImageFilters.getPixels(src);
        int[] dstPixels = ImageFilters.getPixels(dst);

        int rows = matrix.length;
        int cols = matrix[0].length;
        int offsetR = rows / 2;
        int offsetC = cols / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float rTotal = 0.0f;
                float gTotal = 0.0f;
                float bTotal = 0.0f;

                for (int i = 0; i < rows; i++) {
                    int py = Math.min(h - 1, Math.max(0, y + i - offsetR));
                    int rowIdx = py * w;
                    for (int j = 0; j < cols; j++) {
                        int px = Math.min(w - 1, Math.max(0, x + j - offsetC));
                        int rgb = srcPixels[rowIdx + px];

                        float weight = matrix[i][j];
                        rTotal += ((rgb >> 16) & 0xFF) * weight;
                        gTotal += ((rgb >> 8) & 0xFF) * weight;
                        bTotal += (rgb & 0xFF) * weight;
                    }
                }

                rTotal = (rTotal * factor) + bias;
                gTotal = (gTotal * factor) + bias;
                bTotal = (bTotal * factor) + bias;

                int r = Math.min(255, Math.max(0, Math.round(rTotal)));
                int g = Math.min(255, Math.max(0, Math.round(gTotal)));
                int b = Math.min(255, Math.max(0, Math.round(bTotal)));

                dstPixels[y * w + x] = (r << 16) | (g << 8) | b;
            }
        }
        return dst;
    }

    public static float[][] scaleMatrix(float[][] matrix, int scale) {
        if (scale <= 1) return copyMatrix(matrix);
        int n = matrix.length;
        int p = matrix[0].length;
        float[][] nmatrix = new float[n * scale][p * scale];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                float val = matrix[i][j];
                for (int si = 0; si < scale; si++) {
                    for (int sj = 0; sj < scale; sj++) {
                        nmatrix[(i * scale) + si][(j * scale) + sj] = val;
                    }
                }
            }
        }
        return nmatrix;
    }

    public static float[][] normalizeMatrix(float[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        float sum = 0.0f;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                sum += matrix[i][j];
            }
        }

        float absSum = Math.abs(sum);
        if (absSum < 0.00001f) absSum = 1.0f;

        float[][] result = new float[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i][j] = matrix[i][j] / absSum;
            }
        }
        return result;
    }

    public static float[][] copyMatrix(float[][] matrix) {
        float[][] copy = new float[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }
}
