package com.dullbits.drawbot;

import com.dullbits.drawbot.ui.MainWindow;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application launcher for Drawbot Studio.
 */
public final class DrawbotStudio {

    public static void log(String msg) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("drawbot_startup.log", true))) {
            pw.println(LocalDateTime.now() + " [LAUNCHER] " + msg);
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        log("==========================================");
        log("Drawbot Studio starting...");
        log("Java Version : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        log("User Dir     : " + System.getProperty("user.dir"));
        log("OS           : " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));

        // Global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            log("UNCAUGHT EXCEPTION on thread " + thread.getName() + ": " + ex.getMessage());
            try (PrintWriter pw = new PrintWriter(new FileWriter("drawbot_startup.log", true))) {
                ex.printStackTrace(pw);
            } catch (Exception ignored) {}
            try {
                JOptionPane.showMessageDialog(null,
                    "An error occurred during execution:\n" + ex.toString() + "\n\nSee drawbot_startup.log for details.",
                    "Drawbot Studio Error", JOptionPane.ERROR_MESSAGE);
            } catch (Throwable ignored) {}
        });

        // Configure native Direct3D rendering & font anti-aliasing
        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.noddraw", "false");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            log("System LookAndFeel applied: " + UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log("Could not set system LookAndFeel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                log("EDT: Instantiating MainWindow...");
                MainWindow window = new MainWindow();
                log("EDT: MainWindow instantiated successfully.");
                window.setVisible(true);
                log("EDT: MainWindow.setVisible(true) completed.");

                // If an image argument was provided, open it automatically
                if (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) {
                    File target = new File(args[0]);
                    if (target.exists()) {
                        log("EDT: Loading command-line image: " + target.getAbsolutePath());
                        window.loadImageFile(target);
                    }
                }
            } catch (Throwable t) {
                log("FATAL ERROR during MainWindow creation: " + t.getMessage());
                try (PrintWriter pw = new PrintWriter(new FileWriter("drawbot_startup.log", true))) {
                    t.printStackTrace(pw);
                } catch (Exception ignored) {}
                JOptionPane.showMessageDialog(null,
                    "Fatal startup error:\n" + t.toString() + "\n\nCheck drawbot_startup.log for details.",
                    "Drawbot Studio Fatal Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
