package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.color.CopicPalette;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.DrawingSegment;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.ImagePipeline;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Professional graphics workspace canvas.
 * Implements centered paper layout, soft drop-shadow, responsive zoom/pan/rotate,
 * informative empty-state drop-target, layer views, and machine guides.
 */
public final class DrawingCanvas extends JPanel {

    public enum DisplayMode {
        DRAWING("Drawing"),
        PEN_ONLY("Selected Pen"),
        ORIGINAL("Original Image"),
        REFERENCE("Reference Image"),
        LIGHTENED("Lightened Image");

        private final String label;
        DisplayMode(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private final ImagePipeline imagePipeline;
    private final SegmentBuffer segmentBuffer;
    private final MachineConfig machineConfig;
    private final PenDistribution penDistribution;

    private DisplayMode displayMode = DisplayMode.DRAWING;
    private int selectedPen = 0;
    private int displayLineCount = Integer.MAX_VALUE;
    private boolean isGridOn = true;

    // Viewport transform
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private int rotationSteps = 0; // 0, 1, 2, 3 (each 90 degrees)
    private boolean hasAutoFitted = false;

    private Point dragStartPoint = null;
    private Consumer<File> fileDropListener;

    // Animated Processing HUD
    private boolean isProcessing = false;
    private String processingTitle = "Processing Paths...";
    private String processingSubtitle = "Computing geometry...";
    private int hudSpinnerAngle = 0;
    private final javax.swing.Timer hudAnimationTimer;

    public DrawingCanvas(ImagePipeline pipeline, SegmentBuffer buffer,
                         MachineConfig config, PenDistribution distribution) {
        this.imagePipeline = pipeline;
        this.segmentBuffer = buffer;
        this.machineConfig = config;
        this.penDistribution = distribution;

        DrawbotPreferences prefs = DrawbotPreferences.getInstance();
        setBackground(prefs.getCanvasBackground().getColor());
        setDoubleBuffered(true);

        hudAnimationTimer = new javax.swing.Timer(33, e -> {
            if (isProcessing) {
                if (!DrawbotPreferences.getInstance().isReducedMotion()) {
                    hudSpinnerAngle = (hudSpinnerAngle + 12) % 360;
                }
                repaint();
            }
        });

        prefs.addChangeListener(p -> {
            setBackground(p.getCanvasBackground().getColor());
            repaint();
        });

        setupInteractionListeners();
        setupDragAndDrop();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!hasAutoFitted && getWidth() > 50 && getHeight() > 50) {
                    fitToView();
                    hasAutoFitted = true;
                }
            }
        });
    }

    public void setFileDropListener(Consumer<File> listener) {
        this.fileDropListener = listener;
    }

    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
        repaint();
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setSelectedPen(int pen) {
        this.selectedPen = pen;
        repaint();
    }

    public int getSelectedPen() {
        return selectedPen;
    }

    public void setDisplayLineCount(int count) {
        this.displayLineCount = count;
        repaint();
    }

    public int getDisplayLineCount() {
        return displayLineCount;
    }

    public void toggleGrid() {
        this.isGridOn = !this.isGridOn;
        repaint();
    }

    public void setProcessing(boolean processing, String title, String subtitle) {
        this.isProcessing = processing;
        if (title != null) this.processingTitle = title;
        if (subtitle != null) this.processingSubtitle = subtitle;
        if (processing) {
            if (!hudAnimationTimer.isRunning()) hudAnimationTimer.start();
        } else {
            if (hudAnimationTimer.isRunning()) hudAnimationTimer.stop();
        }
        repaint();
    }

    public void updateProcessingSubtitle(String subtitle) {
        this.processingSubtitle = subtitle;
        if (isProcessing) {
            repaint();
        }
    }

    public boolean isGridOn() {
        return isGridOn;
    }

    public void rotate90() {
        this.rotationSteps = (this.rotationSteps + 1) % 4;
        fitToView();
    }

    public void zoomIn() {
        zoomCenter(1.15);
    }

    public void zoomOut() {
        zoomCenter(1.0 / 1.15);
    }

    private void zoomCenter(double factor) {
        double oldZoom = zoom;
        zoom = Math.max(0.02, Math.min(60.0, zoom * factor));
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        panX = cx - (cx - panX) * (zoom / oldZoom);
        panY = cy - (cy - panY) * (zoom / oldZoom);
        repaint();
    }

    public void resetView() {
        zoom = 1.0;
        panX = 0.0;
        panY = 0.0;
        rotationSteps = 0;
        fitToView();
    }

    /**
     * Centers and scales the active paper/image within the viewport with comfortable padding.
     */
    public void fitToView() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        Dimension docSize = getDocumentPixelDimensions();
        double docW = docSize.width;
        double docH = docSize.height;

        double effW = (rotationSteps % 2 == 0) ? docW : docH;
        double effH = (rotationSteps % 2 == 0) ? docH : docW;

        double margin = 36.0;
        double availW = Math.max(10.0, w - margin * 2.0);
        double availH = Math.max(10.0, h - margin * 2.0);

        zoom = Math.min(availW / effW, availH / effH);

        panX = (w - effW * zoom) / 2.0;
        panY = (h - effH * zoom) / 2.0;
        repaint();
    }

    private Dimension getDocumentPixelDimensions() {
        BufferedImage img = getActiveDisplayImage();
        if (img != null) {
            return new Dimension(img.getWidth(), img.getHeight());
        }
        // When empty: convert machine paper mm to logical pixels (2.5 px/mm)
        int pw = (int) Math.round(machineConfig.getPaperWidthMm() * 2.5);
        int ph = (int) Math.round(machineConfig.getPaperHeightMm() * 2.5);
        return new Dimension(Math.max(400, pw), Math.max(500, ph));
    }

    private void setupInteractionListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON2) {
                    dragStartPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStartPoint = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    fitToView();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartPoint != null) {
                    panX += (e.getX() - dragStartPoint.x);
                    panY += (e.getY() - dragStartPoint.y);
                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            double oldZoom = zoom;
            if (e.getWheelRotation() < 0) {
                zoom *= 1.12;
            } else {
                zoom /= 1.12;
            }
            zoom = Math.max(0.02, Math.min(60.0, zoom));

            Point p = e.getPoint();
            panX = p.x - (p.x - panX) * (zoom / oldZoom);
            panY = p.y - (p.y - panY) * (zoom / oldZoom);
            repaint();
        });
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    if (dtde.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (files != null && !files.isEmpty() && fileDropListener != null) {
                            fileDropListener.accept(files.get(0));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (!hasAutoFitted && getWidth() > 50 && getHeight() > 50) {
            fitToView();
            hasAutoFitted = true;
        }

        Dimension docSize = getDocumentPixelDimensions();
        int docW = docSize.width;
        int docH = docSize.height;

        BufferedImage activeImg = getActiveDisplayImage();
        boolean isEmpty = (activeImg == null && segmentBuffer.getLineCount() == 0);

        AffineTransform origTx = g2.getTransform();

        // 1. Apply viewport transformations
        g2.translate(panX, panY);
        g2.scale(zoom, zoom);
        if (rotationSteps != 0) {
            g2.rotate(Math.toRadians(rotationSteps * 90), docW / 2.0, docH / 2.0);
        }

        // 2. Render soft paper drop shadow
        paintPaperDropShadow(g2, docW, docH);

        // 3. Render physical paper sheet
        g2.setColor(DrawbotTheme.BG_PAPER);
        g2.fillRect(0, 0, docW, docH);

        // 4. Content or Empty State
        if (isEmpty) {
            paintEmptyState(g2, docW, docH);
        } else {
            // Render active image layer or vector lines
            if (displayMode == DisplayMode.ORIGINAL && imagePipeline.getOriginalImage() != null) {
                g2.drawImage(imagePipeline.getOriginalImage(), 0, 0, null);
            } else if (displayMode == DisplayMode.REFERENCE && imagePipeline.getReferenceImage() != null) {
                g2.drawImage(imagePipeline.getReferenceImage(), 0, 0, null);
            } else if (displayMode == DisplayMode.LIGHTENED && imagePipeline.getWorkingImage() != null) {
                g2.drawImage(imagePipeline.getWorkingImage(), 0, 0, null);
            } else {
                if (segmentBuffer.getLineCount() > 0) {
                    renderDrawingLines(g2, docW, docH);
                } else if (imagePipeline.getReferenceImage() != null) {
                    g2.drawImage(imagePipeline.getReferenceImage(), 0, 0, null);
                }
            }

            // 5. Render machine guides and grid when not empty
            if (isGridOn) {
                paintMachineGrid(g2, docW, docH);
            }
        }

        // 6. Paper outline border
        g2.setColor(new Color(185, 190, 200));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(0, 0, docW, docH);

        g2.setTransform(origTx);

        if (isProcessing) {
            paintProcessingHUD(g2);
        }

        g2.dispose();
    }

    private void paintProcessingHUD(Graphics2D g2) {
        int w = 340;
        int h = 54;
        int x = (getWidth() - w) / 2;
        int y = getHeight() - h - 24;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Soft Ambient Elevation Shadow (Dual Layer)
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(x, y + 4, w, h, 16, 16);
        g2.setColor(new Color(0, 0, 0, 75));
        g2.fillRoundRect(x + 1, y + 2, w - 2, h, 14, 14);

        // 2. Liquid Glass Translucent Obsidian Base
        g2.setColor(new Color(24, 27, 34, 230));
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // 3. Apple Liquid Glass Specular Top Sheen
        int sheenH = h * 45 / 100;
        Paint sheen = new LinearGradientPaint(
            x, y, x, y + sheenH,
            new float[]{0.0f, 1.0f},
            new Color[]{new Color(255, 255, 255, 45), new Color(255, 255, 255, 0)}
        );
        g2.setPaint(sheen);
        g2.fillRoundRect(x + 1, y + 1, w - 2, sheenH, 13, 13);

        // 4. Dual-Tone Specular Glass Beveled Rim
        Paint rim = new LinearGradientPaint(
            x, y, x, y + h,
            new float[]{0.0f, 0.45f, 1.0f},
            new Color[]{
                new Color(255, 255, 255, 75),
                new Color(255, 255, 255, 15),
                new Color(0, 0, 0, 90)
            }
        );
        g2.setPaint(rim);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, w - 1, h - 1, 14, 14);

        // 5. Animated Vector Spinner
        int spinX = x + 16;
        int spinY = y + 15;
        int spinD = 24;

        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(10, 132, 255, 45));
        g2.drawOval(spinX, spinY, spinD, spinD);

        g2.setColor(DrawbotTheme.ACCENT_PRIMARY);
        g2.drawArc(spinX, spinY, spinD, spinD, -hudSpinnerAngle, 260);

        double rad = Math.toRadians(-hudSpinnerAngle + 260);
        int scx = spinX + spinD / 2;
        int scy = spinY + spinD / 2;
        int sr = spinD / 2;
        int hx = (int) Math.round(scx + sr * Math.cos(rad));
        int hy = (int) Math.round(scy - sr * Math.sin(rad));
        g2.setColor(Color.WHITE);
        g2.fillOval(hx - 2, hy - 2, 4, 4);

        // 6. Title Text
        g2.setFont(DrawbotTheme.FONT_BOLD);
        g2.setColor(DrawbotTheme.TEXT_PRIMARY);
        g2.drawString(processingTitle, x + 50, y + 24);

        // 7. Subtitle / Progress Info Text
        g2.setFont(DrawbotTheme.FONT_SMALL);
        g2.setColor(DrawbotTheme.TEXT_MUTED);
        g2.drawString(processingSubtitle, x + 50, y + 42);
    }

    private void paintPaperDropShadow(Graphics2D g2, int w, int h) {
        int shadowSize = 10;
        for (int i = shadowSize; i > 0; i--) {
            int alpha = (int) (28 * (1.0f - (float) i / shadowSize));
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRoundRect(-i, -i + 3, w + i * 2, h + i * 2, 4, 4);
        }
    }

    private void paintEmptyState(Graphics2D g2, int w, int h) {
        // Subtle inner drop target border
        int inset = 24;
        g2.setColor(new Color(225, 230, 238));
        float[] dash = {6.0f, 4.0f};
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawRoundRect(inset, inset, w - inset * 2, h - inset * 2, 12, 12);

        int cy = h / 2 - 30;

        // Icon: Image upload symbol
        g2.setColor(new Color(170, 180, 195));
        g2.setStroke(new BasicStroke(3.0f));
        int iconW = 48;
        int iconH = 38;
        int ix = (w - iconW) / 2;
        int iy = cy - 40;
        g2.drawRoundRect(ix, iy, iconW, iconH, 6, 6);
        // Sun/circle
        g2.drawOval(ix + 10, iy + 8, 8, 8);
        // Mountain peak
        g2.drawLine(ix + 6, iy + iconH - 6, ix + 22, iy + 16);
        g2.drawLine(ix + 22, iy + 16, ix + 34, iy + 26);
        g2.drawLine(ix + 34, iy + 26, ix + 42, iy + iconH - 6);

        // Primary text
        g2.setColor(new Color(65, 70, 82));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        FontMetrics fm1 = g2.getFontMetrics();
        String msg1 = "Drag & Drop Image Here";
        g2.drawString(msg1, (w - fm1.stringWidth(msg1)) / 2, cy + 32);

        // Secondary guidance
        g2.setColor(new Color(110, 118, 132));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm2 = g2.getFontMetrics();
        String msg2 = "or click \"Open Image...\" in the toolbar to begin";
        g2.drawString(msg2, (w - fm2.stringWidth(msg2)) / 2, cy + 54);

        // Format info badge
        g2.setColor(new Color(145, 155, 170));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm3 = g2.getFontMetrics();
        String msg3 = "Supported formats: PNG, JPG, BMP";
        g2.drawString(msg3, (w - fm3.stringWidth(msg3)) / 2, cy + 76);
    }

    private BufferedImage getActiveDisplayImage() {
        if (displayMode == DisplayMode.ORIGINAL && imagePipeline.getOriginalImage() != null) {
            return imagePipeline.getOriginalImage();
        }
        if (imagePipeline.getReferenceImage() != null) {
            return imagePipeline.getReferenceImage();
        }
        return imagePipeline.getOriginalImage();
    }

    private void renderDrawingLines(Graphics2D g2, int imgW, int imgH) {
        int lineCount = Math.min(displayLineCount, segmentBuffer.getLineCount());
        if (lineCount <= 0) return;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.88f));
        g2.setStroke(new BasicStroke(0.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int lastPen = -1;
        Color currentColor = Color.BLACK;

        for (int i = 0; i < lineCount; i++) {
            DrawingSegment seg = segmentBuffer.get(i);
            if (!seg.penDown) continue;
            if (displayMode == DisplayMode.PEN_ONLY && seg.penNumber != selectedPen) continue;

            if (seg.penNumber != lastPen) {
                String markerCode = penDistribution.getMarkerCodeForPen(seg.penNumber);
                currentColor = CopicPalette.getOriginalColor(markerCode);
                g2.setColor(currentColor);
                lastPen = seg.penNumber;
            }

            g2.drawLine((int) Math.round(seg.x1), (int) Math.round(seg.y1),
                        (int) Math.round(seg.x2), (int) Math.round(seg.y2));
        }
    }

    private void paintMachineGrid(Graphics2D g2, int imgW, int imgH) {
        float gcodeScale = machineConfig.calculateGcodeScale(imgW, imgH);
        float offsetX = machineConfig.calculateGcodeOffsetX(imgW, gcodeScale);
        float offsetY = machineConfig.calculateGcodeOffsetY(imgH, gcodeScale);

        // Printable bounds (Crisp dashed line using dynamic preference grid color)
        g2.setColor(DrawbotPreferences.getInstance().getGridColor().getColor());
        float[] dash = {4.0f, 4.0f};
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawRect(0, 0, imgW, imgH);

        // Origin marker (Red crosshair at G-code origin)
        int ox = (int) Math.round(offsetX);
        int oy = (int) Math.round(offsetY);
        g2.setColor(new Color(220, 50, 50, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(ox - 8, oy, ox + 8, oy);
        g2.drawLine(ox, oy - 8, ox, oy + 8);
    }
}
