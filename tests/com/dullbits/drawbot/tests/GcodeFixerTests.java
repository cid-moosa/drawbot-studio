package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.export.GcodeFixer;
import java.io.File;
import java.util.List;

public final class GcodeFixerTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running GcodeFixerTests ---");
        testPatchReferenceGcode();
        testCustomLineFix();
        System.out.println("GcodeFixerTests: ALL PASSED\n");
    }

    private static void testPatchReferenceGcode() throws Exception {
        File backup = new File("referance/drawing_original_backup.gcode");
        File refFile = backup.exists() ? backup : new File("referance/drawing.gcode");
        if (!refFile.exists()) {
            System.out.println("  [SKIP] referance/drawing.gcode not found");
            return;
        }

        File outFile = new File("builds/test_output_gcode/drawing_patched_test.gcode");
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }

        GcodeFixer.FixResult result = GcodeFixer.fixLine(refFile, outFile, 9, "G0 Z30");
        if (!result.success) {
            throw new AssertionError("GcodeFixer failed: " + result.message);
        }

        if (result.targetLineNumber != 9) {
            throw new AssertionError("Expected target line 9, got " + result.targetLineNumber);
        }
        if (!"G0 Z1.5".equals(result.originalLine.trim()) && !"G0 Z30".equals(result.originalLine.trim())) {
            throw new AssertionError("Expected original line 9 to be 'G0 Z1.5' or 'G0 Z30', got: '" + result.originalLine + "'");
        }
        if (!"G0 Z30".equals(result.replacedLine.trim())) {
            throw new AssertionError("Expected replaced line to be 'G0 Z30', got: '" + result.replacedLine + "'");
        }

        List<String> lines = GcodeFixer.readHeaderLines(outFile, 12);
        if (lines.size() < 10) {
            throw new AssertionError("Expected at least 10 lines in output");
        }

        // Verify line 7, 8, 9, 10
        String l7 = lines.get(6); // line 7 (0-indexed 6)
        String l8 = lines.get(7); // line 8 (0-indexed 7)
        String l9 = lines.get(8); // line 9 (0-indexed 8)
        String l10 = lines.get(9); // line 10 (0-indexed 9)

        if (!"G0 Z1.5".equals(l7.trim())) {
            throw new AssertionError("Line 7 should remain 'G0 Z1.5', got: " + l7);
        }
        if (!"G21;".equals(l8.trim())) {
            throw new AssertionError("Line 8 should remain 'G21;', got: " + l8);
        }
        if (!"G0 Z30".equals(l9.trim())) {
            throw new AssertionError("Line 9 MUST BE 'G0 Z30', got: " + l9);
        }
        if (!l10.contains("G1 F3000.0")) {
            throw new AssertionError("Line 10 should be the first move, got: " + l10);
        }

        System.out.println("  [PASS] testPatchReferenceGcode - verified line 9 G0 Z1.5 -> G0 Z30 across " + result.totalLines + " lines");
    }

    private static void testCustomLineFix() throws Exception {
        File temp = File.createTempFile("test_gcode_", ".gcode");
        temp.deleteOnExit();

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            sb.append("LINE_").append(i).append("\n");
        }
        java.nio.file.Files.writeString(temp.toPath(), sb.toString());

        GcodeFixer.FixResult res = GcodeFixer.fixLine(temp, temp, 5, "CUSTOM_REPLACEMENT");
        if (!res.success) {
            throw new AssertionError("Custom line fix failed");
        }

        List<String> lines = GcodeFixer.readHeaderLines(temp, 10);
        if (!"CUSTOM_REPLACEMENT".equals(lines.get(4))) {
            throw new AssertionError("Expected line 5 to be replaced");
        }
        if (!"LINE_4".equals(lines.get(3)) || !"LINE_6".equals(lines.get(5))) {
            throw new AssertionError("Surrounding lines must remain untouched");
        }

        System.out.println("  [PASS] testCustomLineFix");
    }
}
