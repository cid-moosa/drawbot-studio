package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.export.GcodeFixer;
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Modern modal dialog for patching machine G-code files.
 * Specifically updates initial rapid safe Z-clearance height (Line 9: G0 Z1.5 -> G0 Z30).
 */
public final class GcodeFixerDialog extends JDialog {

    private File currentFile;
    private final JTextField filePathField;
    private final JSpinner lineSpinner;
    private final JTextField replacementField;
    private final JTextArea previewOriginalArea;
    private final JTextArea previewPatchedArea;
    private final JLabel statusLabel;
    private final JButton overwriteBtn;
    private final JButton saveAsBtn;

    public GcodeFixerDialog(Frame owner) {
        super(owner, "G-Code Z-Height Fixer (Secret Clearance Tool)", true);
        setSize(820, 720);
        setPreferredSize(new Dimension(820, 720));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);

        // --- 1. Header Banner ---
        JPanel headerPanel = new JPanel(new BorderLayout(8, 6));
        headerPanel.setBackground(DrawbotTheme.BG_CARD);
        headerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel("G-Code Z-Height Safe Clearance Fixer");
        titleLbl.setFont(DrawbotTheme.FONT_TITLE);
        titleLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel("<html>Corrects initial clearance moves in CNC/Drawbot G-code (e.g. changing <b>Line 9: G0 Z1.5 \u2794 G0 Z30</b> to prevent dragging across clips).</html>");
        descLbl.setFont(DrawbotTheme.FONT_REGULAR);
        descLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(descLbl, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Center Content ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(DrawbotTheme.BG_WINDOW);
        centerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // File selection card
        JPanel fileCard = createCardPanel("Target G-Code File");
        filePathField = new JTextField();
        filePathField.setBackground(DrawbotTheme.BG_INPUT);
        filePathField.setForeground(DrawbotTheme.TEXT_PRIMARY);
        filePathField.setFont(DrawbotTheme.FONT_MONO);
        filePathField.setEditable(false);
        filePathField.setBorder(new LineBorder(DrawbotTheme.BORDER_SUBTLE, 1));

        JPanel fileBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        fileBtnPanel.setOpaque(false);

        JButton browseBtn = DrawbotTheme.createSecondaryButton("Browse...");
        browseBtn.setPreferredSize(new Dimension(85, 28));
        browseBtn.addActionListener(e -> {
            File chosen = FileDialogHelper.chooseGcodeFile(this);
            if (chosen != null) {
                loadFile(chosen);
            }
        });

        JButton loadRefBtn = DrawbotTheme.createSecondaryButton("Reference G-Code");
        loadRefBtn.setPreferredSize(new Dimension(140, 28));
        loadRefBtn.setToolTipText("Load referance/drawing.gcode directly");
        loadRefBtn.addActionListener(e -> {
            File refFile = new File("referance/drawing.gcode");
            if (refFile.exists()) {
                loadFile(refFile);
            } else {
                JOptionPane.showMessageDialog(this, "File not found: " + refFile.getAbsolutePath(), "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        });

        fileBtnPanel.add(loadRefBtn);
        fileBtnPanel.add(browseBtn);

        JPanel fileInner = new JPanel(new BorderLayout(8, 0));
        fileInner.setOpaque(false);
        fileInner.add(filePathField, BorderLayout.CENTER);
        fileInner.add(fileBtnPanel, BorderLayout.EAST);
        fileCard.add(fileInner, BorderLayout.CENTER);
        centerPanel.add(fileCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // Settings card
        JPanel settingsCard = createCardPanel("Patch Parameters");
        JPanel settingsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        settingsGrid.setOpaque(false);

        JLabel lineLbl = new JLabel("Target Line Number:");
        lineLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        lineLbl.setFont(DrawbotTheme.FONT_REGULAR);
        lineSpinner = new JSpinner(new SpinnerNumberModel(9, 1, 100000, 1));
        lineSpinner.setPreferredSize(new Dimension(80, 28));
        lineSpinner.addChangeListener(e -> updatePreviews());

        JLabel repLbl = new JLabel("Replacement Instruction:");
        repLbl.setForeground(DrawbotTheme.TEXT_SECONDARY);
        repLbl.setFont(DrawbotTheme.FONT_REGULAR);
        replacementField = new JTextField("G0 Z30", 12);
        replacementField.setBackground(DrawbotTheme.BG_INPUT);
        replacementField.setForeground(Color.WHITE);
        replacementField.setCaretColor(Color.WHITE);
        replacementField.setFont(DrawbotTheme.FONT_MONO);
        replacementField.setBorder(new LineBorder(DrawbotTheme.BORDER_SUBTLE, 1));
        replacementField.addActionListener(e -> updatePreviews());

        settingsGrid.add(lineLbl);
        settingsGrid.add(lineSpinner);
        settingsGrid.add(repLbl);
        settingsGrid.add(replacementField);
        settingsCard.add(settingsGrid, BorderLayout.CENTER);
        centerPanel.add(settingsCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // Side-by-side Preview Card
        JPanel previewCard = createCardPanel("G-Code Header Comparison (First 15 Lines)");
        JPanel previewSplit = new JPanel(new GridLayout(1, 2, 10, 0));
        previewSplit.setOpaque(false);

        // Before
        JPanel origBox = new JPanel(new BorderLayout(0, 4));
        origBox.setOpaque(false);
        JLabel origLbl = new JLabel("Original Header:");
        origLbl.setForeground(DrawbotTheme.TEXT_MUTED);
        origLbl.setFont(DrawbotTheme.FONT_BOLD);
        previewOriginalArea = createMonospaceArea();
        origBox.add(origLbl, BorderLayout.NORTH);
        origBox.add(new JScrollPane(previewOriginalArea), BorderLayout.CENTER);

        // After
        JPanel patchBox = new JPanel(new BorderLayout(0, 4));
        patchBox.setOpaque(false);
        JLabel patchLbl = new JLabel("Patched Header (Safe Z30):");
        patchLbl.setForeground(new Color(110, 190, 120));
        patchLbl.setFont(DrawbotTheme.FONT_BOLD);
        previewPatchedArea = createMonospaceArea();
        patchBox.add(patchLbl, BorderLayout.NORTH);
        patchBox.add(new JScrollPane(previewPatchedArea), BorderLayout.CENTER);

        previewSplit.add(origBox);
        previewSplit.add(patchBox);
        previewCard.add(previewSplit, BorderLayout.CENTER);
        centerPanel.add(previewCard);

        add(centerPanel, BorderLayout.CENTER);

        // --- 3. Footer Bar ---
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0));
        footerPanel.setBackground(DrawbotTheme.BG_CARD);
        footerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        statusLabel = new JLabel("Select a G-code file or load reference drawing.");
        statusLabel.setForeground(DrawbotTheme.TEXT_SECONDARY);
        statusLabel.setFont(DrawbotTheme.FONT_REGULAR);
        footerPanel.add(statusLabel, BorderLayout.WEST);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        overwriteBtn = DrawbotTheme.createPrimaryButton("Overwrite File", DrawbotTheme.ACCENT_WARNING);
        overwriteBtn.setEnabled(false);
        overwriteBtn.addActionListener(e -> applyPatch(true));

        saveAsBtn = DrawbotTheme.createSecondaryButton("Save Fixed As...");
        saveAsBtn.setEnabled(false);
        saveAsBtn.addActionListener(e -> applyPatch(false));

        JButton closeBtn = DrawbotTheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        btnBox.add(saveAsBtn);
        btnBox.add(overwriteBtn);
        btnBox.add(closeBtn);
        footerPanel.add(btnBox, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);

        // Auto-load reference if available
        File refFile = new File("referance/drawing.gcode");
        if (refFile.exists()) {
            loadFile(refFile);
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setOpaque(true);
        card.setBackground(DrawbotTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(DrawbotTheme.FONT_BOLD);
        titleLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        card.add(titleLbl, BorderLayout.NORTH);
        return card;
    }

    private JTextArea createMonospaceArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(new Color(24, 25, 28));
        area.setForeground(DrawbotTheme.TEXT_PRIMARY);
        area.setEditable(false);
        area.setRows(11);
        area.setBorder(new EmptyBorder(4, 6, 4, 6));
        return area;
    }

    public void loadFile(File file) {
        this.currentFile = file;
        this.filePathField.setText(file.getAbsolutePath());
        updatePreviews();
        overwriteBtn.setEnabled(true);
        saveAsBtn.setEnabled(true);
        statusLabel.setText("Loaded: " + file.getName() + " (" + (file.length() / 1024) + " KB)");
    }

    private void updatePreviews() {
        if (currentFile == null || !currentFile.exists()) {
            return;
        }

        int targetLine = (int) lineSpinner.getValue();
        String rep = replacementField.getText().trim();

        List<String> lines = GcodeFixer.readHeaderLines(currentFile, 15);
        StringBuilder sbOrig = new StringBuilder();
        StringBuilder sbPatch = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            String original = lines.get(i);
            sbOrig.append(String.format("%2d: %s%n", lineNum, original));

            if (lineNum == targetLine) {
                sbPatch.append(String.format("%2d: %s  <-- PATCHED%n", lineNum, rep));
            } else {
                sbPatch.append(String.format("%2d: %s%n", lineNum, original));
            }
        }

        previewOriginalArea.setText(sbOrig.toString());
        previewPatchedArea.setText(sbPatch.toString());
        previewOriginalArea.setCaretPosition(0);
        previewPatchedArea.setCaretPosition(0);
    }

    private void applyPatch(boolean overwrite) {
        if (currentFile == null || !currentFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please select a valid G-code file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int targetLine = (int) lineSpinner.getValue();
        String rep = replacementField.getText().trim();
        if (rep.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Replacement instruction cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File targetOut = currentFile;
        if (!overwrite) {
            String defaultName = currentFile.getName();
            int dot = defaultName.lastIndexOf('.');
            if (dot > 0) {
                defaultName = defaultName.substring(0, dot) + "_fixed" + defaultName.substring(dot);
            } else {
                defaultName += "_fixed.gcode";
            }
            targetOut = FileDialogHelper.chooseSaveGcodeFile(this, defaultName);
            if (targetOut == null) {
                return;
            }
        } else {
            int conf = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to overwrite '" + currentFile.getName() + "'?\nLine " + targetLine + " will be changed to '" + rep + "'.",
                "Confirm Overwrite",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (conf != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            GcodeFixer.FixResult res = GcodeFixer.fixLine(currentFile, targetOut, targetLine, rep);
            if (res.success) {
                statusLabel.setText(res.message);
                JOptionPane.showMessageDialog(
                    this,
                    res.message + "\n\nOutput saved to:\n" + targetOut.getAbsolutePath(),
                    "G-Code Patch Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadFile(targetOut);
            } else {
                statusLabel.setText("Error: " + res.message);
                JOptionPane.showMessageDialog(this, res.message, "Patch Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            statusLabel.setText("Failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to patch G-code: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
