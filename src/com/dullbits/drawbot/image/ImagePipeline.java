package com.dullbits.drawbot.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Manages the three fundamental image representations of the Drawbot pipeline:
 *  1. originalImage: Unmodified raw image loaded from file/URL.
 *  2. referenceImage: Image after cropping, scaling, convolution, borders, and desaturation.
 *  3. workingImage: Live mutable image used during pathfinding where pixels are lightened.
 */
public final class ImagePipeline {
    private BufferedImage originalImage;
    private BufferedImage referenceImage;
    private BufferedImage workingImage;

    public ImagePipeline() {}

    public synchronized void setOriginalImage(BufferedImage img) {
        this.originalImage = copyImage(img);
        this.referenceImage = copyImage(img);
        this.workingImage = copyImage(img);
    }

    public synchronized BufferedImage getOriginalImage() {
        return originalImage;
    }

    public synchronized BufferedImage getReferenceImage() {
        return referenceImage;
    }

    public synchronized void setReferenceImage(BufferedImage ref) {
        this.referenceImage = copyImage(ref);
        this.workingImage = copyImage(ref);
    }

    public synchronized BufferedImage getWorkingImage() {
        return workingImage;
    }

    public synchronized void setWorkingImage(BufferedImage work) {
        this.workingImage = copyImage(work);
    }

    public synchronized void resetWorkingToReference() {
        if (referenceImage != null) {
            this.workingImage = copyImage(referenceImage);
        }
    }

    public static BufferedImage copyImage(BufferedImage src) {
        if (src == null) return null;
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    public static int[] getPixelData(BufferedImage img) {
        if (img == null) return null;
        if (img.getType() == BufferedImage.TYPE_INT_RGB || img.getType() == BufferedImage.TYPE_INT_ARGB) {
            return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        }
        BufferedImage rgb = copyImage(img);
        return ((DataBufferInt) rgb.getRaster().getDataBuffer()).getData();
    }
}
