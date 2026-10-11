package com.dullbits.drawbot.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Specialized utility for patching machine G-code files.
 * Specifically corrects initial rapid Z-height safe clearance moves
 * (e.g., line 9 G0 Z1.5 -> G0 Z30).
 */
public final class GcodeFixer {

    public static final class FixResult {
        public final boolean success;
        public final String message;
        public final int targetLineNumber; // 1-indexed
        public final String originalLine;
        public final String replacedLine;
        public final int totalLines;
        public final List<String> headerPreview; // First N lines after fix

        public FixResult(boolean success, String message, int targetLineNumber,
                         String originalLine, String replacedLine, int totalLines,
                         List<String> headerPreview) {
            this.success = success;
            this.message = message;
            this.targetLineNumber = targetLineNumber;
            this.originalLine = originalLine;
            this.replacedLine = replacedLine;
            this.totalLines = totalLines;
            this.headerPreview = headerPreview != null ? headerPreview : new ArrayList<>();
        }
    }

    private GcodeFixer() {}

    /**
     * Reads a G-code file and patches a specific line (e.g. line 9 to G0 Z30).
     *
     * @param inputFile Source G-code file
     * @param outputFile Target destination (can be same as inputFile for in-place overwrite)
     * @param targetLine 1-indexed line number (e.g. 9)
     * @param replacementText The new instruction (e.g. "G0 Z30")
     * @return FixResult detailing the changes made
     */
    public static FixResult fixLine(File inputFile, File outputFile, int targetLine, String replacementText) throws Exception {
        if (!inputFile.exists() || !inputFile.isFile()) {
            return new FixResult(false, "Input file does not exist: " + inputFile.getPath(), 0, "", "", 0, null);
        }

        List<String> lines = new ArrayList<>();
        String originalLine = "";
        int lineIndex = 0;
        int targetIdx = targetLine - 1; // 0-indexed

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (lineIndex == targetIdx) {
                    originalLine = line;
                    lines.add(replacementText);
                } else {
                    lines.add(line);
                }
                lineIndex++;
            }
        }

        if (targetIdx < 0 || targetIdx >= lines.size()) {
            return new FixResult(false, "File only has " + lines.size() + " lines; line " + targetLine + " does not exist.",
                targetLine, "", "", lines.size(), null);
        }

        // Write output using temp file to safely prevent file corruption
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        File tempOut = File.createTempFile("gcode_fix_", ".tmp", parentDir != null ? parentDir : new File("."));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempOut), StandardCharsets.UTF_8))) {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                writer.newLine();
            }
        }

        // Atomically replace target file
        java.nio.file.Files.move(
            tempOut.toPath(),
            outputFile.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE
        );

        List<String> preview = new ArrayList<>();
        int previewCount = Math.min(20, lines.size());
        for (int i = 0; i < previewCount; i++) {
            preview.add(lines.get(i));
        }

        return new FixResult(
            true,
            "Successfully patched line " + targetLine + " from '" + originalLine + "' to '" + replacementText + "' (" + lines.size() + " total lines).",
            targetLine,
            originalLine,
            replacementText,
            lines.size(),
            preview
        );
    }

    /**
     * Reads the first lines of a G-code file for preview inspection.
     */
    public static List<String> readHeaderLines(File file, int maxLines) {
        List<String> list = new ArrayList<>();
        if (!file.exists() || !file.isFile()) {
            return list;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && list.size() < maxLines) {
                list.add(line);
            }
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Automatic smart fixer for J Tech / Inkscape gcode headers:
     * Scans header, locates line 9 and updates it to G0 Z30.
     */
    public static FixResult autoFix(File inputFile, File outputFile) throws Exception {
        return fixLine(inputFile, outputFile, 9, "G0 Z30");
    }

    public static void main(String[] args) {
        try {
            File in = args.length > 0 ? new File(args[0]) : new File("referance/drawing.gcode");
            File out = args.length > 1 ? new File(args[1]) : in;
            int lineNum = args.length > 2 ? Integer.parseInt(args[2]) : 9;
            String rep = args.length > 3 ? args[3] : "G0 Z30";

            FixResult res = fixLine(in, out, lineNum, rep);
            System.out.println(res.message);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
