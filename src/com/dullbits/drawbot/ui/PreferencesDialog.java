package com.dullbits.drawbot.ui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dedicated Liquid Glass Preferences and UI Customization modal dialog.
 * Enables live theme switching, accent palette customization, glass opacity tuning,
 * canvas background selection, workspace layout presets, and accessibility controls.
 */
public final class PreferencesDialog extends JDialog {

    private final DrawbotPreferences prefs;
    private final Runnable onPreferencesUpdated;

    private JTabbedPane tabbedPane;
    private JPanel livePreviewCard;
    private JLabel previewTitle;
    private JLabel previewSubtitle;
    private JButton previewAccentBtn;

    // Appearance controls
    private final ButtonGroup themeGroup = new ButtonGroup();
    private final JPanel accentPalettePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    private JSlider opacitySlider;
    private JLabel opacityValLbl;
    private JSlider specularSlider;
    private JLabel specularValLbl;
    private JSlider radiusSlider;
    private JLabel radiusValLbl;

    // Canvas & Workspace controls
    private JComboBox<DrawbotPreferences.CanvasBackground> canvasBgCombo;
    private JComboBox<DrawbotPreferences.GridColorPreset> gridColorCombo;
    private JCheckBox gridEnabledCheck;
    private JComboBox<DrawbotPreferences.ToolbarMode> toolbarModeCombo;
    private JComboBox<DrawbotPreferences.WorkspaceProfile> workspaceProfileCombo;

    // Accessibility controls
    private JCheckBox reducedMotionCheck;
    private JCheckBox reducedTransparencyCheck;
    private JCheckBox highReadabilityCheck;

    public PreferencesDialog(Frame owner, Runnable onPreferencesUpdated) {
        super(owner, "Drawbot Studio Preferences", true);
        this.prefs = DrawbotPreferences.getInstance();
        this.onPreferencesUpdated = onPreferencesUpdated;

        setSize(760, 600);
        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);
        setLayout(new BorderLayout());

        // Header Panel with Specular Refraction Luster
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                DrawbotTheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();

                LinearGradientPaint luster = new LinearGradientPaint(
                    0, 0, 0, h,
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(255, 255, 255, 18), new Color(255, 255, 255, 0)}
                );
                g2.setPaint(luster);
                g2.fillRect(0, 0, w, h);

                g2.setColor(DrawbotTheme.BORDER_GLASS_BOTTOM);
                g2.drawLine(0, h - 2, w, h - 2);
                g2.setColor(DrawbotTheme.BORDER_SUBTLE);
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        headerPanel.setBackground(DrawbotTheme.BG_TOOLBAR);
        headerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titleLbl = new JLabel("Preferences & Customization");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);

        JLabel subtitleLbl = new JLabel("Personalize appearance, materials, canvas environment, and accessibility");
        subtitleLbl.setFont(DrawbotTheme.FONT_REGULAR);
        subtitleLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subtitleLbl, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Tabs
        tabbedPane = new JTabbedPane();
        DrawbotTheme.styleTabbedPane(tabbedPane);
        tabbedPane.setBorder(new EmptyBorder(12, 16, 8, 16));

        tabbedPane.addTab("Appearance & Themes", buildAppearanceTab());
        tabbedPane.addTab("Canvas & Workspace", buildCanvasTab());
        tabbedPane.addTab("Accessibility & Motion", buildAccessibilityTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(DrawbotTheme.BG_STATUS);
        bottomBar.setBorder(new EmptyBorder(12, 20, 14, 20));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton resetTabBtn = DrawbotTheme.createSecondaryButton("Reset Tab Defaults");
        resetTabBtn.setToolTipText("Reset settings in the currently active tab to default values");
        resetTabBtn.addActionListener(e -> resetActiveTab());

        JButton resetAllBtn = DrawbotTheme.createSecondaryButton("Reset All Preferences...");
        resetAllBtn.setToolTipText("Reset all interface preferences to factory defaults");
        resetAllBtn.addActionListener(e -> confirmResetAll());

        leftActions.add(resetTabBtn);
        leftActions.add(resetAllBtn);

        JButton closeBtn = DrawbotTheme.createPrimaryButton("Close", DrawbotTheme.ACCENT_PRIMARY);
        closeBtn.addActionListener(e -> dispose());

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(closeBtn, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        syncControlsFromPreferences();
    }

    private JPanel buildAppearanceTab() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Theme Presets Card
        JPanel themeCard = createStyledSectionCard("Interface Theme Preset");
        themeCard.setLayout(new GridLayout(2, 2, 10, 10));

        for (DrawbotPreferences.ThemePreset tp : DrawbotPreferences.ThemePreset.values()) {
            JToggleButton btn = createThemePill(tp);
            themeCard.add(btn);
        }
        tab.add(themeCard);
        tab.add(Box.createVerticalStrut(12));

        // 2. Accent Color Card
        JPanel accentCard = createStyledSectionCard("Accent & Focus Tint");
        accentCard.setLayout(new BorderLayout());

        accentPalettePanel.setOpaque(false);
        buildAccentPalette();
        accentCard.add(accentPalettePanel, BorderLayout.CENTER);

        tab.add(accentCard);
        tab.add(Box.createVerticalStrut(12));

        // 3. Materials & Glass Sliders Card
        JPanel glassCard = createStyledSectionCard("Liquid Glass Materials & Edge Curvature");
        glassCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Opacity
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel opLbl = new JLabel("Glass Opacity:");
        opLbl.setFont(DrawbotTheme.FONT_BOLD);
        opLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        glassCard.add(opLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.60;
        opacitySlider = new JSlider(40, 100, (int)(prefs.getGlassOpacity() * 100));
        opacitySlider.setOpaque(false);
        opacitySlider.addChangeListener(e -> {
            float val = opacitySlider.getValue() / 100.0f;
            opacityValLbl.setText(opacitySlider.getValue() + "%");
            prefs.setGlassOpacity(val);
            notifyUpdate();
        });
        glassCard.add(opacitySlider, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        opacityValLbl = new JLabel((int)(prefs.getGlassOpacity() * 100) + "%");
        opacityValLbl.setFont(DrawbotTheme.FONT_MONO);
        opacityValLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        glassCard.add(opacityValLbl, gbc);

        // Specular Luster
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.25;
        JLabel specLbl = new JLabel("Specular Sheen:");
        specLbl.setFont(DrawbotTheme.FONT_BOLD);
        specLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        glassCard.add(specLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.60;
        specularSlider = new JSlider(0, 100, (int)(prefs.getSpecularIntensity() * 100));
        specularSlider.setOpaque(false);
        specularSlider.addChangeListener(e -> {
            float val = specularSlider.getValue() / 100.0f;
            specularValLbl.setText(specularSlider.getValue() + "%");
            prefs.setSpecularIntensity(val);
            notifyUpdate();
        });
        glassCard.add(specularSlider, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        specularValLbl = new JLabel((int)(prefs.getSpecularIntensity() * 100) + "%");
        specularValLbl.setFont(DrawbotTheme.FONT_MONO);
        specularValLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        glassCard.add(specularValLbl, gbc);

        // Corner Radius
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        JLabel radLbl = new JLabel("Corner Radius:");
        radLbl.setFont(DrawbotTheme.FONT_BOLD);
        radLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        glassCard.add(radLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.60;
        radiusSlider = new JSlider(4, 14, prefs.getCornerRadius());
        radiusSlider.setOpaque(false);
        radiusSlider.addChangeListener(e -> {
            int val = radiusSlider.getValue();
            radiusValLbl.setText(val + " px");
            prefs.setCornerRadius(val);
            notifyUpdate();
        });
        glassCard.add(radiusSlider, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        radiusValLbl = new JLabel(prefs.getCornerRadius() + " px");
        radiusValLbl.setFont(DrawbotTheme.FONT_MONO);
        radiusValLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        glassCard.add(radiusValLbl, gbc);

        tab.add(glassCard);
        tab.add(Box.createVerticalStrut(12));

        // 4. Live Preview Card
        livePreviewCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                DrawbotTheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();

                // Background base
                g2.setColor(DrawbotTheme.BG_CARD);
                g2.fillRoundRect(0, 0, w, h, DrawbotTheme.CORNER_RADIUS_CARD, DrawbotTheme.CORNER_RADIUS_CARD);

                // Top Specular Luster
                LinearGradientPaint luster = new LinearGradientPaint(
                    0, 0, 0, Math.min(30, h),
                    new float[]{0.0f, 1.0f},
                    new Color[]{DrawbotTheme.SPECULAR_HIGHLIGHT, new Color(255, 255, 255, 0)}
                );
                g2.setPaint(luster);
                g2.fillRoundRect(1, 1, w - 2, Math.min(30, h), DrawbotTheme.CORNER_RADIUS_CARD, DrawbotTheme.CORNER_RADIUS_CARD);

                // Glass Rim
                g2.setColor(DrawbotTheme.BORDER_GLASS_TOP);
                g2.drawRoundRect(0, 0, w - 1, h - 1, DrawbotTheme.CORNER_RADIUS_CARD, DrawbotTheme.CORNER_RADIUS_CARD);
                g2.dispose();
            }
        };
        livePreviewCard.setOpaque(false);
        livePreviewCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        previewTitle = new JLabel("Live Theme Preview Surface");
        previewTitle.setFont(DrawbotTheme.FONT_TITLE);
        previewTitle.setForeground(DrawbotTheme.TEXT_PRIMARY);

        previewSubtitle = new JLabel("Buttons, glass borders, and fonts update dynamically across the entire app");
        previewSubtitle.setFont(DrawbotTheme.FONT_SMALL);
        previewSubtitle.setForeground(DrawbotTheme.TEXT_MUTED);

        JPanel previewTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        previewTextPanel.setOpaque(false);
        previewTextPanel.add(previewTitle);
        previewTextPanel.add(previewSubtitle);

        previewAccentBtn = DrawbotTheme.createPrimaryButton("Active Accent Action", DrawbotTheme.ACCENT_PRIMARY);
        previewAccentBtn.setIcon(DrawbotTheme.createPlayIcon(10, Color.WHITE));

        livePreviewCard.add(previewTextPanel, BorderLayout.CENTER);
        livePreviewCard.add(previewAccentBtn, BorderLayout.EAST);

        tab.add(livePreviewCard);
        return tab;
    }

    private JPanel buildCanvasTab() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Canvas Environment Card
        JPanel canvasCard = createStyledSectionCard("Canvas Viewport & Grid Guides");
        canvasCard.setLayout(new GridLayout(3, 2, 12, 12));

        JLabel bgLbl = new JLabel("Canvas Background Tone:");
        bgLbl.setFont(DrawbotTheme.FONT_BOLD);
        bgLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        canvasBgCombo = new JComboBox<>(DrawbotPreferences.CanvasBackground.values());
        DrawbotTheme.styleComboBox(canvasBgCombo, 220, 32);
        canvasBgCombo.addActionListener(e -> {
            prefs.setCanvasBackground((DrawbotPreferences.CanvasBackground) canvasBgCombo.getSelectedItem());
            notifyUpdate();
        });
        canvasCard.add(bgLbl);
        canvasCard.add(canvasBgCombo);

        JLabel gridLbl = new JLabel("Machine Grid Guides:");
        gridLbl.setFont(DrawbotTheme.FONT_BOLD);
        gridLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        gridEnabledCheck = new JCheckBox("Show machine boundary grid and origin crosshair by default");
        gridEnabledCheck.setFont(DrawbotTheme.FONT_REGULAR);
        gridEnabledCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        gridEnabledCheck.setOpaque(false);
        gridEnabledCheck.addActionListener(e -> {
            prefs.setGridEnabled(gridEnabledCheck.isSelected());
            notifyUpdate();
        });
        canvasCard.add(gridLbl);
        canvasCard.add(gridEnabledCheck);

        JLabel gridColorLbl = new JLabel("Grid Guide Color:");
        gridColorLbl.setFont(DrawbotTheme.FONT_BOLD);
        gridColorLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        gridColorCombo = new JComboBox<>(DrawbotPreferences.GridColorPreset.values());
        DrawbotTheme.styleComboBox(gridColorCombo, 220, 32);
        gridColorCombo.addActionListener(e -> {
            prefs.setGridColor((DrawbotPreferences.GridColorPreset) gridColorCombo.getSelectedItem());
            notifyUpdate();
        });
        canvasCard.add(gridColorLbl);
        canvasCard.add(gridColorCombo);

        tab.add(canvasCard);
        tab.add(Box.createVerticalStrut(12));

        // 2. Workspace & Toolbar Density Card
        JPanel wsCard = createStyledSectionCard("Workspace Layout & Command Bar Density");
        wsCard.setLayout(new GridLayout(2, 2, 12, 12));

        JLabel tbLbl = new JLabel("Toolbar Button Mode:");
        tbLbl.setFont(DrawbotTheme.FONT_BOLD);
        tbLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        toolbarModeCombo = new JComboBox<>(DrawbotPreferences.ToolbarMode.values());
        DrawbotTheme.styleComboBox(toolbarModeCombo, 220, 32);
        toolbarModeCombo.addActionListener(e -> {
            prefs.setToolbarMode((DrawbotPreferences.ToolbarMode) toolbarModeCombo.getSelectedItem());
            notifyUpdate();
        });
        wsCard.add(tbLbl);
        wsCard.add(toolbarModeCombo);

        JLabel wsLbl = new JLabel("Startup Workspace Profile:");
        wsLbl.setFont(DrawbotTheme.FONT_BOLD);
        wsLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        workspaceProfileCombo = new JComboBox<>(DrawbotPreferences.WorkspaceProfile.values());
        DrawbotTheme.styleComboBox(workspaceProfileCombo, 220, 32);
        workspaceProfileCombo.addActionListener(e -> {
            prefs.setWorkspaceProfile((DrawbotPreferences.WorkspaceProfile) workspaceProfileCombo.getSelectedItem());
            notifyUpdate();
        });
        wsCard.add(wsLbl);
        wsCard.add(workspaceProfileCombo);

        tab.add(wsCard);
        tab.add(Box.createVerticalGlue());
        return tab;
    }

    private JPanel buildAccessibilityTab() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel a11yCard = createStyledSectionCard("Accessibility & Performance Modes");
        a11yCard.setLayout(new GridLayout(3, 1, 10, 10));

        reducedMotionCheck = new JCheckBox("Reduced Motion Mode (Pause animated spinners, HUD rotations, and pulses)");
        reducedMotionCheck.setFont(DrawbotTheme.FONT_REGULAR);
        reducedMotionCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        reducedMotionCheck.setOpaque(false);
        reducedMotionCheck.addActionListener(e -> {
            prefs.setReducedMotion(reducedMotionCheck.isSelected());
            notifyUpdate();
        });
        a11yCard.add(reducedMotionCheck);

        reducedTransparencyCheck = new JCheckBox("Reduced Transparency (Solid 100% opaque surfaces for maximum readability)");
        reducedTransparencyCheck.setFont(DrawbotTheme.FONT_REGULAR);
        reducedTransparencyCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        reducedTransparencyCheck.setOpaque(false);
        reducedTransparencyCheck.addActionListener(e -> {
            prefs.setReducedTransparency(reducedTransparencyCheck.isSelected());
            notifyUpdate();
        });
        a11yCard.add(reducedTransparencyCheck);

        highReadabilityCheck = new JCheckBox("High Contrast Text & Strong Borders (WCAG 2.1 AAA contrast guidelines)");
        highReadabilityCheck.setFont(DrawbotTheme.FONT_REGULAR);
        highReadabilityCheck.setForeground(DrawbotTheme.TEXT_PRIMARY);
        highReadabilityCheck.setOpaque(false);
        highReadabilityCheck.addActionListener(e -> {
            prefs.setHighReadability(highReadabilityCheck.isSelected());
            notifyUpdate();
        });
        a11yCard.add(highReadabilityCheck);

        tab.add(a11yCard);
        tab.add(Box.createVerticalGlue());
        return tab;
    }

    private void buildAccentPalette() {
        accentPalettePanel.removeAll();
        for (DrawbotPreferences.AccentColorPreset ac : DrawbotPreferences.AccentColorPreset.values()) {
            boolean isSelected = (prefs.getAccentColor() == ac);
            JButton pill = new JButton(ac.getDisplayName()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    DrawbotTheme.applyQualityRendering(g2);
                    int w = getWidth();
                    int h = getHeight();

                    // Pill background
                    g2.setColor(ac.getColor());
                    g2.fillRoundRect(0, 0, w, h, 14, 14);

                    // Selection ring
                    if (prefs.getAccentColor() == ac) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setFont(DrawbotTheme.FONT_BOLD);
            pill.setForeground(Color.WHITE);
            pill.setContentAreaFilled(false);
            pill.setBorderPainted(false);
            pill.setFocusPainted(false);
            pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pill.setPreferredSize(new Dimension(140, 30));
            pill.addActionListener(e -> {
                prefs.setAccentColor(ac);
                buildAccentPalette();
                notifyUpdate();
            });
            accentPalettePanel.add(pill);
        }
        accentPalettePanel.revalidate();
        accentPalettePanel.repaint();
    }

    private JToggleButton createThemePill(DrawbotPreferences.ThemePreset tp) {
        JToggleButton btn = new JToggleButton("<html><b>" + tp.getDisplayName() + "</b><br><font size='2' color='#9CA3AF'>" + tp.getDescription() + "</font></html>");
        themeGroup.add(btn);
        btn.setFont(DrawbotTheme.FONT_REGULAR);
        btn.setForeground(DrawbotTheme.TEXT_PRIMARY);
        btn.setBackground(DrawbotTheme.BG_CARD_SUBTLE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(prefs.getTheme() == tp ? DrawbotTheme.BORDER_FOCUS : DrawbotTheme.BORDER_SUBTLE, prefs.getTheme() == tp ? 2 : 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        btn.setSelected(prefs.getTheme() == tp);
        btn.addActionListener(e -> {
            prefs.setTheme(tp);
            notifyUpdate();
        });
        return btn;
    }

    private static JPanel createStyledSectionCard(String title) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(),
                    " " + title + " ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    DrawbotTheme.FONT_HEADER,
                    DrawbotTheme.TEXT_PRIMARY
                ),
                new EmptyBorder(6, 12, 10, 12)
            )
        ));
        return card;
    }

    private void syncControlsFromPreferences() {
        if (opacitySlider != null) opacitySlider.setValue((int)(prefs.getGlassOpacity() * 100));
        if (opacityValLbl != null) opacityValLbl.setText((int)(prefs.getGlassOpacity() * 100) + "%");
        if (specularSlider != null) specularSlider.setValue((int)(prefs.getSpecularIntensity() * 100));
        if (specularValLbl != null) specularValLbl.setText((int)(prefs.getSpecularIntensity() * 100) + "%");
        if (radiusSlider != null) radiusSlider.setValue(prefs.getCornerRadius());
        if (radiusValLbl != null) radiusValLbl.setText(prefs.getCornerRadius() + " px");

        if (canvasBgCombo != null) canvasBgCombo.setSelectedItem(prefs.getCanvasBackground());
        if (gridEnabledCheck != null) gridEnabledCheck.setSelected(prefs.isGridEnabled());
        if (gridColorCombo != null) gridColorCombo.setSelectedItem(prefs.getGridColor());
        if (toolbarModeCombo != null) toolbarModeCombo.setSelectedItem(prefs.getToolbarMode());
        if (workspaceProfileCombo != null) workspaceProfileCombo.setSelectedItem(prefs.getWorkspaceProfile());

        if (reducedMotionCheck != null) reducedMotionCheck.setSelected(prefs.isReducedMotion());
        if (reducedTransparencyCheck != null) reducedTransparencyCheck.setSelected(prefs.isReducedTransparency());
        if (highReadabilityCheck != null) highReadabilityCheck.setSelected(prefs.isHighReadability());

        buildAccentPalette();
    }

    private void notifyUpdate() {
        DrawbotTheme.applyPreferences(prefs);
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);
        if (livePreviewCard != null) {
            previewTitle.setForeground(DrawbotTheme.TEXT_PRIMARY);
            previewSubtitle.setForeground(DrawbotTheme.TEXT_MUTED);
            previewAccentBtn.setBackground(DrawbotTheme.ACCENT_PRIMARY);
            livePreviewCard.repaint();
        }
        if (onPreferencesUpdated != null) {
            onPreferencesUpdated.run();
        }
        repaint();
    }

    private void resetActiveTab() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx == 0) {
            prefs.resetAppearance();
        } else if (idx == 1) {
            prefs.resetCanvas();
        } else if (idx == 2) {
            prefs.resetAccessibility();
        }
        syncControlsFromPreferences();
        notifyUpdate();
    }

    private void confirmResetAll() {
        int res = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all interface and workspace preferences to default values?",
            "Reset All Preferences",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (res == JOptionPane.YES_OPTION) {
            prefs.resetAll();
            syncControlsFromPreferences();
            notifyUpdate();
        }
    }
}
