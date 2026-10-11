package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.pfm.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

/**
 * Sidebar control panel with structured, compact tabs:
 *  - Algorithm selector & human-friendly numeric parameter spinboxes
 *  - Pen management & distribution
 *  - Machine geometry & coordinate settings
 *  - Stroke limit & budget controls
 *
 * Fully corrected layout: inputs have fixed heights (never stretched)
 * and high WCAG contrast against dark backgrounds.
 */
public final class ControlPanel extends JPanel {

    private final List<PathFindingModule> pfmModules = new ArrayList<>();
    private int currentPfmIndex = 0;

    private final JComboBox<String> pfmCombo;
    private final JTextArea pfmDescText;
    private final JPanel pfmParamsPanel;

    private int macroStrengthPct = 50;
    private int macroDetailLevel = 2;
    private boolean isAdvancedExpanded = false;
    private boolean isSyncingMacro = false;
    private final Map<String, JSpinner> advancedSpinners = new LinkedHashMap<>();

    private final ImageAdjustmentsPanel imageAdjustPanel;
    private final PenDistributionPanel penPanel;
    private final MachineConfig machineConfig;
    private final SegmentBuffer segmentBuffer;
    private final Runnable onConfigChanged;

    private final JTabbedPane tabbedPane;
    private JSlider strokeBudgetSlider;
    private JLabel strokeBudgetLabel;

    public ControlPanel(MachineConfig config, PenDistribution distribution,
                        SegmentBuffer buffer, com.dullbits.drawbot.image.ImageAdjustments adjustments,
                        Runnable onChange, Runnable onAdjustChange) {
        this.machineConfig = config;
        this.segmentBuffer = buffer;
        this.onConfigChanged = onChange;

        pfmModules.add(new PfmOriginal());
        pfmModules.add(new PfmDarkestRay());
        pfmModules.add(new PfmSpiral());
        pfmModules.add(new PfmWaveform());
        pfmModules.add(new PfmCrosshatch());
        pfmModules.add(new PfmConcentric());
        pfmModules.add(new PfmSquares());

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(320, 450));
        setPreferredSize(new Dimension(360, 750));
        setBackground(DrawbotTheme.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, DrawbotTheme.BORDER_SUBTLE));

        this.tabbedPane = new JTabbedPane();
        DrawbotTheme.styleTabbedPane(tabbedPane);

        // --- TAB 1: Algorithms ---
        JPanel algoTab = new JPanel(new BorderLayout(0, 8));
        algoTab.setOpaque(false);
        algoTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top: Selector & Description Card
        JPanel topCard = new JPanel(new BorderLayout(0, 6));
        topCard.setOpaque(true);
        topCard.setBackground(DrawbotTheme.BG_CARD);
        topCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 10, 10)
        ));

        JLabel algoHeader = new JLabel("Path Finding Algorithm");
        algoHeader.setFont(DrawbotTheme.FONT_TITLE);
        algoHeader.setForeground(DrawbotTheme.TEXT_PRIMARY);
        topCard.add(algoHeader, BorderLayout.NORTH);

        String[] pfmNames = pfmModules.stream().map(PathFindingModule::getName).toArray(String[]::new);
        pfmCombo = new JComboBox<>(pfmNames);
        DrawbotTheme.styleComboBox(pfmCombo, 320, 30);
        topCard.add(pfmCombo, BorderLayout.CENTER);

        pfmDescText = new JTextArea(3, 20);
        pfmDescText.setWrapStyleWord(true);
        pfmDescText.setLineWrap(true);
        pfmDescText.setEditable(false);
        pfmDescText.setOpaque(false);
        pfmDescText.setForeground(DrawbotTheme.TEXT_SECONDARY);
        pfmDescText.setFont(DrawbotTheme.FONT_REGULAR);
        pfmDescText.setBorder(new EmptyBorder(4, 2, 2, 2));
        topCard.add(pfmDescText, BorderLayout.SOUTH);
        algoTab.add(topCard, BorderLayout.NORTH);

        // Center: Parameters Container
        pfmParamsPanel = new JPanel();
        pfmParamsPanel.setLayout(new BoxLayout(pfmParamsPanel, BoxLayout.Y_AXIS));
        pfmParamsPanel.setOpaque(false);
        pfmParamsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JScrollPane paramsScroll = new JScrollPane(pfmParamsPanel);
        paramsScroll.setOpaque(false);
        paramsScroll.getViewport().setOpaque(false);
        paramsScroll.setBorder(null);
        paramsScroll.getVerticalScrollBar().setUnitIncrement(16);
        algoTab.add(paramsScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Algorithm", algoTab);

        // --- TAB 2: Image Adjust ---
        imageAdjustPanel = new ImageAdjustmentsPanel(adjustments, onAdjustChange);
        tabbedPane.addTab("Image Adjust", imageAdjustPanel);

        // --- TAB 3: Pens ---
        penPanel = new PenDistributionPanel(distribution, buffer, onChange);
        tabbedPane.addTab("Pens", penPanel);

        // --- TAB 4: Machine & Geometry ---
        JPanel machineTab = buildMachineTab();
        tabbedPane.addTab("Machine", machineTab);

        // --- TAB 5: Stroke Budget ---
        JPanel strokeTab = buildStrokeBudgetTab();
        tabbedPane.addTab("Strokes", strokeTab);

        add(tabbedPane, BorderLayout.CENTER);

        pfmCombo.addActionListener(e -> {
            currentPfmIndex = pfmCombo.getSelectedIndex();
            updatePfmControls();
        });
        updatePfmControls();
    }

    public PathFindingModule getSelectedPfm() {
        return pfmModules.get(currentPfmIndex);
    }

    public void setPfmIndex(int index) {
        if (index >= 0 && index < pfmModules.size()) {
            pfmCombo.setSelectedIndex(index);
        }
    }

    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void setOnGenerateAction(Runnable action) {
        if (imageAdjustPanel != null) {
            imageAdjustPanel.setOnGenerateAction(action);
        }
    }

    public ImageAdjustmentsPanel getImageAdjustPanel() {
        return imageAdjustPanel;
    }

    public void setAdvancedExpanded(boolean expanded) {
        this.isAdvancedExpanded = expanded;
        updatePfmControls();
    }

    public boolean isAdvancedExpanded() {
        return isAdvancedExpanded;
    }

    public void updateStrokeBudgetRange(int maxLines) {
        if (strokeBudgetSlider != null) {
            strokeBudgetSlider.setMaximum(Math.max(1, maxLines));
            strokeBudgetSlider.setValue(maxLines);
            updateBudgetLabel(maxLines, maxLines);
        }
        penPanel.refresh();
    }

    private void updateBudgetLabel(int current, int max) {
        if (strokeBudgetLabel != null) {
            strokeBudgetLabel.setText(String.format("Showing %,d / %,d lines", current, max));
        }
    }

    /**
     * Builds user-friendly high-level controls (Drawing Strength & Style)
     * and collapsible Advanced Parameters for micro-tuning individual spinners.
     */
    private void updatePfmControls() {
        PathFindingModule pfm = getSelectedPfm();
        pfmDescText.setText(pfm.getDescription());
        pfmParamsPanel.removeAll();
        advancedSpinners.clear();

        // ==============================================================
        // CARD 1: USER-FRIENDLY DRAWING STYLE & STRENGTH (EASY ACCESS)
        // ==============================================================
        JPanel easyCard = new JPanel();
        easyCard.setLayout(new BoxLayout(easyCard, BoxLayout.Y_AXIS));
        easyCard.setOpaque(true);
        easyCard.setBackground(DrawbotTheme.BG_CARD);
        easyCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(10, 10, 10, 10)
        ));
        easyCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel easyTitle = new JLabel("Drawing Style & Strength");
        easyTitle.setFont(DrawbotTheme.FONT_TITLE);
        easyTitle.setForeground(DrawbotTheme.TEXT_PRIMARY);
        easyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        easyCard.add(easyTitle);

        JLabel easySubtitle = new JLabel("Fast controls to adjust drawing intensity and stroke density");
        easySubtitle.setFont(DrawbotTheme.FONT_SMALL);
        easySubtitle.setForeground(DrawbotTheme.TEXT_MUTED);
        easySubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        easyCard.add(easySubtitle);
        easyCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Quick Preset Buttons Bar
        JPanel presetBar = new JPanel(new GridLayout(1, 4, 4, 0));
        presetBar.setOpaque(false);
        presetBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        presetBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton lightBtn = DrawbotTheme.createSecondaryButton("Light");
        lightBtn.setFont(DrawbotTheme.FONT_SMALL);
        JButton balancedBtn = DrawbotTheme.createSecondaryButton("Balanced");
        balancedBtn.setFont(DrawbotTheme.FONT_SMALL);
        JButton denseBtn = DrawbotTheme.createSecondaryButton("Dense");
        denseBtn.setFont(DrawbotTheme.FONT_SMALL);
        JButton intenseBtn = DrawbotTheme.createSecondaryButton("Intense");
        intenseBtn.setFont(DrawbotTheme.FONT_SMALL);

        presetBar.add(lightBtn);
        presetBar.add(balancedBtn);
        presetBar.add(denseBtn);
        presetBar.add(intenseBtn);
        easyCard.add(presetBar);
        easyCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Drawing Strength Slider Row
        JLabel strengthValLbl = new JLabel(formatStrengthLabel(macroStrengthPct));
        easyCard.add(createSliderHeaderRow("Drawing Strength (Density):", strengthValLbl));

        JSlider strengthSlider = createFriendlySlider(10, 100, macroStrengthPct, val -> {
            macroStrengthPct = val;
            strengthValLbl.setText(formatStrengthLabel(val));
            applyMacroSettings(macroStrengthPct, macroDetailLevel);
        });
        easyCard.add(strengthSlider);
        easyCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Line Detail / Fineness Slider Row
        JLabel detailValLbl = new JLabel(formatDetailLabel(macroDetailLevel));
        easyCard.add(createSliderHeaderRow("Line Fineness / Detail:", detailValLbl));

        JSlider detailSlider = createFriendlySlider(1, 4, macroDetailLevel, val -> {
            macroDetailLevel = val;
            detailValLbl.setText(formatDetailLabel(val));
            applyMacroSettings(macroStrengthPct, macroDetailLevel);
        });
        easyCard.add(detailSlider);

        // Preset action handlers
        lightBtn.addActionListener(e -> {
            strengthSlider.setValue(25);
            detailSlider.setValue(2);
        });
        balancedBtn.addActionListener(e -> {
            strengthSlider.setValue(50);
            detailSlider.setValue(2);
        });
        denseBtn.addActionListener(e -> {
            strengthSlider.setValue(75);
            detailSlider.setValue(3);
        });
        intenseBtn.addActionListener(e -> {
            strengthSlider.setValue(100);
            detailSlider.setValue(3);
        });

        pfmParamsPanel.add(easyCard);
        pfmParamsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // ==============================================================
        // CARD 2: ADVANCED ALGORITHM PARAMETERS (COLLAPSIBLE POWER-USER)
        // ==============================================================
        JPanel advancedCard = new JPanel();
        advancedCard.setLayout(new BoxLayout(advancedCard, BoxLayout.Y_AXIS));
        advancedCard.setOpaque(true);
        advancedCard.setBackground(DrawbotTheme.BG_CARD);
        advancedCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 8, 10)
        ));
        advancedCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header bar with Expand/Collapse toggle button
        JPanel advHeader = new JPanel(new BorderLayout());
        advHeader.setOpaque(false);
        advHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        advHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel advTitle = new JLabel("Advanced Parameters");
        advTitle.setFont(DrawbotTheme.FONT_TITLE);
        advTitle.setForeground(DrawbotTheme.TEXT_PRIMARY);
        advHeader.add(advTitle, BorderLayout.WEST);

        JButton advToggleBtn = DrawbotTheme.createSecondaryButton(isAdvancedExpanded ? "Hide \u25B2" : "Show \u25BC");
        advToggleBtn.setPreferredSize(new Dimension(75, 26));
        advHeader.add(advToggleBtn, BorderLayout.EAST);
        advancedCard.add(advHeader);

        // Advanced rows container
        JPanel advRowsContainer = new JPanel();
        advRowsContainer.setLayout(new BoxLayout(advRowsContainer, BoxLayout.Y_AXIS));
        advRowsContainer.setOpaque(false);
        advRowsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        advRowsContainer.setVisible(isAdvancedExpanded);

        advRowsContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        JLabel advSub = new JLabel("Direct numeric micro-tuning for engine parameters:");
        advSub.setFont(DrawbotTheme.FONT_SMALL);
        advSub.setForeground(DrawbotTheme.TEXT_MUTED);
        advSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        advRowsContainer.add(advSub);
        advRowsContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        pfm.getParameters().forEach((key, val) -> {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            row.setPreferredSize(new Dimension(320, 34));
            row.setMinimumSize(new Dimension(280, 34));
            row.setBorder(new EmptyBorder(2, 2, 2, 2));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            String displayName = formatParameterName(key);
            JLabel lbl = new JLabel(displayName);
            lbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
            lbl.setFont(DrawbotTheme.FONT_REGULAR);
            lbl.setToolTipText("Internal parameter: " + key);
            row.add(lbl, BorderLayout.WEST);

            JSpinner spinner = createSpinnerForParam(key, val, pfm);
            advancedSpinners.put(key, spinner);
            row.add(spinner, BorderLayout.EAST);

            advRowsContainer.add(row);
            advRowsContainer.add(Box.createRigidArea(new Dimension(0, 2)));
        });

        advToggleBtn.addActionListener(e -> {
            isAdvancedExpanded = !isAdvancedExpanded;
            advRowsContainer.setVisible(isAdvancedExpanded);
            advToggleBtn.setText(isAdvancedExpanded ? "Hide \u25B2" : "Show \u25BC");
            pfmParamsPanel.revalidate();
            pfmParamsPanel.repaint();
        });

        advancedCard.add(advRowsContainer);
        pfmParamsPanel.add(advancedCard);

        // Synchronize initial macro settings to the newly selected algorithm
        applyMacroSettings(macroStrengthPct, macroDetailLevel);

        pfmParamsPanel.add(Box.createVerticalGlue());
        pfmParamsPanel.revalidate();
        pfmParamsPanel.repaint();
    }

    private void applyMacroSettings(int strengthPct, int detailLevel) {
        PathFindingModule pfm = getSelectedPfm();
        isSyncingMacro = true;
        try {
            String id = pfm.getId();
            if ("PFM_original".equals(id) || "PFM_darkest_ray".equals(id)) {
                int baseSquiggles = "PFM_darkest_ray".equals(id) ? 1200 : 1000;
                int squiggles = Math.round(baseSquiggles * (strengthPct / 50.0f));
                squiggles = Math.max(100, Math.min(4500, squiggles));

                int lighten = Math.round(9.0f * (50.0f / (float) strengthPct));
                lighten = Math.max(2, Math.min(30, lighten));

                int tests = detailLevel == 1 ? 2 : (detailLevel == 2 ? 4 : (detailLevel == 3 ? 8 : 12));
                if ("PFM_darkest_ray".equals(id)) tests *= 2;

                pfm.setParameter("squiggleLength", squiggles);
                pfm.setParameter("adjustBrightness", lighten);
                pfm.setParameter("tests", tests);
            } else if ("PFM_spiral".equals(id)) {
                float dist = 4.0f * (50.0f / (float) strengthPct);
                dist = Math.max(1.0f, Math.min(10.0f, dist));

                float amp = 3.0f * (strengthPct / 50.0f);
                amp = Math.max(1.0f, Math.min(8.0f, amp));

                float density = detailLevel == 1 ? 25.0f : (detailLevel == 2 ? 40.0f : (detailLevel == 3 ? 70.0f : 110.0f));

                pfm.setParameter("dist", dist);
                pfm.setParameter("ampScale", amp);
                pfm.setParameter("density", density);
            } else if ("PFM_waveform".equals(id)) {
                int spacing = Math.round(6.0f * (50.0f / (float) strengthPct));
                spacing = Math.max(2, Math.min(16, spacing));

                float amp = 18.0f * (strengthPct / 50.0f);
                amp = Math.max(4.0f, Math.min(40.0f, amp));

                int stepX = detailLevel == 1 ? 5 : (detailLevel == 2 ? 3 : (detailLevel == 3 ? 2 : 1));

                pfm.setParameter("lineSpacing", spacing);
                pfm.setParameter("amplitude", amp);
                pfm.setParameter("stepX", stepX);
            } else if ("PFM_crosshatch".equals(id)) {
                int pitch = Math.round(6.0f * (50.0f / (float) strengthPct));
                pitch = Math.max(2, Math.min(14, pitch));

                int layers = strengthPct <= 30 ? 1 : (strengthPct <= 65 ? 2 : (strengthPct <= 85 ? 3 : 4));
                int stepX = detailLevel == 1 ? 6 : (detailLevel == 2 ? 4 : (detailLevel == 3 ? 3 : 2));

                pfm.setParameter("pitch", pitch);
                pfm.setParameter("maxLayers", layers);
                pfm.setParameter("stepX", stepX);
            } else if ("PFM_concentric".equals(id)) {
                float pitch = 4.5f * (50.0f / (float) strengthPct);
                pitch = Math.max(1.5f, Math.min(12.0f, pitch));

                float amp = 10.0f * (strengthPct / 50.0f);
                amp = Math.max(4.0f, Math.min(25.0f, amp));

                float density = detailLevel == 1 ? 35.0f : (detailLevel == 2 ? 60.0f : (detailLevel == 3 ? 95.0f : 140.0f));

                pfm.setParameter("ringPitch", pitch);
                pfm.setParameter("amplitude", amp);
                pfm.setParameter("density", density);
            } else if ("PFM_squares".equals(id)) {
                int spacing = Math.round(8.0f * (50.0f / (float) strengthPct));
                spacing = Math.max(3, Math.min(18, spacing));

                int stepX = detailLevel == 1 ? 6 : (detailLevel == 2 ? 4 : (detailLevel == 3 ? 3 : 2));

                pfm.setParameter("lineSpacing", spacing);
                pfm.setParameter("stepX", stepX);
            }

            updateSpinnerValuesFromPfm(pfm);
        } finally {
            isSyncingMacro = false;
        }
    }

    private void updateSpinnerValuesFromPfm(PathFindingModule pfm) {
        pfm.getParameters().forEach((key, val) -> {
            JSpinner sp = advancedSpinners.get(key);
            if (sp != null) {
                if (val instanceof Float || val instanceof Double) {
                    sp.setValue(((Number) val).doubleValue());
                } else if (val instanceof Integer) {
                    sp.setValue(((Number) val).intValue());
                }
            }
        });
    }

    private static String formatStrengthLabel(int val) {
        if (val <= 30) return val + "% (Light)";
        if (val <= 65) return val + "% (Balanced)";
        if (val <= 85) return val + "% (Dense)";
        return val + "% (Intense)";
    }

    private static String formatDetailLabel(int level) {
        switch (level) {
            case 1: return "Coarse (Fast)";
            case 2: return "Medium (Standard)";
            case 3: return "Fine Detail";
            case 4: return "Ultra-Fine";
            default: return "Level " + level;
        }
    }

    private static JSlider createFriendlySlider(int min, int max, int initVal, java.util.function.Consumer<Integer> onChange) {
        JSlider slider = new JSlider(min, max, initVal);
        slider.setOpaque(false);
        slider.setForeground(DrawbotTheme.ACCENT_PRIMARY);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(e -> onChange.accept(slider.getValue()));
        return slider;
    }

    private static JPanel createSliderHeaderRow(String title, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
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

    private static String formatParameterName(String raw) {
        switch (raw) {
            case "squiggleLength": return "Squiggle Length";
            case "adjustBrightness": return "Lighten Step";
            case "desiredBrightness": return "Target Brightness";
            case "squigglesTillFirstChange": return "First Pen Change";
            case "tests": return "Sample Tests";
            case "dist": return "Track Pitch (Dist)";
            case "density": return "Point Density";
            case "ampScale": return "Amplitude Scale";
            case "maskBrightness": return "Paper Cutoff";
            case "lineLength": return "Step Length";
            case "lineSpacing": return "Scanline Pitch";
            case "amplitude": return "Wave Amplitude";
            case "stepX": return "Sample Step";
            case "cutoffBrightness": return "Threshold";
            case "pitch": return "Hatch Pitch";
            case "maxLayers": return "Hatch Passes";
            case "shadowThreshold": return "Shadow Cutoff";
            case "ringPitch": return "Ring Pitch";
            default: return raw;
        }
    }

    private JSpinner createSpinnerForParam(String key, Object val, PathFindingModule pfm) {
        SpinnerModel model;
        if (val instanceof Float || val instanceof Double) {
            float fVal = ((Number) val).floatValue();
            float step = 1.0f;
            float min = 0.0f;
            float max = 1000.0f;

            if ("dist".equals(key)) { min = 0.5f; max = 50.0f; step = 0.5f; }
            else if ("density".equals(key)) { min = 1.0f; max = 500.0f; step = 1.0f; }
            else if ("ampScale".equals(key)) { min = 0.1f; max = 20.0f; step = 0.1f; }
            else if ("amplitude".equals(key)) { min = 1.0f; max = 80.0f; step = 1.0f; }
            else if ("ringPitch".equals(key)) { min = 1.0f; max = 50.0f; step = 0.5f; }
            else if ("shadowThreshold".equals(key)) { min = 10.0f; max = 255.0f; step = 5.0f; }
            else if ("cutoffBrightness".equals(key)) { min = 50.0f; max = 255.0f; step = 1.0f; }
            else if ("desiredBrightness".equals(key)) { min = 10.0f; max = 255.0f; step = 1.0f; }

            // Ensure min <= fVal <= max is strictly guaranteed
            if (fVal < min) fVal = min;
            if (fVal > max) max = fVal + 10.0f;

            model = new SpinnerNumberModel((double) fVal, (double) min, (double) max, (double) step);
        } else {
            int iVal = ((Number) val).intValue();
            int min = 1;
            int max = 10000;
            int step = 1;

            if ("squiggleLength".equals(key)) { min = 10; max = 5000; step = 10; }
            else if ("adjustBrightness".equals(key)) { min = 1; max = 50; step = 1; }
            else if ("squigglesTillFirstChange".equals(key)) { min = 1; max = 1000; step = 5; }
            else if ("tests".equals(key)) { min = 1; max = 50; step = 1; }
            else if ("maskBrightness".equals(key)) { min = 0; max = 255; step = 1; }
            else if ("lineLength".equals(key)) { min = 2; max = 100; step = 1; }
            else if ("lineSpacing".equals(key)) { min = 2; max = 100; step = 1; }
            else if ("stepX".equals(key)) { min = 1; max = 50; step = 1; }
            else if ("pitch".equals(key)) { min = 2; max = 50; step = 1; }
            else if ("maxLayers".equals(key)) { min = 1; max = 4; step = 1; }

            // Ensure min <= iVal <= max is strictly guaranteed
            if (iVal < min) iVal = min;
            if (iVal > max) max = iVal + 10;

            model = new SpinnerNumberModel(iVal, min, max, step);
        }

        JSpinner spinner = DrawbotTheme.createCompactSpinner(model, 85);
        spinner.addChangeListener((ChangeEvent e) -> {
            if (isSyncingMacro) return;
            Object currentVal = spinner.getValue();
            if (val instanceof Float || val instanceof Double) {
                pfm.setParameter(key, ((Number) currentVal).floatValue());
            } else {
                pfm.setParameter(key, ((Number) currentVal).intValue());
            }
        });
        return spinner;
    }

    private JPanel buildMachineTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(DrawbotTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Machine & Canvas Geometry");
        title.setFont(DrawbotTheme.FONT_TITLE);
        title.setForeground(DrawbotTheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title);

        addSpinnerRow(card, "Paper Width (mm):", machineConfig.getPaperWidthMm(), 10.0f, 2000.0f, 1.0f,
                      v -> machineConfig.setPaperWidthMm(v));
        addSpinnerRow(card, "Paper Height (mm):", machineConfig.getPaperHeightMm(), 10.0f, 2000.0f, 1.0f,
                      v -> machineConfig.setPaperHeightMm(v));
        addSpinnerRow(card, "Image Width (mm):", machineConfig.getImageWidthMm(), 10.0f, 2000.0f, 1.0f,
                      v -> machineConfig.setImageWidthMm(v));
        addSpinnerRow(card, "Image Height (mm):", machineConfig.getImageHeightMm(), 10.0f, 2000.0f, 1.0f,
                      v -> machineConfig.setImageHeightMm(v));
        addSpinnerRow(card, "Pen Width (mm):", machineConfig.getPenWidthMm(), 0.05f, 10.0f, 0.05f,
                      v -> machineConfig.setPenWidthMm(v));
        addSpinnerRow(card, "Origin Offset (mm):", machineConfig.getPaperTopToOriginMm(), 0.0f, 500.0f, 1.0f,
                      v -> machineConfig.setPaperTopToOriginMm(v));

        card.add(Box.createVerticalGlue());
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private void addSpinnerRow(JPanel container, String label, float initVal, float min, float max, float step,
                               java.util.function.Consumer<Float> setter) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setPreferredSize(new Dimension(320, 34));
        row.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel l = new JLabel(label);
        l.setForeground(DrawbotTheme.TEXT_PRIMARY);
        l.setFont(DrawbotTheme.FONT_REGULAR);
        row.add(l, BorderLayout.WEST);

        SpinnerNumberModel model = new SpinnerNumberModel((double) initVal, (double) min, (double) max, (double) step);
        JSpinner spinner = DrawbotTheme.createCompactSpinner(model, 85);
        spinner.addChangeListener(e -> {
            setter.accept(((Number) spinner.getValue()).floatValue());
            if (onConfigChanged != null) onConfigChanged.run();
        });
        row.add(spinner, BorderLayout.EAST);

        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JPanel buildStrokeBudgetTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(DrawbotTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel topBox = new JPanel(new GridLayout(2, 1, 0, 4));
        topBox.setOpaque(false);
        JLabel title = new JLabel("Stroke Budget & Line Limiter");
        title.setFont(DrawbotTheme.FONT_TITLE);
        title.setForeground(DrawbotTheme.TEXT_PRIMARY);
        topBox.add(title);

        strokeBudgetLabel = new JLabel("Showing 0 / 0 lines");
        strokeBudgetLabel.setFont(DrawbotTheme.FONT_MONO);
        strokeBudgetLabel.setForeground(DrawbotTheme.TEXT_SECONDARY);
        topBox.add(strokeBudgetLabel);
        card.add(topBox, BorderLayout.NORTH);

        strokeBudgetSlider = new JSlider(0, 1000, 1000);
        strokeBudgetSlider.setOpaque(false);
        strokeBudgetSlider.setForeground(DrawbotTheme.TEXT_MUTED);
        strokeBudgetSlider.addChangeListener(e -> {
            int val = strokeBudgetSlider.getValue();
            updateBudgetLabel(val, strokeBudgetSlider.getMaximum());
            if (onConfigChanged != null) onConfigChanged.run();
        });
        card.add(strokeBudgetSlider, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setOpaque(false);
        JButton lessBtn = DrawbotTheme.createSecondaryButton("-10,000 Lines");
        JButton moreBtn = DrawbotTheme.createSecondaryButton("+10,000 Lines");
        lessBtn.addActionListener(e -> strokeBudgetSlider.setValue(Math.max(0, strokeBudgetSlider.getValue() - 10000)));
        moreBtn.addActionListener(e -> strokeBudgetSlider.setValue(Math.min(strokeBudgetSlider.getMaximum(), strokeBudgetSlider.getValue() + 10000)));
        btnPanel.add(lessBtn);
        btnPanel.add(moreBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    public int getDisplayedStrokeLimit() {
        return strokeBudgetSlider != null ? strokeBudgetSlider.getValue() : Integer.MAX_VALUE;
    }

    public void updateThemeStyles() {
        setBackground(DrawbotTheme.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, DrawbotTheme.BORDER_SUBTLE));
        DrawbotTheme.styleTabbedPane(tabbedPane);
        updateComponentColors(this);
        revalidate();
        repaint();
    }

    private void updateComponentColors(Container container) {
        if (container instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) container;
            for (int i = 0; i < tp.getTabCount(); i++) {
                Component tabComp = tp.getComponentAt(i);
                if (tabComp instanceof Container) {
                    updateComponentColors((Container) tabComp);
                }
            }
        }
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                comp.setForeground(DrawbotTheme.TEXT_PRIMARY);
            } else if (comp instanceof JTextArea) {
                comp.setForeground(DrawbotTheme.TEXT_SECONDARY);
            } else if (comp instanceof JPanel) {
                if (comp.isOpaque() && comp != this) {
                    comp.setBackground(DrawbotTheme.BG_CARD);
                }
                updateComponentColors((Container) comp);
            }
        }
    }
}
