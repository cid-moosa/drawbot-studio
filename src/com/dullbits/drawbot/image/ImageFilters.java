package com.dullbits.drawbot.image;

import com.dullbits.drawbot.core.MachineConfig;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * Fundamental image transformation algorithms matching Image_Tools.pde:
 *  - rotate90CCW: Auto-rotates landscape images 90 degrees CCW
 *  - cropToRatio: Center-crops to machine aspect ratio
 *  - scaleToWidth: High-quality resize maintaining aspect ratio
 *  - desaturate: Grayscale luminance conversion
 *  - averageBrightness: Average luminance across all pixels
 *  - lightenPixel: Incremental pixel lightening with micro-randomness
 */
public final class ImageFilters {
    private static final Random RNG = new Random(3);

    private ImageFilters() {}

    /**
     * Rotates image 90 degrees counter-clockwise if width > height to fit plotter portrait orientation.
     * Preserves exact coordinate mapping from Image_Tools.pde:
     *   loc2 = y + (img.width - x) * img2.width
     */
    public static BufferedImage rotateIfLandscape(BufferedImage src) {
        if (src.getWidth() <= src.getHeight()) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);

        int[] srcPixels = getPixels(src);
        int[] dstPixels = getPixels(dst);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int loc1 = x + y * w;
                int loc2 = y + (w - 1 - x) * h;
                if (loc2 >= 0 && loc2 < dstPixels.length) {
                    dstPixels[loc2] = srcPixels[loc1];
                }
            }
        }
        return dst;
    }

    /**
     * Center crops the image to the desired aspect ratio (e.g., 28 / 36 = 0.77778).
     */
    public static BufferedImage cropToRatio(BufferedImage src, float desiredRatio) {
        float currentRatio = (float) src.getWidth() / (float) src.getHeight();
        int w = src.getWidth();
        int h = src.getHeight();

        int cropX = 0;
        int cropY = 0;
        int cropW = w;
        int cropH = h;

        if (currentRatio < desiredRatio) {
            // Taller than desired: crop top and bottom
            cropW = w;
            cropH = (int) (w / desiredRatio);
            cropY = (h - cropH) / 2;
        } else {
            // Wider than desired: crop left and right
            cropW = (int) (h * desiredRatio);
            cropH = h;
            cropX = (w - cropW) / 2;
        }

        BufferedImage dst = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, cropW, cropH, cropX, cropY, cropX + cropW, cropY + cropH, null);
        g.dispose();
        return dst;
    }

    /**
     * Resizes the image to targetWidth while maintaining aspect ratio.
     */
    public static BufferedImage scaleToWidth(BufferedImage src, int targetWidth) {
        if (src.getWidth() == targetWidth) {
            return src;
        }
        int targetHeight = (int) Math.round((double) src.getHeight() * targetWidth / src.getWidth());
        if (targetHeight <= 0) targetHeight = 1;

        BufferedImage dst = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return dst;
    }

    /**
     * Converts image to grayscale luminance: 0.299 R + 0.587 G + 0.114 B.
     */
    public static BufferedImage desaturate(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] srcPixels = getPixels(src);
        int[] dstPixels = getPixels(dst);

        for (int i = 0; i < srcPixels.length; i++) {
            int rgb = srcPixels[i];
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int lum = (int) (0.299f * r + 0.587f * g + 0.114f * b + 0.5f);
            lum = Math.min(255, Math.max(0, lum));
            dstPixels[i] = (lum << 16) | (lum << 8) | lum;
        }
        return dst;
    }

    /**
     * Calculates the average brightness of the image (0.0 to 255.0).
     */
    public static float averageBrightness(BufferedImage src) {
        int[] pixels = getPixels(src);
        if (pixels == null || pixels.length == 0) return 255.0f;
        double sum = 0.0;
        for (int rgb : pixels) {
            sum += brightness(rgb);
        }
        return (float) (sum / pixels.length);
    }

    /**
     * Returns the brightness (0.0 - 255.0) of a single RGB integer.
     */
    public static float brightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Math.max(r, Math.max(g, b));
    }

    /**
     * Incremental pixel lightening matching Image_Tools.pde:
     *   r += adjustbrightness + random(0, 0.01)
     *   r = constrain(r, 0, 255)
     */
    public static void lightenPixel(int[] pixels, int width, int x, int y, int adjustBrightness) {
        int loc = y * width + x;
        if (loc < 0 || loc >= pixels.length) return;

        float b = brightness(pixels[loc]);
        b += adjustBrightness + (RNG.nextFloat() * 0.01f);
        int lum = Math.min(255, Math.max(0, (int) Math.round(b)));
        pixels[loc] = (lum << 16) | (lum << 8) | lum;
    }

    public static int[] getPixels(BufferedImage img) {
        if (img == null) return null;
        if (img.getType() == BufferedImage.TYPE_INT_RGB || img.getType() == BufferedImage.TYPE_INT_ARGB) {
            return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        }
        BufferedImage copy = ImagePipeline.copyImage(img);
        return ((DataBufferInt) copy.getRaster().getDataBuffer()).getData();
    }

    /**
     * Adjusts brightness (-100 to +100), contrast (-100 to +100), and optional inversion.
     */
    public static BufferedImage adjustBrightnessContrast(BufferedImage src, int brightnessOffset, int contrastOffset, boolean invert) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[] srcPixels = getPixels(src);
        int[] dstPixels = getPixels(dst);

        float factor = (259.0f * (contrastOffset + 255.0f)) / (255.0f * (259.0f - contrastOffset));

        for (int i = 0; i < srcPixels.length; i++) {
            int rgb = srcPixels[i];
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            if (contrastOffset != 0) {
                r = Math.min(255, Math.max(0, (int) (factor * (r - 128) + 128)));
                g = Math.min(255, Math.max(0, (int) (factor * (g - 128) + 128)));
                b = Math.min(255, Math.max(0, (int) (factor * (b - 128) + 128)));
            }

            if (brightnessOffset != 0) {
                r = Math.min(255, Math.max(0, r + brightnessOffset));
                g = Math.min(255, Math.max(0, g + brightnessOffset));
                b = Math.min(255, Math.max(0, b + brightnessOffset));
            }

            if (invert) {
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
            }

            dstPixels[i] = (r << 16) | (g << 8) | b;
        }
        return dst;
    }

    /**
     * Applies orientation transforms (rotation, flip, optional auto-rotate) to the source image.
     */
    public static BufferedImage applyOrientation(BufferedImage src, int rotationAngle, boolean flipH, boolean flipV, boolean autoRotateLandscape) {
        if (src == null) return null;

        BufferedImage current = src;
        if (autoRotateLandscape && current.getWidth() > current.getHeight()) {
            current = rotateIfLandscape(current);
        }

        int normAngle = rotationAngle % 360;
        if (normAngle < 0) normAngle += 360;

        boolean needRotate = (normAngle != 0);
        boolean needFlip = (flipH || flipV);

        if (!needRotate && !needFlip) {
            return current;
        }

        int srcW = current.getWidth();
        int srcH = current.getHeight();
        int newW = (normAngle == 90 || normAngle == 270) ? srcH : srcW;
        int newH = (normAngle == 90 || normAngle == 270) ? srcW : srcH;

        BufferedImage dst = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, newW, newH);

        // Apply rotation
        if (normAngle == 90) {
            g2.translate(newW, 0);
            g2.rotate(Math.PI / 2);
        } else if (normAngle == 180) {
            g2.translate(newW, newH);
            g2.rotate(Math.PI);
        } else if (normAngle == 270) {
            g2.translate(0, newH);
            g2.rotate(3 * Math.PI / 2);
        }

        // Apply flip
        if (flipH) {
            g2.translate(srcW, 0);
            g2.scale(-1, 1);
        }
        if (flipV) {
            g2.translate(0, srcH);
            g2.scale(1, -1);
        }

        g2.drawImage(current, 0, 0, null);
        g2.dispose();
        return dst;
    }

    /**
     * Fits and frames an image into the machine drawing frame according to ImageAdjustments.
     * Prevents unwanted cropping by offering FIT_TO_FRAME (Contain) alongside Cover and Match Aspect.
     */
    public static BufferedImage fitImageToFrame(BufferedImage raw, MachineConfig config, ImageAdjustments adj) {
        if (raw == null) return null;

        // 1. Apply user orientation (rotation and flipping)
        BufferedImage oriented = applyOrientation(
            raw,
            adj.getRotationAngle(),
            adj.isFlipHorizontal(),
            adj.isFlipVertical(),
            adj.isAutoRotateLandscape()
        );

        int origW = oriented.getWidth();
        int origH = oriented.getHeight();

        // 2. Compute physical target frame dimensions based on machine settings
        int frameW = (int) Math.round(config.getImageWidthMm() / config.getPenWidthMm());
        int frameH = (int) Math.round(config.getImageHeightMm() / config.getPenWidthMm());
        frameW = Math.max(200, frameW);
        frameH = Math.max(200, frameH);

        ImageAdjustments.FitMode mode = adj.getFitMode();

        // In MATCH_ASPECT mode, adapt frame dimensions to image ratio
        if (mode == ImageAdjustments.FitMode.MATCH_ASPECT) {
            frameH = (int) Math.max(200, Math.round((float) frameW * origH / origW));
        }

        BufferedImage framed = new BufferedImage(frameW, frameH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = framed.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background with clean paper white
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, frameW, frameH);

        float userScale = adj.getFrameScale();
        float margin = adj.getFrameMargin();
        float usableW = frameW * Math.max(0.1f, 1.0f - margin * 2.0f);
        float usableH = frameH * Math.max(0.1f, 1.0f - margin * 2.0f);

        int drawX, drawY, drawW, drawH;

        if (mode == ImageAdjustments.FitMode.FIT_TO_FRAME || mode == ImageAdjustments.FitMode.MATCH_ASPECT) {
            // UNIFORM CONTAIN — NEVER CROPS ANY PART OF THE IMAGE
            float scale = Math.min(usableW / origW, usableH / origH) * userScale;
            drawW = (int) Math.round(origW * scale);
            drawH = (int) Math.round(origH * scale);
            drawX = (int) Math.round((frameW - drawW) / 2.0f + adj.getOffsetX() * frameW);
            drawY = (int) Math.round((frameH - drawH) / 2.0f + adj.getOffsetY() * frameH);
        } else if (mode == ImageAdjustments.FitMode.FILL_FRAME) {
            // COVER — Crops edges to fill the entire frame
            float scale = Math.max((float) frameW / origW, (float) frameH / origH) * userScale;
            drawW = (int) Math.round(origW * scale);
            drawH = (int) Math.round(origH * scale);
            drawX = (int) Math.round((frameW - drawW) / 2.0f + adj.getOffsetX() * frameW);
            drawY = (int) Math.round((frameH - drawH) / 2.0f + adj.getOffsetY() * frameH);
        } else {
            // STRETCH — Non-uniform stretch to frame
            drawW = (int) Math.round(usableW * userScale);
            drawH = (int) Math.round(usableH * userScale);
            drawX = (int) Math.round((frameW - drawW) / 2.0f + adj.getOffsetX() * frameW);
            drawY = (int) Math.round((frameH - drawH) / 2.0f + adj.getOffsetY() * frameH);
        }

        g2.drawImage(oriented, drawX, drawY, drawW, drawH, null);
        g2.dispose();

        return framed;
    }
}
