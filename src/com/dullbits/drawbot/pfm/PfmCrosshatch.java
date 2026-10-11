package com.dullbits.drawbot.pfm;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.BorderFilter;
import com.dullbits.drawbot.image.ConvolutionEngine;
import com.dullbits.drawbot.image.ImageFilters;
import com.dullbits.drawbot.image.ImagePipeline;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * PFM_crosshatch: Multi-Angle Cross-Hatching & Engraving.
 * Classical printmaking and plotter shading: layers diagonal (45 deg / 135 deg)
 * and orthogonal lines with density proportional to darkness, mapping passes
 * seamlessly across multi-pen sets.
 */
public final class PfmCrosshatch implements PathFindingModule {
    private int pitch = 7;             // Spacing between hatch lines in pixels
    private int maxLayers = 4;         // 1 to 4 hatch passes (45, 135, 0, 90 deg)
    private float shadowThreshold = 220.0f; // Brightness cutoff above which no ink is laid

    @Override
    public String getId() { return "PFM_crosshatch"; }

    @Override
    public String getName() { return "Multi-Angle Crosshatch (Etching)"; }

    @Override
    public String getDescription() {
        return "Classical engraving and hatching: progressive 45/135 degree diamond grid shading matching traditional copperplate prints.";
    }

    @Override
    public void preProcessing(ImagePipeline pipeline, MachineConfig config) {
        if (pipeline.getReferenceImage() != null) {
            pipeline.resetWorkingToReference();
            return;
        }
        BufferedImage img = pipeline.getOriginalImage();
        if (img == null) return;

        img = ImageFilters.rotateIfLandscape(img);
        float targetRatio = config.getImageWidthMm() / config.getImageHeightMm();
        img = ImageFilters.cropToRatio(img, targetRatio);

        int targetWidth = (int) (config.getImageWidthMm() / config.getPenWidthMm());
        img = ImageFilters.scaleToWidth(img, Math.min(1000, targetWidth));

        img = ConvolutionEngine.unsharpen(img, 3);
        img = BorderFilter.applyBorder(img, "b1.png", 0, 0);
        img = ImageFilters.desaturate(img);

        pipeline.setReferenceImage(img);
        pipeline.resetWorkingToReference();
    }

    @Override
    public void findPath(ImagePipeline pipeline, SegmentBuffer buffer, MachineConfig config, ProgressCallback callback) {
        BufferedImage work = pipeline.getWorkingImage();
        if (work == null) return;

        int w = work.getWidth();
        int h = work.getHeight();
        int[] pixels = ImageFilters.getPixels(work);
        int penCount = config.getPenCount();

        // Pass 1: 45 degrees (d = x + y)
        // Pass 2: 135 degrees (d = x - y)
        // Pass 3: 0 degrees (horizontal)
        // Pass 4: 90 degrees (vertical)
        float[] thresholds = {
            shadowThreshold,
            shadowThreshold * 0.75f,
            shadowThreshold * 0.50f,
            shadowThreshold * 0.25f
        };

        int passes = Math.min(4, Math.max(1, maxLayers));
        float lastX = 0, lastY = 0;

        for (int p = 0; p < passes; p++) {
            if (callback != null && callback.isCancelled()) break;

            float thresh = thresholds[p];
            int penNumber = (int) Math.min(penCount - 1, ((float) p / Math.max(1, passes - 1)) * (penCount - 1));

            if (p == 0) {
                // Pass 1: 45 degrees
                for (int d = 0; d < w + h; d += pitch) {
                    if (callback != null && callback.isCancelled()) break;
                    hatchDiagonal(buffer, pixels, w, h, d, true, thresh, penNumber, lastX, lastY);
                }
            } else if (p == 1) {
                // Pass 2: 135 degrees
                for (int d = -h; d < w; d += pitch) {
                    if (callback != null && callback.isCancelled()) break;
                    hatchDiagonal(buffer, pixels, w, h, d, false, thresh, penNumber, lastX, lastY);
                }
            } else if (p == 2) {
                // Pass 3: Horizontal (0 degrees)
                for (int y = 0; y < h; y += pitch) {
                    if (callback != null && callback.isCancelled()) break;
                    hatchHorizontal(buffer, pixels, w, h, y, thresh, penNumber);
                }
            } else {
                // Pass 4: Vertical (90 degrees)
                for (int x = 0; x < w; x += pitch) {
                    if (callback != null && callback.isCancelled()) break;
                    hatchVertical(buffer, pixels, w, h, x, thresh, penNumber);
                }
            }

            if (callback != null) {
                callback.onProgress(buffer.getLineCount(), (float) (p + 1) / passes,
                    String.format("Crosshatch Pass %d / %d (%d lines)", p + 1, passes, buffer.getLineCount()));
            }
        }
    }

    private void hatchDiagonal(SegmentBuffer buffer, int[] pixels, int w, int h, int d, boolean forward,
                               float thresh, int pen, float lastX, float lastY) {
        int step = 3;
        boolean inStroke = false;
        int segStartX = 0, segStartY = 0;

        int startX = forward ? Math.max(0, d - (h - 1)) : Math.max(0, d);
        int endX = forward ? Math.min(w - 1, d) : Math.min(w - 1, d + (h - 1));

        for (int x = startX; x <= endX; x += step) {
            int y = forward ? (d - x) : (x - d);
            if (y < 0 || y >= h) continue;

            float b = ImageFilters.brightness(pixels[y * w + x]);
            boolean draw = (b < thresh);

            if (draw) {
                if (!inStroke) {
                    segStartX = x;
                    segStartY = y;
                    inStroke = true;
                }
            } else {
                if (inStroke) {
                    buffer.addLine(pen, true, segStartX, segStartY, x, y);
                    inStroke = false;
                }
            }
        }
        if (inStroke) {
            int endY = forward ? (d - endX) : (endX - d);
            buffer.addLine(pen, true, segStartX, segStartY, endX, endY);
        }
    }

    private void hatchHorizontal(SegmentBuffer buffer, int[] pixels, int w, int h, int y, float thresh, int pen) {
        int step = 3;
        boolean inStroke = false;
        int segStartX = 0;

        for (int x = 0; x < w; x += step) {
            float b = ImageFilters.brightness(pixels[y * w + x]);
            boolean draw = (b < thresh);
            if (draw) {
                if (!inStroke) { segStartX = x; inStroke = true; }
            } else {
                if (inStroke) {
                    buffer.addLine(pen, true, segStartX, y, x, y);
                    inStroke = false;
                }
            }
        }
        if (inStroke) buffer.addLine(pen, true, segStartX, y, w - 1, y);
    }

    private void hatchVertical(SegmentBuffer buffer, int[] pixels, int w, int h, int x, float thresh, int pen) {
        int step = 3;
        boolean inStroke = false;
        int segStartY = 0;

        for (int y = 0; y < h; y += step) {
            float b = ImageFilters.brightness(pixels[y * w + x]);
            boolean draw = (b < thresh);
            if (draw) {
                if (!inStroke) { segStartY = y; inStroke = true; }
            } else {
                if (inStroke) {
                    buffer.addLine(pen, true, x, segStartY, x, y);
                    inStroke = false;
                }
            }
        }
        if (inStroke) buffer.addLine(pen, true, x, segStartY, x, h - 1);
    }

    @Override
    public void postProcessing(SegmentBuffer buffer, PenDistribution distribution) {
        buffer.computeContinuations();
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pitch", pitch);
        params.put("maxLayers", maxLayers);
        params.put("shadowThreshold", shadowThreshold);
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("pitch".equals(key)) pitch = ((Number) value).intValue();
        else if ("maxLayers".equals(key)) maxLayers = ((Number) value).intValue();
        else if ("shadowThreshold".equals(key)) shadowThreshold = ((Number) value).floatValue();
    }
}
