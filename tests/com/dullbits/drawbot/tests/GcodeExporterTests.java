package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.export.GcodeExporter;
import java.io.File;
import java.nio.file.Files;

/**
 * Test Group D: G-code tests.
 * Validates generated G-code syntax, coordinate calculations, pen transitions,
 * headers, trailers, and boundary test marks.
 */
public final class GcodeExporterTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running GcodeExporterTests ---");
        testGcodeExport();
        System.out.println("GcodeExporterTests: ALL PASSED\n");
    }

    private static void testGcodeExport() throws Exception {
        File tempDir = new File("builds/test_output_gcode");
        if (!tempDir.exists()) tempDir.mkdirs();

        SegmentBuffer buffer = new SegmentBuffer();
        // Add sample segments for pen 0 and pen 1
        buffer.addLine(0, true, 10, 10, 50, 50);
        buffer.addLine(0, true, 50, 50, 100, 50);
        buffer.addLine(1, true, 100, 50, 120, 80);

        MachineConfig config = new MachineConfig();
        PenDistribution dist = new PenDistribution(6);

        GcodeExporter.ExportResult result = GcodeExporter.exportAll(
            tempDir, "test_sample", buffer, config, dist,
            buffer.getLineCount(), 200, 200
        );

        if (result.generatedFiles.size() != 7) { // 6 pens + 1 test file
            throw new AssertionError("Expected 7 G-code output files, got: " + result.generatedFiles.size());
        }

        // Verify pen 0 file content
        File pen0File = new File(tempDir, "gcode_test_sample_pen0_100.txt");
        if (!pen0File.exists()) {
            throw new AssertionError("Pen 0 G-code file missing: " + pen0File.getName());
        }
        String content = Files.readString(pen0File.toPath());
        if (!content.contains("G21") || !content.contains("G90") || !content.contains("G1 Z1") || !content.contains("G1 Z0")) {
            throw new AssertionError("Pen 0 file missing standard G-code movement commands");
        }
        if (!content.contains("G1 X0 Y0")) {
            throw new AssertionError("Pen 0 file missing trailer home command");
        }

        // Verify test file content
        File testFile = new File(tempDir, "gcode_test_sample_test.txt");
        if (!testFile.exists()) {
            throw new AssertionError("Boundary test G-code file missing");
        }
        String testContent = Files.readString(testFile.toPath());
        if (!testContent.contains("(Upper left)") || !testContent.contains("(Lower right)")) {
            throw new AssertionError("Boundary test marks missing");
        }
    }
}
