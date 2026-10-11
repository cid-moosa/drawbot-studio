package com.dullbits.drawbot.ui;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.export.GcodeExporter;
import com.dullbits.drawbot.export.PdfExporter;
import com.dullbits.drawbot.export.SvgExporter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * High-contrast modal dialog for configuring and executing all vector and G-code export routines.
 */
public final class ExportDialog extends JDialog {

    private final SegmentBuffer segmentBuffer;
    private final MachineConfig machineConfig;
    private final PenDistribution penDistribution;
    private final int displayLineCount;
    private final int imgWidth;
    private final int imgHeight;

    private final JTextField dirField;
    private final JTextField nameField;
    private final JCheckBox gcodeCheck;
    private final JCheckBox testGcodeCheck;
    private final JCheckBox svgCheck;
    private final JCheckBox combinedPdfCheck;
    private final JCheckBox perPenPdfCheck;
    private final JTextArea logArea;

    public ExportDialog(Frame owner, String defaultBaseName, SegmentBuffer buffer,
                        MachineConfig config, PenDistribution distribution,
                        int displayLineCount, int imgWidth, int imgHeight) {
        super(owner, "Export Drawing & G-Code Files", true);
        this.segmentBuffer = buffer;
        this.machineConfig = config;
        this.penDistribution = distribution;
        this.displayLineCount = displayLineCount;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;

        setSize(560, 520);
        setMinimumSize(new Dimension(500, 460));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(DrawbotTheme.BG_WINDOW);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Output Directory Row Card
        JPanel dirCard = new JPanel(new BorderLayout(8, 6));
        dirCard.setOpaque(true);
        dirCard.setBackground(DrawbotTheme.BG_CARD);
        dirCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel dirLbl = new JLabel("Destination Directory:");
        dirLbl.setFont(DrawbotTheme.FONT_BOLD);
        dirLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        dirCard.add(dirLbl, BorderLayout.NORTH);

        File defaultDir = new File("gcode");
        dirField = new JTextField(defaultDir.getAbsolutePath());
        dirField.setBackground(DrawbotTheme.BG_INPUT);
        dirField.setForeground(DrawbotTheme.TEXT_PRIMARY);
        dirField.setFont(DrawbotTheme.FONT_MONO);
        dirField.setBorder(new LineBorder(DrawbotTheme.BORDER_SUBTLE, 1));
        dirCard.add(dirField, BorderLayout.CENTER);

        JPanel dirBtnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        dirBtnBox.setOpaque(false);

        JButton downloadsBtn = DrawbotTheme.createSecondaryButton("Downloads");
        downloadsBtn.setPreferredSize(new Dimension(90, 28));
        downloadsBtn.setToolTipText("Set export directory to Downloads folder");
        downloadsBtn.addActionListener(e -> dirField.setText(FileDialogHelper.getDownloadsFolder().getAbsolutePath()));

        JButton browseBtn = DrawbotTheme.createSecondaryButton("Browse...");
        browseBtn.setPreferredSize(new Dimension(85, 28));
        browseBtn.addActionListener(e -> {
            File chosen = FileDialogHelper.chooseDirectory(this, "Select Export Directory", new File(dirField.getText()));
            if (chosen != null) {
                dirField.setText(chosen.getAbsolutePath());
            }
        });

        dirBtnBox.add(downloadsBtn);
        dirBtnBox.add(browseBtn);
        dirCard.add(dirBtnBox, BorderLayout.EAST);
        formPanel.add(dirCard);
        formPanel.add(Box.createVerticalStrut(10));

        // 2. Base Filename Card
        JPanel nameCard = new JPanel(new BorderLayout(8, 6));
        nameCard.setOpaque(true);
        nameCard.setBackground(DrawbotTheme.BG_CARD);
        nameCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createGlassCardBorder(DrawbotTheme.CORNER_RADIUS_CARD),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel nameLbl = new JLabel("Base Export Name (Prefix):");
        nameLbl.setFont(DrawbotTheme.FONT_BOLD);
        nameLbl.setForeground(DrawbotTheme.TEXT_PRIMARY);
        nameCard.add(nameLbl, BorderLayout.NORTH);

        nameField = new JTextField(defaultBaseName != null ? defaultBaseName : "drawbot_output");
        nameField.setBackground(DrawbotTheme.BG_INPUT);
        nameField.setForeground(DrawbotTheme.TEXT_PRIMARY);
        nameField.setFont(DrawbotTheme.FONT_REGULAR);
        nameField.setBorder(new LineBorder(DrawbotTheme.BORDER_SUBTLE, 1));
        nameCard.add(nameField, BorderLayout.CENTER);
        formPanel.add(nameCard);
        formPanel.add(Box.createVerticalStrut(10));

        // 3. Format Checkboxes Card
        JPanel formatsCard = new JPanel(new GridLayout(3, 2, 8, 6));
        formatsCard.setOpaque(true);
        formatsCard.setBackground(DrawbotTheme.BG_CARD);
        formatsCard.setBorder(BorderFactory.createCompoundBorder(
            DrawbotTheme.createSectionBorder("Export Formats"),
            new EmptyBorder(8, 10, 8, 10)
        ));

        gcodeCheck = createCheckbox("G-Code Per Pen (.txt)", true);
        testGcodeCheck = createCheckbox("Boundary Test G-Code (_test.txt)", true);
        svgCheck = createCheckbox("Combined Vector SVG (.svg)", true);
        combinedPdfCheck = createCheckbox("Combined Multi-Pen PDF (.pdf)", true);
        perPenPdfCheck = createCheckbox("Individual Per-Pen PDFs", true);

        formatsCard.add(gcodeCheck);
        formatsCard.add(testGcodeCheck);
        formatsCard.add(svgCheck);
        formatsCard.add(combinedPdfCheck);
        formatsCard.add(perPenPdfCheck);
        formPanel.add(formatsCard);
        formPanel.add(Box.createVerticalStrut(10));

        // 4. Log text area
        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        logArea.setBackground(DrawbotTheme.BG_INPUT);
        logArea.setForeground(new Color(150, 225, 160));
        logArea.setFont(DrawbotTheme.FONT_MONO);
        logArea.setBorder(new EmptyBorder(4, 6, 4, 6));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(DrawbotTheme.BORDER_SUBTLE, 1));
        formPanel.add(logScroll);

        add(formPanel, BorderLayout.CENTER);

        // 5. Action Buttons (Open Folder, Export Files, Close)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(true);
        btnPanel.setBackground(DrawbotTheme.BG_TOOLBAR);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, DrawbotTheme.BORDER_SUBTLE));

        JButton openFolderBtn = DrawbotTheme.createSecondaryButton("Open Folder");
        openFolderBtn.setEnabled(false);
        openFolderBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(dirField.getText().trim()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open folder: " + ex.getMessage());
            }
        });

        JButton runExportBtn = DrawbotTheme.createPrimaryButton("Export Files", DrawbotTheme.ACCENT_WARNING);
        runExportBtn.addActionListener(e -> runExport(openFolderBtn));

        JButton closeBtn = DrawbotTheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(openFolderBtn);
        btnPanel.add(runExportBtn);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JCheckBox createCheckbox(String label, boolean selected) {
        JCheckBox cb = new JCheckBox(label, selected);
        cb.setOpaque(false);
        cb.setForeground(DrawbotTheme.TEXT_PRIMARY);
        cb.setFont(DrawbotTheme.FONT_REGULAR);
        cb.setFocusPainted(false);
        return cb;
    }

    private void runExport(JButton openFolderBtn) {
        File dir = new File(dirField.getText().trim());
        String baseName = nameField.getText().trim();
        if (baseName.isEmpty()) baseName = "drawbot_output";

        logArea.setText("Starting export to: " + dir.getAbsolutePath() + "\n");
        List<File> generated = new ArrayList<>();

        try {
            if (gcodeCheck.isSelected() || testGcodeCheck.isSelected()) {
                GcodeExporter.ExportResult res = GcodeExporter.exportAll(
                    dir, baseName, segmentBuffer, machineConfig, penDistribution,
                    displayLineCount, imgWidth, imgHeight);

                for (File f : res.generatedFiles) {
                    logArea.append("Generated G-Code: " + f.getName() + "\n");
                    generated.add(f);
                }
            }

            if (svgCheck.isSelected()) {
                File svgFile = SvgExporter.exportSvg(
                    dir, baseName, segmentBuffer, machineConfig, penDistribution,
                    displayLineCount, imgWidth, imgHeight);
                logArea.append("Generated SVG: " + svgFile.getName() + "\n");
                generated.add(svgFile);
            }

            if (combinedPdfCheck.isSelected() || perPenPdfCheck.isSelected()) {
                List<File> pdfs = PdfExporter.exportAllPdf(
                    dir, baseName, segmentBuffer, machineConfig, penDistribution,
                    displayLineCount, imgWidth, imgHeight);
                for (File f : pdfs) {
                    logArea.append("Generated PDF: " + f.getName() + "\n");
                    generated.add(f);
                }
            }

            logArea.append("\nSUCCESS: " + generated.size() + " files generated successfully.");
            openFolderBtn.setEnabled(true);
        } catch (Exception ex) {
            logArea.append("\nERROR during export: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
