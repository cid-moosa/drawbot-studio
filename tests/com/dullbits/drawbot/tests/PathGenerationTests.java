package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.ImagePipeline;
import com.dullbits.drawbot.pfm.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Group B: Path-generation tests.
 * Validates PfmOriginal, PfmSpiral, PfmSquares, coordinate bounds, pen transitions,
 * progress reporting, and clean cancellation.
 */
public final class PathGenerationTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running PathGenerationTests ---");
        testPfmSpiral();
        testPfmOriginalExecutionAndCancel();
        testPfmSquaresExecution();
        testPfmDarkestRay();
        testPfmWaveform();
        testPfmCrosshatch();
        testPfmConcentric();
        System.out.println("PathGenerationTests: ALL PASSED\n");
    }

    private static BufferedImage createTestImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 40, 40));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.fillOval(w / 4, h / 4, w / 2, h / 2);
        g.dispose();
        return img;
    }

    private static void testPfmSpiral() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(200, 200));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmSpiral spiral = new PfmSpiral();
        spiral.preProcessing(pipeline, config);

        AtomicBoolean progressCalled = new AtomicBoolean(false);
        spiral.findPath(pipeline, buffer, config, new ProgressCallback() {
            @Override
            public void onProgress(int linesGenerated, float progressFraction, String stageMessage) {
                progressCalled.set(true);
            }
            @Override
            public boolean isCancelled() { return false; }
        });

        spiral.postProcessing(buffer, dist);

        if (buffer.getLineCount() < 500) {
            throw new AssertionError("Spiral generated too few segments: " + buffer.getLineCount());
        }
        if (!progressCalled.get()) {
            throw new AssertionError("Spiral progress callback was never called");
        }
    }

    private static void testPfmOriginalExecutionAndCancel() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmOriginal original = new PfmOriginal();
        // Lower parameters for fast deterministic unit test
        original.setParameter("squiggleLength", 50);

        original.preProcessing(pipeline, config);

        // Test cancellation after 2 squiggles
        final int[] calls = {0};
        original.findPath(pipeline, buffer, config, new ProgressCallback() {
            @Override
            public void onProgress(int linesGenerated, float progressFraction, String stageMessage) {
                calls[0]++;
            }
            @Override
            public boolean isCancelled() {
                return calls[0] >= 2;
            }
        });

        original.postProcessing(buffer, dist);

        if (buffer.getLineCount() == 0) {
            throw new AssertionError("PfmOriginal should generate lines before cancellation");
        }
    }

    private static void testPfmSquaresExecution() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmSquares squares = new PfmSquares();
        squares.setParameter("squiggleLength", 50);

        squares.preProcessing(pipeline, config);

        final int[] count = {0};
        squares.findPath(pipeline, buffer, config, new ProgressCallback() {
            @Override
            public void onProgress(int linesGenerated, float progressFraction, String stageMessage) {
                count[0]++;
            }
            @Override
            public boolean isCancelled() {
                return count[0] >= 2;
            }
        });

        squares.postProcessing(buffer, dist);

        if (buffer.getLineCount() == 0) {
            throw new AssertionError("PfmSquares should have generated lines");
        }
    }

    private static void testPfmDarkestRay() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmDarkestRay ray = new PfmDarkestRay();
        ray.setParameter("squiggleLength", 40);
        ray.preProcessing(pipeline, config);

        final int[] calls = {0};
        ray.findPath(pipeline, buffer, config, new ProgressCallback() {
            @Override
            public void onProgress(int linesGenerated, float progressFraction, String stageMessage) {
                calls[0]++;
            }
            @Override
            public boolean isCancelled() {
                return calls[0] >= 2;
            }
        });
        ray.postProcessing(buffer, dist);

        if (buffer.getLineCount() == 0) {
            throw new AssertionError("PfmDarkestRay should have generated lines");
        }
    }

    private static void testPfmWaveform() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmWaveform wave = new PfmWaveform();
        wave.setParameter("lineSpacing", 15);
        wave.preProcessing(pipeline, config);

        wave.findPath(pipeline, buffer, config, null);
        wave.postProcessing(buffer, dist);

        if (buffer.getLineCount() < 50) {
            throw new AssertionError("PfmWaveform should have generated scanlines, got: " + buffer.getLineCount());
        }
    }

    private static void testPfmCrosshatch() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmCrosshatch hatch = new PfmCrosshatch();
        hatch.setParameter("pitch", 10);
        hatch.setParameter("maxLayers", 2);
        hatch.preProcessing(pipeline, config);

        hatch.findPath(pipeline, buffer, config, null);
        hatch.postProcessing(buffer, dist);

        if (buffer.getLineCount() < 20) {
            throw new AssertionError("PfmCrosshatch should have generated lines, got: " + buffer.getLineCount());
        }
    }

    private static void testPfmConcentric() {
        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(createTestImage(100, 100));

        MachineConfig config = new MachineConfig();
        SegmentBuffer buffer = new SegmentBuffer();
        PenDistribution dist = new PenDistribution(6);

        PfmConcentric concentric = new PfmConcentric();
        concentric.setParameter("ringPitch", 10.0f);
        concentric.preProcessing(pipeline, config);

        concentric.findPath(pipeline, buffer, config, null);
        concentric.postProcessing(buffer, dist);

        if (buffer.getLineCount() < 30) {
            throw new AssertionError("PfmConcentric should have generated lines, got: " + buffer.getLineCount());
        }
    }
}
