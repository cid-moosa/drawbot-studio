package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.color.CopicPalette;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.SegmentBuffer;

/**
 * Test Group C: Pen-distribution tests.
 * Validates distribution math, normalization, preset availability,
 * and Copic color matching.
 */
public final class PenDistributionTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running PenDistributionTests ---");
        testEvenDistribution();
        testMonochromeDistribution();
        testLightenAndDarken();
        testPresetsAndCopicLookup();
        System.out.println("PenDistributionTests: ALL PASSED\n");
    }

    private static void testEvenDistribution() {
        PenDistribution dist = new PenDistribution(6);
        dist.setEvenDistribution(6000);
        dist.normalize(6000);

        for (int p = 0; p < 6; p++) {
            float weight = dist.getPenWeight(p);
            if (Math.abs(weight - 1000.0f) > 1.0f) {
                throw new AssertionError("Even distribution pen " + p + " mismatch: " + weight);
            }
            float pct = dist.getPercentage(p);
            if (Math.abs(pct - (100.0f / 6.0f)) > 0.1f) {
                throw new AssertionError("Even percentage mismatch: " + pct);
            }
        }
    }

    private static void testMonochromeDistribution() {
        PenDistribution dist = new PenDistribution(6);
        dist.setMonochromeDistribution(1000);
        dist.normalize(1000);

        if (Math.abs(dist.getPenWeight(0) - 1000.0f) > 0.01f) {
            throw new AssertionError("Monochrome pen 0 should be 1000, got: " + dist.getPenWeight(0));
        }
        for (int p = 1; p < 6; p++) {
            if (dist.getPenWeight(p) != 0.0f) {
                throw new AssertionError("Monochrome pen " + p + " should be 0, got: " + dist.getPenWeight(p));
            }
        }
    }

    private static void testLightenAndDarken() {
        PenDistribution dist = new PenDistribution(6);
        dist.setEvenDistribution(6000);
        dist.lighten();
        // Lighten increases pen 5 more than pen 1
        if (dist.getPenWeight(5) <= dist.getPenWeight(1)) {
            throw new AssertionError("Lighten should amplify higher pen indices more than lower");
        }

        dist.setEvenDistribution(6000);
        dist.darken();
        // Darken decreases pen 5 more than pen 1
        if (dist.getPenWeight(5) >= dist.getPenWeight(1)) {
            throw new AssertionError("Darken should reduce higher pen indices more than lower");
        }
    }

    private static void testPresetsAndCopicLookup() {
        if (CopicPalette.PRESETS.size() != 18) {
            throw new AssertionError("Expected 18 Copic presets, got: " + CopicPalette.PRESETS.size());
        }

        // Test Black marker 100
        CopicPalette.Marker black = CopicPalette.getOriginalMarker("100");
        if (black == null || !black.name.toLowerCase().contains("black")) {
            throw new AssertionError("Marker 100 Black not found");
        }

        // Test Closest color lookup for pure black (0x000000)
        String closest = CopicPalette.getClosestOriginal(0x000000);
        if (closest == null || closest.isEmpty()) {
            throw new AssertionError("Closest color lookup returned empty");
        }
    }
}
