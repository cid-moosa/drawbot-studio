package com.dullbits.drawbot.core;

import java.util.Arrays;

/**
 * Compact, dynamically resizable storage for drawing segments.
 * Replaces the static 10,000,000 reference array in original BotDrawing.pde,
 * drastically reducing heap consumption and GC overhead.
 */
public final class SegmentBuffer {
    private static final int INITIAL_CAPACITY = 65536;
    private DrawingSegment[] segments;
    private int count;

    public SegmentBuffer() {
        this(INITIAL_CAPACITY);
    }

    public SegmentBuffer(int initialCapacity) {
        this.segments = new DrawingSegment[Math.max(64, initialCapacity)];
        this.count = 0;
    }

    public synchronized void addLine(int penNumber, boolean penDown, float x1, float y1, float x2, float y2) {
        if (count == segments.length) {
            int newCap = segments.length + (segments.length >> 1); // 1.5x growth
            if (newCap < 0) {
                newCap = Integer.MAX_VALUE - 8;
            }
            segments = Arrays.copyOf(segments, newCap);
        }
        segments[count++] = new DrawingSegment(penDown, penNumber, x1, y1, x2, y2);
    }

    public synchronized int getLineCount() {
        return count;
    }

    public synchronized DrawingSegment get(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Count: " + count);
        }
        return segments[index];
    }

    public synchronized void clear() {
        // Clear references for garbage collection
        Arrays.fill(segments, 0, count, null);
        count = 0;
    }

    /**
     * Flags segments whose start matches the previous segment's end with identical pen state,
     * allowing polyline coalescing in SVG exporters.
     */
    public synchronized void computeContinuations() {
        float prevX = 123456.0f;
        float prevY = 654321.0f;
        boolean prevPenDown = false;
        int prevPenNumber = -1;

        for (int i = 0; i < count; i++) {
            DrawingSegment seg = segments[i];
            if (prevX == seg.x1 && prevY == seg.y1 && prevPenDown == seg.penDown && prevPenNumber == seg.penNumber) {
                seg.penContinuation = true;
            } else {
                seg.penContinuation = false;
            }
            prevX = seg.x2;
            prevY = seg.y2;
            prevPenDown = seg.penDown;
            prevPenNumber = seg.penNumber;
        }
    }

    /**
     * Distributes pen numbers across the stroke budget according to normalized pen percentages.
     */
    public synchronized void distributePenChanges(int displayLimit, float[] penDistribution, int totalPens) {
        int limit = Math.min(displayLimit, count);
        if (limit <= 0 || totalPens <= 0) {
            return;
        }

        int p = 0;
        float pAccum = 0.0f;

        for (int i = 0; i < limit; i++) {
            if (p < totalPens - 1 && (i + 1) > (penDistribution[p] + pAccum)) {
                pAccum += penDistribution[p];
                p++;
                if (p >= totalPens) {
                    p = totalPens - 1;
                }
            }
            segments[i].penNumber = p;
        }
    }

    /**
     * Distributes pen numbers evenly across all active pens.
     */
    public synchronized void distributeEvenly(int displayLimit, int totalPens) {
        int limit = Math.min(displayLimit, count);
        if (limit <= 0 || totalPens <= 0) {
            return;
        }

        for (int i = 0; i < limit; i++) {
            int pen = (int) (((long) i * totalPens) / limit);
            segments[i].penNumber = Math.min(pen, totalPens - 1);
        }
    }

    /**
     * Calculates the bounding coordinates of active drawn lines up to displayLimit.
     */
    public synchronized void calculateBounds(int displayLimit, Limit xLimit, Limit yLimit) {
        xLimit.reset();
        yLimit.reset();
        int limit = Math.min(displayLimit, count);
        for (int i = 0; i < limit; i++) {
            DrawingSegment seg = segments[i];
            if (seg.penDown) {
                xLimit.update(seg.x1);
                xLimit.update(seg.x2);
                yLimit.update(seg.y1);
                yLimit.update(seg.y2);
            }
        }
    }
}
