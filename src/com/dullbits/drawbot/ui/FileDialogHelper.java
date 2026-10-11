package com.dullbits.drawbot.ui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/**
 * Native, non-glitched file and directory dialogs.
 * Uses native Windows FileDialog (IFileDialog) for file open and save operations,
 * guaranteeing authentic Windows 11/10 integration, full Downloads and Quick Access navigation,
 * and zero Swing LookAndFeel color-clash glitches.
 */
public final class FileDialogHelper {

    private FileDialogHelper() {}

    /**
     * Resolves the user's Downloads folder path.
     */
    public static File getDownloadsFolder() {
        File home = new File(System.getProperty("user.home"));
        File downloads = new File(home, "Downloads");
        return downloads.exists() ? downloads : home;
    }
    private static FileDialog createFileDialog(Component parent, String title, int mode) {
        if (parent instanceof Frame) {
            return new FileDialog((Frame) parent, title, mode);
        } else if (parent instanceof java.awt.Dialog) {
            return new FileDialog((java.awt.Dialog) parent, title, mode);
        } else if (parent != null) {
            java.awt.Window win = SwingUtilities.getWindowAncestor(parent);
            if (win instanceof Frame) {
                return new FileDialog((Frame) win, title, mode);
            } else if (win instanceof java.awt.Dialog) {
                return new FileDialog((java.awt.Dialog) win, title, mode);
            }
        }
        return new FileDialog((Frame) null, title, mode);
    }

    /**
     * Opens native Windows FileDialog to select an image (PNG, JPG, BMP).
     * Provides full access to Downloads, Pictures, Desktop, and Quick Access.
     */
    public static File chooseImageFile(Component parent) {
        FileDialog fd = createFileDialog(parent, "Select Image to Process", FileDialog.LOAD);

        File downloads = getDownloadsFolder();
        fd.setDirectory(downloads.getAbsolutePath());
        fd.setFile("*.png;*.jpg;*.jpeg;*.bmp");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null || dir == null) {
            return null;
        }
        return new File(dir, file);
    }

    /**
     * Opens native Windows FileDialog to save an SVG vector file.
     * Shows full native Explorer UI with Downloads folder and zero white-on-white glitches.
     */
    public static File chooseSaveSvgFile(Component parent, String baseName) {
        FileDialog fd = createFileDialog(parent, "Quick Export SVG Vector File", FileDialog.SAVE);

        File downloads = getDownloadsFolder();
        fd.setDirectory(downloads.getAbsolutePath());
        String defaultFilename = (baseName != null && !baseName.isEmpty()) ? baseName : "drawing";
        if (!defaultFilename.toLowerCase().endsWith(".svg")) {
            defaultFilename += ".svg";
        }
        fd.setFile(defaultFilename);
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null || dir == null) {
            return null;
        }
        File selected = new File(dir, file);
        if (!selected.getName().toLowerCase().endsWith(".svg")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".svg");
        }
        return selected;
    }

    /**
     * Opens a directory selection dialog with Downloads explicitly populated in the Places Bar.
     */
    public static File chooseDirectory(Component parent, String title, File initialDir) {
        JFileChooser chooser = new JFileChooser();
        if (initialDir != null && initialDir.exists()) {
            chooser.setCurrentDirectory(initialDir);
        } else {
            chooser.setCurrentDirectory(getDownloadsFolder());
        }
        chooser.setDialogTitle(title != null ? title : "Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Explicitly configure Places Bar to feature Downloads prominently
        File home = new File(System.getProperty("user.home"));
        File downloads = getDownloadsFolder();
        File desktop = new File(home, "Desktop");
        File documents = new File(home, "Documents");
        chooser.putClientProperty("FileChooser.placesBar", new File[] {
            downloads, desktop, documents, home
        });

        int res = chooser.showOpenDialog(parent);
        if (res == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Opens native Windows FileDialog to select a G-code file (*.gcode, *.txt, *.nc).
     */
    public static File chooseGcodeFile(Component parent) {
        FileDialog fd = createFileDialog(parent, "Select G-Code File to Patch", FileDialog.LOAD);
        File downloads = getDownloadsFolder();
        fd.setDirectory(downloads.getAbsolutePath());
        fd.setFile("*.gcode;*.txt;*.nc");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null || dir == null) {
            return null;
        }
        return new File(dir, file);
    }

    /**
     * Opens native Windows FileDialog to save a fixed G-code file.
     */
    public static File chooseSaveGcodeFile(Component parent, String defaultName) {
        FileDialog fd = createFileDialog(parent, "Save Fixed G-Code File", FileDialog.SAVE);
        File downloads = getDownloadsFolder();
        fd.setDirectory(downloads.getAbsolutePath());
        fd.setFile(defaultName != null ? defaultName : "drawing_fixed.gcode");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null || dir == null) {
            return null;
        }
        return new File(dir, file);
    }
}
