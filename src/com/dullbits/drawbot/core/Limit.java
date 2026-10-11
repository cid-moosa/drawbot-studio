package com.dullbits.drawbot.core;

/**
 * Tracks lower and upper bounds of 1D and 2D coordinate extents.
 */
public final class Limit {
    public float min;
    public float max;

    public Limit() {
        reset();
    }

    public void reset() {
        this.min = Float.POSITIVE_INFINITY;
        this.max = Float.NEGATIVE_INFINITY;
    }

    public void update(float value) {
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }

    public boolean hasBounds() {
        return min <= max && !Float.isInfinite(min) && !Float.isInfinite(max);
    }
}
