package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.async.GenerationWorker;
import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.BorderFilter;
import com.dullbits.drawbot.image.ConvolutionEngine;
import com.dullbits.drawbot.image.ImageAdjustments;
import com.dullbits.drawbot.image.ImageFilters;
import com.dullbits.drawbot.image.ImagePipeline;
import com.dullbits.drawbot.image.UrlImageDownloader;
import com.dullbits.drawbot.pfm.PathFindingModule;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Main application window for Drawbot Studio.
 * Unites the multi-panel workspace, interactive canvas, sidebar controls,
 * menu bars, keybindings, and background worker lifecycle.
 */
public final class MainWindow extends JFrame {

    private final ImagePipeline imagePipeline;
    private final SegmentBuffer segmentBuffer;
    private final MachineConfig machineConfig;
    private final PenDistribution penDistribution;

    private final DrawingCanvas canvas;
    private final ImageAdjustments imageAdjustments = new ImageAdjustments();
    private final ToolbarPanel toolbar;
    private final ControlPanel controlPanel;
    private final StatusBarPanel statusBar;
    private final JSplitPane splitPane;
    private final JPanel topContainer;

    private GenerationWorker currentWorker = null;
    private String currentBaseName = "drawbot_project";
    private File currentImageFile = null;

    public MainWindow() {
        // Clean Unicode em-dash (\u2014) to guarantee zero encoding corruption
        super("Drawbot Studio \u2014 Professional Drawing & G-Code Generator");

        // Apply active user preferences before building UI
        DrawbotTheme.applyPreferences(DrawbotPreferences.getInstance());

        this.imagePipeline = new ImagePipeline();
        this.segmentBuffer = new SegmentBuffer();
        this.machineConfig = new MachineConfig();
        this.penDistribution = new PenDistribution(machineConfig.getPenCount());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Responsive screen-adaptive sizing
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int initialW = Math.max(950, Math.min(1380, (int) (screen.width * 0.88)));
            int initialH = Math.max(620, Math.min(880, (int) (screen.height * 0.88)));
            setSize(initialW, initialH);
            setMinimumSize(new Dimension(880, 580));
        } catch (Exception e) {
            setSize(1280, 800);
            setMinimumSize(new Dimension(880, 580));
        }
        setLocationRelativeTo(null);
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);
        setLayout(new BorderLayout());

        // Set application window and taskbar icon
        try {
            File iconFile = new File("assets/icon.png");
            if (iconFile.exists()) {
                setIconImage(ImageIO.read(iconFile));
            } else {
                var stream = getClass().getResourceAsStream("/assets/icon.png");
                if (stream != null) {
                    setIconImage(ImageIO.read(stream));
                }
            }
        } catch (Exception ignored) {}

        // 1. Build Components
        canvas = new DrawingCanvas(imagePipeline, segmentBuffer, machineConfig, penDistribution);
        toolbar = new ToolbarPanel();
        controlPanel = new ControlPanel(machineConfig, penDistribution, segmentBuffer, imageAdjustments,
                                        this::onConfigOrStrokesChanged, this::runPreprocessing);
        statusBar = new StatusBarPanel();

        // 2. Assemble Layout: Top portion unified into a single dark chrome container with Liquid Glass luster
        JMenuBar mb = setupMenuBar();
        mb.setOpaque(false);
        mb.setBorder(null);

        this.topContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                DrawbotTheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();

                // Subtle Liquid Glass top luster gradient
                LinearGradientPaint glassLuster = new LinearGradientPaint(
                    0, 0, 0, Math.min(32, h),
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(255, 255, 255, 12), new Color(255, 255, 255, 0)}
                );
                g2.setPaint(glassLuster);
                g2.fillRect(0, 0, w, Math.min(32, h));

                // Dual-tone bottom divider: 1px dark contact shadow + 1px specular highlight rim
                g2.setColor(DrawbotTheme.BORDER_GLASS_BOTTOM);
                g2.drawLine(0, h - 2, w, h - 2);
                g2.setColor(DrawbotTheme.BORDER_SUBTLE);
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        topContainer.setBackground(DrawbotTheme.BG_TOOLBAR);
        topContainer.setBorder(null);
        topContainer.add(mb, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, controlPanel);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setBackground(DrawbotTheme.BG_WINDOW);
        splitPane.resetToPreferredSizes();
        splitPane.setDividerLocation(0.72);
        add(splitPane, BorderLayout.CENTER);

        add(statusBar, BorderLayout.SOUTH);

        // 3. Setup Actions & Events
        setupToolbarEvents();
        setupKeyBindings();
        controlPanel.setOnGenerateAction(this::startGeneration);

        canvas.setFileDropListener(this::loadImageFile);
        statusBar.setCancelAction(this::stopGeneration);

        // 4. Apply startup workspace profile and toolbar density
        toolbar.updateToolbarDensity(DrawbotPreferences.getInstance().getToolbarMode());
        applyWorkspaceProfile(DrawbotPreferences.getInstance().getWorkspaceProfile());
    }

    private JMenuBar setupMenuBar() {
        JMenuBar mb = new JMenuBar();
        DrawbotTheme.styleMenuBar(mb);

        // File Menu
        JMenu fileMenu = new JMenu("File");
        DrawbotTheme.styleMenu(fileMenu);

        JMenuItem openItem = createStyledItem("Open Image...", e -> chooseImageFile());
        JMenuItem urlItem = createStyledItem("Download Image from URL...", e -> promptUrlDownload());
        JMenuItem quickSvgItem = createStyledItem("Quick Export SVG... (V)", e -> exportQuickSvg());
        JMenuItem exportItem = createStyledItem("Export All (G-Code, SVG, PDF)...", e -> openExportDialog());
        JMenuItem exitItem = createStyledItem("Exit", e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(urlItem);
        DrawbotTheme.addMenuSeparator(fileMenu);
        fileMenu.add(quickSvgItem);
        fileMenu.add(exportItem);
        DrawbotTheme.addMenuSeparator(fileMenu);
        fileMenu.add(exitItem);
        mb.add(fileMenu);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        DrawbotTheme.styleMenu(editMenu);
        editMenu.add(createStyledItem("Image Adjustments & Filters...", e -> controlPanel.setSelectedTab(1)));
        DrawbotTheme.addMenuSeparator(editMenu);
        editMenu.add(createStyledItem("Distribute Pens: Even (1/N)", e -> {
            penDistribution.setEvenDistribution(segmentBuffer.getLineCount());
            controlPanel.repaint();
            canvas.repaint();
        }));
        editMenu.add(createStyledItem("Distribute Pens: Monochrome", e -> {
            penDistribution.setMonochromeDistribution(segmentBuffer.getLineCount());
            controlPanel.repaint();
            canvas.repaint();
        }));
        DrawbotTheme.addMenuSeparator(editMenu);
        editMenu.add(createStyledItem("Preferences... (Ctrl+,)", e -> openPreferencesDialog()));
        mb.add(editMenu);

        // Process Menu
        JMenu processMenu = new JMenu("Process");
        DrawbotTheme.styleMenu(processMenu);
        processMenu.add(createStyledItem("\u25B6 Generate Drawing Paths", e -> startGeneration()));
        processMenu.add(createStyledItem("\u25A0 Stop Generation", e -> stopGeneration()));
        DrawbotTheme.addMenuSeparator(processMenu);
        processMenu.add(createStyledItem("Algorithm: Original Squiggles (Area)", e -> controlPanel.setPfmIndex(0)));
        processMenu.add(createStyledItem("Algorithm: Darkest-Seeking Rays (Global)", e -> controlPanel.setPfmIndex(1)));
        processMenu.add(createStyledItem("Algorithm: Modulated Spiral", e -> controlPanel.setPfmIndex(2)));
        processMenu.add(createStyledItem("Algorithm: Horizontal Waveforms (Scanlines)", e -> controlPanel.setPfmIndex(3)));
        processMenu.add(createStyledItem("Algorithm: Multi-Angle Crosshatch (Etching)", e -> controlPanel.setPfmIndex(4)));
        processMenu.add(createStyledItem("Algorithm: Concentric Rings (Ripples)", e -> controlPanel.setPfmIndex(5)));
        processMenu.add(createStyledItem("Algorithm: Wavy Squares", e -> controlPanel.setPfmIndex(6)));
        mb.add(processMenu);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        DrawbotTheme.styleMenu(viewMenu);
        viewMenu.add(createStyledItem("Fit Drawing to View", e -> canvas.fitToView()));
        viewMenu.add(createStyledItem("Reset View (1:1)", e -> canvas.resetView()));
        viewMenu.add(createStyledItem("Rotate 90\u00B0 Clockwise", e -> canvas.rotate90()));
        viewMenu.add(createStyledItem("Toggle Machine Grid & Guides", e -> {
            toolbar.getGridToggle().setSelected(!toolbar.getGridToggle().isSelected());
            canvas.toggleGrid();
        }));
        DrawbotTheme.addMenuSeparator(viewMenu);

        JMenu profilesMenu = new JMenu("Workspace Profiles");
        DrawbotTheme.styleMenu(profilesMenu);
        profilesMenu.add(createStyledItem("Standard Studio", e -> applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.STANDARD)));
        profilesMenu.add(createStyledItem("Drawing Focus (Canvas Maximized)", e -> applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.DRAWING_FOCUS)));
        profilesMenu.add(createStyledItem("Algorithm Tuning (Advanced Parameters)", e -> applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.ALGORITHM_TUNING)));
        profilesMenu.add(createStyledItem("Machine Setup (Machine Tab + Grid)", e -> applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.MACHINE_SETUP)));
        profilesMenu.add(createStyledItem("Compact Workspace (Icon-Only)", e -> applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile.COMPACT)));
        viewMenu.add(profilesMenu);

        JMenu themesMenu = new JMenu("Theme Preset");
        DrawbotTheme.styleMenu(themesMenu);
        themesMenu.add(createStyledItem("Dark Theme (Default)", e -> {
            DrawbotPreferences.getInstance().setTheme(DrawbotPreferences.ThemePreset.LIQUID_GLASS_DARK);
            onPreferencesUpdated();
        }));
        themesMenu.add(createStyledItem("Deep Dark", e -> {
            DrawbotPreferences.getInstance().setTheme(DrawbotPreferences.ThemePreset.OBSIDIAN_DARK);
            onPreferencesUpdated();
        }));
        themesMenu.add(createStyledItem("Light Theme", e -> {
            DrawbotPreferences.getInstance().setTheme(DrawbotPreferences.ThemePreset.MONTEREY_LIGHT);
            onPreferencesUpdated();
        }));
        themesMenu.add(createStyledItem("High Contrast", e -> {
            DrawbotPreferences.getInstance().setTheme(DrawbotPreferences.ThemePreset.HIGH_CONTRAST);
            onPreferencesUpdated();
        }));
        viewMenu.add(themesMenu);

        DrawbotTheme.addMenuSeparator(viewMenu);
        viewMenu.add(createStyledItem("View: Drawing Output", e -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.DRAWING)));
        viewMenu.add(createStyledItem("View: Reference Image", e -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.REFERENCE)));
        viewMenu.add(createStyledItem("View: Original Image", e -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.ORIGINAL)));
        mb.add(viewMenu);

        // Tools Menu (Includes Secret Z30 G-Code Fixer & Preferences)
        JMenu toolsMenu = new JMenu("Tools");
        DrawbotTheme.styleMenu(toolsMenu);
        toolsMenu.add(createStyledItem("Workspace Preferences... (Ctrl+,)", e -> openPreferencesDialog()));
        toolsMenu.add(createStyledItem("G-Code Z-Height Fixer... (F9)", e -> openGcodeFixerDialog()));
        mb.add(toolsMenu);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        DrawbotTheme.styleMenu(helpMenu);
        helpMenu.add(createStyledItem("G-Code Z30 Clearance Fixer...", e -> openGcodeFixerDialog()));
        DrawbotTheme.addMenuSeparator(helpMenu);
        helpMenu.add(createStyledItem("About Drawbot Studio", e -> {
            JOptionPane.showMessageDialog(this,
                "Drawbot Studio \u2014 Professional Drawing & G-Code Generator\n\n" +
                "Engineered from Drawbot_image_to_gcode_v2 (GPL-3.0)\n" +
                "High-performance artistic path generator with live preview,\n" +
                "integrated image adjustments, multi-pen Copic sets, and instant SVG/G-code export.",
                "About Drawbot Studio", JOptionPane.INFORMATION_MESSAGE);
        }));
        mb.add(helpMenu);

        return mb;
    }

    private static JMenuItem createStyledItem(String text, java.awt.event.ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        DrawbotTheme.styleMenuItem(item);
        item.addActionListener(al);
        return item;
    }

    private void setupToolbarEvents() {
        toolbar.getOpenBtn().addActionListener(e -> chooseImageFile());
        toolbar.getUrlBtn().addActionListener(e -> promptUrlDownload());
        toolbar.getGenerateBtn().addActionListener(e -> startGeneration());
        toolbar.getStopBtn().addActionListener(e -> stopGeneration());

        toolbar.getModeCombo().addActionListener(e -> {
            DrawingCanvas.DisplayMode mode = (DrawingCanvas.DisplayMode) toolbar.getModeCombo().getSelectedItem();
            canvas.setDisplayMode(mode);
            toolbar.getPenFilterCombo().setEnabled(mode == DrawingCanvas.DisplayMode.PEN_ONLY);
        });

        toolbar.getPenFilterCombo().addActionListener(e -> {
            int idx = toolbar.getPenFilterCombo().getSelectedIndex();
            canvas.setSelectedPen(Math.max(0, idx - 1));
        });

        toolbar.getRotateBtn().addActionListener(e -> canvas.rotate90());
        toolbar.getFitBtn().addActionListener(e -> canvas.fitToView());
        toolbar.getResetBtn().addActionListener(e -> canvas.resetView());
        toolbar.getGridToggle().addActionListener(e -> canvas.toggleGrid());
        toolbar.getQuickSvgBtn().addActionListener(e -> exportQuickSvg());
        toolbar.getExportBtn().addActionListener(e -> openExportDialog());
        toolbar.getAdjustBtn().addActionListener(e -> controlPanel.setSelectedTab(1));
        toolbar.getSettingsBtn().addActionListener(e -> openPreferencesDialog());
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        registerKey(im, am, "zoomIn", KeyStroke.getKeyStroke(']'), () -> canvas.zoomIn());
        registerKey(im, am, "zoomOut", KeyStroke.getKeyStroke('['), () -> canvas.zoomOut());
        registerKey(im, am, "resetView", KeyStroke.getKeyStroke('\\'), () -> canvas.resetView());
        registerKey(im, am, "rotate", KeyStroke.getKeyStroke('r'), () -> canvas.rotate90());
        registerKey(im, am, "grid", KeyStroke.getKeyStroke('G'), () -> {
            toolbar.getGridToggle().setSelected(!toolbar.getGridToggle().isSelected());
            canvas.toggleGrid();
        });
        registerKey(im, am, "drawingMode", KeyStroke.getKeyStroke('d'), () -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.DRAWING));
        registerKey(im, am, "originalMode", KeyStroke.getKeyStroke('O'), () -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.ORIGINAL));
        registerKey(im, am, "refMode", KeyStroke.getKeyStroke('o'), () -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.REFERENCE));
        registerKey(im, am, "lightMode", KeyStroke.getKeyStroke('l'), () -> toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.LIGHTENED));
        registerKey(im, am, "quickSvg", KeyStroke.getKeyStroke('v'), this::exportQuickSvg);
        registerKey(im, am, "export", KeyStroke.getKeyStroke('g'), this::openExportDialog);
        registerKey(im, am, "stop", KeyStroke.getKeyStroke('s'), this::stopGeneration);
        registerKey(im, am, "gcodeFixer", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0), this::openGcodeFixerDialog);
        registerKey(im, am, "preferences", KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), this::openPreferencesDialog);
    }

    private void registerKey(InputMap im, ActionMap am, String name, KeyStroke ks, Runnable act) {
        im.put(ks, name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                act.run();
            }
        });
    }

    public void chooseImageFile() {
        File file = FileDialogHelper.chooseImageFile(this);
        if (file != null) {
            loadImageFile(file);
        }
    }

    public void loadImageFile(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                JOptionPane.showMessageDialog(this, "The selected file is not a supported image format.", "Error Loading Image", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.currentImageFile = file;
            String name = file.getName();
            int dotIdx = name.lastIndexOf('.');
            this.currentBaseName = (dotIdx > 0) ? name.substring(0, dotIdx) : name;

            imagePipeline.setOriginalImage(img);
            runPreprocessing();
            toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.REFERENCE);
            controlPanel.setSelectedTab(1); // Select Image Adjust tab so user can adjust framing
            canvas.fitToView();
            statusBar.setStatus("Loaded image: " + file.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ") \u2014 Fit to frame without crop. Click \u25B6 Generate when ready.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void promptUrlDownload() {
        String url = JOptionPane.showInputDialog(this, "Enter direct image URL (JPEG/PNG/BMP):", "Download Image", JOptionPane.PLAIN_MESSAGE);
        if (url != null && !url.trim().isEmpty()) {
            statusBar.setStatus("Downloading image from URL...");
            new Thread(() -> {
                try {
                    BufferedImage img = UrlImageDownloader.downloadImage(url);
                    SwingUtilities.invokeLater(() -> {
                        imagePipeline.setOriginalImage(img);
                        this.currentBaseName = "downloaded_image";
                        runPreprocessing();
                        toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.REFERENCE);
                        controlPanel.setSelectedTab(1);
                        canvas.fitToView();
                        statusBar.setStatus("Downloaded image successfully (" + img.getWidth() + "x" + img.getHeight() + ") \u2014 Frame adjusted. Click \u25B6 Generate when ready.");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "URL download failed: " + ex.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
                        statusBar.setStatus("URL download failed.");
                    });
                }
            }).start();
        }
    }

    public void runPreprocessing() {
        BufferedImage orig = imagePipeline.getOriginalImage();
        if (orig == null) return;
        PathFindingModule pfm = controlPanel.getSelectedPfm();
        statusBar.startProcessing("Preprocessing image frame & tone...");
        canvas.setProcessing(true, "Framing & Adjusting Image", pfm.getName());

        // 1. Frame original image onto machine drawing area according to user framing controls (No Crop!)
        BufferedImage ref = ImageFilters.fitImageToFrame(orig, machineConfig, imageAdjustments);

        // 2. Interactive Tone & Contrast adjustments
        if (imageAdjustments.getBrightness() != 0 || imageAdjustments.getContrast() != 0 || imageAdjustments.isInvert()) {
            ref = ImageFilters.adjustBrightnessContrast(
                ref,
                imageAdjustments.getBrightness(),
                imageAdjustments.getContrast(),
                imageAdjustments.isInvert()
            );
        }

        // 3. Detail sharpness
        if (imageAdjustments.getUnsharpStrength() > 0) {
            ref = ConvolutionEngine.unsharpen(ref, imageAdjustments.getUnsharpStrength());
        }

        // 4. Border vignette blending
        if (!"none".equalsIgnoreCase(imageAdjustments.getBorderName())) {
            ref = BorderFilter.applyBorder(ref, imageAdjustments.getBorderName(), 0, 0);
        }

        // 5. Desaturate to pure drawing luminance
        ref = ImageFilters.desaturate(ref);

        imagePipeline.setReferenceImage(ref);
        imagePipeline.resetWorkingToReference();

        canvas.setProcessing(false, null, null);
        statusBar.stopProcessing("Ready \u2014 Fitted to frame (" + ref.getWidth() + "x" + ref.getHeight() + ")");
        canvas.repaint();
    }

    public void startGeneration() {
        if (imagePipeline.getOriginalImage() == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentWorker != null && !currentWorker.isDone()) {
            return;
        }

        runPreprocessing();

        toolbar.getGenerateBtn().setEnabled(false);
        toolbar.getStopBtn().setEnabled(true);
        toolbar.getModeCombo().setSelectedItem(DrawingCanvas.DisplayMode.DRAWING);

        PathFindingModule pfm = controlPanel.getSelectedPfm();
        statusBar.startProcessing("Generating paths with " + pfm.getName() + "...");
        canvas.setProcessing(true, "Generating Paths", pfm.getName());

        currentWorker = new GenerationWorker(
            pfm, imagePipeline, segmentBuffer, machineConfig, penDistribution,
            canvas, statusBar, () -> {
                toolbar.getGenerateBtn().setEnabled(true);
                toolbar.getStopBtn().setEnabled(false);
                controlPanel.updateStrokeBudgetRange(segmentBuffer.getLineCount());
                canvas.repaint();
            }
        );
        currentWorker.execute();
    }

    public void stopGeneration() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
            statusBar.setStatus("Stopping generation...");
        }
    }

    private void onConfigOrStrokesChanged() {
        int limit = controlPanel.getDisplayedStrokeLimit();
        canvas.setDisplayLineCount(limit);
        canvas.repaint();
    }

    public void exportQuickSvg() {
        if (segmentBuffer.getLineCount() == 0) {
            JOptionPane.showMessageDialog(this, "No drawing paths generated yet. Click 'Generate' first.",
                "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File targetFile = FileDialogHelper.chooseSaveSvgFile(this, currentBaseName);
        if (targetFile != null) {

            try {
                BufferedImage ref = imagePipeline.getReferenceImage();
                int w = ref != null ? ref.getWidth() : (int) machineConfig.getImageWidthMm();
                int h = ref != null ? ref.getHeight() : (int) machineConfig.getImageHeightMm();
                int strokeLimit = controlPanel.getDisplayedStrokeLimit();

                com.dullbits.drawbot.export.SvgExporter.exportSvgDirect(
                    targetFile, segmentBuffer, machineConfig, penDistribution, strokeLimit, w, h
                );
                statusBar.setStatus("Quick SVG exported: " + targetFile.getName());

                int opt = JOptionPane.showConfirmDialog(
                    this,
                    "SVG Vector drawing exported successfully!\n\nFile: " + targetFile.getName() +
                    "\nLocation: " + targetFile.getAbsolutePath() +
                    "\n\nWould you like to open the output folder in Explorer?",
                    "Quick SVG Export Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                if (opt == JOptionPane.YES_OPTION && targetFile.getParentFile() != null) {
                    try {
                        Desktop.getDesktop().open(targetFile.getParentFile());
                    } catch (Exception ignored) {}
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export SVG: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openExportDialog() {
        if (segmentBuffer.getLineCount() == 0) {
            JOptionPane.showMessageDialog(this, "No drawing paths generated yet. Click 'Generate' first.",
                "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        BufferedImage ref = imagePipeline.getReferenceImage();
        int w = ref != null ? ref.getWidth() : (int) machineConfig.getImageWidthMm();
        int h = ref != null ? ref.getHeight() : (int) machineConfig.getImageHeightMm();

        ExportDialog dialog = new ExportDialog(
            this, currentBaseName, segmentBuffer, machineConfig, penDistribution,
            controlPanel.getDisplayedStrokeLimit(), w, h
        );
        dialog.setVisible(true);
    }

    public void openGcodeFixerDialog() {
        GcodeFixerDialog dialog = new GcodeFixerDialog(this);
        dialog.setVisible(true);
    }

    public void openPreferencesDialog() {
        PreferencesDialog dialog = new PreferencesDialog(this, this::onPreferencesUpdated);
        dialog.setVisible(true);
    }

    public void applyWorkspaceProfile(DrawbotPreferences.WorkspaceProfile profile) {
        if (profile == null) return;
        switch (profile) {
            case DRAWING_FOCUS -> {
                splitPane.setDividerLocation(0.98);
            }
            case ALGORITHM_TUNING -> {
                splitPane.setDividerLocation(0.68);
                controlPanel.setSelectedTab(0);
                controlPanel.setAdvancedExpanded(true);
            }
            case MACHINE_SETUP -> {
                splitPane.setDividerLocation(0.70);
                controlPanel.setSelectedTab(2); // Machine setup tab
                if (!toolbar.getGridToggle().isSelected()) {
                    toolbar.getGridToggle().setSelected(true);
                    canvas.toggleGrid();
                }
            }
            case COMPACT -> {
                toolbar.updateToolbarDensity(DrawbotPreferences.ToolbarMode.COMPACT);
                splitPane.setDividerLocation(0.75);
            }
            case STANDARD -> {
                toolbar.updateToolbarDensity(DrawbotPreferences.ToolbarMode.COMFORTABLE);
                splitPane.setDividerLocation(0.72);
                controlPanel.setSelectedTab(0);
                controlPanel.setAdvancedExpanded(false);
            }
        }
    }

    public void onPreferencesUpdated() {
        DrawbotPreferences prefs = DrawbotPreferences.getInstance();
        DrawbotTheme.applyPreferences(prefs);
        toolbar.updateToolbarDensity(prefs.getToolbarMode());
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);
        topContainer.setBackground(DrawbotTheme.BG_TOOLBAR);
        splitPane.setBackground(DrawbotTheme.BG_WINDOW);
        canvas.setBackground(prefs.getCanvasBackground().getColor());
        controlPanel.updateThemeStyles();
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    public DrawingCanvas getCanvas() { return canvas; }
    public ToolbarPanel getToolbar() { return toolbar; }
    public ControlPanel getControlPanel() { return controlPanel; }
    public StatusBarPanel getStatusBar() { return statusBar; }
    public SegmentBuffer getSegmentBuffer() { return segmentBuffer; }
    public ImagePipeline getImagePipeline() { return imagePipeline; }
    public com.dullbits.drawbot.core.MachineConfig getMachineConfig() { return machineConfig; }
    public com.dullbits.drawbot.color.PenDistribution getPenDistribution() { return penDistribution; }
}
