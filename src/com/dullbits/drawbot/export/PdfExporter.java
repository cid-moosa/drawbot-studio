package com.dullbits.drawbot.export;

import com.dullbits.drawbot.color.CopicPalette;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.DrawingSegment;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * High-performance, pure Java vector PDF 1.4 exporter.
 * Faithful implementation of BotDrawing.pde:
 *  - render_to_pdf: Combined multi-pen vector PDF
 *  - render_each_pen_to_pdf: Individual vector PDF per active pen
 *  - Renders lines in reverse order matching the original processing sketch
 *  - Standard Adobe Acrobat / PDF.js compatible vector commands
 */
public final class PdfExporter {

    private PdfExporter() {}

    public static List<File> exportAllPdf(File outputDir, String baseName, SegmentBuffer buffer,
                                          MachineConfig config, PenDistribution distribution,
                                          int displayLineCount, int imgWidth, int imgHeight) throws Exception {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> generated = new ArrayList<>();
        int lineCount = Math.min(displayLineCount, buffer.getLineCount());
        int penCount = config.getPenCount();

        // 1. Export combined PDF
        File combinedFile = new File(outputDir, "gcode_" + baseName + ".pdf");
        exportPdfFile(combinedFile, buffer, distribution, lineCount, -1, imgWidth, imgHeight);
        generated.add(combinedFile);

        // 2. Export per-pen PDF files
        for (int p = 0; p < penCount; p++) {
            String markerCode = distribution.getMarkerCodeForPen(p);
            File penFile = new File(outputDir, "gcode_" + baseName + "_pen" + p + "_" + markerCode + ".pdf");
            exportPdfFile(penFile, buffer, distribution, lineCount, p, imgWidth, imgHeight);
            generated.add(penFile);
        }

        return generated;
    }

    private static void exportPdfFile(File targetFile, SegmentBuffer buffer, PenDistribution distribution,
                                      int lineCount, int targetPen, int widthPt, int heightPt) throws Exception {
        ByteArrayOutputStream streamContent = new ByteArrayOutputStream();

        // PDF coordinate system has (0,0) at bottom-left. Flip Y so (0,0) is top-left matching image space.
        String initTransform = String.format(Locale.US, "1 0 0 -1 0 %d cm\n0.65 w\n1 J\n1 j\n", heightPt);
        streamContent.write(initTransform.getBytes(StandardCharsets.US_ASCII));

        int lastRgb = -1;

        // Render lines in reverse order matching BotDrawing.pde (for (int i=line_count; i>0; i--))
        for (int i = lineCount - 1; i >= 0; i--) {
            DrawingSegment seg = buffer.get(i);
            if (!seg.penDown) continue;
            if (targetPen >= 0 && seg.penNumber != targetPen) continue;

            String markerCode = distribution.getMarkerCodeForPen(seg.penNumber);
            int rgb = CopicPalette.getOriginalRgb(markerCode);

            if (rgb != lastRgb) {
                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;
                String colorCmd = String.format(Locale.US, "%.3f %.3f %.3f RG\n", r, g, b);
                streamContent.write(colorCmd.getBytes(StandardCharsets.US_ASCII));
                lastRgb = rgb;
            }

            String lineCmd = String.format(Locale.US, "%.2f %.2f m %.2f %.2f l S\n",
                                           seg.x1, seg.y1, seg.x2, seg.y2);
            streamContent.write(lineCmd.getBytes(StandardCharsets.US_ASCII));
        }

        byte[] contentBytes = streamContent.toByteArray();

        // Write complete PDF structure
        try (OutputStream out = new FileOutputStream(targetFile)) {
            List<Long> offsets = new ArrayList<>();
            long currentOffset = 0;

            byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII);
            out.write(header);
            currentOffset += header.length;

            // Obj 1: Catalog
            offsets.add(currentOffset);
            byte[] obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII);
            out.write(obj1);
            currentOffset += obj1.length;

            // Obj 2: Pages
            offsets.add(currentOffset);
            byte[] obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII);
            out.write(obj2);
            currentOffset += obj2.length;

            // Obj 3: Page
            offsets.add(currentOffset);
            String pageStr = String.format(Locale.US,
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %d %d] /Contents 4 0 R >>\nendobj\n",
                widthPt, heightPt);
            byte[] obj3 = pageStr.getBytes(StandardCharsets.US_ASCII);
            out.write(obj3);
            currentOffset += obj3.length;

            // Obj 4: Contents
            offsets.add(currentOffset);
            String streamHeader = String.format(Locale.US,
                "4 0 obj\n<< /Length %d >>\nstream\n", contentBytes.length);
            byte[] shBytes = streamHeader.getBytes(StandardCharsets.US_ASCII);
            out.write(shBytes);
            out.write(contentBytes);
            byte[] streamFooter = "\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII);
            out.write(streamFooter);
            currentOffset += shBytes.length + contentBytes.length + streamFooter.length;

            // XREF table
            long xrefOffset = currentOffset;
            out.write("xref\n0 5\n0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (long off : offsets) {
                out.write(String.format(Locale.US, "%010d 00000 n \n", off).getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer
            String trailer = String.format(Locale.US,
                "trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", xrefOffset);
            out.write(trailer.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
