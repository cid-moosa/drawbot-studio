package com.dullbits.drawbot.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Loads vignette and border overlays from assets/border/,
 * inverts, blurs, and blends onto images using ADD mode matching Image_Tools.pde:
 *   temp_boarder.resize(img.width, img.height);
 *   temp_boarder.filter(GRAY);
 *   temp_boarder.filter(INVERT);
 *   temp_boarder.filter(BLUR, blur);
 *   img.blend(temp_boarder, shrink, shrink, ..., ADD);
 */
public final class BorderFilter {

    private BorderFilter() {}

    /**
     * Applies a border vignette overlay onto src image.
     */
    public static BufferedImage applyBorder(BufferedImage src, String borderFilename, int shrink, int blurRadius) {
        BufferedImage borderImg = loadBorderAsset(borderFilename);
        if (borderImg == null) {
            return src;
        }

        int w = src.getWidth();
        int h = src.getHeight();

        // 1. Resize border to image dimensions (preserving alpha)
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(borderImg, 0, 0, w, h, null);
        g.dispose();

        // 2. Extract border overlay mask
        // Transparent PNG borders define the vignette in the alpha channel.
        int[] borderPixels = ((java.awt.image.DataBufferInt) resized.getRaster().getDataBuffer()).getData();

        // 3. Optional blur pass
        if (blurRadius > 0) {
            resized = ConvolutionEngine.blur(resized);
            borderPixels = ((java.awt.image.DataBufferInt) resized.getRaster().getDataBuffer()).getData();
        }

        // 4. ADD Blend onto src copy
        BufferedImage dst = ImagePipeline.copyImage(src);
        int[] dstPixels = ImageFilters.getPixels(dst);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int bx = Math.min(w - 1, Math.max(0, x + shrink));
                int by = Math.min(h - 1, Math.max(0, y + shrink));
                int bRgb = borderPixels[by * w + bx];

                // If border image has transparency, intensity is alpha. Otherwise luminance.
                int alpha = (bRgb >> 24) & 0xFF;
                int bVal = alpha;
                if (alpha == 255) {
                    int r = (bRgb >> 16) & 0xFF;
                    int gr = (bRgb >> 8) & 0xFF;
                    int b = bRgb & 0xFF;
                    bVal = (int) (0.299f * r + 0.587f * gr + 0.114f * b);
                }

                if (bVal == 0) continue; // Center of transparent border: no change

                int sRgb = dstPixels[y * w + x];
                int sr = (sRgb >> 16) & 0xFF;
                int sg = (sRgb >> 8) & 0xFF;
                int sb = sRgb & 0xFF;

                // ADD blending (additive clamp to 255)
                int nr = Math.min(255, sr + bVal);
                int ng = Math.min(255, sg + bVal);
                int nb = Math.min(255, sb + bVal);

                dstPixels[y * w + x] = (nr << 16) | (ng << 8) | nb;
            }
        }
        return dst;
    }

    private static BufferedImage loadBorderAsset(String filename) {
        try {
            // First try classpath / assets
            InputStream in = BorderFilter.class.getResourceAsStream("/border/" + filename);
            if (in != null) {
                return ImageIO.read(in);
            }
            // Check assets/border/ directory
            File f = new File("assets/border/" + filename);
            if (f.exists()) {
                return ImageIO.read(f);
            }
            // Fallback to reference directory
            File f2 = new File("referance/Drawbot_image_to_gcode_v2/Drawbot_image_to_gcode_v2/boarder/" + filename);
            if (f2.exists()) {
                return ImageIO.read(f2);
            }
        } catch (Exception e) {
            System.err.println("Warning: could not load border asset " + filename + ": " + e.getMessage());
        }
        return null;
    }
}
