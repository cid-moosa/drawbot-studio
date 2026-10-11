package com.dullbits.drawbot.tests;

/**
 * Master automated test runner executing all test suites for Drawbot Studio.
 */
public final class AllTestsRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  DRAWBOT STUDIO — AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================\n");

        long startTime = System.currentTimeMillis();
        int passCount = 0;

        try {
            ImageProcessingTests.runAll();
            passCount++;

            PathGenerationTests.runAll();
            passCount++;

            PenDistributionTests.runAll();
            passCount++;

            GcodeExporterTests.runAll();
            passCount++;

            SvgAndPdfExporterTests.runAll();
            passCount++;

            GcodeFixerTests.runAll();
            passCount++;

            PreferencesTests.runAll();
            passCount++;

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("=================================================");
            System.out.printf("  RESULT: SUCCESS — %d/7 Test Suites Passed in %d ms%n", passCount, elapsed);
            System.out.println("=================================================");
            System.exit(0);
        } catch (Throwable t) {
            System.err.println("\nTEST FAILED WITH EXCEPTION:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
