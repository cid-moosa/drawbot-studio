package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.export.GcodeExporter;
import com.dullbits.drawbot.export.PdfExporter;
import com.dullbits.drawbot.export.SvgExporter;
import com.dullbits.drawbot.image.ImagePipeline;
import com.dullbits.drawbot.pfm.PathFindingModule;
import com.dullbits.drawbot.pfm.PfmOriginal;
import com.dullbits.drawbot.pfm.PfmSpiral;
import com.dullbits.drawbot.pfm.PfmSquares;
import com.dullbits.drawbot.pfm.ProgressCallback;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Performance Profiling and Benchmark Suite.
 * Measures execution time, throughput (segments/sec), export latency, and heap memory.
 */
public final class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  DRAWBOT STUDIO — PERFORMANCE BENCHMARK SUITE");
        System.out.println("=================================================\n");

        File testImageFile = new File("assets/samples/github1.png");
        if (!testImageFile.exists()) {
            testImageFile = new File("assets/samples/cat1.jpg");
        }

        System.out.println("Test Fixture Image: " + testImageFile.getAbsolutePath());
        System.out.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        System.out.println("Runtime: " + System.getProperty("java.runtime.name") + " " + System.getProperty("java.version"));
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        // 1. Image Loading Benchmark
        long t0 = System.nanoTime();
        BufferedImage rawImg = ImageIO.read(testImageFile);
        long tImgLoad = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf(Locale.US, "1. Image Load Time: %d ms (%dx%d pixels, %d KB)%n",
            tImgLoad, rawImg.getWidth(), rawImg.getHeight(), testImageFile.length() / 1024);

        MachineConfig config = new MachineConfig();
        PenDistribution distribution = new PenDistribution(6);

        // 2. Preprocessing & Pathfinding Benchmarks for each algorithm
        runBenchmarkForPfm("PFM_original (Squiggles)", new PfmOriginal(), rawImg, config, distribution);
        runBenchmarkForPfm("PFM_spiral (Modulated Spiral)", new PfmSpiral(), rawImg, config, distribution);
        runBenchmarkForPfm("PFM_squares (Wavy Squares)", new PfmSquares(), rawImg, config, distribution);

        System.out.println("\n=================================================");
        System.out.println("  ALL BENCHMARKS COMPLETED SUCCESSFULLY");
        System.out.println("=================================================");
    }

    private static void runBenchmarkForPfm(String label, PathFindingModule pfm, BufferedImage src,
                                           MachineConfig config, PenDistribution distribution) throws Exception {
        System.out.println("\n-------------------------------------------------");
        System.out.println("Benchmark: " + label);
        System.out.println("-------------------------------------------------");

        ImagePipeline pipeline = new ImagePipeline();
        pipeline.setOriginalImage(src);
        SegmentBuffer buffer = new SegmentBuffer();

        // A. Preprocessing Time
        long t0 = System.nanoTime();
        pfm.preProcessing(pipeline, config);
        long tPreprocess = (System.nanoTime() - t0) / 1_000_000;
        BufferedImage ref = pipeline.getReferenceImage();
        System.out.printf(Locale.US, "  - Preprocessing Time: %d ms (Output: %dx%d)%n",
            tPreprocess, ref.getWidth(), ref.getHeight());

        // B. Pathfinding Time
        long t1 = System.nanoTime();
        pfm.findPath(pipeline, buffer, config, new ProgressCallback() {
            @Override
            public void onProgress(int lines, float fraction, String msg) {}
            @Override
            public boolean isCancelled() { return buffer.getLineCount() >= 5000; } // Benchmark sample size 5000 lines
        });
        long tPathGen = (System.nanoTime() - t1) / 1_000_000;
        int lines = buffer.getLineCount();
        double segPerSec = (lines / (double) Math.max(1, tPathGen)) * 1000.0;
        System.out.printf(Locale.US, "  - Pathfinding Time:   %d ms (Generated %,d lines, %,.0f seg/sec)%n",
            tPathGen, lines, segPerSec);

        // C. Post-processing Time
        long t2 = System.nanoTime();
        pfm.postProcessing(buffer, distribution);
        long tPostProcess = (System.nanoTime() - t2) / 1_000_000;
        System.out.printf(Locale.US, "  - Post-Processing:    %d ms%n", tPostProcess);

        // D. Export Times
        File outDir = new File("builds/bench_outputs");
        if (!outDir.exists()) outDir.mkdirs();

        long tGcode0 = System.nanoTime();
        GcodeExporter.exportAll(outDir, "bench_" + pfm.getId(), buffer, config, distribution, lines, ref.getWidth(), ref.getHeight());
        long tGcode = (System.nanoTime() - tGcode0) / 1_000_000;

        long tSvg0 = System.nanoTime();
        SvgExporter.exportSvg(outDir, "bench_" + pfm.getId(), buffer, config, distribution, lines, ref.getWidth(), ref.getHeight());
        long tSvg = (System.nanoTime() - tSvg0) / 1_000_000;

        long tPdf0 = System.nanoTime();
        PdfExporter.exportAllPdf(outDir, "bench_" + pfm.getId(), buffer, config, distribution, lines, ref.getWidth(), ref.getHeight());
        long tPdf = (System.nanoTime() - tPdf0) / 1_000_000;

        System.out.printf(Locale.US, "  - G-Code Export:      %d ms (7 files)%n", tGcode);
        System.out.printf(Locale.US, "  - SVG Export:         %d ms (Polyline optimized)%n", tSvg);
        System.out.printf(Locale.US, "  - Vector PDF Export:  %d ms (7 PDF files)%n", tPdf);

        // Memory stats
        long usedHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        System.out.printf(Locale.US, "  - Heap Memory in Use: %d MB%n", usedHeap);
    }
}
