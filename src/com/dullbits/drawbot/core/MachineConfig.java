package com.dullbits.drawbot.core;

import java.util.Locale;

/**
 * Machine dimensions, drawing constraints, and coordinate conversion parameters.
 * Preserves the exact physical defaults from Drawbot_image_to_gcode_v2.pde:
 *  - Paper size: 32 x 40 inches (812.8 x 1016.0 mm)
 *  - Image size: 28 x 36 inches (711.2 x 914.4 mm)
 *  - Top to origin: 285 mm
 *  - Pen width: 0.65 mm
 *  - Pen count: 6
 */
public final class MachineConfig {
    public static final float DEFAULT_PAPER_WIDTH_MM = 32.0f * 25.4f;   // 812.8 mm
    public static final float DEFAULT_PAPER_HEIGHT_MM = 40.0f * 25.4f;  // 1016.0 mm
    public static final float DEFAULT_IMAGE_WIDTH_MM = 28.0f * 25.4f;   // 711.2 mm
    public static final float DEFAULT_IMAGE_HEIGHT_MM = 36.0f * 25.4f;  // 914.4 mm
    public static final float DEFAULT_TOP_TO_ORIGIN_MM = 285.0f;
    public static final float DEFAULT_PEN_WIDTH_MM = 0.65f;
    public static final int DEFAULT_PEN_COUNT = 6;

    private float paperWidthMm = DEFAULT_PAPER_WIDTH_MM;
    private float paperHeightMm = DEFAULT_PAPER_HEIGHT_MM;
    private float imageWidthMm = DEFAULT_IMAGE_WIDTH_MM;
    private float imageHeightMm = DEFAULT_IMAGE_HEIGHT_MM;
    private float paperTopToOriginMm = DEFAULT_TOP_TO_ORIGIN_MM;
    private float penWidthMm = DEFAULT_PEN_WIDTH_MM;
    private int penCount = DEFAULT_PEN_COUNT;
    private int gcodeDecimals = 2;
    private char gcodeDecimalSeparator = '.';
    private int svgDecimals = 2;
    private float gridScale = 25.4f; // 25.4 for inches, 10 for cm

    public MachineConfig() {}

    public float getPaperWidthMm() { return paperWidthMm; }
    public void setPaperWidthMm(float val) { this.paperWidthMm = val; }

    public float getPaperHeightMm() { return paperHeightMm; }
    public void setPaperHeightMm(float val) { this.paperHeightMm = val; }

    public float getImageWidthMm() { return imageWidthMm; }
    public void setImageWidthMm(float val) { this.imageWidthMm = val; }

    public float getImageHeightMm() { return imageHeightMm; }
    public void setImageHeightMm(float val) { this.imageHeightMm = val; }

    public float getPaperTopToOriginMm() { return paperTopToOriginMm; }
    public void setPaperTopToOriginMm(float val) { this.paperTopToOriginMm = val; }

    public float getPenWidthMm() { return penWidthMm; }
    public void setPenWidthMm(float val) { this.penWidthMm = val; }

    public int getPenCount() { return penCount; }
    public void setPenCount(int val) { this.penCount = Math.max(1, val); }

    public int getGcodeDecimals() { return gcodeDecimals; }
    public void setGcodeDecimals(int val) { this.gcodeDecimals = val; }

    public char getGcodeDecimalSeparator() { return gcodeDecimalSeparator; }
    public void setGcodeDecimalSeparator(char val) { this.gcodeDecimalSeparator = val; }

    public int getSvgDecimals() { return svgDecimals; }
    public void setSvgDecimals(int val) { this.svgDecimals = val; }

    public float getGridScale() { return gridScale; }
    public void setGridScale(float val) { this.gridScale = val; }

    public float calculateGcodeScale(int imgWidth, int imgHeight) {
        if (imgWidth <= 0 || imgHeight <= 0) return 1.0f;
        float scaleX = imageWidthMm / imgWidth;
        float scaleY = imageHeightMm / imgHeight;
        return Math.min(scaleX, scaleY);
    }

    public float calculateGcodeOffsetX(int imgWidth, float gcodeScale) {
        return -(imgWidth * gcodeScale / 2.0f);
    }

    public float calculateGcodeOffsetY(int imgHeight, float gcodeScale) {
        return -(paperTopToOriginMm - (paperHeightMm - (imgHeight * gcodeScale)) / 2.0f);
    }

    public String formatGcodeNumber(float value) {
        String s = String.format(Locale.US, "%." + gcodeDecimals + "f", value);
        if (gcodeDecimalSeparator != '.') {
            s = s.replace('.', gcodeDecimalSeparator);
        }
        return s;
    }

    public String formatSvgNumber(float value) {
        return String.format(Locale.US, "%." + svgDecimals + "f", value);
    }
}
