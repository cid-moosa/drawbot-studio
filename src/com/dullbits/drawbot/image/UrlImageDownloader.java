package com.dullbits.drawbot.image;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Safe image downloading utility.
 * Replaces the brittle startup clipboard regex fetch from the original sketch,
 * enforcing explicit user actions, timeouts, content-type checks, and size limits.
 */
public final class UrlImageDownloader {
    public static final int CONNECT_TIMEOUT_MS = 10_000;
    public static final int READ_TIMEOUT_MS = 15_000;
    public static final int MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB

    private UrlImageDownloader() {}

    public static BufferedImage downloadImage(String urlString) throws Exception {
        if (urlString == null || !urlString.trim().toLowerCase().matches("^https?://.*")) {
            throw new IllegalArgumentException("Invalid URL protocol. Must start with http:// or https://");
        }

        URL url = new URL(urlString.trim());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "DrawbotStudio/1.0");

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new Exception("HTTP error code: " + status + " (" + conn.getResponseMessage() + ")");
        }

        int contentLength = conn.getContentLength();
        if (contentLength > MAX_FILE_SIZE_BYTES) {
            throw new Exception("File too large (" + (contentLength / 1024 / 1024) + " MB). Maximum allowed is 25 MB.");
        }

        try (InputStream in = conn.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new Exception("The URL did not return a valid readable image format (JPEG/PNG/BMP/GIF).");
            }
            return img;
        } finally {
            conn.disconnect();
        }
    }
}
