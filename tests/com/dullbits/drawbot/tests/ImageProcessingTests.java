package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.image.ConvolutionEngine;
import com.dullbits.drawbot.image.ImageFilters;
import com.dullbits.drawbot.image.ImagePipeline;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Test Group A: Image-processing tests.
 * Validates aspect cropping, landscape auto-rotation, resizing, desaturation,
 * convolution unsharp masking, and brightness analysis.
 */
public final class ImageProcessingTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running ImageProcessingTests ---");
        testRotationLandscape();
        testRotationPortrait();
        testCropAspectRatio();
        testScaleWidth();
        testDesaturation();
        testAverageBrightness();
        testConvolutionUnsharp();
        System.out.println("ImageProcessingTests: ALL PASSED\n");
    }

    private static void testRotationLandscape() {
        // Landscape image: 400 x 200 -> should rotate 90 CCW to 200 x 400
        BufferedImage img = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        BufferedImage rotated = ImageFilters.rotateIfLandscape(img);
        if (rotated.getWidth() != 200 || rotated.getHeight() != 400) {
            throw new AssertionError("Expected 200x400 after rotation, got: " + rotated.getWidth() + "x" + rotated.getHeight());
        }
    }

    private static void testRotationPortrait() {
        // Portrait image: 200 x 400 -> should remain 200 x 400
        BufferedImage img = new BufferedImage(200, 400, BufferedImage.TYPE_INT_RGB);
        BufferedImage notRotated = ImageFilters.rotateIfLandscape(img);
        if (notRotated.getWidth() != 200 || notRotated.getHeight() != 400) {
            throw new AssertionError("Portrait image should not rotate");
        }
    }

    private static void testCropAspectRatio() {
        // Source: 300 x 300 (ratio 1.0). Target: 28 / 36 = 0.77778
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        float targetRatio = 28.0f / 36.0f;
        BufferedImage cropped = ImageFilters.cropToRatio(img, targetRatio);

        float resRatio = (float) cropped.getWidth() / (float) cropped.getHeight();
        if (Math.abs(resRatio - targetRatio) > 0.02f) {
            throw new AssertionError("Cropped ratio mismatch: " + resRatio + " vs " + targetRatio);
        }
        if (cropped.getHeight() != 300 || cropped.getWidth() != (int) (300 * targetRatio)) {
            throw new AssertionError("Unexpected crop dimensions: " + cropped.getWidth() + "x" + cropped.getHeight());
        }
    }

    private static void testScaleWidth() {
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        BufferedImage scaled = ImageFilters.scaleToWidth(img, 500);
        if (scaled.getWidth() != 500 || scaled.getHeight() != 1000) {
            throw new AssertionError("Scale failed: " + scaled.getWidth() + "x" + scaled.getHeight());
        }
    }

    private static void testDesaturation() {
        // Red image (255, 0, 0) -> luminance should be 0.299 * 255 = ~76 for all channels
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 10);
        g.dispose();

        BufferedImage gray = ImageFilters.desaturate(img);
        int rgb = gray.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;
        int gr = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        if (r != gr || gr != b || Math.abs(r - 76) > 2) {
            throw new AssertionError("Desaturation mismatch: r=" + r + " g=" + gr + " b=" + b);
        }
    }

    private static void testAverageBrightness() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE); // brightness = 255
        g.fillRect(0, 0, 10, 10);
        g.dispose();

        float avg = ImageFilters.averageBrightness(img);
        if (Math.abs(avg - 255.0f) > 0.01f) {
            throw new AssertionError("White image average brightness should be 255, got: " + avg);
        }
    }

    private static void testConvolutionUnsharp() {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        BufferedImage unsharp = ConvolutionEngine.unsharpen(img, 3);
        if (unsharp.getWidth() != 30 || unsharp.getHeight() != 30) {
            throw new AssertionError("Unsharp output dimensions changed");
        }
    }
}
