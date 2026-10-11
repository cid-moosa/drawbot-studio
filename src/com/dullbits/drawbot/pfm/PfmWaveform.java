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
 * PFM_waveform: Modulated Horizontal Waveforms & Scanlines.
 * Iconic plotter relief landscape design where parallel horizontal raster
 * scanlines oscillate vertically in proportion to local image darkness.
 */
public final class PfmWaveform implements PathFindingModule {
    private int lineSpacing = 10;       // Vertical spacing between scanlines (px)
    private float amplitude = 18.0f;    // Maximum vertical wave displacement (px)
    private int stepX = 3;              // Horizontal sampling step (px)
    private float cutoffBrightness = 248.0f; // Brightness threshold to suppress flat paper areas

    @Override
    public String getId() { return "PFM_waveform"; }

    @Override
    public String getName() { return "Horizontal Waveforms (Scanlines)"; }

    @Override
    public String getDescription() {
        return "Iconic raster wave scanlines: horizontal parallel paths undulating vertically into 3D relief based on darkness.";
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
        img = ImageFilters.scaleToWidth(img, Math.min(1200, targetWidth));

        img = ConvolutionEngine.unsharpen(img, 3);
        img = BorderFilter.applyBorder(img, "b6.png", 0, 0);
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

        int totalRows = (h - 20) / Math.max(1, lineSpacing);
        int rowIndex = 0;

        float lastY = 10;
        float lastX = 0;

        for (int rowY = 10; rowY < h - 10; rowY += lineSpacing) {
            if (callback != null && callback.isCancelled()) {
                break;
            }
            rowIndex++;

            boolean penDown = false;
            float prevPlotX = 0;
            float prevPlotY = rowY;

            // Travel back and forth (serpentine / boustrophedon) to minimize rapid pen air travels
            boolean ltr = (rowIndex % 2 == 1);
            int startX = ltr ? 0 : w - 1;
            int endX = ltr ? w : -1;
            int dir = ltr ? stepX : -stepX;

            for (int x = startX; (ltr ? x < endX : x > endX); x += dir) {
                int sampleX = Math.max(0, Math.min(w - 1, x));
                int sampleY = Math.max(0, Math.min(h - 1, rowY));
                int loc = sampleY * w + sampleX;

                float b = ImageFilters.brightness(pixels[loc]);
                float darkness = Math.max(0.0f, (255.0f - b) / 255.0f);

                // Vertical deflection upwards in dark areas
                float deflection = -amplitude * (darkness * darkness); // Quadratic emphasis on deep shadows
                float plotX = sampleX;
                float plotY = sampleY + deflection;

                int penNumber = (int) Math.min(penCount - 1, Math.max(0, (b / 255.0f) * penCount));

                if (!penDown) {
                    // Travel move to start of line
                    buffer.addLine(penNumber, false, lastX, lastY, plotX, plotY);
                    penDown = true;
                } else {
                    buffer.addLine(penNumber, true, prevPlotX, prevPlotY, plotX, plotY);
                }

                prevPlotX = plotX;
                prevPlotY = plotY;
                lastX = plotX;
                lastY = plotY;
            }

            if (callback != null && rowIndex % 4 == 0) {
                float prog = (float) rowIndex / Math.max(1, totalRows);
                callback.onProgress(buffer.getLineCount(), prog,
                    String.format("Scanline row %d / %d (%d lines)", rowIndex, totalRows, buffer.getLineCount()));
            }
        }
    }

    @Override
    public void postProcessing(SegmentBuffer buffer, PenDistribution distribution) {
        buffer.computeContinuations();
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lineSpacing", lineSpacing);
        params.put("amplitude", amplitude);
        params.put("stepX", stepX);
        params.put("cutoffBrightness", cutoffBrightness);
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("lineSpacing".equals(key)) lineSpacing = ((Number) value).intValue();
        else if ("amplitude".equals(key)) amplitude = ((Number) value).floatValue();
        else if ("stepX".equals(key)) stepX = ((Number) value).intValue();
        else if ("cutoffBrightness".equals(key)) cutoffBrightness = ((Number) value).floatValue();
    }
}
