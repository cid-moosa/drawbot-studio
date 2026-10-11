package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.ui.DrawbotPreferences;
import com.dullbits.drawbot.ui.DrawbotTheme;
import com.dullbits.drawbot.ui.MainWindow;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class PreferencesTests {

    public static void runAll() throws Exception {
        System.out.println("--- Running PreferencesTests ---");
        testDefaultValues();
        testThemeAndAccentMutation();
        testSerializationRoundTrip();
        testCategoryResets();
        testWorkspaceProfiles();
        System.out.println("PreferencesTests: ALL PASSED\n");
    }

    private static void testDefaultValues() {
        DrawbotPreferences prefs = DrawbotPreferences.getInstance();
        prefs.resetAll();

        assertCondition(prefs.getTheme() == DrawbotPreferences.ThemePreset.LIQUID_GLASS_DARK, "Default theme should be LIQUID_GLASS_DARK");
        assertCondition(prefs.getAccentColor() == DrawbotPreferences.AccentColorPreset.APPLE_BLUE, "Default accent should be APPLE_BLUE");
        assertCondition(Math.abs(prefs.getGlassOpacity() - 0.85f) < 0.01f, "Default glass opacity should be 0.85");
        assertCondition(prefs.getCornerRadius() == 8, "Default corner radius should be 8");
        assertCondition(prefs.getToolbarMode() == DrawbotPreferences.ToolbarMode.COMFORTABLE, "Default toolbar mode should be COMFORTABLE");
        assertCondition(!prefs.isReducedMotion(), "Reduced motion should be false by default");
        assertCondition(!prefs.isReducedTransparency(), "Reduced transparency should be false by default");
        System.out.println("  [PASS] testDefaultValues");
    }

    private static void testThemeAndAccentMutation() {
        DrawbotPreferences prefs = DrawbotPreferences.getInstance();

        // Switch to Monterey Light
        prefs.setTheme(DrawbotPreferences.ThemePreset.MONTEREY_LIGHT);
        prefs.setAccentColor(DrawbotPreferences.AccentColorPreset.EMERALD_GREEN);
        DrawbotTheme.applyPreferences(prefs);

        assertCondition(DrawbotTheme.ACCENT_PRIMARY.equals(DrawbotPreferences.AccentColorPreset.EMERALD_GREEN.getColor()),
            "DrawbotTheme.ACCENT_PRIMARY should match Emerald Green");
        assertCondition(DrawbotTheme.BG_WINDOW.getRed() > 200, "Monterey Light window background should be bright silver/white");
        assertCondition(DrawbotTheme.TEXT_PRIMARY.getRed() < 50, "Monterey Light text primary should be high-contrast dark");

        // Switch to High Contrast Dark
        prefs.setTheme(DrawbotPreferences.ThemePreset.HIGH_CONTRAST);
        DrawbotTheme.applyPreferences(prefs);
        assertCondition(DrawbotTheme.BG_WINDOW.equals(Color.BLACK), "High Contrast window background should be pure black");
        assertCondition(DrawbotTheme.TEXT_PRIMARY.equals(Color.WHITE), "High Contrast text primary should be pure white");

        // Reset
        prefs.resetAll();
        DrawbotTheme.applyPreferences(prefs);
        System.out.println("  [PASS] testThemeAndAccentMutation");
    }

    private static void testSerializationRoundTrip() {
        DrawbotPreferences prefs = DrawbotPreferences.getInstance();
        prefs.setTheme(DrawbotPreferences.ThemePreset.OBSIDIAN_DARK);
        prefs.setAccentColor(DrawbotPreferences.AccentColorPreset.PURPLE);
        prefs.setGlassOpacity(0.65f);
        prefs.setSpecularIntensity(0.40f);
        prefs.setCornerRadius(12);
        prefs.setToolbarMode(DrawbotPreferences.ToolbarMode.COMPACT);
        prefs.setReducedMotion(true);
        prefs.save();

        // Reload
        prefs.load();
        assertCondition(prefs.getTheme() == DrawbotPreferences.ThemePreset.OBSIDIAN_DARK, "Persisted theme should be OBSIDIAN_DARK");
        assertCondition(prefs.getAccentColor() == DrawbotPreferences.AccentColorPreset.PURPLE, "Persisted accent should be PURPLE");
        assertCondition(Math.abs(prefs.getGlassOpacity() - 0.65f) < 0.05f, "Persisted opacity should be ~0.65");
        assertCondition(prefs.getCornerRadius() == 12, "Persisted corner radius should be 12");
        assertCondition(prefs.getToolbarMode() == DrawbotPreferences.ToolbarMode.COMPACT, "Persisted toolbar mode should be COMPACT");
        assertCondition(prefs.isReducedMotion(), "Persisted reduced motion should be true");

        prefs.resetAll();
        System.out.println("  [PASS] testSerializationRoundTrip");
    }

    private static void testCategoryResets() {
        DrawbotPreferences prefs = DrawbotPreferences.getInstance();

        // Mutate appearance & canvas
        prefs.setTheme(DrawbotPreferences.ThemePreset.MONTEREY_LIGHT);
        prefs.setGridEnabled(false);
        prefs.setReducedMotion(true);

        // Reset appearance only
        prefs.resetAppearance();
        assertCondition(prefs.getTheme() == DrawbotPreferences.ThemePreset.LIQUID_GLASS_DARK, "Appearance should be reset to default");
        assertCondition(!prefs.isGridEnabled(), "Grid should still be disabled after appearance-only reset");
        assertCondition(prefs.isReducedMotion(), "Reduced motion should still be enabled after appearance-only reset");

        // Reset canvas only
        prefs.resetCanvas();
        assertCondition(prefs.isGridEnabled(), "Grid should now be reset to enabled");
        assertCondition(prefs.isReducedMotion(), "Reduced motion should still be enabled");

        // Reset accessibility
        prefs.resetAccessibility();
        assertCondition(!prefs.isReducedMotion(), "Reduced motion should now be reset to false");

        System.out.println("  [PASS] testCategoryResets");
    }

    private static void testWorkspaceProfiles() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("  [PASS] testWorkspaceProfiles (Headless environment detected - verified profile persistence)");
            for (DrawbotPreferences.WorkspaceProfile profile : DrawbotPreferences.WorkspaceProfile.values()) {
                DrawbotPreferences.getInstance().setWorkspaceProfile(profile);
                assertCondition(DrawbotPreferences.getInstance().getWorkspaceProfile() == profile, "Profile should match");
            }
            return;
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainWindow win = new MainWindow();
                win.setSize(1280, 800);

                // Apply all 5 profiles without throwing exceptions
                for (DrawbotPreferences.WorkspaceProfile profile : DrawbotPreferences.WorkspaceProfile.values()) {
                    win.applyWorkspaceProfile(profile);
                    win.doLayout();
                    win.validate();
                }
                win.dispose();
                System.out.println("  [PASS] testWorkspaceProfiles");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static void assertCondition(boolean cond, String message) {
        if (!cond) {
            throw new AssertionError("Preferences Assertion Failed: " + message);
        }
    }
}
