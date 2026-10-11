package com.dullbits.drawbot.pfm;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.BorderFilter;
import com.dullbits.drawbot.image.ConvolutionEngine;
import com.dullbits.drawbot.image.ImageFilters;
import com.dullbits.drawbot.image.ImagePipeline;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * PFM_spiral: Archimedean spiral radiating outward with amplitude modulated by pixel brightness.
 * Faithful port of PFM_spiral.pde.
 */
public final class PfmSpiral implements PathFindingModule {
    private float dist = 7.0f;
    private float density = 75.0f;
    private float ampScale = 4.5f;
    private int maskBrightness = 240;

    @Override
    public String getId() { return "PFM_spiral"; }

    @Override
    public String getName() { return "Modulated Spiral"; }

    @Override
    public String getDescription() {
        return "Continuous outward Archimedean spiral whose line amplitude fluctuates inversely with pixel brightness.";
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
        img = ImageFilters.scaleToWidth(img, 1000);
        img = ConvolutionEngine.unsharpen(img, 3);
        img = BorderFilter.applyBorder(img, "b6.png", 0, 0);
        img = ImageFilters.desaturate(img);

        pipeline.setReferenceImage(img);
        pipeline.resetWorkingToReference();
    }

    @Override
    public void findPath(ImagePipeline pipeline, SegmentBuffer buffer, MachineConfig config, ProgressCallback callback) {
        BufferedImage img = pipeline.getWorkingImage();
        if (img == null) return;

        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = ImageFilters.getPixels(img);

        float radius = dist / 2.0f;
        float k = density / radius;
        float alpha = k;
        radius += dist / (360.0f / k);

        float endRadius = (float) Math.sqrt(Math.pow(w / 2.0, 2) + Math.pow(h / 2.0, 2));

        float x = (float) (radius * Math.cos(Math.toRadians(alpha)) + w / 2.0);
        float y = (float) (-radius * Math.sin(Math.toRadians(alpha)) + h / 2.0);

        buffer.addLine(0, false, 0, 0, x, y);

        float oldX = x;
        float oldY = y;
        int penCount = config.getPenCount();

        int loopCount = 0;

        while (radius < endRadius) {
            if (callback != null && (loopCount++ % 500 == 0)) {
                if (callback.isCancelled()) break;
                float progress = Math.min(1.0f, radius / endRadius);
                callback.onProgress(buffer.getLineCount(), progress,
                    String.format(Locale.US, "Spiral Radius: %.1f / %.1f mm", radius, endRadius));
            }

            k = (density / 2.0f) / radius;
            alpha += k;
            radius += dist / (360.0f / k);

            x = (float) (radius * Math.cos(Math.toRadians(alpha)) + w / 2.0);
            y = (float) (-radius * Math.sin(Math.toRadians(alpha)) + h / 2.0);

            float xa = x, ya = y;
            float xb = x, yb = y;
            boolean penDown = false;
            int penNumber = 0;

            if (x >= 0 && x < w && y > 0 && y < h) {
                int loc = (int) y * w + (int) x;
                float bVal = ImageFilters.brightness(pixels[loc]);

                // Map brightness: 0 -> dist * ampScale, 255 -> 0
                float b = ((255.0f - bVal) / 255.0f) * (dist * ampScale);

                float aradius = radius + (b / dist);
                xa = (float) (aradius * Math.cos(Math.toRadians(alpha)) + w / 2.0);
                ya = (float) (-aradius * Math.sin(Math.toRadians(alpha)) + h / 2.0);

                k = (density / 2.0f) / radius;
                alpha += k;
                radius += dist / (360.0f / k);

                float bradius = radius - (b / dist);
                xb = (float) (bradius * Math.cos(Math.toRadians(alpha)) + w / 2.0);
                yb = (float) (-bradius * Math.sin(Math.toRadians(alpha)) + h / 2.0);

                penDown = (bVal < maskBrightness);
                penNumber = (int) Math.round((bVal / 255.0f) * (penCount - 1));
                penNumber = Math.min(penCount - 1, Math.max(0, penNumber));
            } else {
                penDown = false;
                xa = x; ya = y;
                xb = x; yb = y;
            }

            buffer.addLine(penNumber, penDown, oldX, oldY, xa, ya);
            buffer.addLine(penNumber, penDown, xa, ya, xb, yb);
            oldX = xb;
            oldY = yb;
        }

        if (callback != null) {
            callback.onProgress(buffer.getLineCount(), 1.0f, "Spiral generation complete");
        }
    }

    @Override
    public void postProcessing(SegmentBuffer buffer, PenDistribution distribution) {
        buffer.computeContinuations();
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("dist", dist);
        params.put("density", density);
        params.put("ampScale", ampScale);
        params.put("maskBrightness", maskBrightness);
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("dist".equals(key)) dist = ((Number) value).floatValue();
        else if ("density".equals(key)) density = ((Number) value).floatValue();
        else if ("ampScale".equals(key)) ampScale = ((Number) value).floatValue();
        else if ("maskBrightness".equals(key)) maskBrightness = ((Number) value).intValue();
    }
}
