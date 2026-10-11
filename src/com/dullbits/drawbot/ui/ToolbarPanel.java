package com.dullbits.drawbot.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Redesigned top action toolbar providing structured, high-contrast,
 * logically-grouped workflows: File, Processing, Preview & Navigation, and Export.
 */
public final class ToolbarPanel extends JPanel {

    private final JButton openBtn;
    private final JButton urlBtn;
    private final JButton generateBtn;
    private final JButton stopBtn;

    private final JComboBox<DrawingCanvas.DisplayMode> modeCombo;
    private final JComboBox<String> penFilterCombo;
    private final JButton fitBtn;
    private final JButton resetBtn;
    private final JButton rotateBtn;
    private final JToggleButton gridToggle;

    private final JButton quickSvgBtn;
    private final JButton exportBtn;
    private final JButton adjustBtn;
    private final JButton settingsBtn;

    public ToolbarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 7));
        setOpaque(false);
        setBorder(null);
        setPreferredSize(new Dimension(1280, 48));

        // ==========================================
        // GROUP 1: FILE OPERATIONS
        // ==========================================
        openBtn = DrawbotTheme.createPrimaryButton("Open Image...", DrawbotTheme.ACCENT_PRIMARY);
        openBtn.setIcon(DrawbotTheme.createFolderIcon(14, Color.WHITE));
        openBtn.setToolTipText("Open an image file from disk (PNG, JPEG, BMP)");
        urlBtn = DrawbotTheme.createSecondaryButton("URL Image...");
        urlBtn.setIcon(DrawbotTheme.createGlobeIcon(14, Color.WHITE));
        urlBtn.setToolTipText("Download and load an image from a web URL");

        add(openBtn);
        add(urlBtn);

        add(createDivider());

        // ==========================================
        // GROUP 2: PROCESSING & PATH GENERATION
        // ==========================================
        generateBtn = DrawbotTheme.createPrimaryButton("Generate", DrawbotTheme.ACCENT_SUCCESS);
        generateBtn.setIcon(DrawbotTheme.createPlayIcon(10, Color.WHITE));
        generateBtn.setToolTipText("Execute the selected path-finding algorithm on the image");

        stopBtn = DrawbotTheme.createPrimaryButton("Stop", DrawbotTheme.ACCENT_DANGER);
        stopBtn.setIcon(DrawbotTheme.createStopIcon(10, Color.WHITE));
        stopBtn.setToolTipText("Cancel active drawing generation");
        stopBtn.setEnabled(false);

        add(generateBtn);
        add(stopBtn);

        add(createDivider());

        // ==========================================
        // GROUP 3: PREVIEW & VIEWPORT NAVIGATION
        // ==========================================
        JLabel viewLbl = new JLabel("View:");
        viewLbl.setFont(DrawbotTheme.FONT_BOLD);
        viewLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        add(viewLbl);

        modeCombo = new JComboBox<>(DrawingCanvas.DisplayMode.values());
        DrawbotTheme.styleComboBox(modeCombo, 130, 32);
        modeCombo.setToolTipText("Select active viewport display layer");
        add(modeCombo);

        penFilterCombo = new JComboBox<>(new String[]{"All Pens", "Pen 0", "Pen 1", "Pen 2", "Pen 3", "Pen 4", "Pen 5"});
        DrawbotTheme.styleComboBox(penFilterCombo, 95, 32);
        penFilterCombo.setToolTipText("Filter canvas preview to a specific pen layer");
        penFilterCombo.setEnabled(false);
        add(penFilterCombo);

        fitBtn = DrawbotTheme.createSecondaryButton("Fit View");
        fitBtn.setIcon(DrawbotTheme.createFitIcon(14, Color.WHITE));
        fitBtn.setToolTipText("Fit the drawing sheet to the current window size (Double-click canvas)");
        resetBtn = DrawbotTheme.createSecondaryButton("Reset");
        resetBtn.setIcon(DrawbotTheme.createResetIcon(14, Color.WHITE));
        resetBtn.setToolTipText("Reset zoom and pan position to 1:1");
        rotateBtn = DrawbotTheme.createSecondaryButton("Rotate 90\u00B0");
        rotateBtn.setIcon(DrawbotTheme.createRotateIcon(14, Color.WHITE));
        rotateBtn.setToolTipText("Rotate drawing sheet 90 degrees clockwise (Key: R)");
        gridToggle = DrawbotTheme.createToggleButton("Grid", true);
        gridToggle.setIcon(DrawbotTheme.createGridIcon(14, Color.WHITE));
        gridToggle.setToolTipText("Toggle machine boundaries, paper guides, and G-code origin markers (Key: G)");

        add(fitBtn);
        add(resetBtn);
        add(rotateBtn);
        add(gridToggle);

        add(createDivider());

        // ==========================================
        // GROUP 4: VECTOR & G-CODE EXPORT
        // ==========================================
        quickSvgBtn = DrawbotTheme.createPrimaryButton("Export SVG", DrawbotTheme.ACCENT_PURPLE);
        quickSvgBtn.setIcon(DrawbotTheme.createSvgIcon(14, Color.WHITE));
        quickSvgBtn.setToolTipText("1-Click Export directly to SVG vector file (Key: V)");

        exportBtn = DrawbotTheme.createPrimaryButton("Export All...", DrawbotTheme.ACCENT_WARNING);
        exportBtn.setIcon(DrawbotTheme.createExportIcon(14, Color.WHITE));
        exportBtn.setToolTipText("Full export dialog: G-Code CNC, SVG, and multi-pen PDF documents");

        add(quickSvgBtn);
        add(exportBtn);

        add(createDivider());

        // ==========================================
        // GROUP 5: IMAGE ADJUSTMENTS & PREFERENCES
        // ==========================================
        adjustBtn = DrawbotTheme.createSecondaryButton("Adjust Image...");
        adjustBtn.setIcon(DrawbotTheme.createSlidersIcon(14, Color.WHITE));
        adjustBtn.setToolTipText("Fine-tune image brightness, contrast, unsharp detail, and vignette border");
        add(adjustBtn);

        add(createDivider());

        settingsBtn = DrawbotTheme.createSecondaryButton("Preferences...");
        settingsBtn.setIcon(DrawbotTheme.createGearIcon(14, Color.WHITE));
        settingsBtn.setToolTipText("Customize appearance themes, canvas colors, workspace layouts, and accessibility (Ctrl+,)");
        add(settingsBtn);
    }

    public void updateToolbarDensity(DrawbotPreferences.ToolbarMode mode) {
        boolean compact = (mode == DrawbotPreferences.ToolbarMode.COMPACT);
        openBtn.setText(compact ? "" : "Open Image...");
        urlBtn.setText(compact ? "" : "URL Image...");
        generateBtn.setText(compact ? "" : "Generate");
        stopBtn.setText(compact ? "" : "Stop");
        fitBtn.setText(compact ? "" : "Fit View");
        resetBtn.setText(compact ? "" : "Reset");
        rotateBtn.setText(compact ? "" : "Rotate 90\u00B0");
        gridToggle.setText(compact ? "" : "Grid");
        quickSvgBtn.setText(compact ? "" : "Export SVG");
        exportBtn.setText(compact ? "" : "Export All...");
        adjustBtn.setText(compact ? "" : "Adjust Image...");
        settingsBtn.setText(compact ? "" : "Preferences...");
        revalidate();
        repaint();
    }

    private static JComponent createDivider() {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(8, 26));
                setOpaque(false);
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int h = getHeight();
                int yTop = 4;
                int yBottom = h - 4;
                // Dual-tone vertical glass groove (dark shadow etch + specular highlight)
                g2.setColor(DrawbotTheme.BORDER_GLASS_BOTTOM);
                g2.drawLine(3, yTop, 3, yBottom);
                g2.setColor(DrawbotTheme.SPECULAR_SHEEN);
                g2.drawLine(4, yTop, 4, yBottom);
                g2.dispose();
            }
        };
    }

    public JButton getOpenBtn() { return openBtn; }
    public JButton getUrlBtn() { return urlBtn; }
    public JButton getGenerateBtn() { return generateBtn; }
    public JButton getStopBtn() { return stopBtn; }
    public JComboBox<DrawingCanvas.DisplayMode> getModeCombo() { return modeCombo; }
    public JComboBox<String> getPenFilterCombo() { return penFilterCombo; }
    public JButton getFitBtn() { return fitBtn; }
    public JButton getResetBtn() { return resetBtn; }
    public JButton getRotateBtn() { return rotateBtn; }
    public JToggleButton getGridToggle() { return gridToggle; }
    public JButton getQuickSvgBtn() { return quickSvgBtn; }
    public JButton getExportBtn() { return exportBtn; }
    public JButton getAdjustBtn() { return adjustBtn; }
    public JButton getSettingsBtn() { return settingsBtn; }
}
