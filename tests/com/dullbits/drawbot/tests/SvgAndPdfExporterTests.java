package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.export.PdfExporter;
import com.dullbits.drawbot.export.SvgExporter;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Test Group E: SVG and PDF tests.
 * Validates XML structure of generated SVG files and PDF 1.4 vector streams.
 */
public final class SvgAndPdfExporterTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running SvgAndPdfExporterTests ---");
        testSvgExport();
        testPdfExport();
        System.out.println("SvgAndPdfExporterTests: ALL PASSED\n");
    }

    private static void testSvgExport() throws Exception {
        File tempDir = new File("builds/test_output_svg");
        if (!tempDir.exists()) tempDir.mkdirs();

        SegmentBuffer buffer = new SegmentBuffer();
        buffer.addLine(0, true, 10, 10, 20, 20);
        buffer.addLine(0, true, 20, 20, 30, 30); // continuation
        buffer.addLine(1, true, 40, 40, 50, 50);

        MachineConfig config = new MachineConfig();
        PenDistribution dist = new PenDistribution(6);

        File svg = SvgExporter.exportSvg(tempDir, "sample_vector", buffer, config, dist,
                                         buffer.getLineCount(), 200, 200);

        if (!svg.exists() || svg.length() == 0) {
            throw new AssertionError("SVG file was not created or is empty");
        }

        String content = Files.readString(svg.toPath());
        if (!content.contains("<svg") || !content.contains("</svg>") || !content.contains("<polyline")) {
            throw new AssertionError("SVG content missing required XML tags");
        }
    }

    private static void testPdfExport() throws Exception {
        File tempDir = new File("builds/test_output_pdf");
        if (!tempDir.exists()) tempDir.mkdirs();

        SegmentBuffer buffer = new SegmentBuffer();
        buffer.addLine(0, true, 10, 10, 20, 20);
        buffer.addLine(1, true, 30, 30, 40, 40);

        MachineConfig config = new MachineConfig();
        PenDistribution dist = new PenDistribution(6);

        List<File> pdfs = PdfExporter.exportAllPdf(tempDir, "sample_vector", buffer, config, dist,
                                                   buffer.getLineCount(), 200, 200);

        if (pdfs.size() != 7) { // 1 combined + 6 per-pen
            throw new AssertionError("Expected 7 PDF files, got: " + pdfs.size());
        }

        for (File f : pdfs) {
            if (!f.exists() || f.length() == 0) {
                throw new AssertionError("PDF file empty or missing: " + f.getName());
            }
            byte[] bytes = Files.readAllBytes(f.toPath());
            String header = new String(bytes, 0, Math.min(bytes.length, 8));
            if (!header.startsWith("%PDF-1.")) {
                throw new AssertionError("Invalid PDF magic header in " + f.getName());
            }
            String end = new String(bytes, Math.max(0, bytes.length - 30), Math.min(30, bytes.length));
            if (!end.contains("%%EOF")) {
                throw new AssertionError("Missing %%EOF marker in " + f.getName());
            }
        }
    }
}
