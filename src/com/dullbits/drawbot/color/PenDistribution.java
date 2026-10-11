package com.dullbits.drawbot.color;

import java.util.Arrays;

/**
 * Manages pen allocations and distribution percentages across active drawing pens (default 6 pens).
 * Preserves the exact mathematical weighting rules from Drawbot_image_to_gcode_v2.pde:
 *  - Even distribution
 *  - Black/Monochrome distribution
 *  - Progressive lightening and darkening curve
 *  - Individual pen amplification (+10% / -10%)
 *  - Sum normalization preventing division-by-zero
 */
public final class PenDistribution {
    private final int penCount;
    private final float[] distribution;
    private int currentPresetIndex;

    public PenDistribution() {
        this(6);
    }

    public PenDistribution(int penCount) {
        this.penCount = Math.max(1, penCount);
        this.distribution = new float[this.penCount];
        this.currentPresetIndex = 0;
        setEvenDistribution(1000);
    }

    public int getPenCount() {
        return penCount;
    }

    public int getCurrentPresetIndex() {
        return currentPresetIndex;
    }

    public void setCurrentPresetIndex(int index) {
        if (index >= 0 && index < CopicPalette.PRESETS.size()) {
            this.currentPresetIndex = index;
        }
    }

    public CopicPalette.MarkerSet getCurrentPreset() {
        return CopicPalette.PRESETS.get(currentPresetIndex);
    }

    public String getMarkerCodeForPen(int penIndex) {
        CopicPalette.MarkerSet preset = getCurrentPreset();
        if (penIndex >= 0 && penIndex < preset.markerCodes.length) {
            return preset.markerCodes[penIndex];
        }
        return "100";
    }

    public float getPenWeight(int index) {
        if (index >= 0 && index < penCount) {
            return distribution[index];
        }
        return 0.0f;
    }

    public void setPenWeight(int index, float weight) {
        if (index >= 0 && index < penCount) {
            distribution[index] = Math.max(0.0f, weight);
        }
    }

    public float[] getDistributionCopy() {
        return Arrays.copyOf(distribution, penCount);
    }

    public synchronized void setEvenDistribution(int totalLines) {
        float share = (float) totalLines / penCount;
        for (int i = 0; i < penCount; i++) {
            distribution[i] = share;
        }
    }

    public synchronized void setMonochromeDistribution(int totalLines) {
        Arrays.fill(distribution, 0.0f);
        distribution[0] = totalLines;
    }

    public synchronized void lighten() {
        for (int p = 0; p < penCount; p++) {
            distribution[p] *= (1.00f + (p * 0.05f));
        }
    }

    public synchronized void darken() {
        for (int p = 0; p < penCount; p++) {
            float factor = Math.max(0.1f, 1.00f - (p * 0.05f));
            distribution[p] *= factor;
        }
    }

    public synchronized void adjustPen(int index, float multiplier) {
        if (index >= 0 && index < penCount) {
            distribution[index] = Math.max(0.0f, distribution[index] * multiplier);
        }
    }

    public synchronized void normalize(int totalLines) {
        float sum = 0.0f;
        for (int i = 0; i < penCount; i++) {
            sum += distribution[i];
        }

        if (sum <= 0.0001f) {
            setEvenDistribution(totalLines);
            return;
        }

        for (int i = 0; i < penCount; i++) {
            distribution[i] = (totalLines * distribution[i]) / sum;
        }
    }

    public float getPercentage(int index) {
        float sum = 0.0f;
        for (int i = 0; i < penCount; i++) {
            sum += distribution[i];
        }
        if (sum <= 0.0001f || index < 0 || index >= penCount) {
            return 100.0f / penCount;
        }
        return (distribution[index] / sum) * 100.0f;
    }
}
