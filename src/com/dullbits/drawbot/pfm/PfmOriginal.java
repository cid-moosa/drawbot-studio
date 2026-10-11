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
 * PFM_original: Darkness-seeking squiggles algorithm.
 * Faithful port of PFM_original.pde by Scott Cooper.
 */
public final class PfmOriginal implements PathFindingModule {
    private int squiggleLength = 500;
    private int adjustBrightness = 10;
    private float desiredBrightness = 250.0f;
    private int squigglesTillFirstChange = 190;
    private int tests = 13;

    private final Random rng = new Random(3);

    @Override
    public String getId() { return "PFM_original"; }

    @Override
    public String getName() { return "Original Squiggles"; }

    @Override
    public String getDescription() {
        return "Finds darkest regions and generates organic hand-drawn squiggles, progressively lightening drawn paths.";
    }

    @Override
    public void preProcessing(ImagePipeline pipeline, MachineConfig config) {
        if (pipeline.getReferenceImage() != null) {
            pipeline.resetWorkingToReference();
            return;
        }
        BufferedImage img = pipeline.getOriginalImage();
        if (img == null) return;

        // 1. Rotate if landscape
        img = ImageFilters.rotateIfLandscape(img);

        // 2. Aspect crop
        float targetRatio = config.getImageWidthMm() / config.getImageHeightMm();
        img = ImageFilters.cropToRatio(img, targetRatio);

        // 3. Scale width to pen resolution
        int targetWidth = (int) (config.getImageWidthMm() / config.getPenWidthMm());
        img = ImageFilters.scaleToWidth(img, targetWidth);

        // 4. Unsharp masking
        img = ConvolutionEngine.unsharpen(img, 4);
        img = ConvolutionEngine.unsharpen(img, 3);

        // 5. Border vignette blending
        img = BorderFilter.applyBorder(img, "b1.png", 0, 0);
        img = BorderFilter.applyBorder(img, "b11.png", 0, 0);

        // 6. Desaturate
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

            // Find darkest starting area using 10x downsampling
            int areaSize = 10;
            int downW = Math.max(1, w / areaSize);
            int downH = Math.max(1, h / areaSize);
            float darkestAreaVal = 999.0f;
            int darkestAreaLoc = 0;

            for (int dy = 0; dy < downH; dy++) {
                int srcY = dy * areaSize;
                for (int dx = 0; dx < downW; dx++) {
                    int srcX = dx * areaSize;
                    int loc = srcY * w + srcX;
                    float b = ImageFilters.brightness(pixels[loc]);
                    if (b < darkestAreaVal) {
                        darkestAreaVal = b + rng.nextFloat();
                        darkestAreaLoc = dy * downW + dx;
                    }
                }
            }

            int darkestAreaX = darkestAreaLoc % downW;
            int darkestAreaY = darkestAreaLoc / downW;
            int startX = Math.min(w - 1, darkestAreaX * areaSize + rng.nextInt(areaSize));
            int startY = Math.min(h - 1, darkestAreaY * areaSize + rng.nextInt(areaSize));

            squiggleCount++;

            // Traverse to start point with pen UP
            buffer.addLine(0, false, oldX, oldY, startX, startY);
            oldX = startX;
            oldY = startY;

            int currX = startX;
            int currY = startY;

            // Generate squiggle lines
            for (int s = 0; s < squiggleLength; s++) {
                if (callback != null && (s % 50 == 0) && callback.isCancelled()) {
                    break;
                }

                int lineLength = 3 + rng.nextInt(38); // random(3, 40)
                float startAngle;
                float deltaAngle;

                if (squiggleCount < squigglesTillFirstChange) {
                    startAngle = -72.0f + rng.nextFloat() * 20.0f; // random(-72, -52)
                    deltaAngle = 360.0f / (float) tests;
                } else {
                    startAngle = -72.0f + rng.nextFloat() * 20.0f;
                    deltaAngle = 180.0f + 7.0f / (float) tests;
                }

                float darkestNeighborVal = 257.0f;
                int bestEndX = currX;
                int bestEndY = currY;

                for (int d = 0; d < tests; d++) {
                    float deg = (deltaAngle * d) + startAngle;
                    double rad = Math.toRadians(deg);
                    int testX = (int) (Math.cos(rad) * lineLength) + currX;
                    int testY = (int) (Math.sin(rad) * lineLength) + currY;

                    testX = Math.min(w - 1, Math.max(0, testX));
                    testY = Math.min(h - 1, Math.max(0, testY));

                    List<Bresenham.Point> rayPoints = Bresenham.line(currX, currY, testX, testY);
                    int sumBrightness = 0;
                    int countBrightness = 0;

                    for (Bresenham.Point p : rayPoints) {
                        int loc = p.y * w + p.x;
                        sumBrightness += (int) ImageFilters.brightness(pixels[loc]);
                        countBrightness++;
                        float rayAvg = (float) sumBrightness / countBrightness;
                        if (rayAvg < darkestNeighborVal) {
                            bestEndX = p.x;
                            bestEndY = p.y;
                            darkestNeighborVal = rayAvg;
                        }
                    }
                }

                // Lighten pixels along chosen path
                List<Bresenham.Point> drawnPoints = Bresenham.line(currX, currY, bestEndX, bestEndY);
                for (Bresenham.Point p : drawnPoints) {
                    ImageFilters.lightenPixel(pixels, w, p.x, p.y, adjustBrightness * 5);
                }

                // Add line with pen DOWN
                buffer.addLine(0, true, currX, currY, bestEndX, bestEndY);
                currX = bestEndX;
                currY = bestEndY;
                oldX = currX;
                oldY = currY;
            }

            currentAvgBrightness = ImageFilters.averageBrightness(work);
            if (callback != null) {
                float progress = Math.min(1.0f, currentAvgBrightness / desiredBrightness);
                callback.onProgress(buffer.getLineCount(), progress,
                    String.format(Locale.US, "Squiggle %d | Brightness: %.1f / %.1f",
                        squiggleCount, currentAvgBrightness, desiredBrightness));
            }
        }
    }

    @Override
    public void postProcessing(SegmentBuffer buffer, PenDistribution distribution) {
        distribution.normalize(buffer.getLineCount());
        buffer.distributePenChanges(buffer.getLineCount(), distribution.getDistributionCopy(), distribution.getPenCount());
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
        return params;
    }

    @Override
    public void setParameter(String key, Object value) {
        if ("squiggleLength".equals(key)) squiggleLength = ((Number) value).intValue();
        else if ("adjustBrightness".equals(key)) adjustBrightness = ((Number) value).intValue();
        else if ("desiredBrightness".equals(key)) desiredBrightness = ((Number) value).floatValue();
        else if ("squigglesTillFirstChange".equals(key)) squigglesTillFirstChange = ((Number) value).intValue();
        else if ("tests".equals(key)) tests = ((Number) value).intValue();
    }
}
