package com.dullbits.drawbot.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Real-time status, progress tracking, and worker cancellation bar
 * with clear accessible contrast, animated processing spinner, and pulsing loading indicators.
 */
public final class StatusBarPanel extends JPanel {

    /**
     * Smooth 30 FPS vector rotating spinner component with accent glow.
     */
    public static class AnimatedSpinner extends JComponent {
        private int angle = 0;
        private boolean spinning = false;
        private final Timer timer;

        public AnimatedSpinner() {
            setPreferredSize(new Dimension(16, 16));
            setMinimumSize(new Dimension(16, 16));
            setMaximumSize(new Dimension(16, 16));
            setOpaque(false);
            setVisible(false);

            timer = new Timer(33, e -> {
                if (spinning) {
                    angle = (angle + 12) % 360;
                    repaint();
                }
            });
        }

        public void start() {
            spinning = true;
            setVisible(true);
            if (!timer.isRunning()) timer.start();
            repaint();
        }

        public void stop() {
            spinning = false;
            if (timer.isRunning()) timer.stop();
            setVisible(false);
            repaint();
        }

        public boolean isSpinning() {
            return spinning;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!spinning) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int pad = 2;
            int d = getWidth() - pad * 2;

            // Background subtle ring
            g2.setColor(new Color(10, 132, 255, 45));
            g2.drawOval(pad, pad, d, d);

            // Rotating accent sweep
            g2.setColor(DrawbotTheme.ACCENT_PRIMARY);
            g2.drawArc(pad, pad, d, d, -angle, 250);

            // Glowing tip dot
            double rad = Math.toRadians(-angle + 250);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int r = d / 2;
            int hx = (int) Math.round(cx + r * Math.cos(rad));
            int hy = (int) Math.round(cy - r * Math.sin(rad));
            g2.setColor(Color.WHITE);
            g2.fillOval(hx - 2, hy - 2, 4, 4);

            g2.dispose();
        }
    }

    private final AnimatedSpinner spinner;
    private final JLabel statusLabel;
    private final JLabel statsLabel;
    private final JProgressBar progressBar;
    private final JButton cancelButton;
    private Runnable cancelAction;

    private String baseStatus = "Ready";
    private int pulseDots = 0;
    private final Timer pulseTimer;

    public StatusBarPanel() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, DrawbotTheme.BORDER_SUBTLE),
            new EmptyBorder(4, 12, 4, 12)
        ));
        setBackground(DrawbotTheme.BG_STATUS);
        setPreferredSize(new Dimension(800, 32));

        // Left: Animated spinner + status message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        leftPanel.setOpaque(false);

        spinner = new AnimatedSpinner();
        leftPanel.add(spinner);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(DrawbotTheme.TEXT_PRIMARY);
        statusLabel.setFont(DrawbotTheme.FONT_REGULAR);
        leftPanel.add(statusLabel);

        add(leftPanel, BorderLayout.WEST);

        // Right: Stats + Progress Bar + Cancel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        statsLabel = new JLabel("Lines: 0 | Heap: 0 MB");
        statsLabel.setForeground(DrawbotTheme.TEXT_MUTED);
        statsLabel.setFont(DrawbotTheme.FONT_MONO);
        rightPanel.add(statsLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(160, 16));
        progressBar.setStringPainted(true);
        progressBar.setForeground(DrawbotTheme.ACCENT_PRIMARY);
        progressBar.setBackground(DrawbotTheme.BG_INPUT);
        progressBar.setFont(DrawbotTheme.FONT_SMALL);
        progressBar.setBorder(BorderFactory.createLineBorder(DrawbotTheme.BORDER_SUBTLE, 1));
        progressBar.setVisible(false);
        rightPanel.add(progressBar);

        cancelButton = DrawbotTheme.createPrimaryButton("Cancel", DrawbotTheme.ACCENT_DANGER);
        cancelButton.setPreferredSize(new Dimension(75, 24));
        cancelButton.setFont(DrawbotTheme.FONT_BOLD);
        cancelButton.setVisible(false);
        cancelButton.addActionListener(e -> {
            if (cancelAction != null) {
                cancelAction.run();
            }
        });
        rightPanel.add(cancelButton);

        add(rightPanel, BorderLayout.EAST);

        // Pulsing dots animation for active processing
        pulseTimer = new Timer(380, e -> {
            if (spinner.isSpinning()) {
                pulseDots = (pulseDots % 3) + 1;
                StringBuilder sb = new StringBuilder(baseStatus);
                for (int i = 0; i < pulseDots; i++) {
                    sb.append('.');
                }
                statusLabel.setText(sb.toString());
            }
        });
    }

    public void setCancelAction(Runnable action) {
        this.cancelAction = action;
    }

    public void setStatus(String status) {
        this.baseStatus = (status != null && status.endsWith("...")) ? status.substring(0, status.length() - 3) : status;
        statusLabel.setText(status);
    }

    /**
     * Starts animated spinner and pulsing ellipsis for loading / preprocessing / generation.
     */
    public void startProcessing(String message) {
        this.baseStatus = (message != null && message.endsWith("...")) ? message.substring(0, message.length() - 3) : message;
        pulseDots = 0;
        statusLabel.setText(message);
        spinner.start();
        if (!pulseTimer.isRunning()) pulseTimer.start();
    }

    /**
     * Stops animated spinner and sets final resting status text.
     */
    public void stopProcessing(String finalMessage) {
        spinner.stop();
        if (pulseTimer.isRunning()) pulseTimer.stop();
        setStatus(finalMessage);
    }

    public void setProgress(float fraction, boolean visible) {
        progressBar.setVisible(visible);
        cancelButton.setVisible(visible);
        if (visible) {
            progressBar.setValue(Math.round(fraction * 100));
        }
    }

    public void updateStats(int linesCount) {
        long usedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        statsLabel.setText(String.format("Lines: %,d | Heap: %d MB", linesCount, usedMb));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // 1px top specular glass highlight rim
        g2.setColor(DrawbotTheme.SPECULAR_HIGHLIGHT);
        g2.drawLine(0, 0, getWidth(), 0);
        g2.dispose();
    }
}
