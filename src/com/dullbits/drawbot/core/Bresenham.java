package com.dullbits.drawbot.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Bresenham line and circle rasterization algorithms.
 * Directly translated and optimized from Jack Elton Bresenham's integer line algorithm (1962).
 */
public final class Bresenham {

    public static final class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Bresenham() {}

    /**
     * Computes all raster pixel coordinates along the line from (x0, y0) to (x1, y1).
     */
    public static List<Point> line(int x0, int y0, int x1, int y1) {
        List<Point> points = new ArrayList<>();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            points.add(new Point(x0, y0));
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return points;
    }

    /**
     * Midpoint circle rasterization algorithm generating ordered points on circle circumference.
     */
    public static List<Point> midpointCircle(int x0, int y0, int radius) {
        List<Point> pnts = new ArrayList<>();
        List<Point> p1 = new ArrayList<>();
        List<Point> p2 = new ArrayList<>();
        List<Point> p3 = new ArrayList<>();
        List<Point> p4 = new ArrayList<>();
        List<Point> p5 = new ArrayList<>();
        List<Point> p6 = new ArrayList<>();
        List<Point> p7 = new ArrayList<>();
        List<Point> p8 = new ArrayList<>();

        int x = radius;
        int y = 0;
        int err = 0;

        while (x >= y) {
            p1.add(new Point(x0 + x, y0 + y));
            p2.add(new Point(x0 + y, y0 + x));
            p3.add(new Point(x0 - y, y0 + x));
            p4.add(new Point(x0 - x, y0 + y));
            p5.add(new Point(x0 - x, y0 - y));
            p6.add(new Point(x0 - y, y0 - x));
            p7.add(new Point(x0 + y, y0 - x));
            p8.add(new Point(x0 + x, y0 - y));

            if (err <= 0) {
                y += 1;
                err += 2 * y + 1;
            }
            if (err > 0) {
                x -= 1;
                err -= 2 * x + 1;
            }
        }

        pnts.addAll(p1);
        pnts.addAll(p2);
        pnts.addAll(p3);
        pnts.addAll(p4);
        pnts.addAll(p5);
        pnts.addAll(p6);
        pnts.addAll(p7);
        pnts.addAll(p8);
        return pnts;
    }
}
