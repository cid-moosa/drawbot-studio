package com.dullbits.drawbot.image;

/**
 * Encapsulates interactive user image framing and adjustments:
 *  - Frame Fit Mode: Fit to Frame (No Crop), Fill Frame (Crop), Match Aspect, Stretch
 *  - Orientation: Rotation angle (0, 90, 180, 270), Flip Horizontal, Flip Vertical, Auto-Rotate
 *  - Framing Scale & Position: Zoom (50%-200%), Offset X/Y, Frame Margin
 *  - Tone & Filters: Brightness, contrast, negative/inversion, sharpness, and vignette borders
 */
public final class ImageAdjustments {

    public enum FitMode {
        FIT_TO_FRAME("Fit Entire Image (No Crop)"), // Preserves 100% of image with margins
        FILL_FRAME("Fill Frame (Crop to Fit)"),     // Center crop to fill
        MATCH_ASPECT("Match Image Aspect Ratio"),   // Adjusts frame to image
        STRETCH("Stretch to Fill");

        private final String label;
        FitMode(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    // Framing & Orientation
    private FitMode fitMode = FitMode.FIT_TO_FRAME; // DEFAULT: ZERO CROPPING
    private int rotationAngle = 0;                  // 0, 90, 180, 270 degrees
    private boolean flipHorizontal = false;
    private boolean flipVertical = false;
    private boolean autoRotateLandscape = false;    // Default: false (respect user orientation)
    private float frameScale = 1.0f;                // 0.5 to 2.0 (50% to 200%)
    private float offsetX = 0.0f;                   // -0.5 to +0.5 (-50% to +50%)
    private float offsetY = 0.0f;                   // -0.5 to +0.5 (-50% to +50%)
    private float frameMargin = 0.02f;              // 0.0 to 0.25 (0% to 25%, default 2% margin)

    // Image Tone & Filters
    private int brightness = 0;                     // -100 to +100
    private int contrast = 0;                       // -100 to +100
    private boolean invert = false;                 // Invert luminance
    private int unsharpStrength = 3;                // 0 to 10
    private String borderName = "none";             // "none", "b1.png"..."b13.png"

    public ImageAdjustments() {}

    // Framing Getters & Setters
    public FitMode getFitMode() { return fitMode; }
    public void setFitMode(FitMode mode) { this.fitMode = mode != null ? mode : FitMode.FIT_TO_FRAME; }

    public int getRotationAngle() { return rotationAngle; }
    public void setRotationAngle(int angle) {
        int norm = angle % 360;
        if (norm < 0) norm += 360;
        this.rotationAngle = (norm / 90) * 90;
    }
    public void rotateCW() { setRotationAngle(rotationAngle + 90); }
    public void rotateCCW() { setRotationAngle(rotationAngle - 90); }

    public boolean isFlipHorizontal() { return flipHorizontal; }
    public void setFlipHorizontal(boolean flip) { this.flipHorizontal = flip; }
    public void toggleFlipHorizontal() { this.flipHorizontal = !this.flipHorizontal; }

    public boolean isFlipVertical() { return flipVertical; }
    public void setFlipVertical(boolean flip) { this.flipVertical = flip; }
    public void toggleFlipVertical() { this.flipVertical = !this.flipVertical; }

    public boolean isAutoRotateLandscape() { return autoRotateLandscape; }
    public void setAutoRotateLandscape(boolean auto) { this.autoRotateLandscape = auto; }

    public float getFrameScale() { return frameScale; }
    public void setFrameScale(float scale) { this.frameScale = Math.max(0.5f, Math.min(2.5f, scale)); }

    public float getOffsetX() { return offsetX; }
    public void setOffsetX(float x) { this.offsetX = Math.max(-0.5f, Math.min(0.5f, x)); }

    public float getOffsetY() { return offsetY; }
    public void setOffsetY(float y) { this.offsetY = Math.max(-0.5f, Math.min(0.5f, y)); }

    public float getFrameMargin() { return frameMargin; }
    public void setFrameMargin(float m) { this.frameMargin = Math.max(0.0f, Math.min(0.3f, m)); }

    // Tone Getters & Setters
    public int getBrightness() { return brightness; }
    public void setBrightness(int b) { this.brightness = Math.max(-100, Math.min(100, b)); }

    public int getContrast() { return contrast; }
    public void setContrast(int c) { this.contrast = Math.max(-100, Math.min(100, c)); }

    public boolean isInvert() { return invert; }
    public void setInvert(boolean inv) { this.invert = inv; }

    public int getUnsharpStrength() { return unsharpStrength; }
    public void setUnsharpStrength(int s) { this.unsharpStrength = Math.max(0, Math.min(10, s)); }

    public String getBorderName() { return borderName; }
    public void setBorderName(String b) { this.borderName = b != null ? b : "none"; }

    public void resetFraming() {
        this.fitMode = FitMode.FIT_TO_FRAME;
        this.rotationAngle = 0;
        this.flipHorizontal = false;
        this.flipVertical = false;
        this.autoRotateLandscape = false;
        this.frameScale = 1.0f;
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        this.frameMargin = 0.02f;
    }

    public void resetTone() {
        this.brightness = 0;
        this.contrast = 0;
        this.invert = false;
        this.unsharpStrength = 3;
        this.borderName = "none";
    }

    public void reset() {
        resetFraming();
        resetTone();
    }
}
