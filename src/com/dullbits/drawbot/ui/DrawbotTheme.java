package com.dullbits.drawbot.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Centralized Design System and Theme Tokens for Drawbot Studio.
 * Guarantees accessible WCAG contrast, consistent typography, custom button styling,
 * and compact parameter controls across all Windows DPI configurations.
 */
public final class DrawbotTheme {

    private DrawbotTheme() {}

    // --- Theme Color & Layout Tokens ---
    public static Color BG_WINDOW        = new Color(20, 22, 27);       // Base window background (#14161B)
    public static Color BG_WORKSPACE     = new Color(16, 17, 21);       // Canvas workspace background (#101115)
    public static Color BG_SIDEBAR       = new Color(24, 27, 34);       // Sidebar background (#181B22)
    public static Color BG_CARD          = new Color(32, 36, 46);       // Card background (#20242E)
    public static Color BG_CARD_SUBTLE   = new Color(26, 29, 38);       // Inset panel background (#1A1D26)
    public static Color BG_INPUT         = new Color(18, 20, 26);       // Input field background (#12141A)
    public static Color BG_TOOLBAR       = new Color(24, 26, 33);       // Toolbar surface background (#181A21)
    public static Color BG_STATUS        = new Color(18, 20, 26);       // Status bar background (#12141A)
    public static Color BG_PAPER         = Color.WHITE;                 // Drawing canvas paper
    public static Color BG_SELECTION     = new Color(10, 132, 255, 180);// Selection highlight
    public static Color BG_GLASS_SURFACE = new Color(36, 40, 52, 210); // Translucent card surface

    // Surface Highlights & Shadows
    public static Color SPECULAR_HIGHLIGHT = new Color(255, 255, 255, 60); // Top edge rim
    public static Color SPECULAR_SHEEN     = new Color(255, 255, 255, 25); // Top sheen
    public static Color BORDER_GLASS_TOP   = new Color(255, 255, 255, 45); // Top edge line
    public static Color BORDER_GLASS_BOTTOM= new Color(0, 0, 0, 85);       // Bottom edge shadow

    // Accessible Typography & Contrast (WCAG 2.1 AAA / AA)
    public static Color TEXT_PRIMARY     = new Color(255, 255, 255);    // Crisp white text
    public static Color TEXT_SECONDARY   = new Color(214, 218, 226);    // Secondary silver text
    public static Color TEXT_MUTED       = new Color(156, 163, 175);    // Muted units & labels
    public static Color TEXT_DISABLED    = new Color(100, 106, 118);    // Disabled text

    // Accents & Actions
    public static Color ACCENT_PRIMARY   = new Color(10, 132, 255);      // Primary blue (#0A84FF)
    public static Color ACCENT_PRIMARY_HOVER = new Color(30, 144, 255); // #1E90FF
    public static Color ACCENT_PRIMARY_PRESS = new Color(0, 113, 227);  // #0071E3
    public static Color ACCENT_SUCCESS   = new Color(48, 209, 88);      // Green (#30D158, Generate)
    public static Color ACCENT_SUCCESS_HOVER = new Color(58, 222, 99);  // #3ADE63
    public static Color ACCENT_DANGER    = new Color(255, 69, 58);      // Red (#FF453A, Stop)
    public static Color ACCENT_DANGER_HOVER = new Color(255, 90, 80);   // #FF5A50
    public static Color ACCENT_WARNING   = new Color(255, 159, 10);     // Orange (#FF9F0A, Export)
    public static Color ACCENT_WARNING_HOVER = new Color(255, 175, 35); // #FFAF23
    public static Color ACCENT_PURPLE    = new Color(191, 90, 242);     // Purple (#BF5AF2, SVG Export)
    public static Color ACCENT_PURPLE_HOVER = new Color(205, 110, 250); // #CD6EFA
    public static Color ACCENT_SECONDARY = new Color(46, 50, 62);       // Secondary button (#2E323E)
    public static Color ACCENT_SECONDARY_HOVER = new Color(58, 64, 78); // #3A404E
    public static Color ACCENT_HOVER     = new Color(30, 144, 255);

    // Borders & Focus
    public static Color BORDER_SUBTLE    = new Color(52, 57, 70);       // Subtle borders (#343946)
    public static Color BORDER_STRONG    = new Color(72, 80, 96);       // Interactive borders (#485060)
    public static Color BORDER_FOCUS     = new Color(10, 132, 255);     // Focus ring (#0A84FF)

    // Standard Geometry & Spacing Tokens
    public static int CORNER_RADIUS      = 6;                           // Button corner radius
    public static int CORNER_RADIUS_CARD = 8;                           // Card corner radius
    public static final int CONTROL_HEIGHT     = 32;                          // Standard 32px desktop control height
    public static final int INPUT_HEIGHT       = 28;                          // Form input height
    public static final int SPACING_SMALL      = 6;
    public static final int SPACING_MEDIUM     = 10;
    public static final int SPACING_LARGE      = 16;

    // Typography Scale
    public static Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 13);
    public static Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 12);
    public static Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 11);
    public static Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 11);

    public static Color calculateHover(Color c) {
        if (c == null) return Color.GRAY;
        int r = Math.min(255, c.getRed() + 25);
        int g = Math.min(255, c.getGreen() + 25);
        int b = Math.min(255, c.getBlue() + 25);
        return new Color(r, g, b, c.getAlpha());
    }

    public static Color calculatePress(Color c) {
        if (c == null) return Color.DARK_GRAY;
        int r = Math.max(0, c.getRed() - 25);
        int g = Math.max(0, c.getGreen() - 25);
        int b = Math.max(0, c.getBlue() - 25);
        return new Color(r, g, b, c.getAlpha());
    }

    /**
     * Dynamically updates all theme tokens in memory according to the provided preferences.
     */
    public static void applyPreferences(DrawbotPreferences prefs) {
        if (prefs == null) return;

        // 1. Accent Color
        ACCENT_PRIMARY = prefs.getAccentColor().getColor();
        ACCENT_PRIMARY_HOVER = calculateHover(ACCENT_PRIMARY);
        ACCENT_PRIMARY_PRESS = calculatePress(ACCENT_PRIMARY);
        BORDER_FOCUS = ACCENT_PRIMARY;

        // 2. Corner Radii
        CORNER_RADIUS_CARD = prefs.getCornerRadius();
        CORNER_RADIUS = Math.max(4, CORNER_RADIUS_CARD - 2);

        // 3. Theme Palettes
        DrawbotPreferences.ThemePreset tp = prefs.getTheme();
        boolean opaque = prefs.isReducedTransparency() || tp == DrawbotPreferences.ThemePreset.OBSIDIAN_DARK;
        float specularMult = prefs.getSpecularIntensity();
        float opacityMult = prefs.getGlassOpacity();

        if (tp == DrawbotPreferences.ThemePreset.MONTEREY_LIGHT) {
            BG_WINDOW = new Color(238, 240, 244);
            BG_WORKSPACE = prefs.getCanvasBackground() == DrawbotPreferences.CanvasBackground.DARK_OBSIDIAN 
                ? new Color(224, 226, 232) : prefs.getCanvasBackground().getColor();
            BG_SIDEBAR = new Color(246, 247, 250);
            BG_CARD = Color.WHITE;
            BG_CARD_SUBTLE = new Color(242, 244, 248);
            BG_INPUT = Color.WHITE;
            BG_TOOLBAR = new Color(242, 244, 248);
            BG_STATUS = new Color(236, 238, 243);
            BG_PAPER = Color.WHITE;
            BG_GLASS_SURFACE = new Color(255, 255, 255, (int)(230 * opacityMult));

            TEXT_PRIMARY = new Color(25, 28, 35);
            TEXT_SECONDARY = new Color(65, 72, 85);
            TEXT_MUTED = new Color(110, 118, 132);
            TEXT_DISABLED = new Color(165, 172, 185);

            BORDER_SUBTLE = new Color(212, 216, 224);
            BORDER_STRONG = new Color(185, 191, 202);
            BORDER_GLASS_TOP = new Color(255, 255, 255, (int)(200 * specularMult));
            BORDER_GLASS_BOTTOM = new Color(0, 0, 0, (int)(35 * opacityMult));
            SPECULAR_HIGHLIGHT = new Color(255, 255, 255, (int)(180 * specularMult));
            SPECULAR_SHEEN = new Color(255, 255, 255, (int)(75 * specularMult));

            ACCENT_SECONDARY = new Color(230, 234, 240);
            ACCENT_SECONDARY_HOVER = new Color(220, 225, 233);
        } else if (tp == DrawbotPreferences.ThemePreset.HIGH_CONTRAST) {
            BG_WINDOW = Color.BLACK;
            BG_WORKSPACE = Color.BLACK;
            BG_SIDEBAR = new Color(12, 12, 12);
            BG_CARD = new Color(20, 20, 20);
            BG_CARD_SUBTLE = new Color(15, 15, 15);
            BG_INPUT = Color.BLACK;
            BG_TOOLBAR = new Color(14, 14, 14);
            BG_STATUS = new Color(10, 10, 10);
            BG_PAPER = Color.WHITE;
            BG_GLASS_SURFACE = new Color(25, 25, 25);

            TEXT_PRIMARY = Color.WHITE;
            TEXT_SECONDARY = new Color(240, 240, 240);
            TEXT_MUTED = new Color(200, 200, 200);
            TEXT_DISABLED = new Color(130, 130, 130);

            BORDER_SUBTLE = new Color(100, 100, 100);
            BORDER_STRONG = new Color(160, 160, 160);
            BORDER_GLASS_TOP = Color.WHITE;
            BORDER_GLASS_BOTTOM = Color.BLACK;
            SPECULAR_HIGHLIGHT = Color.WHITE;
            SPECULAR_SHEEN = new Color(255, 255, 255, 40);

            ACCENT_SECONDARY = new Color(35, 35, 35);
            ACCENT_SECONDARY_HOVER = new Color(55, 55, 55);
        } else if (tp == DrawbotPreferences.ThemePreset.OBSIDIAN_DARK) {
            BG_WINDOW = new Color(18, 20, 24);
            BG_WORKSPACE = prefs.getCanvasBackground().getColor();
            BG_SIDEBAR = new Color(22, 24, 30);
            BG_CARD = new Color(28, 31, 38);
            BG_CARD_SUBTLE = new Color(22, 24, 30);
            BG_INPUT = new Color(15, 17, 21);
            BG_TOOLBAR = new Color(22, 24, 30);
            BG_STATUS = new Color(18, 20, 24);
            BG_PAPER = Color.WHITE;
            BG_GLASS_SURFACE = new Color(30, 33, 41);

            TEXT_PRIMARY = Color.WHITE;
            TEXT_SECONDARY = new Color(214, 218, 226);
            TEXT_MUTED = new Color(156, 163, 175);
            TEXT_DISABLED = new Color(100, 106, 118);

            BORDER_SUBTLE = new Color(46, 50, 60);
            BORDER_STRONG = new Color(64, 70, 82);
            BORDER_GLASS_TOP = new Color(255, 255, 255, 20);
            BORDER_GLASS_BOTTOM = new Color(0, 0, 0, 90);
            SPECULAR_HIGHLIGHT = new Color(255, 255, 255, 20);
            SPECULAR_SHEEN = new Color(255, 255, 255, 10);

            ACCENT_SECONDARY = new Color(40, 44, 54);
            ACCENT_SECONDARY_HOVER = new Color(52, 57, 68);
        } else {
            // Default LIQUID_GLASS_DARK
            BG_WINDOW = new Color(20, 22, 27);
            BG_WORKSPACE = prefs.getCanvasBackground().getColor();
            BG_SIDEBAR = new Color(24, 27, 34);
            BG_CARD = new Color(32, 36, 46);
            BG_CARD_SUBTLE = new Color(26, 29, 38);
            BG_INPUT = new Color(18, 20, 26);
            BG_TOOLBAR = new Color(24, 26, 33);
            BG_STATUS = new Color(18, 20, 26);
            BG_PAPER = Color.WHITE;
            int surfAlpha = opaque ? 255 : (int)(210 * opacityMult);
            BG_GLASS_SURFACE = new Color(36, 40, 52, surfAlpha);

            TEXT_PRIMARY = Color.WHITE;
            TEXT_SECONDARY = new Color(214, 218, 226);
            TEXT_MUTED = new Color(156, 163, 175);
            TEXT_DISABLED = new Color(100, 106, 118);

            BORDER_SUBTLE = new Color(52, 57, 70);
            BORDER_STRONG = new Color(72, 80, 96);
            int specTop = opaque ? 15 : (int)(60 * specularMult);
            int specSheen = opaque ? 10 : (int)(25 * specularMult);
            SPECULAR_HIGHLIGHT = new Color(255, 255, 255, specTop);
            SPECULAR_SHEEN = new Color(255, 255, 255, specSheen);
            BORDER_GLASS_TOP = new Color(255, 255, 255, (int)(45 * specularMult));
            BORDER_GLASS_BOTTOM = new Color(0, 0, 0, (int)(85 * opacityMult));

            ACCENT_SECONDARY = new Color(46, 50, 62);
            ACCENT_SECONDARY_HOVER = new Color(58, 64, 78);
        }
    }

    /**
     * Applies high-fidelity anti-aliasing and quality rendering hints for Apple Liquid Glass rendering.
     */
    public static void applyQualityRendering(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Custom-painted modern button that completely bypasses Windows LookAndFeel
     * bugs, ensuring 100% reliable background colors, hover highlights, and legible text.
     */
    public static class StyledButton extends JButton {
        private final Color baseBg;
        private final Color hoverBg;
        private final Color pressBg;
        private final Color fgColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public StyledButton(String text, Color bg, Color fg) {
            super(text);
            this.baseBg = bg;
            this.hoverBg = calculateHover(bg);
            this.pressBg = calculatePress(bg);
            this.fgColor = fg != null ? fg : Color.WHITE;

            setFont(FONT_BOLD);
            setForeground(fgColor);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(4, 12, 4, 12));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) { isHovered = true; repaint(); }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false; isPressed = false; repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) { isPressed = true; repaint(); }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false; repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int textW = fm != null && getText() != null ? fm.stringWidth(getText()) : 50;
            int iconW = getIcon() != null ? getIcon().getIconWidth() + 6 : 0;
            return new Dimension(Math.max(68, textW + iconW + 28), 32);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width + 20, 32);
        }

        private static Color calculateHover(Color c) {
            int r = Math.min(255, c.getRed() + 25);
            int g = Math.min(255, c.getGreen() + 25);
            int b = Math.min(255, c.getBlue() + 25);
            return new Color(r, g, b);
        }

        private static Color calculatePress(Color c) {
            int r = Math.max(0, c.getRed() - 25);
            int g = Math.max(0, c.getGreen() - 25);
            int b = Math.max(0, c.getBlue() - 25);
            return new Color(r, g, b);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Soft Ambient Elevation Shadow
            if (isEnabled() && !isPressed) {
                g2.setColor(new Color(0, 0, 0, isHovered ? 45 : 22));
                g2.fillRoundRect(0, 2, w, h - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2);
            }

            Color currentBg;
            if (!isEnabled()) {
                currentBg = new Color(38, 41, 50);
            } else if (isPressed) {
                currentBg = pressBg;
            } else if (isHovered) {
                currentBg = hoverBg;
            } else {
                currentBg = baseBg;
            }

            // 2. Liquid Glass Base Fill
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS * 2, CORNER_RADIUS * 2);

            // 3. Apple Liquid Glass Specular Top Sheen (Luster Layer)
            if (isEnabled()) {
                int sheenH = Math.max(8, h * 45 / 100);
                Paint sheen = new LinearGradientPaint(
                    0, 0, 0, sheenH,
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(255, 255, 255, isHovered ? 55 : 30), new Color(255, 255, 255, 0)}
                );
                g2.setPaint(sheen);
                g2.fillRoundRect(1, 1, w - 2, sheenH, CORNER_RADIUS * 2 - 1, CORNER_RADIUS * 2 - 1);
            }

            // 4. Dual-Tone Specular Glass Rim Outline (Beveled Refraction)
            if (isEnabled()) {
                Paint rim = new LinearGradientPaint(
                    0, 0, 0, h,
                    new float[]{0.0f, 0.45f, 1.0f},
                    new Color[]{
                        new Color(255, 255, 255, isHovered ? 80 : 48),
                        new Color(255, 255, 255, 12),
                        new Color(0, 0, 0, 70)
                    }
                );
                g2.setPaint(rim);
            } else {
                g2.setColor(new Color(42, 45, 52));
            }
            g2.drawRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2);

            // 5. Draw Icon and Label Centered
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int strW = getText() != null ? fm.stringWidth(getText()) : 0;
            Icon icon = getIcon();
            int iconW = icon != null ? icon.getIconWidth() : 0;
            int gap = (icon != null && strW > 0) ? 6 : 0;
            int totalContentW = iconW + gap + strW;

            int startX = (w - totalContentW) / 2;
            if (icon != null) {
                int iconY = (h - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, startX, iconY);
                startX += iconW + gap;
            }

            if (getText() != null && !getText().isEmpty()) {
                int strH = fm.getAscent();
                int y = (h + strH) / 2 - 2;
                g2.setColor(isEnabled() ? fgColor : TEXT_DISABLED);
                g2.drawString(getText(), startX, y);
            }

            // 6. Visible Apple Focus Ring (Accessibility)
            if (hasFocus() && isEnabled()) {
                g2.setColor(BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, CORNER_RADIUS * 2, CORNER_RADIUS * 2);
            }

            g2.dispose();
        }
    }

    /**
     * Creates a consistent primary action button.
     */
    public static JButton createPrimaryButton(String text, Color bg) {
        StyledButton btn = new StyledButton(text, bg, Color.WHITE);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, CONTROL_HEIGHT));
        return btn;
    }

    /**
     * Creates a secondary toolbar button.
     */
    public static JButton createSecondaryButton(String text) {
        StyledButton btn = new StyledButton(text, ACCENT_SECONDARY, TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, CONTROL_HEIGHT));
        return btn;
    }

    /**
     * Creates a sleek toggle button.
     */
    public static JToggleButton createToggleButton(String text, boolean initial) {
        JToggleButton btn = new JToggleButton(text, initial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                boolean sel = isSelected();

                // 1. Ambient Shadow
                g2.setColor(new Color(0, 0, 0, sel ? 40 : 20));
                g2.fillRoundRect(0, 2, w, h - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2);

                // 2. Liquid Glass Base
                Color bg = sel ? ACCENT_PRIMARY : (getModel().isRollover() ? ACCENT_SECONDARY_HOVER : ACCENT_SECONDARY);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS * 2, CORNER_RADIUS * 2);

                // 3. Specular Top Sheen
                int sheenH = Math.max(8, h * 45 / 100);
                Paint sheen = new LinearGradientPaint(
                    0, 0, 0, sheenH,
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(255, 255, 255, sel ? 65 : (getModel().isRollover() ? 45 : 25)), new Color(255, 255, 255, 0)}
                );
                g2.setPaint(sheen);
                g2.fillRoundRect(1, 1, w - 2, sheenH, CORNER_RADIUS * 2 - 1, CORNER_RADIUS * 2 - 1);

                // 4. Dual-Tone Glass Rim
                Paint rim = new LinearGradientPaint(
                    0, 0, 0, h,
                    new float[]{0.0f, 0.45f, 1.0f},
                    new Color[]{
                        new Color(255, 255, 255, sel ? 85 : 45),
                        new Color(255, 255, 255, 12),
                        new Color(0, 0, 0, 65)
                    }
                );
                g2.setPaint(rim);
                g2.drawRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2);

                // Focus Ring
                if (isFocusOwner()) {
                    g2.setColor(BORDER_FOCUS);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, CORNER_RADIUS * 2, CORNER_RADIUS * 2);
                }

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int strW = fm.stringWidth(getText());
                int strH = fm.getAscent();
                int x = (w - strW) / 2;
                int y = (h + strH) / 2 - 2;

                g2.setColor(sel ? Color.WHITE : TEXT_SECONDARY);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 32));
        return btn;
    }

    /**
     * Creates a vector Play (triangle) icon.
     */
    public static Icon createPlayIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                int pad = 2;
                int[] px = {x + pad, x + size - pad, x + pad};
                int[] py = {y + pad, y + size / 2, y + size - pad};
                g2.fillPolygon(px, py, 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Stop (square) icon.
     */
    public static Icon createStopIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                int pad = 2;
                g2.fillRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2, 2, 2);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Gear / Preferences icon.
     */
    public static Icon createGearIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                int cx = x + size / 2;
                int cy = y + size / 2;
                int rOuter = size / 2 - 1;
                int rInner = size / 2 - 3;
                int rHole = Math.max(2, size / 5);

                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // 6 gear teeth
                for (int i = 0; i < 6; i++) {
                    double angle = Math.toRadians(i * 60);
                    int x1 = (int) Math.round(cx + rInner * Math.cos(angle));
                    int y1 = (int) Math.round(cy + rInner * Math.sin(angle));
                    int x2 = (int) Math.round(cx + rOuter * Math.cos(angle));
                    int y2 = (int) Math.round(cy + rOuter * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx - rInner, cy - rInner, rInner * 2, rInner * 2);
                g2.drawOval(cx - rHole, cy - rHole, rHole * 2, rHole * 2);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Folder / Open File icon.
     */
    public static Icon createFolderIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int w = size - 2;
                int h = size - 4;
                int ox = x + 1;
                int oy = y + 2;
                // Folder tab
                g2.drawLine(ox, oy + 2, ox + w / 3, oy + 2);
                g2.drawLine(ox + w / 3, oy + 2, ox + w / 2, oy + 5);
                g2.drawLine(ox + w / 2, oy + 5, ox + w, oy + 5);
                // Folder body
                g2.drawRoundRect(ox, oy + 5, w, h - 3, 2, 2);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Web / URL Download icon.
     */
    public static Icon createGlobeIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.4f));
                int pad = 2;
                int d = size - pad * 2;
                g2.drawOval(x + pad, y + pad, d, d);
                g2.drawLine(x + pad, y + size / 2, x + pad + d, y + size / 2);
                g2.drawOval(x + pad + d / 4, y + pad, d / 2, d);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Fit-to-Window icon.
     */
    public static Icon createFitIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.4f));
                int pad = 2;
                int w = size - pad * 2;
                int arm = w / 3;
                int ox = x + pad;
                int oy = y + pad;
                // 4 corners
                g2.drawLine(ox, oy + arm, ox, oy);
                g2.drawLine(ox, oy, ox + arm, oy);

                g2.drawLine(ox + w - arm, oy, ox + w, oy);
                g2.drawLine(ox + w, oy, ox + w, oy + arm);

                g2.drawLine(ox, oy + w - arm, ox, oy + w);
                g2.drawLine(ox, oy + w, ox + arm, oy + w);

                g2.drawLine(ox + w - arm, oy + w, ox + w, oy + w);
                g2.drawLine(ox + w, oy + w, ox + w, oy + w - arm);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Reset View (1:1) icon.
     */
    public static Icon createResetIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(9, size - 4)));
                FontMetrics fm = g2.getFontMetrics();
                String text = "1:1";
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Rotate 90\u00B0 icon.
     */
    public static Icon createRotateIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 2;
                int d = size - pad * 2;
                g2.drawArc(x + pad, y + pad, d, d, 40, 270);
                // Arrow head at top
                int ax = x + size - pad - 2;
                int ay = y + pad + 2;
                g2.drawLine(ax, ay, ax + 4, ay + 1);
                g2.drawLine(ax, ay, ax + 1, ay + 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Grid Lattice icon.
     */
    public static Icon createGridIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.2f));
                int pad = 2;
                int w = size - pad * 2;
                g2.drawRect(x + pad, y + pad, w, w);
                g2.drawLine(x + pad + w / 3, y + pad, x + pad + w / 3, y + pad + w);
                g2.drawLine(x + pad + 2 * w / 3, y + pad, x + pad + 2 * w / 3, y + pad + w);
                g2.drawLine(x + pad, y + pad + w / 3, x + pad + w, y + pad + w / 3);
                g2.drawLine(x + pad, y + pad + 2 * w / 3, x + pad + w, y + pad + 2 * w / 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector SVG Pen / Vector Curve icon.
     */
    public static Icon createSvgIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 2;
                // Draw bezier anchor line + points
                int x1 = x + pad;
                int y1 = y + size - pad;
                int x2 = x + size - pad;
                int y2 = y + pad;
                g2.drawLine(x1, y1, x2, y2);
                g2.fillRect(x1 - 1, y1 - 2, 4, 4);
                g2.fillRect(x2 - 2, y2 - 1, 4, 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Export Tray icon.
     */
    public static Icon createExportIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 2;
                int w = size - pad * 2;
                int ox = x + pad;
                int oy = y + pad;
                // Tray
                g2.drawLine(ox, oy + w / 2, ox, oy + w);
                g2.drawLine(ox, oy + w, ox + w, oy + w);
                g2.drawLine(ox + w, oy + w, ox + w, oy + w / 2);
                // Arrow up
                int cx = ox + w / 2;
                g2.drawLine(cx, oy + w - 3, cx, oy);
                g2.drawLine(cx - 3, oy + 3, cx, oy);
                g2.drawLine(cx + 3, oy + 3, cx, oy);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a vector Sliders / Adjustments icon.
     */
    public static Icon createSlidersIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c != null && !c.isEnabled() ? TEXT_DISABLED : color);
                g2.setStroke(new BasicStroke(1.3f));
                int pad = 2;
                int w = size - pad * 2;
                int y1 = y + pad + w / 4;
                int y2 = y + pad + 3 * w / 4;

                // Slider 1
                g2.drawLine(x + pad, y1, x + pad + w, y1);
                g2.fillRect(x + pad + w / 3 - 2, y1 - 3, 4, 6);

                // Slider 2
                g2.drawLine(x + pad, y2, x + pad + w, y2);
                g2.fillRect(x + pad + 2 * w / 3 - 2, y2 - 3, 4, 6);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /**
     * Creates a compact, well-proportioned numeric spinner that NEVER stretches vertically.
     */
    public static JSpinner createCompactSpinner(SpinnerModel model, int width) {
        JSpinner spinner = new JSpinner(model);
        Dimension dim = new Dimension(width, 26);
        spinner.setPreferredSize(dim);
        spinner.setMaximumSize(dim);
        spinner.setMinimumSize(dim);

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(TEXT_PRIMARY);
            tf.setFont(FONT_MONO);
            tf.setHorizontalAlignment(JTextField.RIGHT);
            tf.setBorder(new EmptyBorder(2, 4, 2, 4));
        }
        spinner.setBorder(new LineBorder(BORDER_SUBTLE, 1));
        for (Component child : spinner.getComponents()) {
            if (child instanceof JButton) {
                JButton arrow = (JButton) child;
                arrow.setBackground(BG_CARD);
            }
        }
        return spinner;
    }

    static {
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.disabledBackground", BG_INPUT);
        UIManager.put("ComboBox.disabledForeground", TEXT_DISABLED);
        UIManager.put("ComboBox.selectionBackground", ACCENT_PRIMARY);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("MenuBar.background", BG_STATUS);
        UIManager.put("MenuBar.foreground", TEXT_PRIMARY);
        UIManager.put("Menu.background", BG_STATUS);
        UIManager.put("Menu.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.background", BG_CARD);
        UIManager.put("MenuItem.foreground", TEXT_PRIMARY);
        UIManager.put("PopupMenuUI", "javax.swing.plaf.basic.BasicPopupMenuUI");
        UIManager.put("PopupMenu.background", BG_CARD);
        UIManager.put("PopupMenu.foreground", TEXT_PRIMARY);
        UIManager.put("Separator.background", BG_CARD);
        UIManager.put("Separator.foreground", BORDER_SUBTLE);
        UIManager.put("Separator.highlight", BG_CARD);
        UIManager.put("Separator.shadow", BORDER_SUBTLE);
        UIManager.put("PopupMenuSeparator.background", BG_CARD);
        UIManager.put("PopupMenuSeparator.foreground", BORDER_SUBTLE);
        UIManager.put("PopupMenuSeparator.highlight", BG_CARD);
        UIManager.put("PopupMenuSeparator.shadow", BORDER_SUBTLE);
    }

    /**
     * Styles a combo box with clean dark styling across all states.
     */
    public static void styleComboBox(JComboBox<?> combo, int width, int height) {
        Dimension dim = new Dimension(width, height);
        combo.setPreferredSize(dim);
        combo.setMaximumSize(dim);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_REGULAR);
        combo.setFocusable(false);

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_INPUT);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(combo.isEnabled() ? TEXT_SECONDARY : TEXT_DISABLED);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        int[] px = {cx - 4, cx + 4, cx};
                        int[] py = {cy - 2, cy - 2, cy + 3};
                        g2.fillPolygon(px, py, 3);
                        g2.dispose();
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_INPUT);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                ListCellRenderer<Object> r = (ListCellRenderer<Object>) comboBox.getRenderer();
                Component c = r.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(comboBox.getFont());
                c.setForeground(comboBox.isEnabled() ? TEXT_PRIMARY : TEXT_DISABLED);
                c.setBackground(BG_INPUT);
                currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, false);
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBackground(isSelected ? ACCENT_PRIMARY : BG_INPUT);
                l.setForeground(combo.isEnabled() ? TEXT_PRIMARY : TEXT_DISABLED);
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
        combo.setBorder(new LineBorder(BORDER_SUBTLE, 1));
    }

    /**
     * Styles a JTabbedPane with Apple Liquid Glass pill tabs and specular indicators.
     */
    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(BG_SIDEBAR);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(FONT_BOLD);
        tabs.setFocusable(false);

        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(6, 16, 6, 16);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(1, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    // Liquid Glass Active Tab Pill
                    Paint glassBg = new LinearGradientPaint(
                        x, y, x, y + h,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(42, 48, 62), BG_CARD}
                    );
                    g2.setPaint(glassBg);
                    g2.fillRoundRect(x + 1, y + 1, w - 2, h + 2, 6, 6);

                    // Specular Top Sheen on Active Tab
                    Paint sheen = new LinearGradientPaint(
                        x, y, x, y + h / 2,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(255, 255, 255, 45), new Color(255, 255, 255, 0)}
                    );
                    g2.setPaint(sheen);
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h / 2, 5, 5);

                    // Glowing Apple Accent Indicator Line
                    g2.setColor(ACCENT_PRIMARY);
                    g2.fillRoundRect(x + 4, y + 1, w - 8, 3, 2, 2);
                } else {
                    g2.setColor(BG_SIDEBAR);
                    g2.fillRect(x, y, w, h);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    // Dual-tone specular outline on active tab
                    Paint rim = new LinearGradientPaint(
                        x, y, x, y + h,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(255, 255, 255, 60), new Color(0, 0, 0, 60)}
                    );
                    g2.setPaint(rim);
                    g2.drawRoundRect(x + 1, y + 1, w - 3, h + 1, 6, 6);
                } else {
                    g2.setColor(BORDER_SUBTLE);
                    g2.drawRect(x, y, w - 1, h - 1);
                }
                g2.dispose();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_SUBTLE);
                g2.drawLine(0, 0, tabPane.getWidth(), 0);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                               int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Eliminate default dashed focus rectangle
            }
        });
    }

    /**
     * Creates a reusable Apple Liquid Glass card border with dual-tone specular highlights.
     */
    public static javax.swing.border.Border createGlassCardBorder(int radius) {
        return new javax.swing.border.Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Paint rim = new LinearGradientPaint(
                    x, y, x, y + height,
                    new float[]{0.0f, 0.45f, 1.0f},
                    new Color[]{
                        new Color(255, 255, 255, 55),
                        new Color(255, 255, 255, 12),
                        new Color(0, 0, 0, 85)
                    }
                );
                g2.setPaint(rim);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
                g2.dispose();
            }
            @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
            @Override public boolean isBorderOpaque() { return false; }
        };
    }

    /**
     * Custom popup menu UI that overrides native Windows LookAndFeel popup painting,
     * ensuring a seamless Apple Liquid Glass elevated card and preventing white border leaks.
     */
    public static class DarkPopupMenuUI extends javax.swing.plaf.basic.BasicPopupMenuUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            // 1. Ambient Elevation Shadow
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(1, 2, w - 2, h - 2, 8, 8);

            // 2. Liquid Glass Obsidian Base
            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, w, h, 8, 8);

            // 3. Top Specular Luster Sheen
            int sheenH = Math.min(36, h / 3);
            Paint sheen = new LinearGradientPaint(
                0, 0, 0, sheenH,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(255, 255, 255, 24), new Color(255, 255, 255, 0)}
            );
            g2.setPaint(sheen);
            g2.fillRoundRect(1, 1, w - 2, sheenH, 7, 7);

            g2.dispose();
            super.paint(g, c);
        }
    }

    /**
     * Custom separator UI that ensures dark card background and crisp subtle glass line.
     */
    public static class DarkSeparatorUI extends javax.swing.plaf.basic.BasicPopupMenuSeparatorUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(BG_CARD);
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
            // Subtle 1px translucent glass etch
            g2.setColor(new Color(255, 255, 255, 28));
            int y = c.getHeight() / 2;
            g2.drawLine(10, y, c.getWidth() - 10, y);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(0, 7);
        }
    }

    /**
     * Creates a custom-painted dark separator for popup menus.
     */
    public static JSeparator createMenuSeparator() {
        JSeparator sep = new JPopupMenu.Separator();
        styleSeparator(sep);
        return sep;
    }

    /**
     * Adds an elegantly styled dark separator to a JMenu.
     */
    public static void addMenuSeparator(JMenu menu) {
        menu.add(createMenuSeparator());
    }

    /**
     * Styles an existing separator with dark card background and subtle border line.
     */
    public static void styleSeparator(JSeparator sep) {
        sep.setOpaque(true);
        sep.setBackground(BG_CARD);
        sep.setForeground(BORDER_SUBTLE);
        sep.setPreferredSize(new Dimension(0, 6));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        sep.setUI(new DarkSeparatorUI());
    }

    /**
     * Styles a JMenuBar with dark background and subtle border.
     */
    public static void styleMenuBar(JMenuBar mb) {
        mb.setOpaque(true);
        mb.setBackground(BG_TOOLBAR);
        mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
        mb.setUI(new javax.swing.plaf.basic.BasicMenuBarUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                g.setColor(BG_TOOLBAR);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
                g.setColor(BORDER_SUBTLE);
                g.drawLine(0, c.getHeight() - 1, c.getWidth(), c.getHeight() - 1);
            }
        });
    }

    /**
     * Styles a JPopupMenu with Apple Liquid Glass card background,
     * dual-tone specular borders, and auto-styling of menu items and separators.
     */
    public static void stylePopupMenu(JPopupMenu pop) {
        pop.setUI(new DarkPopupMenuUI());
        pop.setOpaque(true);
        pop.setBackground(BG_CARD);
        pop.setBorder(BorderFactory.createCompoundBorder(
            createGlassCardBorder(8),
            new EmptyBorder(4, 3, 4, 3)
        ));
        pop.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                Component child = e.getChild();
                if (child instanceof JSeparator) {
                    styleSeparator((JSeparator) child);
                } else if (child instanceof JMenuItem) {
                    styleMenuItem((JMenuItem) child);
                }
            }
        });
        for (Component c : pop.getComponents()) {
            if (c instanceof JSeparator) {
                styleSeparator((JSeparator) c);
            } else if (c instanceof JMenuItem) {
                styleMenuItem((JMenuItem) c);
            }
        }
    }

    /**
     * Styles top-level JMenu with modern typography, glass pill hover highlights, and auto-styled separators.
     */
    public static void styleMenu(JMenu menu) {
        menu.setOpaque(false);
        menu.setForeground(TEXT_PRIMARY);
        menu.setFont(FONT_REGULAR);
        menu.setMargin(new Insets(3, 8, 3, 8));
        menu.setBorder(new EmptyBorder(4, 10, 4, 10));
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menu.setUI(new javax.swing.plaf.basic.BasicMenuUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                if (menuItem.isArmed() || menuItem.isSelected()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = menuItem.getWidth();
                    int h = menuItem.getHeight();

                    // Liquid Glass Translucent Pill on Menu Item
                    Paint glassPill = new LinearGradientPaint(
                        0, 2, 0, h - 2,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(255, 255, 255, 42), new Color(255, 255, 255, 18)}
                    );
                    g2.setPaint(glassPill);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);

                    // Specular Top Rim on Active Menu Item
                    g2.setColor(new Color(255, 255, 255, 65));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, 6, 6);

                    g2.dispose();
                }
            }

            @Override
            protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(menuItem.getFont());
                g2.setColor(menuItem.isEnabled() ? TEXT_PRIMARY : TEXT_DISABLED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                g2.dispose();
            }
        });
        stylePopupMenu(menu.getPopupMenu());
    }

    /**
     * Styles JMenuItem with Apple Liquid Glass aesthetics:
     * rounded pill hover highlight (#0A84FF) with specular light sheen, 28px consistent row height,
     * vertically centered typography, and accessible state contrast.
     */
    public static void styleMenuItem(JMenuItem item) {
        item.setOpaque(false);
        item.setBackground(BG_CARD);
        item.setForeground(TEXT_PRIMARY);
        item.setFont(FONT_REGULAR);
        item.setBorder(new EmptyBorder(4, 14, 4, 14));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
            @Override
            public Dimension getPreferredSize(JComponent c) {
                Dimension d = super.getPreferredSize(c);
                return new Dimension(Math.max(190, d.width + 16), Math.max(28, d.height));
            }

            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                if (menuItem.isEnabled() && (menuItem.isArmed() || menuItem.isSelected())) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = menuItem.getWidth();
                    int h = menuItem.getHeight();

                    // Apple Liquid Glass Blue Hover Pill
                    Paint pill = new LinearGradientPaint(
                        0, 1, 0, h - 1,
                        new float[]{0.0f, 1.0f},
                        new Color[]{ACCENT_PRIMARY_HOVER, ACCENT_PRIMARY_PRESS}
                    );
                    g2.setPaint(pill);
                    g2.fillRoundRect(3, 1, w - 6, h - 2, 5, 5);

                    // Specular Top Highlight Rim on Hover Pill
                    Paint rim = new LinearGradientPaint(
                        0, 1, 0, h / 2,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(255, 255, 255, 85), new Color(255, 255, 255, 0)}
                    );
                    g2.setPaint(rim);
                    g2.fillRoundRect(3, 1, w - 6, h / 2, 5, 5);

                    g2.dispose();
                }
            }

            @Override
            protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(menuItem.getFont());
                Color textColor;
                if (!menuItem.isEnabled()) {
                    textColor = TEXT_DISABLED;
                } else if (menuItem.isArmed() || menuItem.isSelected()) {
                    textColor = Color.WHITE;
                } else {
                    textColor = TEXT_PRIMARY;
                }
                g2.setColor(textColor);
                FontMetrics fm = g2.getFontMetrics();
                int y = (menuItem.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textRect.x, y);
                g2.dispose();
            }
        });
    }

    /**
     * Factory method to create a JFileChooser with Downloads prominently placed in Places Bar
     * and clean, unglitched UI styling.
     */
    public static JFileChooser createFileChooser(String title, File initialDir) {
        JFileChooser chooser = (initialDir != null && initialDir.exists())
            ? new JFileChooser(initialDir)
            : new JFileChooser();
        if (title != null) {
            chooser.setDialogTitle(title);
        }

        // Explicitly populate Places Bar with Downloads, Desktop, Documents, Home
        File home = new File(System.getProperty("user.home"));
        File downloads = new File(home, "Downloads");
        File desktop = new File(home, "Desktop");
        File documents = new File(home, "Documents");
        chooser.putClientProperty("FileChooser.placesBar", new File[] {
            downloads.exists() ? downloads : home,
            desktop,
            documents,
            home
        });
        return chooser;
    }

    public static void styleFileChooser(JFileChooser chooser) {
        // Ensure Places Bar has Downloads folder
        File home = new File(System.getProperty("user.home"));
        File downloads = new File(home, "Downloads");
        File desktop = new File(home, "Desktop");
        File documents = new File(home, "Documents");
        chooser.putClientProperty("FileChooser.placesBar", new File[] {
            downloads.exists() ? downloads : home,
            desktop,
            documents,
            home
        });
    }

    /**
     * Creates a titled section header with clear contrast.
     */
    public static Border createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
            " " + title + " ",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_TITLE,
            TEXT_PRIMARY
        );
    }
}
