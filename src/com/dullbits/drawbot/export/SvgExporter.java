package com.dullbits.drawbot.export;

import com.dullbits.drawbot.color.CopicPalette;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.DrawingSegment;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Inkscape-compatible SVG 1.1 vector exporter.
 * Faithful implementation of Gcode.pde create_svg_file:
 *  - Orders pens backwards from lightest to darkest so dark strokes render on top
 *  - Groups strokes by Copic marker ID in `<g id="...">` tags
 *  - Combines contiguous connected segments into compact `<polyline points="...">` structures
 */
public final class SvgExporter {

    private SvgExporter() {}

    public static File exportSvg(File outputDir, String baseName, SegmentBuffer buffer,
                                 MachineConfig config, PenDistribution distribution,
                                 int displayLineCount, int imgWidth, int imgHeight) throws Exception {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File svgFile = new File(outputDir, "gcode_" + baseName + ".svg");
        exportSvgDirect(svgFile, buffer, config, distribution, displayLineCount, imgWidth, imgHeight);
        return svgFile;
    }

    public static File exportSvgDirect(File svgFile, SegmentBuffer buffer,
                                       MachineConfig config, PenDistribution distribution,
                                       int displayLineCount, int imgWidth, int imgHeight) throws Exception {
        if (svgFile.getParentFile() != null && !svgFile.getParentFile().exists()) {
            svgFile.getParentFile().mkdirs();
        }
        float gcodeScale = config.calculateGcodeScale(imgWidth, imgHeight);
        float svgDpi = 96.0f / 25.4f; // 96 DPI standard

        int lineCount = Math.min(displayLineCount, buffer.getLineCount());
        int penCount = config.getPenCount();

        buffer.computeContinuations();

        try (PrintWriter out = new PrintWriter(svgFile, StandardCharsets.UTF_8.name())) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            String widthMm = config.formatSvgNumber(imgWidth * gcodeScale);
            String heightMm = config.formatSvgNumber(imgHeight * gcodeScale);
            out.println("<svg width=\"" + widthMm + "mm\" height=\"" + heightMm + "mm\" " +
                        "xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");

            // Loop pens backwards so darkest lines are on top
            for (int p = penCount - 1; p >= 0; p--) {
                String markerCode = distribution.getMarkerCodeForPen(p);
                out.println("  <g id=\"" + markerCode + "\">");

                boolean drawingPolyline = false;

                for (int i = 0; i < lineCount; i++) {
                    DrawingSegment seg = buffer.get(i);
                    if (seg.penNumber == p) {
                        float scaledX1 = seg.x1 * gcodeScale * svgDpi;
                        float scaledY1 = seg.y1 * gcodeScale * svgDpi;
                        float scaledX2 = seg.x2 * gcodeScale * svgDpi;
                        float scaledY2 = seg.y2 * gcodeScale * svgDpi;

                        if (!seg.penContinuation && drawingPolyline) {
                            out.println("\" />");
                            drawingPolyline = false;
                        }

                        if (seg.penDown) {
                            if (seg.penContinuation && drawingPolyline) {
                                out.print(" " + config.formatSvgNumber(scaledX2) + "," + config.formatSvgNumber(scaledY2));
                            } else {
                                int rgb = CopicPalette.getOriginalRgb(markerCode);
                                String hexColor = String.format("%06X", (rgb & 0xFFFFFF));
                                out.print("    <polyline fill=\"none\" stroke=\"#" + hexColor + "\" " +
                                          "stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                                out.print(config.formatSvgNumber(scaledX1) + "," + config.formatSvgNumber(scaledY1) + " " +
                                          config.formatSvgNumber(scaledX2) + "," + config.formatSvgNumber(scaledY2));
                                drawingPolyline = true;
                            }
                        }
                    }
                }

                if (drawingPolyline) {
                    out.println("\" />");
                    drawingPolyline = false;
                }

                out.println("  </g>");
            }

            out.println("</svg>");
        }
        return svgFile;
    }
}
