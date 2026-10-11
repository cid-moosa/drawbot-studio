package com.dullbits.drawbot.tests;

import com.dullbits.drawbot.pfm.PathFindingModule;
import com.dullbits.drawbot.pfm.PfmSpiral;
import com.dullbits.drawbot.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CaptureMainWindow {

    public static void validateHierarchy(Component c) {
        c.doLayout();
        c.validate();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                validateHierarchy(child);
            }
        }
    }

    private static void saveSnapshot(Container cp, int w, int h, String outPath) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        cp.printAll(g);
        g.dispose();

        File out = new File(outPath);
        ImageIO.write(img, "PNG", out);
        System.out.println("Wrote screenshot: " + out.getAbsolutePath() + " (" + out.length() + " bytes)");
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                int w = 1280;
                int h = 800;
                String baseDir = args.length > 0 ? args[0] : 
                    (new File("C:/Users/CIDMOOSA/.gemini/antigravity-ide/brain/70716596-0adb-4d78-9b08-a7be8f8fcc82/").exists() ?
                     "C:/Users/CIDMOOSA/.gemini/antigravity-ide/brain/70716596-0adb-4d78-9b08-a7be8f8fcc82/" : "builds/captures/");
                new File(baseDir).mkdirs();

                MainWindow win = new MainWindow();
                win.setSize(w, h);
                win.setVisible(true);
                Container cp = win.getContentPane();
                cp.setSize(w, h);

                for (Component c : cp.getComponents()) {
                    if (c instanceof JSplitPane) {
                        ((JSplitPane) c).setDividerLocation(880);
                    }
                }
                validateHierarchy(cp);

                // 1. Empty state
                saveSnapshot(cp, w, h, baseDir + "repaired_gui_empty.png");

                // 2. Load sample image
                File sample = new File("assets/samples/cat1.jpg");
                if (sample.exists()) {
                    win.loadImageFile(sample);
                    cp.setSize(w, h);
                    for (Component c : cp.getComponents()) {
                        if (c instanceof JSplitPane) {
                            ((JSplitPane) c).setDividerLocation(880);
                        }
                    }
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "repaired_gui_loaded.png");

                    // 3. Generate paths
                    if (win.getImagePipeline().getWorkingImage() != null) {
                        PfmSpiral spiral = new PfmSpiral();
                        spiral.findPath(win.getImagePipeline(), win.getSegmentBuffer(), new com.dullbits.drawbot.core.MachineConfig(),
                            new com.dullbits.drawbot.pfm.ProgressCallback() {
                                @Override public void onProgress(int lines, float fraction, String msg) {}
                                @Override public boolean isCancelled() { return false; }
                            });
                        win.getToolbar().getModeCombo().setSelectedItem(com.dullbits.drawbot.ui.DrawingCanvas.DisplayMode.DRAWING);
                        win.getStatusBar().updateStats(win.getSegmentBuffer().getLineCount());
                        win.getStatusBar().setStatus("Generation completed: " + win.getSegmentBuffer().getLineCount() + " lines");
                        win.getControlPanel().updateStrokeBudgetRange(win.getSegmentBuffer().getLineCount());
                        win.getCanvas().repaint();
                        validateHierarchy(cp);
                        saveSnapshot(cp, w, h, baseDir + "repaired_gui_generated.png");
                    }

                    // 4. Image Adjust Tab (Tab 1)
                    win.getControlPanel().setSelectedTab(1);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "repaired_gui_adjust.png");

                    // 5. Pens Tab (Tab 2)
                    win.getControlPanel().setSelectedTab(2);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "repaired_gui_pens.png");

                    // 6. Machine Tab (Tab 3)
                    win.getControlPanel().setSelectedTab(3);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "repaired_gui_machine.png");

                    // 7. Waveform Scanlines
                    win.getSegmentBuffer().clear();
                    com.dullbits.drawbot.pfm.PfmWaveform wave = new com.dullbits.drawbot.pfm.PfmWaveform();
                    win.getControlPanel().setPfmIndex(3);
                    wave.findPath(win.getImagePipeline(), win.getSegmentBuffer(), new com.dullbits.drawbot.core.MachineConfig(), null);
                    win.getStatusBar().updateStats(win.getSegmentBuffer().getLineCount());
                    win.getStatusBar().setStatus("Horizontal Waveform generation: " + win.getSegmentBuffer().getLineCount() + " lines");
                    win.getControlPanel().setSelectedTab(0);
                    win.getCanvas().repaint();
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "repaired_gui_waveform.png");

                    // 7b. Advanced Parameters Expanded Card
                    win.getControlPanel().setPfmIndex(0); // Original Squiggles
                    win.getControlPanel().setAdvancedExpanded(true);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "ui_advanced_parameters_expanded.png");
                    win.getControlPanel().setAdvancedExpanded(false);

                    // 8. Processing Animation State (Spinner + Canvas HUD)
                    win.getStatusBar().startProcessing("Preprocessing image with Original Squiggles...");
                    win.getCanvas().setProcessing(true, "Preprocessing Image", "Sampling luminance grid \u2014 Original Squiggles");
                    win.getCanvas().repaint();
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "ui_processing_animation.png");
                    win.getCanvas().setProcessing(false, null, null);
                    win.getStatusBar().stopProcessing("Ready");

                    // 9. User Image Framing Test (Verify No Crop on Character Image)
                    File userImg = new File("C:/Users/CIDMOOSA/.gemini/antigravity-ide/brain/70716596-0adb-4d78-9b08-a7be8f8fcc82/.user_uploaded/media_1791599214691.png");
                    if (userImg.exists()) {
                        win.loadImageFile(userImg);
                        win.getCanvas().fitToView();
                        validateHierarchy(cp);
                        saveSnapshot(cp, w, h, baseDir + "user_character_framed_no_crop.png");
                    }

                    // 10. Popup Menu with Dark Separators (Fix Verification)
                    JMenuBar mb = (JMenuBar) ((JPanel) cp.getComponent(0)).getComponent(0);
                    // 9. Real Edit Dropdown Menu in Live Window Context
                    JMenu editMenu = mb.getMenu(1);
                    editMenu.doClick();
                    win.getLayeredPane().doLayout();
                    win.getLayeredPane().validate();

                    // Capture live window with Edit dropdown open
                    BufferedImage winImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D gWin = winImg.createGraphics();
                    gWin.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gWin.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    win.getLayeredPane().printAll(gWin);
                    gWin.dispose();

                    // Crop close-up around the menu bar, Edit menu, toolbar, and popup
                    BufferedImage cropOpen = winImg.getSubimage(0, 0, 420, 200);
                    ImageIO.write(cropOpen, "PNG", new File(baseDir + "ui_dropdown_menu_live.png"));
                    System.out.println("Wrote screenshot: " + baseDir + "ui_dropdown_menu_live.png (" + new File(baseDir + "ui_dropdown_menu_live.png").length() + " bytes)");

                    // Hover over first item: "Image Adjustments & Filters..."
                    JMenuItem item0 = (JMenuItem) editMenu.getPopupMenu().getComponent(0);
                    item0.getModel().setArmed(true);
                    item0.getModel().setSelected(true);

                    BufferedImage winImgHover = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D gWinHover = winImgHover.createGraphics();
                    gWinHover.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gWinHover.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    win.getLayeredPane().printAll(gWinHover);
                    gWinHover.dispose();

                    BufferedImage cropHover = winImgHover.getSubimage(0, 0, 420, 200);
                    ImageIO.write(cropHover, "PNG", new File(baseDir + "ui_dropdown_menu_live_hovered.png"));
                    System.out.println("Wrote screenshot: " + baseDir + "ui_dropdown_menu_live_hovered.png (" + new File(baseDir + "ui_dropdown_menu_live_hovered.png").length() + " bytes)");

                    item0.getModel().setArmed(false);
                    item0.getModel().setSelected(false);
                    MenuSelectionManager.defaultManager().clearSelectedPath();

                    // 10. Dark JFileChooser (Fix Verification)
                    JFileChooser chooser = DrawbotTheme.createFileChooser("Select Image to Process", new File("."));
                    chooser.setSize(680, 440);
                    validateHierarchy(chooser);
                    saveSnapshot(chooser, 680, 440, baseDir + "ui_dark_file_chooser.png");

                    // 11. Export Dialog with Downloads button (Fix Verification)
                    com.dullbits.drawbot.ui.ExportDialog expDlg = new com.dullbits.drawbot.ui.ExportDialog(
                        win, "test_output", win.getSegmentBuffer(), win.getMachineConfig(),
                        new com.dullbits.drawbot.color.PenDistribution(), 500, 800, 600
                    );
                    expDlg.pack();
                    expDlg.setSize(600, 620);
                    expDlg.getContentPane().validate();
                    validateHierarchy(expDlg.getContentPane());
                    saveSnapshot(expDlg.getContentPane(), 600, 620, baseDir + "ui_export_dialog.png");
                    expDlg.dispose();

                    // 12. G-Code Fixer Dialog (Secret Z30 Clearance Tool)
                    com.dullbits.drawbot.ui.GcodeFixerDialog fixDlg = new com.dullbits.drawbot.ui.GcodeFixerDialog(win);
                    fixDlg.pack();
                    fixDlg.setSize(820, 720);
                    fixDlg.getContentPane().validate();
                    validateHierarchy(fixDlg.getContentPane());
                    saveSnapshot(fixDlg.getContentPane(), 820, 720, baseDir + "ui_gcode_fixer_dialog.png");
                    fixDlg.dispose();

                    // 13. Liquid Glass Preferences Dialog
                    com.dullbits.drawbot.ui.PreferencesDialog prefDlg = new com.dullbits.drawbot.ui.PreferencesDialog(win, null);
                    prefDlg.pack();
                    prefDlg.setSize(760, 600);
                    prefDlg.getContentPane().validate();
                    validateHierarchy(prefDlg.getContentPane());
                    saveSnapshot(prefDlg.getContentPane(), 760, 600, baseDir + "ui_preferences_dialog.png");
                    prefDlg.dispose();

                    // 14. Workspace Profile: Drawing Focus (Canvas Maximized)
                    win.applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.DRAWING_FOCUS);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "ui_workspace_drawing_focus.png");

                    // 15. Theme Preset: Monterey Light (Studio Light Theme)
                    DrawbotPreferences.getInstance().setTheme(DrawbotPreferences.ThemePreset.MONTEREY_LIGHT);
                    DrawbotPreferences.getInstance().setAccentColor(DrawbotPreferences.AccentColorPreset.APPLE_BLUE);
                    win.onPreferencesUpdated();
                    win.applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.STANDARD);
                    validateHierarchy(cp);
                    saveSnapshot(cp, w, h, baseDir + "ui_monterey_light_theme.png");

                    // Restore default theme for clean state
                    DrawbotPreferences.getInstance().resetAll();
                    win.onPreferencesUpdated();
                }

                win.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
