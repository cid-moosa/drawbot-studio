package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.image.ImageAdjustments;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Interactive image framing and adjustments control panel.
 * Gives artists and plotters complete real-time control to adjust the image
 * into the drawing frame FIRST before computing sketch paths:
 *  - Frame Fit Mode: Fit to Frame (No Crop), Fill Frame (Crop), Match Aspect, Stretch
 *  - Orientation: 90 CW / CCW rotation, Flip H / V, Auto-rotate toggle
 *  - Framing Position & Scale: Zoom 50%-200%, X/Y Pan Offsets, Margin
 *  - Tone & Filters: Brightness, contrast, unsharp sharpness, negative, vignette borders
 */
public final class ImageAdjustmentsPanel extends JPanel {

    private final ImageAdjustments adjustments;
    private final Runnable onAdjustChanged;
    private Runnable onGenerateAction;

    // Frame fit controls
    private JComboBox<ImageAdjustments.FitMode> fitModeCombo;
    private JSlider marginSlider;
    private JLabel marginValLabel;

    // Orientation controls
    private JLabel orientationLabel;
    private JCheckBox autoRotateCheck;

    // Position & Scale controls
    private JSlider scaleSlider;
    private JLabel scaleValLabel;
    private JSlider offsetXSlider;
    private JLabel offsetXValLabel;
    private JSlider offsetYSlider;
    private JLabel offsetYValLabel;

    // Tone controls
    private JSlider brightnessSlider;
    private JLabel brightnessValLabel;
    private JSlider contrastSlider;
    private JLabel contrastValLabel;
    private JSlider unsharpSlider;
    private JLabel unsharpValLabel;
    private JCheckBox invertCheck;
    private JComboBox<String> borderCombo;

    private static final String[] BORDER_LABELS = {
        "None (Clean Edge)",
        "Border 1 - Classic Vignette 1 (b1.png)",
        "Border 2 - Classic Vignette 2 (b2.png)",
        "Border 3 - Grunge Vignette 3 (b3.png)",
        "Border 4 - Grunge Vignette 4 (b4.png)",
        "Border 5 - Grunge Frame 1 (b5.png)",
        "Border 6 - Soft Oval Frame (b6.png)",
        "Border 7 - Grunge Frame 3 (b7.png)",
        "Border 8 - Grunge Frame 4 (b8.png)",
        "Border 9 - Grunge Frame 5 (b9.png)",
        "Border 10 - Grunge Frame 6 (b10.png)",
        "Border 11 - Karma Heavy Grunge (b11.png)",
        "Border 12 - Distressed Border III (b12.png)",
        "Border 13 - Kuschelirmel Grunge (b13.png)"
    };

    private static final String[] BORDER_FILES = {
        "none", "b1.png", "b2.png", "b3.png", "b4.png", "b5.png",
        "b6.png", "b7.png", "b8.png", "b9.png", "b10.png", "b11.png", "b12.png", "b13.png"
    };

    public ImageAdjustmentsPanel(ImageAdjustments adjustments, Runnable onChange) {
        this(adjustments, onChange, null);
    }

    public ImageAdjustmentsPanel(ImageAdjustments adjustments, Runnable onChange, Runnable onGenerate) {
        this.adjustments = adjustments;
        this.onAdjustChanged = onChange;
        this.onGenerateAction = onGenerate;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ==============================================================
        // ACTION BANNER: Adjust First -> Generate
        // ==============================================================
        JPanel actionCard = new JPanel(new BorderLayout(6, 6));
        actionCard.setOpaque(true);
        actionCard.setBackground(DrawbotTheme.BG_CARD);
        actionCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel stepLbl = new JLabel("1. Adjust image framing   2. Click Generate");
        stepLbl.setFont(DrawbotTheme.FONT_BOLD);
        stepLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        actionCard.add(stepLbl, BorderLayout.WEST);

        JButton genBtn = DrawbotTheme.createPrimaryButton("Generate", DrawbotTheme.ACCENT_SUCCESS);
        genBtn.setIcon(DrawbotTheme.createPlayIcon(10, Color.WHITE));
        genBtn.setPreferredSize(new Dimension(100, 28));
        genBtn.addActionListener(e -> {
            if (onGenerateAction != null) {
                onGenerateAction.run();
            }
        });
        actionCard.add(genBtn, BorderLayout.EAST);
        actionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        actionCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(actionCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // ==============================================================
        // CARD 1: FRAME FITTING MODE & MARGINS
        // ==============================================================
        JPanel fitCard = createSectionCard("Frame Fitting Mode");

        fitCard.add(createLabel("How image fits the drawing frame:"));
        fitCard.add(Box.createRigidArea(new Dimension(0, 4)));

        fitModeCombo = new JComboBox<>(ImageAdjustments.FitMode.values());
        fitModeCombo.setSelectedItem(adjustments.getFitMode());
        DrawbotTheme.styleComboBox(fitModeCombo, 320, 28);
        fitModeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        fitModeCombo.addActionListener(e -> {
            adjustments.setFitMode((ImageAdjustments.FitMode) fitModeCombo.getSelectedItem());
            notifyChange();
        });
        fitCard.add(fitModeCombo);
        fitCard.add(Box.createRigidArea(new Dimension(0, 8)));

        fitCard.add(createHeaderRow("Frame Edge Margin:", marginValLabel = new JLabel("2%")));
        marginSlider = createSlider(0, 25, (int) Math.round(adjustments.getFrameMargin() * 100), v -> {
            adjustments.setFrameMargin(v / 100.0f);
            marginValLabel.setText(v + "%");
            notifyChange();
        });
        fitCard.add(marginSlider);

        content.add(fitCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // ==============================================================
        // CARD 2: ORIENTATION & ROTATION
        // ==============================================================
        JPanel orientCard = createSectionCard("Image Orientation");

        JPanel btnRow = new JPanel(new GridLayout(1, 4, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton rotCwBtn = DrawbotTheme.createSecondaryButton("\u27F3 90\u00B0 CW");
        rotCwBtn.setToolTipText("Rotate 90 degrees clockwise");
        rotCwBtn.addActionListener(e -> {
            adjustments.rotateCW();
            updateOrientationLabel();
            notifyChange();
        });

        JButton rotCcwBtn = DrawbotTheme.createSecondaryButton("\u27F2 90\u00B0 CCW");
        rotCcwBtn.setToolTipText("Rotate 90 degrees counter-clockwise");
        rotCcwBtn.addActionListener(e -> {
            adjustments.rotateCCW();
            updateOrientationLabel();
            notifyChange();
        });

        JButton flipHBtn = DrawbotTheme.createSecondaryButton("\u21C4 Flip H");
        flipHBtn.setToolTipText("Flip horizontally (mirror)");
        flipHBtn.addActionListener(e -> {
            adjustments.toggleFlipHorizontal();
            updateOrientationLabel();
            notifyChange();
        });

        JButton flipVBtn = DrawbotTheme.createSecondaryButton("\u21C5 Flip V");
        flipVBtn.setToolTipText("Flip vertically");
        flipVBtn.addActionListener(e -> {
            adjustments.toggleFlipVertical();
            updateOrientationLabel();
            notifyChange();
        });

        btnRow.add(rotCwBtn);
        btnRow.add(rotCcwBtn);
        btnRow.add(flipHBtn);
        btnRow.add(flipVBtn);
        orientCard.add(btnRow);
        orientCard.add(Box.createRigidArea(new Dimension(0, 6)));

        orientationLabel = new JLabel("Current: 0\u00B0 Normal");
        orientationLabel.setFont(DrawbotTheme.FONT_SMALL);
        orientationLabel.setForeground(DrawbotTheme.TEXT_MUTED);
        orientationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orientCard.add(orientationLabel);
        orientCard.add(Box.createRigidArea(new Dimension(0, 6)));

        autoRotateCheck = new JCheckBox("Auto-rotate landscape images 90\u00B0", adjustments.isAutoRotateLandscape());
        autoRotateCheck.setOpaque(false);
        autoRotateCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        autoRotateCheck.setFont(DrawbotTheme.FONT_REGULAR);
        autoRotateCheck.setFocusPainted(false);
        autoRotateCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoRotateCheck.addActionListener(e -> {
            adjustments.setAutoRotateLandscape(autoRotateCheck.isSelected());
            notifyChange();
        });
        orientCard.add(autoRotateCheck);

        content.add(orientCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // ==============================================================
        // CARD 3: POSITION & SCALE IN FRAME
        // ==============================================================
        JPanel posCard = createSectionCard("Position & Scale in Frame");

        posCard.add(createHeaderRow("Scale / Zoom:", scaleValLabel = new JLabel("100%")));
        scaleSlider = createSlider(50, 200, (int) Math.round(adjustments.getFrameScale() * 100), v -> {
            adjustments.setFrameScale(v / 100.0f);
            scaleValLabel.setText(v + "%");
            notifyChange();
        });
        posCard.add(scaleSlider);
        posCard.add(Box.createRigidArea(new Dimension(0, 6)));

        posCard.add(createHeaderRow("Horizontal Shift (X):", offsetXValLabel = new JLabel("0%")));
        offsetXSlider = createSlider(-50, 50, (int) Math.round(adjustments.getOffsetX() * 100), v -> {
            adjustments.setOffsetX(v / 100.0f);
            offsetXValLabel.setText(v + "%");
            notifyChange();
        });
        posCard.add(offsetXSlider);
        posCard.add(Box.createRigidArea(new Dimension(0, 6)));

        posCard.add(createHeaderRow("Vertical Shift (Y):", offsetYValLabel = new JLabel("0%")));
        offsetYSlider = createSlider(-50, 50, (int) Math.round(adjustments.getOffsetY() * 100), v -> {
            adjustments.setOffsetY(v / 100.0f);
            offsetYValLabel.setText(v + "%");
            notifyChange();
        });
        posCard.add(offsetYSlider);
        posCard.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton centerBtn = DrawbotTheme.createSecondaryButton("Center & Reset Framing");
        centerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBtn.addActionListener(e -> {
            adjustments.resetFraming();
            fitModeCombo.setSelectedItem(ImageAdjustments.FitMode.FIT_TO_FRAME);
            marginSlider.setValue(2);
            scaleSlider.setValue(100);
            offsetXSlider.setValue(0);
            offsetYSlider.setValue(0);
            autoRotateCheck.setSelected(false);
            updateOrientationLabel();
            notifyChange();
        });
        posCard.add(centerBtn);

        content.add(posCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // ==============================================================
        // CARD 4: IMAGE TONE & FILTERS
        // ==============================================================
        JPanel toneCard = createSectionCard("Image Tone & Filters");

        toneCard.add(createHeaderRow("Brightness:", brightnessValLabel = new JLabel("0")));
        brightnessSlider = createSlider(-100, 100, adjustments.getBrightness(), v -> {
            adjustments.setBrightness(v);
            brightnessValLabel.setText(String.valueOf(v));
            notifyChange();
        });
        toneCard.add(brightnessSlider);
        toneCard.add(Box.createRigidArea(new Dimension(0, 6)));

        toneCard.add(createHeaderRow("Contrast:", contrastValLabel = new JLabel("0")));
        contrastSlider = createSlider(-100, 100, adjustments.getContrast(), v -> {
            adjustments.setContrast(v);
            contrastValLabel.setText(String.valueOf(v));
            notifyChange();
        });
        toneCard.add(contrastSlider);
        toneCard.add(Box.createRigidArea(new Dimension(0, 6)));

        toneCard.add(createHeaderRow("Detail Unsharp Sharpness:", unsharpValLabel = new JLabel("3")));
        unsharpSlider = createSlider(0, 10, adjustments.getUnsharpStrength(), v -> {
            adjustments.setUnsharpStrength(v);
            unsharpValLabel.setText(String.valueOf(v));
            notifyChange();
        });
        toneCard.add(unsharpSlider);
        toneCard.add(Box.createRigidArea(new Dimension(0, 8)));

        invertCheck = new JCheckBox("Invert Colors (Negative)", adjustments.isInvert());
        invertCheck.setOpaque(false);
        invertCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        invertCheck.setFont(DrawbotTheme.FONT_REGULAR);
        invertCheck.setFocusPainted(false);
        invertCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        invertCheck.addActionListener(e -> {
            adjustments.setInvert(invertCheck.isSelected());
            notifyChange();
        });
        toneCard.add(invertCheck);
        toneCard.add(Box.createRigidArea(new Dimension(0, 8)));

        toneCard.add(createLabel("Border Vignette Blending:"));
        toneCard.add(Box.createRigidArea(new Dimension(0, 4)));
        borderCombo = new JComboBox<>(BORDER_LABELS);
        DrawbotTheme.styleComboBox(borderCombo, 320, 28);
        borderCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        borderCombo.addActionListener(e -> {
            int idx = borderCombo.getSelectedIndex();
            if (idx >= 0 && idx < BORDER_FILES.length) {
                adjustments.setBorderName(BORDER_FILES[idx]);
                notifyChange();
            }
        });
        toneCard.add(borderCombo);
        toneCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton resetAllBtn = DrawbotTheme.createSecondaryButton("Reset All Adjustments");
        resetAllBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetAllBtn.addActionListener(e -> resetDefaults());
        toneCard.add(resetAllBtn);

        content.add(toneCard);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnGenerateAction(Runnable action) {
        this.onGenerateAction = action;
    }

    private void updateOrientationLabel() {
        StringBuilder sb = new StringBuilder("Current: ");
        sb.append(adjustments.getRotationAngle()).append("\u00B0");
        if (adjustments.isFlipHorizontal()) sb.append(" | Flipped H");
        if (adjustments.isFlipVertical()) sb.append(" | Flipped V");
        orientationLabel.setText(sb.toString());
    }

    private static JPanel createSectionCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(DrawbotTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(DrawbotTheme.FONT_TITLE);
        t.setForeground(DrawbotTheme.TEXT_PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        return card;
    }

    private static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(DrawbotTheme.TEXT_PRIMARY);
        l.setFont(DrawbotTheme.FONT_BOLD);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel createHeaderRow(String title, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(title);
        l.setForeground(DrawbotTheme.TEXT_PRIMARY);
        l.setFont(DrawbotTheme.FONT_BOLD);
        row.add(l, BorderLayout.WEST);

        valLbl.setForeground(DrawbotTheme.ACCENT_PRIMARY);
        valLbl.setFont(DrawbotTheme.FONT_MONO);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    private static JSlider createSlider(int min, int max, int init, java.util.function.Consumer<Integer> onVal) {
        JSlider slider = new JSlider(min, max, init);
        slider.setOpaque(false);
        slider.setForeground(DrawbotTheme.BORDER_SUBTLE);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(e -> onVal.accept(slider.getValue()));
        return slider;
    }

    private void notifyChange() {
        if (onAdjustChanged != null) {
            onAdjustChanged.run();
        }
    }

    public void resetDefaults() {
        adjustments.reset();
        fitModeCombo.setSelectedItem(ImageAdjustments.FitMode.FIT_TO_FRAME);
        marginSlider.setValue(2);
        scaleSlider.setValue(100);
        offsetXSlider.setValue(0);
        offsetYSlider.setValue(0);
        autoRotateCheck.setSelected(false);
        updateOrientationLabel();

        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        unsharpSlider.setValue(3);
        invertCheck.setSelected(false);
        borderCombo.setSelectedIndex(0);
        notifyChange();
    }
}
