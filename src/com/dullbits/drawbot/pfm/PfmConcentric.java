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
 * PFM_concentric: Modulated Concentric Circles & Ripple Waves.
 * Generates expanding concentric circular rings radiating outward from the center,
 * with radial frequency and amplitude modulated by local image luminance.
 */
public final class PfmConcentric implements PathFindingModule {
    private float ringPitch = 6.5f;     // Distance between concentric rings (px)
    private float ampScale = 4.0f;      // Amplitude modulation of radial ripple
    private float density = 60.0f;      // Angular sample density (points per quadrant)
    private float maskBrightness = 242.0f; // Cutoff brightness for white background

    @Override
    public String getId() { return "PFM_concentric"; }

    @Override
    public String getName() { return "Concentric Rings (Ripples)"; }

    @Override
    public String getDescription() {
        return "Concentric ripples: circular waves expanding outward from the center, undulating radially with image darkness.";
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

        float centerX = w / 2.0f;
        float centerY = h / 2.0f;
        float maxRadius = (float) Math.sqrt((centerX * centerX) + (centerY * centerY));

        float lastX = centerX;
        float lastY = centerY;

        int ringCount = (int) (maxRadius / Math.max(1.0f, ringPitch));
        int currentRing = 0;

        for (float r = ringPitch; r < maxRadius; r += ringPitch) {
            if (callback != null && callback.isCancelled()) break;
            currentRing++;

            // Circumference determines number of angular steps
            int steps = Math.max(36, (int) ((2.0 * Math.PI * r / 100.0) * density));
            float angleStep = (float) (2.0 * Math.PI / steps);

            boolean inStroke = false;
            float prevX = 0, prevY = 0;
            int prevPen = 0;

            for (int s = 0; s <= steps; s++) {
                float angle = s * angleStep;
                float cosA = (float) Math.cos(angle);
                float sinA = (float) Math.sin(angle);

                int sampleX = (int) (centerX + r * cosA);
                int sampleY = (int) (centerY + r * sinA);

                if (sampleX >= 0 && sampleX < w && sampleY >= 0 && sampleY < h) {
                    float b = ImageFilters.brightness(pixels[sampleY * w + sampleX]);
                    float darkness = Math.max(0.0f, (255.0f - b) / 255.0f);

                    if (b <= maskBrightness) {
                        float modulatedR = r + (ampScale * darkness * ringPitch * 0.35f);
                        float px = centerX + modulatedR * cosA;
                        float py = centerY + modulatedR * sinA;

                        int penNumber = (int) Math.min(penCount - 1, Math.max(0, (b / 255.0f) * penCount));

                        if (!inStroke) {
                            buffer.addLine(penNumber, false, lastX, lastY, px, py);
                            inStroke = true;
                        } else {
                            buffer.addLine(penNumber, true, prevX, prevY, px, py);
                        }

                        prevX = px;
                        prevY = py;
                        prevPen = penNumber;
                        lastX = px;
                        lastY = py;
                    } else {
                        inStroke = false;
                    }
                } else {
                    inStroke = false;
                }
            }

            if (callback != null && currentRing % 4 == 0) {
                float prog = (float) currentRing / Math.max(1, ringCount);
                callback.onProgress(buffer.getLineCount(), prog,
                    String.format("Ring %d / %d (%d lines)", currentRing, ringCount, buffer.getLineCount()));
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
        params.put("ringPitch", ringPitch);
        params.put("ampScale", ampScale);
        params.put("density", density);
        params.put("maskBrightness", maskBrightness);
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("ringPitch".equals(key)) ringPitch = ((Number) value).floatValue();
        else if ("ampScale".equals(key)) ampScale = ((Number) value).floatValue();
        else if ("density".equals(key)) density = ((Number) value).floatValue();
        else if ("maskBrightness".equals(key)) maskBrightness = ((Number) value).floatValue();
    }
}
