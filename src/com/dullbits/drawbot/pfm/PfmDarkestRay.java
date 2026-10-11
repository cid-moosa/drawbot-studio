package com.dullbits.drawbot.pfm;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.Bresenham;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.BorderFilter;
import com.dullbits.drawbot.image.ConvolutionEngine;
import com.dullbits.drawbot.image.ImageFilters;
import com.dullbits.drawbot.image.ImagePipeline;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * PFM_darkest_ray: Global single-darkest pixel rayfinding algorithm.
 * Faithful implementation of the original global find_darkest() routine from PFM_original.pde.
 * Unlike area-downsampling, this focuses with pinpoint accuracy on focal points (eyes, pupils, contours).
 */
public final class PfmDarkestRay implements PathFindingModule {
    private int squiggleLength = 400;
    private int adjustBrightness = 12;
    private float desiredBrightness = 248.0f;
    private int squigglesTillFirstChange = 150;
    private int tests = 16;
    private int lineLength = 15;

    private final Random rng = new Random(3);

    @Override
    public String getId() { return "PFM_darkest_ray"; }

    @Override
    public String getName() { return "Darkest-Seeking Rays (Global)"; }

    @Override
    public String getDescription() {
        return "Global single-pixel darkest search: concentrates with pinpoint accuracy on high-contrast focal points and deep shadows.";
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
        img = ImageFilters.scaleToWidth(img, targetWidth);

        img = ConvolutionEngine.unsharpen(img, 4);
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

        int squiggleCount = 0;
        float currentAvgBrightness = ImageFilters.averageBrightness(work);

        int oldX = 0;
        int oldY = 0;

        while (currentAvgBrightness <= desiredBrightness) {
            if (callback != null && callback.isCancelled()) {
                break;
            }

            // Global single-darkest pixel search from PFM_original.pde lines 81-95
            float darkestVal = 257.0f;
            int darkestLoc = 0;
            for (int loc = 0; loc < pixels.length; loc++) {
                float r = ImageFilters.brightness(pixels[loc]);
                if (r < darkestVal) {
                    darkestVal = r + (rng.nextFloat() * 0.9f);
                    darkestLoc = loc;
                }
            }

            int curX = darkestLoc % w;
            int curY = darkestLoc / w;
            squiggleCount++;

            // Select starting pen based on squiggle progression
            int penNumber = 0;
            if (squiggleCount > squigglesTillFirstChange) {
                penNumber = Math.min(config.getPenCount() - 1,
                    (int) (((float) (squiggleCount - squigglesTillFirstChange) / 800.0f) * config.getPenCount()));
            }

            int[] neighbor = findDarkestNeighbor(pixels, w, h, curX, curY);
            buffer.addLine(penNumber, false, oldX, oldY, neighbor[0], neighbor[1]);
            curX = neighbor[0];
            curY = neighbor[1];

            for (int s = 0; s < squiggleLength; s++) {
                neighbor = findDarkestNeighbor(pixels, w, h, curX, curY);
                int nextX = neighbor[0];
                int nextY = neighbor[1];

                List<Bresenham.Point> drawnPoints = Bresenham.line(curX, curY, nextX, nextY);
                for (Bresenham.Point p : drawnPoints) {
                    ImageFilters.lightenPixel(pixels, w, p.x, p.y, adjustBrightness * 5);
                }
                buffer.addLine(penNumber, true, curX, curY, nextX, nextY);

                curX = nextX;
                curY = nextY;
            }

            oldX = curX;
            oldY = curY;

            if (squiggleCount % 5 == 0) {
                currentAvgBrightness = ImageFilters.averageBrightness(work);
                if (callback != null) {
                    float progress = Math.min(1.0f, currentAvgBrightness / desiredBrightness);
                    callback.onProgress(buffer.getLineCount(), progress,
                        String.format("Squiggle #%d | Brightness: %.1f / %.1f", squiggleCount, currentAvgBrightness, desiredBrightness));
                }
            }
        }
    }

    private int[] findDarkestNeighbor(int[] pixels, int w, int h, int startX, int startY) {
        float darkestVal = 257.0f;
        int bestX = startX;
        int bestY = startY;

        float deltaAngle = (float) (2.0 * Math.PI / tests);
        float startAngle = (float) (rng.nextFloat() * 2.0 * Math.PI);

        for (int d = 0; d < tests; d++) {
            float angle = startAngle + (d * deltaAngle);
            int targetX = Math.max(0, Math.min(w - 1, startX + (int) Math.round(Math.cos(angle) * lineLength)));
            int targetY = Math.max(0, Math.min(h - 1, startY + (int) Math.round(Math.sin(angle) * lineLength)));

            List<Bresenham.Point> pts = Bresenham.line(startX, startY, targetX, targetY);
            if (pts.isEmpty()) continue;

            int sum = 0;
            int count = 0;
            for (Bresenham.Point p : pts) {
                int loc = p.y * w + p.x;
                if (loc >= 0 && loc < pixels.length) {
                    sum += (int) ImageFilters.brightness(pixels[loc]);
                    count++;
                }
            }

            if (count > 0) {
                float avg = (float) sum / count;
                if (avg < darkestVal) {
                    darkestVal = avg;
                    bestX = targetX;
                    bestY = targetY;
                }
            }
        }
        return new int[]{bestX, bestY};
    }

    @Override
    public void postProcessing(SegmentBuffer buffer, PenDistribution distribution) {
        buffer.computeContinuations();
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("squiggleLength", squiggleLength);
        params.put("adjustBrightness", adjustBrightness);
        params.put("desiredBrightness", desiredBrightness);
        params.put("squigglesTillFirstChange", squigglesTillFirstChange);
        params.put("tests", tests);
        params.put("lineLength", lineLength);
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("squiggleLength".equals(key)) squiggleLength = ((Number) value).intValue();
        else if ("adjustBrightness".equals(key)) adjustBrightness = ((Number) value).intValue();
        else if ("desiredBrightness".equals(key)) desiredBrightness = ((Number) value).floatValue();
        else if ("squigglesTillFirstChange".equals(key)) squigglesTillFirstChange = ((Number) value).intValue();
        else if ("tests".equals(key)) tests = ((Number) value).intValue();
        else if ("lineLength".equals(key)) lineLength = ((Number) value).intValue();
    }
}
