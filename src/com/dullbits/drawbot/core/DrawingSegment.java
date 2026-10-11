package com.dullbits.drawbot.core;

/**
 * Represents a single directed 2D line segment in image pixel coordinates.
 */
public final class DrawingSegment {
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public boolean penDown;
    public int penNumber;
    public boolean penContinuation;

    public DrawingSegment(boolean penDown, int penNumber, float x1, float y1, float x2, float y2) {
        this.penDown = penDown;
        this.penNumber = penNumber;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.penContinuation = false;
    }

    public DrawingSegment(DrawingSegment other) {
        this.penDown = other.penDown;
        this.penNumber = other.penNumber;
        this.x1 = other.x1;
        this.y1 = other.y1;
        this.x2 = other.x2;
        this.y2 = other.y2;
        this.penContinuation = other.penContinuation;
    }

    public float length() {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
