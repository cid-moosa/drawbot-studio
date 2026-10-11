package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.color.CopicPalette;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.SegmentBuffer;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Graphical pen and palette management panel with high-contrast controls,
 * color swatches, allocation indicators, and balanced spacing.
 */
public final class PenDistributionPanel extends JPanel {

    private final PenDistribution penDistribution;
    private final SegmentBuffer segmentBuffer;
    private final Runnable onDistributionChanged;

    private JComboBox<String> presetCombo;
    private final JPanel pensContainer;

    public PenDistributionPanel(PenDistribution distribution, SegmentBuffer buffer, Runnable onChange) {
        this.penDistribution = distribution;
        this.segmentBuffer = buffer;
        this.onDistributionChanged = onChange;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setOpaque(false);

        // 1. Preset Selector Card
        JPanel topCard = new JPanel(new BorderLayout(0, 6));
        topCard.setOpaque(true);
        topCard.setBackground(DrawbotTheme.BG_CARD);
        topCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 10, 10)
        ));

        JLabel presetLbl = new JLabel("Palette & Marker Preset");
        presetLbl.setFont(DrawbotTheme.FONT_TITLE);
        presetLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        topCard.add(presetLbl, BorderLayout.NORTH);

        String[] presetNames = new String[CopicPalette.PRESETS.size()];
        for (int i = 0; i < presetNames.length; i++) {
            presetNames[i] = CopicPalette.PRESETS.get(i).name;
        }
        presetCombo = new JComboBox<>(presetNames);
        DrawbotTheme.styleComboBox(presetCombo, 320, 30);
        presetCombo.setSelectedIndex(penDistribution.getCurrentPresetIndex());
        presetCombo.addActionListener(e -> {
            penDistribution.setCurrentPresetIndex(presetCombo.getSelectedIndex());
            rebuildPenList();
            notifyChange();
        });
        topCard.add(presetCombo, BorderLayout.CENTER);
        add(topCard, BorderLayout.NORTH);

        // 2. Pens List Container
        pensContainer = new JPanel();
        pensContainer.setLayout(new BoxLayout(pensContainer, BoxLayout.Y_AXIS));
        pensContainer.setOpaque(true);
        pensContainer.setBackground(DrawbotTheme.BG_CARD);
        pensContainer.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 8, 8, 8)
        ));
        rebuildPenList();

        JScrollPane scroll = new JScrollPane(pensContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // 3. Quick Action Buttons
        JPanel actionsCard = new JPanel(new GridLayout(2, 2, 6, 6));
        actionsCard.setOpaque(true);
        actionsCard.setBackground(DrawbotTheme.BG_CARD);
        actionsCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 8, 8, 8)
        ));

        JButton evenBtn = DrawbotTheme.createSecondaryButton("Even (1/N)");
        evenBtn.addActionListener(e -> {
            penDistribution.setEvenDistribution(segmentBuffer.getLineCount());
            rebuildPenList();
            notifyChange();
        });

        JButton monoBtn = DrawbotTheme.createSecondaryButton("Monochrome");
        monoBtn.addActionListener(e -> {
            penDistribution.setMonochromeDistribution(segmentBuffer.getLineCount());
            rebuildPenList();
            notifyChange();
        });

        JButton lightenBtn = DrawbotTheme.createSecondaryButton("Lighten");
        lightenBtn.addActionListener(e -> {
            penDistribution.lighten();
            penDistribution.normalize(segmentBuffer.getLineCount());
            rebuildPenList();
            notifyChange();
        });

        JButton darkenBtn = DrawbotTheme.createSecondaryButton("Darken");
        darkenBtn.addActionListener(e -> {
            penDistribution.darken();
            penDistribution.normalize(segmentBuffer.getLineCount());
            rebuildPenList();
            notifyChange();
        });

        actionsCard.add(evenBtn);
        actionsCard.add(monoBtn);
        actionsCard.add(lightenBtn);
        actionsCard.add(darkenBtn);
        add(actionsCard, BorderLayout.SOUTH);
    }

    private void rebuildPenList() {
        pensContainer.removeAll();
        int penCount = penDistribution.getPenCount();
        int totalLines = Math.max(1, segmentBuffer.getLineCount());

        JLabel header = new JLabel("Configured Pen Slots");
        header.setFont(DrawbotTheme.FONT_TITLE);
        header.setForeground(DrawbotTheme.TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(0, 4, 8, 4));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        pensContainer.add(header);

        for (int p = 0; p < penCount; p++) {
            final int penIndex = p;
            String code = penDistribution.getMarkerCodeForPen(p);
            Color c = CopicPalette.getOriginalColor(code);
            float pct = penDistribution.getPercentage(p);
            int allocated = (int) Math.round((pct / 100.0) * totalLines);

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            row.setPreferredSize(new Dimension(320, 38));
            row.setBorder(new EmptyBorder(3, 4, 3, 4));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Left: Swatch + Pen identifier
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            left.setOpaque(false);

            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.setColor(DrawbotTheme.BORDER_STRONG);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                    g2.dispose();
                }
            };
            swatch.setPreferredSize(new Dimension(20, 20));
            swatch.setOpaque(false);
            left.add(swatch);

            JLabel penLbl = new JLabel(String.format("Pen %d (%s)", penIndex, code));
            penLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
            penLbl.setFont(DrawbotTheme.FONT_BOLD);
            left.add(penLbl);
            row.add(left, BorderLayout.WEST);

            // Center: Percentage label
            JLabel pctLbl = new JLabel(String.format("%4.1f%% (%d)", pct, allocated));
            pctLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
            pctLbl.setFont(DrawbotTheme.FONT_MONO);
            pctLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(pctLbl, BorderLayout.CENTER);

            // Right: Increment / Decrement mini-buttons
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
            right.setOpaque(false);

            JButton plusBtn = createMiniButton("+");
            plusBtn.addActionListener(e -> {
                penDistribution.adjustPen(penIndex, 1.10f);
                penDistribution.normalize(segmentBuffer.getLineCount());
                rebuildPenList();
                notifyChange();
            });

            JButton minusBtn = createMiniButton("-");
            minusBtn.addActionListener(e -> {
                penDistribution.adjustPen(penIndex, 0.90f);
                penDistribution.normalize(segmentBuffer.getLineCount());
                rebuildPenList();
                notifyChange();
            });

            right.add(minusBtn);
            right.add(plusBtn);
            row.add(right, BorderLayout.EAST);

            pensContainer.add(row);
            pensContainer.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        pensContainer.add(Box.createVerticalGlue());
        pensContainer.revalidate();
        pensContainer.repaint();
    }

    private JButton createMiniButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(70, 75, 85) :
                          (getModel().isRollover() ? new Color(60, 64, 72) : new Color(48, 51, 58));
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(DrawbotTheme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.setColor(DrawbotTheme.TEXT_PRIMARY);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 22));
        btn.setFont(DrawbotTheme.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void notifyChange() {
        if (segmentBuffer.getLineCount() > 0) {
            segmentBuffer.distributePenChanges(segmentBuffer.getLineCount(),
                penDistribution.getDistributionCopy(), penDistribution.getPenCount());
        }
        if (onDistributionChanged != null) {
            onDistributionChanged.run();
        }
    }

    public void refresh() {
        rebuildPenList();
    }
}
