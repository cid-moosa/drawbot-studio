package com.dullbits.drawbot.ui;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Robust, persistent preferences architecture for Drawbot Studio.
 * Stores and manages appearance, canvas, workspace, and accessibility settings.
 * Guaranteed zero impact on drawing algorithms, machine calibration, or project outputs.
 */
public final class DrawbotPreferences {

    public enum ThemePreset {
        LIQUID_GLASS_DARK("Liquid Glass Dark (Default)", "Translucent obsidian surfaces with specular light lusters"),
        OBSIDIAN_DARK("Obsidian Dark (Solid)", "Opaque deep dark theme with zero glass transparency"),
        MONTEREY_LIGHT("Monterey Light", "Clean, high-legibility silver-white studio theme"),
        HIGH_CONTRAST("High Contrast Dark", "WCAG AAA vivid accents, solid contrast borders, enhanced readability");

        private final String displayName;
        private final String description;
        ThemePreset(String name, String desc) { this.displayName = name; this.description = desc; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    public enum AccentColorPreset {
        APPLE_BLUE("Apple System Blue", new Color(10, 132, 255)),
        EMERALD_GREEN("Emerald Green", new Color(48, 209, 88)),
        PURPLE("Electric Purple", new Color(191, 90, 242)),
        ORANGE("System Orange", new Color(255, 159, 10)),
        CRIMSON("Studio Crimson", new Color(255, 69, 58)),
        CYAN("Nordic Cyan", new Color(90, 200, 250));

        private final String displayName;
        private final Color color;
        AccentColorPreset(String name, Color col) { this.displayName = name; this.color = col; }
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
        @Override public String toString() { return displayName; }
    }

    public enum ToolbarMode {
        COMFORTABLE("Icon + Label (Comfortable)", "Displays icons with descriptive text labels"),
        COMPACT("Icon Only (Compact)", "Displays compact icon buttons with hover tooltips");

        private final String displayName;
        private final String description;
        ToolbarMode(String name, String desc) { this.displayName = name; this.description = desc; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    public enum CanvasBackground {
        DARK_OBSIDIAN("Deep Obsidian (#101115)", new Color(16, 17, 21)),
        WHITE_PAPER("Clean Studio Light (#F4F5F8)", new Color(244, 245, 248)),
        STUDIO_GREY("Neutral Studio Grey (#22252C)", new Color(34, 37, 44));

        private final String displayName;
        private final Color color;
        CanvasBackground(String name, Color c) { this.displayName = name; this.color = c; }
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
        @Override public String toString() { return displayName; }
    }

    public enum GridColorPreset {
        APPLE_BLUE("Apple Blue", new Color(40, 110, 240, 160)),
        EMERALD_GREEN("Emerald Green", new Color(48, 209, 88, 160)),
        AMBER_ORANGE("Amber Orange", new Color(255, 159, 10, 160)),
        SLATE_GREY("Subtle Slate", new Color(140, 150, 170, 160));

        private final String displayName;
        private final Color color;
        GridColorPreset(String name, Color c) { this.displayName = name; this.color = c; }
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
        @Override public String toString() { return displayName; }
    }

    public enum WorkspaceProfile {
        STANDARD("Standard Studio", "Balanced layout with canvas and full sidebar controls"),
        DRAWING_FOCUS("Drawing Focus", "Maximized canvas with collapsed sidebar for pure inspection"),
        ALGORITHM_TUNING("Algorithm Tuning", "Expanded sidebar with advanced mathematical parameters open"),
        MACHINE_SETUP("Machine Setup", "Machine calibration panel active with grid guides enabled"),
        COMPACT("Compact Workspace", "Streamlined icon-only toolbar with dense controls");

        private final String displayName;
        private final String description;
        WorkspaceProfile(String name, String desc) { this.displayName = name; this.description = desc; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    private static final DrawbotPreferences INSTANCE = new DrawbotPreferences();
    public static DrawbotPreferences getInstance() { return INSTANCE; }

    // --- Active State Settings ---
    private ThemePreset theme = ThemePreset.LIQUID_GLASS_DARK;
    private AccentColorPreset accentColor = AccentColorPreset.APPLE_BLUE;
    private float glassOpacity = 0.85f;
    private float specularIntensity = 0.80f;
    private int cornerRadius = 8;

    private ToolbarMode toolbarMode = ToolbarMode.COMFORTABLE;
    private CanvasBackground canvasBackground = CanvasBackground.DARK_OBSIDIAN;
    private GridColorPreset gridColor = GridColorPreset.APPLE_BLUE;
    private boolean gridEnabled = true;

    private boolean reducedMotion = false;
    private boolean reducedTransparency = false;
    private boolean highReadability = false;

    private WorkspaceProfile workspaceProfile = WorkspaceProfile.STANDARD;

    private final List<Consumer<DrawbotPreferences>> changeListeners = new ArrayList<>();
    private final File prefsFile;

    private DrawbotPreferences() {
        File targetFile;
        try {
            String userHome = System.getProperty("user.home");
            File configDir = new File(userHome, ".drawbot");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            targetFile = new File(configDir, "preferences.json");
        } catch (Exception e) {
            targetFile = new File("preferences.json");
        }
        this.prefsFile = targetFile;
        load();
    }

    public synchronized void addChangeListener(Consumer<DrawbotPreferences> listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public synchronized void removeChangeListener(Consumer<DrawbotPreferences> listener) {
        changeListeners.remove(listener);
    }

    private synchronized void notifyListeners() {
        for (Consumer<DrawbotPreferences> l : changeListeners) {
            try {
                l.accept(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- Getters & Setters with change tracking & auto-persist ---
    public ThemePreset getTheme() { return theme; }
    public void setTheme(ThemePreset theme) {
        if (theme != null && this.theme != theme) {
            this.theme = theme;
            save();
            notifyListeners();
        }
    }

    public AccentColorPreset getAccentColor() { return accentColor; }
    public void setAccentColor(AccentColorPreset accent) {
        if (accent != null && this.accentColor != accent) {
            this.accentColor = accent;
            save();
            notifyListeners();
        }
    }

    public float getGlassOpacity() { return glassOpacity; }
    public void setGlassOpacity(float val) {
        float clamped = Math.max(0.40f, Math.min(1.0f, val));
        if (Math.abs(this.glassOpacity - clamped) > 0.01f) {
            this.glassOpacity = clamped;
            save();
            notifyListeners();
        }
    }

    public float getSpecularIntensity() { return specularIntensity; }
    public void setSpecularIntensity(float val) {
        float clamped = Math.max(0.0f, Math.min(1.0f, val));
        if (Math.abs(this.specularIntensity - clamped) > 0.01f) {
            this.specularIntensity = clamped;
            save();
            notifyListeners();
        }
    }

    public int getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(int radius) {
        int clamped = Math.max(4, Math.min(16, radius));
        if (this.cornerRadius != clamped) {
            this.cornerRadius = clamped;
            save();
            notifyListeners();
        }
    }

    public ToolbarMode getToolbarMode() { return toolbarMode; }
    public void setToolbarMode(ToolbarMode mode) {
        if (mode != null && this.toolbarMode != mode) {
            this.toolbarMode = mode;
            save();
            notifyListeners();
        }
    }

    public CanvasBackground getCanvasBackground() { return canvasBackground; }
    public void setCanvasBackground(CanvasBackground bg) {
        if (bg != null && this.canvasBackground != bg) {
            this.canvasBackground = bg;
            save();
            notifyListeners();
        }
    }

    public GridColorPreset getGridColor() { return gridColor; }
    public void setGridColor(GridColorPreset gc) {
        if (gc != null && this.gridColor != gc) {
            this.gridColor = gc;
            save();
            notifyListeners();
        }
    }

    public boolean isGridEnabled() { return gridEnabled; }
    public void setGridEnabled(boolean enabled) {
        if (this.gridEnabled != enabled) {
            this.gridEnabled = enabled;
            save();
            notifyListeners();
        }
    }

    public boolean isReducedMotion() { return reducedMotion; }
    public void setReducedMotion(boolean val) {
        if (this.reducedMotion != val) {
            this.reducedMotion = val;
            save();
            notifyListeners();
        }
    }

    public boolean isReducedTransparency() { return reducedTransparency; }
    public void setReducedTransparency(boolean val) {
        if (this.reducedTransparency != val) {
            this.reducedTransparency = val;
            save();
            notifyListeners();
        }
    }

    public boolean isHighReadability() { return highReadability; }
    public void setHighReadability(boolean val) {
        if (this.highReadability != val) {
            this.highReadability = val;
            save();
            notifyListeners();
        }
    }

    public WorkspaceProfile getWorkspaceProfile() { return workspaceProfile; }
    public void setWorkspaceProfile(WorkspaceProfile profile) {
        if (profile != null && this.workspaceProfile != profile) {
            this.workspaceProfile = profile;
            save();
            notifyListeners();
        }
    }

    // --- Reset Methods ---
    public void resetAppearance() {
        this.theme = ThemePreset.LIQUID_GLASS_DARK;
        this.accentColor = AccentColorPreset.APPLE_BLUE;
        this.glassOpacity = 0.85f;
        this.specularIntensity = 0.80f;
        this.cornerRadius = 8;
        save();
        notifyListeners();
    }

    public void resetCanvas() {
        this.canvasBackground = CanvasBackground.DARK_OBSIDIAN;
        this.gridColor = GridColorPreset.APPLE_BLUE;
        this.gridEnabled = true;
        this.toolbarMode = ToolbarMode.COMFORTABLE;
        this.workspaceProfile = WorkspaceProfile.STANDARD;
        save();
        notifyListeners();
    }

    public void resetAccessibility() {
        this.reducedMotion = false;
        this.reducedTransparency = false;
        this.highReadability = false;
        save();
        notifyListeners();
    }

    public void resetAll() {
        this.theme = ThemePreset.LIQUID_GLASS_DARK;
        this.accentColor = AccentColorPreset.APPLE_BLUE;
        this.glassOpacity = 0.85f;
        this.specularIntensity = 0.80f;
        this.cornerRadius = 8;
        this.toolbarMode = ToolbarMode.COMFORTABLE;
        this.canvasBackground = CanvasBackground.DARK_OBSIDIAN;
        this.gridColor = GridColorPreset.APPLE_BLUE;
        this.gridEnabled = true;
        this.reducedMotion = false;
        this.reducedTransparency = false;
        this.highReadability = false;
        this.workspaceProfile = WorkspaceProfile.STANDARD;
        save();
        notifyListeners();
    }

    // --- Persistence (JSON File I/O) ---
    public synchronized void save() {
        try {
            File parent = prefsFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefsFile), StandardCharsets.UTF_8))) {
                writer.write("{\n");
                writer.write("  \"version\": 1,\n");
                writer.write("  \"theme\": \"" + theme.name() + "\",\n");
                writer.write("  \"accentColor\": \"" + accentColor.name() + "\",\n");
                writer.write("  \"glassOpacity\": " + String.format(java.util.Locale.US, "%.2f", glassOpacity) + ",\n");
                writer.write("  \"specularIntensity\": " + String.format(java.util.Locale.US, "%.2f", specularIntensity) + ",\n");
                writer.write("  \"cornerRadius\": " + cornerRadius + ",\n");
                writer.write("  \"toolbarMode\": \"" + toolbarMode.name() + "\",\n");
                writer.write("  \"canvasBackground\": \"" + canvasBackground.name() + "\",\n");
                writer.write("  \"gridColor\": \"" + gridColor.name() + "\",\n");
                writer.write("  \"gridEnabled\": " + gridEnabled + ",\n");
                writer.write("  \"reducedMotion\": " + reducedMotion + ",\n");
                writer.write("  \"reducedTransparency\": " + reducedTransparency + ",\n");
                writer.write("  \"highReadability\": " + highReadability + ",\n");
                writer.write("  \"workspaceProfile\": \"" + workspaceProfile.name() + "\"\n");
                writer.write("}\n");
            }
        } catch (Exception ex) {
            System.err.println("Failed to save preferences: " + ex.getMessage());
        }
    }

    public synchronized void load() {
        if (!prefsFile.exists() || prefsFile.length() == 0) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(prefsFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"theme\":")) {
                    String val = extractStringValue(line);
                    try { this.theme = ThemePreset.valueOf(val); } catch (Exception ignored) {}
                } else if (line.startsWith("\"accentColor\":")) {
                    String val = extractStringValue(line);
                    try { this.accentColor = AccentColorPreset.valueOf(val); } catch (Exception ignored) {}
                } else if (line.startsWith("\"glassOpacity\":")) {
                    this.glassOpacity = extractFloatValue(line, 0.85f);
                } else if (line.startsWith("\"specularIntensity\":")) {
                    this.specularIntensity = extractFloatValue(line, 0.80f);
                } else if (line.startsWith("\"cornerRadius\":")) {
                    this.cornerRadius = (int) extractFloatValue(line, 8.0f);
                } else if (line.startsWith("\"toolbarMode\":")) {
                    String val = extractStringValue(line);
                    try { this.toolbarMode = ToolbarMode.valueOf(val); } catch (Exception ignored) {}
                } else if (line.startsWith("\"canvasBackground\":")) {
                    String val = extractStringValue(line);
                    try { this.canvasBackground = CanvasBackground.valueOf(val); } catch (Exception ignored) {}
                } else if (line.startsWith("\"gridColor\":")) {
                    String val = extractStringValue(line);
                    try { this.gridColor = GridColorPreset.valueOf(val); } catch (Exception ignored) {}
                } else if (line.startsWith("\"gridEnabled\":")) {
                    this.gridEnabled = extractBooleanValue(line, true);
                } else if (line.startsWith("\"reducedMotion\":")) {
                    this.reducedMotion = extractBooleanValue(line, false);
                } else if (line.startsWith("\"reducedTransparency\":")) {
                    this.reducedTransparency = extractBooleanValue(line, false);
                } else if (line.startsWith("\"highReadability\":")) {
                    this.highReadability = extractBooleanValue(line, false);
                } else if (line.startsWith("\"workspaceProfile\":")) {
                    String val = extractStringValue(line);
                    try { this.workspaceProfile = WorkspaceProfile.valueOf(val); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to load preferences: " + ex.getMessage());
        }
    }

    private static String extractStringValue(String line) {
        int firstQuote = line.indexOf(':', 0);
        if (firstQuote < 0) return "";
        int start = line.indexOf('"', firstQuote);
        if (start < 0) return "";
        int end = line.indexOf('"', start + 1);
        if (end < 0) return "";
        return line.substring(start + 1, end).trim();
    }

    private static float extractFloatValue(String line, float fallback) {
        try {
            int colon = line.indexOf(':');
            if (colon < 0) return fallback;
            String raw = line.substring(colon + 1).replace(',', ' ').trim();
            return Float.parseFloat(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean extractBooleanValue(String line, boolean fallback) {
        try {
            int colon = line.indexOf(':');
            if (colon < 0) return fallback;
            String raw = line.substring(colon + 1).replace(',', ' ').trim().toLowerCase();
            return Boolean.parseBoolean(raw);
        } catch (Exception e) {
            return fallback;
        }
    }
}
