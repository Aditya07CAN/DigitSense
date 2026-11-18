package com.digitsense.ui;

import com.digitsense.binary.BinaryNumberConverter;
import com.digitsense.letter.LetterClassifier;
import com.digitsense.ml.Recogniser;
import com.digitsense.roman.RomanNumeralConverter;
import com.digitsense.roman.RomanNumeralClassifier;
import com.digitsense.database.DatabaseManager;
import com.digitsense.image.DigitSegmenter;
import com.digitsense.image.ImagePreprocessor;
import com.digitsense.model.DigitModel;
import com.digitsense.model.ModelFactory;
import com.digitsense.model.PredictionResult;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Modern, colorful desktop window for DigitSense AI Studio.
 */
public class DigitSenseFrame extends JFrame implements ActionListener {

    private final DigitCanvas digitCanvas;
    private final JLabel predictionBadge;
    private final JLabel confidenceBadge;
    private final JLabel latencyBadge;
    private final JLabel alternativesLabel;
    private final JLabel conversionLabel;
    private final JButton predictButton;
    private final JButton openImageButton;
    private final JButton clearButton;
    private final JButton saveButton;
    private final JButton exportButton;
    private final JButton correctButton;
    private final JComboBox<String> numberModeChoice;
    private final JTextField correctionField;
    private final JCheckBox consentCheckbox;
    private final DefaultListModel<String> historyListModel;
    private final JList<String> historyList;
    private JPanel historyPanel;
    private int historyPanelWidth = 230;
    private DatabaseManager databaseManager;
    private final DigitModel digitModel;
    private long lastPredictionId = -1;
    private BufferedImage lastProcessedImage;
    private int lastPredictionCount;

    // ── Navigation & Panels ──
    private final QuickPredictPanel quickPredictPanel;
    private final CompareModelsPanel compareModelsPanel;
    private final JButton viewCanvasButton;
    private final JButton viewCompareButton;
    private final CardLayout centerCardLayout;
    private final JPanel centerCardPanel;

    // ── Stroke & Tools ──
    private JButton activeColorButton;

    public DigitSenseFrame() {
        super("DigitSense AI Studio - Handwritten Digit Recognition & Benchmarking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        digitCanvas = new DigitCanvas(520, 420);

        predictionBadge = UITheme.createBadge("Draw a digit & click Predict", UITheme.BG_CARD_LIGHT, UITheme.TEXT_MAIN);
        predictionBadge.setFont(UITheme.FONT_TITLE);

        confidenceBadge = UITheme.createBadge("Confidence: –", UITheme.BG_INPUT, UITheme.TEXT_MUTED);
        latencyBadge = UITheme.createBadge("Latency: –", UITheme.BG_INPUT, UITheme.TEXT_MUTED);

        alternativesLabel = new JLabel("", JLabel.CENTER);
        alternativesLabel.setFont(UITheme.FONT_SMALL);
        alternativesLabel.setForeground(UITheme.TEXT_MUTED);

        conversionLabel = new JLabel("", JLabel.CENTER);
        conversionLabel.setFont(UITheme.FONT_BOLD);
        conversionLabel.setForeground(UITheme.WARNING);

        predictButton = UITheme.createButton("⚡ Predict", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        openImageButton = UITheme.createButton("📂 Open Image", UITheme.CYAN.darker(), UITheme.CYAN);
        clearButton = UITheme.createButton("🗑 Clear", UITheme.DANGER, UITheme.DANGER_HOVER);
        saveButton = UITheme.createButton("💾 Save Canvas", UITheme.SUCCESS, UITheme.SUCCESS_HOVER);
        exportButton = UITheme.createButton("📊 Export CSV", UITheme.PURPLE, new Color(168, 85, 247));
        correctButton = UITheme.createButton("✔ Submit", UITheme.WARNING, UITheme.WARNING_HOVER);

        numberModeChoice = new JComboBox<>(new String[]{
                "Auto detect",
                "Normal digit mode",
                "Letters mode (A-Z)",
                "Binary mode (0 and 1)",
                "Roman numeral mode"
        });
        numberModeChoice.setBackground(UITheme.BG_INPUT);
        numberModeChoice.setForeground(UITheme.TEXT_MAIN);
        numberModeChoice.setFont(UITheme.FONT_BODY);

        correctionField = new JTextField(2);
        correctionField.setBackground(UITheme.BG_INPUT);
        correctionField.setForeground(UITheme.TEXT_MAIN);
        correctionField.setCaretColor(Color.WHITE);
        correctionField.setFont(UITheme.FONT_HEADER);
        correctionField.setHorizontalAlignment(JTextField.CENTER);

        consentCheckbox = new JCheckBox("Consent");
        consentCheckbox.setForeground(UITheme.TEXT_MUTED);
        consentCheckbox.setBackground(UITheme.BG_SURFACE);
        consentCheckbox.setOpaque(false);
        consentCheckbox.setFont(UITheme.FONT_SMALL);

        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        historyList.setFont(UITheme.FONT_SMALL);
        historyList.setBackground(UITheme.BG_CARD);
        historyList.setForeground(UITheme.TEXT_MAIN);
        historyList.setSelectionBackground(UITheme.BG_CARD_LIGHT);
        historyList.setSelectionForeground(UITheme.TEXT_MAIN);

        digitModel = ModelFactory.loadAvailableModel();

        quickPredictPanel = new QuickPredictPanel();
        compareModelsPanel = new CompareModelsPanel(this);

        viewCanvasButton = UITheme.createButton("🎨 Drawing Studio", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        viewCompareButton = UITheme.createButton("⚡ Model Benchmark Arena", UITheme.BG_CARD_LIGHT, UITheme.BORDER_LIGHT);

        centerCardLayout = new CardLayout();
        centerCardPanel = new JPanel(centerCardLayout);
        centerCardPanel.setOpaque(false);

        initializeDatabase();
        buildWindow();
        registerEvents();
        refreshHistory();
    }

    private void initializeDatabase() {
        try {
            databaseManager = new DatabaseManager();
        } catch (SQLException exception) {
            databaseManager = null;
            System.err.println("Database could not be initialized: " + exception.getMessage());
        }
    }

    private void buildWindow() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ── Top Bar: Brand, Navigation Tabs, and Hero Prediction Card ──
        JPanel topContainer = new JPanel(new BorderLayout(0, 4));
        topContainer.setBackground(UITheme.BG_SURFACE);
        topContainer.setBorder(new EmptyBorder(10, 16, 8, 16));

        // Brand & Tab Header
        JPanel brandRow = new JPanel(new BorderLayout(12, 0));
        brandRow.setOpaque(false);

        JLabel brandLabel = new JLabel("✦ DigitSense AI Studio");
        brandLabel.setFont(UITheme.FONT_BRAND);
        brandLabel.setForeground(UITheme.TEXT_MAIN);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tabRow.setOpaque(false);
        tabRow.add(viewCanvasButton);
        tabRow.add(viewCompareButton);

        brandRow.add(brandLabel, BorderLayout.WEST);
        brandRow.add(tabRow, BorderLayout.EAST);

        // Hero Prediction Card
        JPanel heroCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        heroCard.setOpaque(false);
        heroCard.setLayout(new BorderLayout(8, 4));
        heroCard.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        badgeRow.setOpaque(false);
        badgeRow.add(predictionBadge);
        badgeRow.add(confidenceBadge);
        badgeRow.add(latencyBadge);

        JPanel textRow = new JPanel(new GridLayout(2, 1, 0, 2));
        textRow.setOpaque(false);
        textRow.add(alternativesLabel);
        textRow.add(conversionLabel);

        heroCard.add(badgeRow, BorderLayout.NORTH);
        heroCard.add(textRow, BorderLayout.SOUTH);

        topContainer.add(brandRow, BorderLayout.NORTH);
        topContainer.add(heroCard, BorderLayout.SOUTH);

        // ── Drawing Studio Canvas Workspace ──
        JPanel canvasWorkspace = new JPanel(new BorderLayout(8, 0));
        canvasWorkspace.setBackground(UITheme.BG_MAIN);

        // Canvas Toolbar: Colors, Stroke Widths, Grid Toggle
        JPanel canvasToolBar = createDrawingToolbar();

        JPanel canvasContainer = new JPanel(new BorderLayout(0, 6));
        canvasContainer.setBackground(UITheme.BG_MAIN);
        canvasContainer.setBorder(new EmptyBorder(8, 8, 8, 8));
        canvasContainer.add(canvasToolBar, BorderLayout.NORTH);
        canvasContainer.add(digitCanvas, BorderLayout.CENTER);

        // History Sidebar (Right)
        historyPanel = createHistorySidebar();

        canvasWorkspace.add(canvasContainer, BorderLayout.CENTER);
        canvasWorkspace.add(historyPanel, BorderLayout.EAST);

        // Card Panel: Drawing Canvas vs Compare Models Arena
        centerCardPanel.add(canvasWorkspace, "canvas");
        centerCardPanel.add(compareModelsPanel, "compare");

        // Main Workspace: QuickPredict on Left + Center Cards
        JPanel workspacePanel = new JPanel(new BorderLayout(6, 0));
        workspacePanel.setBackground(UITheme.BG_MAIN);
        workspacePanel.add(quickPredictPanel, BorderLayout.WEST);
        workspacePanel.add(centerCardPanel, BorderLayout.CENTER);

        // ── Bottom Action Control Bar ──
        JPanel bottomBar = createBottomControlBar();

        add(topContainer, BorderLayout.NORTH);
        add(workspacePanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        setSize(1060, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
    }

    private JPanel createDrawingToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(UITheme.BG_SURFACE);
        toolbar.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel colorLabel = new JLabel("Palette:");
        colorLabel.setFont(UITheme.FONT_SMALL);
        colorLabel.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(colorLabel);

        // Vibrant Color Swatches
        Color[] colors = {
                Color.BLACK,
                new Color(59, 130, 246),  // Sky Blue
                new Color(239, 68, 68),   // Crimson
                new Color(16, 185, 129),  // Emerald
                new Color(139, 92, 246),  // Purple
                new Color(249, 115, 22),  // Orange
                new Color(236, 72, 153)   // Pink
        };

        for (Color c : colors) {
            JButton swatch = createColorSwatch(c);
            toolbar.add(swatch);
            if (c.equals(Color.BLACK)) {
                activeColorButton = swatch;
                swatch.setBorder(BorderFactory.createLineBorder(UITheme.CYAN, 2));
            }
        }

        // Eraser Tool
        JButton eraserBtn = UITheme.createButton("Eraser", UITheme.BG_INPUT, UITheme.BORDER_LIGHT);
        eraserBtn.setFont(UITheme.FONT_SMALL);
        eraserBtn.addActionListener(e -> {
            digitCanvas.setEraser(true);
            if (activeColorButton != null) {
                activeColorButton.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
            }
        });
        toolbar.add(eraserBtn);

        // Stroke Width selector
        JLabel strokeLabel = new JLabel("  Stroke:");
        strokeLabel.setFont(UITheme.FONT_SMALL);
        strokeLabel.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(strokeLabel);

        JComboBox<String> strokeChoice = new JComboBox<>(new String[]{"Thin (12px)", "Normal (18px)", "Bold (26px)", "Thick (36px)"});
        strokeChoice.setSelectedIndex(1);
        strokeChoice.setFont(UITheme.FONT_SMALL);
        strokeChoice.setBackground(UITheme.BG_INPUT);
        strokeChoice.setForeground(UITheme.TEXT_MAIN);
        strokeChoice.addActionListener(e -> {
            int idx = strokeChoice.getSelectedIndex();
            int[] sizes = {12, 18, 26, 36};
            digitCanvas.setStrokeWidth(sizes[idx]);
        });
        toolbar.add(strokeChoice);

        // Grid Toggle Button
        JButton gridToggle = UITheme.createButton("Grid", UITheme.BG_INPUT, UITheme.BORDER_LIGHT);
        gridToggle.setFont(UITheme.FONT_SMALL);
        gridToggle.addActionListener(e -> digitCanvas.setShowGrid(!digitCanvas.isShowGrid()));
        toolbar.add(gridToggle);

        return toolbar;
    }

    private JButton createColorSwatch(Color color) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            digitCanvas.setPenColor(color);
            if (activeColorButton != null) {
                activeColorButton.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
            }
            activeColorButton = btn;
            btn.setBorder(BorderFactory.createLineBorder(UITheme.CYAN, 2));
        });
        return btn;
    }

    private JPanel createHistorySidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_SURFACE);
        panel.setPreferredSize(new Dimension(historyPanelWidth, 0));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("🕒 Recent Predictions", JLabel.CENTER);
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setBorder(new EmptyBorder(4, 4, 8, 4));

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add resizable draggable divider
        addResizableDivider(panel);
        return panel;
    }

    private void addResizableDivider(JPanel panel) {
        JPanel divider = new JPanel();
        divider.setBackground(UITheme.BORDER);
        divider.setPreferredSize(new Dimension(6, 0));
        divider.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        panel.add(divider, BorderLayout.WEST);
        final int[] dragStartX = {0};
        final int[] dragStartWidth = {historyPanelWidth};

        divider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                dragStartX[0] = event.getXOnScreen();
                dragStartWidth[0] = historyPanelWidth;
            }
        });
        divider.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                int change = dragStartX[0] - event.getXOnScreen();
                historyPanelWidth = Math.max(160, Math.min(420, dragStartWidth[0] + change));
                panel.setPreferredSize(new Dimension(historyPanelWidth, 0));
                panel.revalidate();
            }
        });
    }

    private JPanel createBottomControlBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottomPanel.setBackground(UITheme.BG_SURFACE);
        bottomPanel.setBorder(new EmptyBorder(6, 10, 8, 10));

        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(UITheme.FONT_SMALL);
        modeLabel.setForeground(UITheme.TEXT_MUTED);

        bottomPanel.add(modeLabel);
        bottomPanel.add(numberModeChoice);
        bottomPanel.add(openImageButton);
        bottomPanel.add(predictButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(exportButton);

        JLabel correctLabel = new JLabel("  Fix digit:");
        correctLabel.setFont(UITheme.FONT_SMALL);
        correctLabel.setForeground(UITheme.TEXT_MUTED);
        bottomPanel.add(correctLabel);
        bottomPanel.add(correctionField);
        bottomPanel.add(consentCheckbox);
        bottomPanel.add(correctButton);

        return bottomPanel;
    }

    private void registerEvents() {
        predictButton.addActionListener(this);
        openImageButton.addActionListener(this);
        clearButton.addActionListener(this);
        saveButton.addActionListener(this);
        exportButton.addActionListener(this);
        correctButton.addActionListener(this);

        // Tab Switching
        viewCanvasButton.addActionListener(event -> {
            centerCardLayout.show(centerCardPanel, "canvas");
            viewCanvasButton.setBackground(UITheme.PRIMARY);
            viewCompareButton.setBackground(UITheme.BG_CARD_LIGHT);
        });
        viewCompareButton.addActionListener(event -> {
            centerCardLayout.show(centerCardPanel, "compare");
            viewCanvasButton.setBackground(UITheme.BG_CARD_LIGHT);
            viewCompareButton.setBackground(UITheme.PRIMARY);
        });
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == clearButton) {
            digitCanvas.clear();
            predictionBadge.setText("Draw a digit & click Predict");
            predictionBadge.setForeground(UITheme.TEXT_MAIN);
            confidenceBadge.setText("Confidence: –");
            latencyBadge.setText("Latency: –");
            alternativesLabel.setText("");
            conversionLabel.setText("");
            lastPredictionCount = 0;
        } else if (event.getSource() == predictButton) {
            startPrediction();
        } else if (event.getSource() == openImageButton) {
            openImage();
        } else if (event.getSource() == saveButton) {
            saveImage(digitCanvas.getDrawingImage(), new File("data/canvas-samples/drawing.png"));
            predictionBadge.setText("Drawing saved to data/canvas-samples/drawing.png");
            predictionBadge.setForeground(UITheme.SUCCESS);
        } else if (event.getSource() == exportButton) {
            exportHistory();
        } else if (event.getSource() == correctButton) {
            saveCorrection();
        }
    }

    private void exportHistory() {
        if (databaseManager == null) {
            predictionBadge.setText("Database is unavailable");
            predictionBadge.setForeground(UITheme.DANGER);
            return;
        }
        try {
            databaseManager.exportPredictions(new File("data/exports/predictions.csv"));
            predictionBadge.setText("Exported to data/exports/predictions.csv");
            predictionBadge.setForeground(UITheme.SUCCESS);
        } catch (SQLException | IOException exception) {
            predictionBadge.setText("History export failed");
            predictionBadge.setForeground(UITheme.DANGER);
            System.err.println("History export failed: " + exception.getMessage());
        }
    }

    private void startPrediction() {
        predictButton.setEnabled(false);
        predictionBadge.setText("Processing inference...");
        predictionBadge.setForeground(UITheme.INFO);
        alternativesLabel.setText("");
        conversionLabel.setText("");
        String selectedMode = (String) numberModeChoice.getSelectedItem();

        Thread predictionThread = new Thread(() -> {
            PredictionOutcome outcome = computePrediction(selectedMode != null ? selectedMode : "Auto detect");
            EventQueue.invokeLater(() -> applyPredictionOutcome(outcome));
        }, "digitsense-prediction-worker");
        predictionThread.start();
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Handwritten Digit Image");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(selectedFile);
            if (image == null) {
                predictionBadge.setText("Unsupported image format");
                predictionBadge.setForeground(UITheme.DANGER);
                return;
            }
            digitCanvas.loadImage(image);
            predictionBadge.setText("Loaded: " + selectedFile.getName());
            predictionBadge.setForeground(UITheme.CYAN);
            confidenceBadge.setText("Click Predict");
            latencyBadge.setText("Ready");
            alternativesLabel.setText("Click Predict to recognize digits");
            conversionLabel.setText("");
            lastPredictionCount = 0;
            lastProcessedImage = null;
        } catch (IOException exception) {
            predictionBadge.setText("Could not open image");
            predictionBadge.setForeground(UITheme.DANGER);
            System.err.println("Image loading failed: " + exception.getMessage());
        }
    }

    private PredictionOutcome computePrediction(String selectedMode) {
        BufferedImage drawing = digitCanvas.getDrawingImage();
        List<BufferedImage> segments = DigitSegmenter.segment(drawing);
        if (segments == null || segments.isEmpty()) {
            return PredictionOutcome.error("Please draw a digit on the canvas first");
        }

        if (selectedMode.equals("Letters mode (A-Z)")) {
            StringBuilder letters = new StringBuilder();
            StringBuilder alternatives = new StringBuilder();
            double totalConfidence = 0.0;
            double totalLatencyMs = 0.0;
            BufferedImage processedSingleImage = null;
            boolean anyLowConfidence = false;

            try {
                for (int index = 0; index < segments.size(); index++) {
                    BufferedImage processedImage = ImagePreprocessor.preprocess(segments.get(index));
                    long predictionStart = System.nanoTime();
                    LetterClassifier.LetterResult lResult = LetterClassifier.classify(processedImage);
                    double latencyMs = (System.nanoTime() - predictionStart) / 1_000_000.0;
                    totalLatencyMs += latencyMs;
                    totalConfidence += lResult.confidence();

                    if (!lResult.confident()) {
                        anyLowConfidence = true;
                    }

                    if (segments.size() == 1) {
                        processedSingleImage = processedImage;
                        writeImage(processedImage, new File("data/preprocessed/last-input.png"));
                    } else {
                        writeImage(processedImage, new File("data/preprocessed/letter-" + (index + 1) + ".png"));
                    }

                    letters.append(lResult.letter());
                    if (index > 0) {
                        alternatives.append(" | ");
                    }
                    alternatives.append("Letter ").append(index + 1).append(": ").append(lResult.topSummary());
                }

                int avgConfPct = (int) Math.round((totalConfidence / segments.size()) * 100);
                String fullWord = letters.toString();
                String predictionDisplay;
                if (segments.size() == 1) {
                    predictionDisplay = fullWord + " (" + avgConfPct + "%)";
                } else {
                    predictionDisplay = "Word: " + fullWord + " (" + avgConfPct + "%)";
                }

                String conversionText;
                if (anyLowConfidence || avgConfPct < 60) {
                    conversionText = "Not sure, try drawing again (Confidence: " + avgConfPct + "%)";
                } else {
                    conversionText = "Mode: English Alphabet (A-Z) | Letters: " + fullWord.length();
                }

                String confStr = "Confidence: " + avgConfPct + "%";
                String latencyStr = String.format("Latency: %.1f ms", totalLatencyMs);

                return new PredictionOutcome(predictionDisplay, confStr, latencyStr, alternatives.toString(),
                        conversionText, -1, segments.size(), processedSingleImage, null);
            } catch (IOException exception) {
                return PredictionOutcome.error("Could not process letter image");
            }
        }

        if (selectedMode.equals("Roman numeral mode")) {
            StringBuilder romanChars = new StringBuilder();
            StringBuilder alternatives = new StringBuilder("Strokes: ");
            double totalConfidence = 0.0;
            double totalLatencyMs = 0.0;
            BufferedImage processedSingleImage = null;

            try {
                for (int index = 0; index < segments.size(); index++) {
                    BufferedImage processedImage = ImagePreprocessor.preprocess(segments.get(index));
                    long predictionStart = System.nanoTime();
                    RomanNumeralClassifier.RomanCharResult rChar = RomanNumeralClassifier.classify(processedImage);
                    double latencyMs = (System.nanoTime() - predictionStart) / 1_000_000.0;
                    totalLatencyMs += latencyMs;
                    totalConfidence += rChar.confidence();

                    if (segments.size() == 1) {
                        processedSingleImage = processedImage;
                        writeImage(processedImage, new File("data/preprocessed/last-input.png"));
                    } else {
                        writeImage(processedImage, new File("data/preprocessed/digit-" + (index + 1) + ".png"));
                    }

                    romanChars.append(rChar.character());
                    if (index > 0) {
                        alternatives.append(" | ");
                    }
                    alternatives.append("Char ").append(index + 1).append(": ").append(rChar.character())
                            .append(" (").append((int) Math.round(rChar.confidence() * 100)).append("%)");
                }

                int confPct = (int) Math.round((totalConfidence / segments.size()) * 100);
                String fullRoman = romanChars.toString();
                String predictionDisplay;
                String conversionText;

                try {
                    int decimalVal = RomanNumeralConverter.toDecimal(fullRoman);
                    RomanNumeralConverter.Conversion rConv = RomanNumeralConverter.toRoman(decimalVal);
                    predictionDisplay = "Roman: " + fullRoman + "  [= " + decimalVal + "]";
                    conversionText = "Formula: " + rConv.displayText();
                } catch (Exception ex) {
                    int sum = 0;
                    StringBuilder charSum = new StringBuilder();
                    for (int i = 0; i < fullRoman.length(); i++) {
                        char c = fullRoman.charAt(i);
                        int val = switch (c) {
                            case 'I' -> 1;
                            case 'V' -> 5;
                            case 'X' -> 10;
                            case 'L' -> 50;
                            case 'C' -> 100;
                            case 'D' -> 500;
                            case 'M' -> 1000;
                            default -> 0;
                        };
                        sum += val;
                        if (i > 0) charSum.append(" + ");
                        charSum.append(val).append('(').append(c).append(')');
                    }
                    predictionDisplay = "Roman: " + fullRoman + "  [= " + sum + "]";
                    conversionText = "Formula: " + charSum + " = " + sum;
                }

                String confStr = "Confidence: " + confPct + "%";
                String latencyStr = String.format("Latency: %.1f ms", totalLatencyMs);

                return new PredictionOutcome(predictionDisplay, confStr, latencyStr, alternatives.toString(),
                        conversionText, -1, segments.size(), processedSingleImage, null);
            } catch (IOException exception) {
                return PredictionOutcome.error("Could not process Roman numeral image");
            }
        }

        StringBuilder predictedText = new StringBuilder("Prediction: ");
        StringBuilder alternatives = new StringBuilder("Alternatives: ");
        StringBuilder binaryBits = new StringBuilder();
        StringBuilder rawDigits = new StringBuilder();
        boolean binaryCandidate = true;
        BufferedImage processedSingleImage = null;
        long predictionId = -1;
        double totalLatencyMs = 0.0;
        int confPct = 0;

        try {
            for (int index = 0; index < segments.size(); index++) {
                BufferedImage processedImage = ImagePreprocessor.preprocess(segments.get(index));
                long predictionStart = System.nanoTime();
                PredictionResult result = digitModel.predict(processedImage);
                double latencyMs = (System.nanoTime() - predictionStart) / 1_000_000.0;
                totalLatencyMs += latencyMs;
                confPct = (int) Math.round(result.confidence() * 100);

                if (segments.size() == 1) {
                    processedSingleImage = processedImage;
                    writeImage(processedImage, new File("data/preprocessed/last-input.png"));
                } else {
                    writeImage(processedImage, new File("data/preprocessed/digit-" + (index + 1) + ".png"));
                }

                if (result.isValidDigit()) {
                    predictedText.append(result.digit());
                    rawDigits.append(result.digit());
                    if (result.digit() == 0 || result.digit() == 1) {
                        binaryBits.append(result.digit());
                    } else {
                        binaryCandidate = false;
                    }
                    if (index > 0) {
                        alternatives.append(" | ");
                    }
                    alternatives.append("Digit ").append(index + 1).append(": ").append(result.alternativesSummary());
                } else {
                    predictedText.append("?");
                    binaryCandidate = false;
                }

                if (databaseManager != null) {
                    predictionId = databaseManager.savePrediction(processedImage,
                            result.digit(), result.confidence(), result.modelVersion(), latencyMs);
                }
            }

            String conversionText = "";
            boolean romanMode = selectedMode.equals("Roman numeral mode");
            boolean binaryMode = selectedMode.equals("Binary mode (0 and 1)")
                    || (selectedMode.equals("Auto detect") && binaryCandidate && binaryBits.length() > 0);

            if (romanMode) {
                if (rawDigits.length() == 0) {
                    conversionText = "Please draw or enter valid digits";
                } else {
                    try {
                        RomanNumeralConverter.Conversion rConv = RomanNumeralConverter.toRoman(rawDigits.toString());
                        conversionText = "Formula: " + rConv.displayText();
                        predictedText = new StringBuilder("Roman: ").append(rConv.roman())
                                .append("  [= ").append(rawDigits).append("]");
                    } catch (Exception ex) {
                        conversionText = "Roman: " + ex.getMessage();
                    }
                }
            } else if (binaryMode) {
                if (!binaryCandidate) {
                    conversionText = "Binary mode accepts only 0 and 1";
                } else {
                    BinaryNumberConverter.Conversion conversion = BinaryNumberConverter.convert(binaryBits.toString());
                    conversionText = "Formula: " + conversion.displayText();
                    predictedText = new StringBuilder("Decimal: ").append(conversion.decimal());
                }
            } else {
                // Auto detect & Normal digit mode:
                // Show decimal prediction AND Roman numeral conversion automatically!
                if (rawDigits.length() > 0) {
                    try {
                        RomanNumeralConverter.Conversion rConv = RomanNumeralConverter.toRoman(rawDigits.toString());
                        conversionText = "Decimal " + rawDigits + " ➔ Roman: " + rConv.roman() + " (" + rConv.breakdown() + ")";
                    } catch (Exception ignored) {
                        conversionText = "Type: Normal decimal digit(s)";
                    }
                } else {
                    conversionText = "Type: Normal decimal digit(s)";
                }
            }

            String confStr = "Confidence: " + confPct + "%";
            String latencyStr = String.format("Latency: %.1f ms", totalLatencyMs);

            return new PredictionOutcome(predictedText.toString(), confStr, latencyStr, alternatives.toString(),
                    conversionText, predictionId, segments.size(), processedSingleImage, null);
        } catch (SQLException | IOException exception) {
            return PredictionOutcome.error("Prediction could not be saved to database");
        }
    }

    private void applyPredictionOutcome(PredictionOutcome outcome) {
        predictButton.setEnabled(true);
        if (outcome.errorMessage() != null) {
            predictionBadge.setText(outcome.errorMessage());
            predictionBadge.setForeground(UITheme.WARNING);
            confidenceBadge.setText("Confidence: –");
            latencyBadge.setText("Latency: –");
            return;
        }

        predictionBadge.setText(outcome.predictionText());
        predictionBadge.setForeground(UITheme.CYAN);
        confidenceBadge.setText(outcome.confidenceText());
        confidenceBadge.setForeground(UITheme.SUCCESS);
        latencyBadge.setText(outcome.latencyText());
        latencyBadge.setForeground(UITheme.WARNING);

        alternativesLabel.setText(outcome.alternativesText());
        conversionLabel.setText(outcome.conversionText());

        lastPredictionId = outcome.predictionId();
        lastPredictionCount = outcome.predictionCount();
        lastProcessedImage = outcome.processedImage();
        refreshHistory();
    }

    private void refreshHistory() {
        if (databaseManager == null) {
            return;
        }
        try {
            historyListModel.clear();
            for (String summary : databaseManager.getRecentPredictionSummaries(12)) {
                historyListModel.addElement(summary);
            }
        } catch (SQLException exception) {
            historyListModel.clear();
            historyListModel.addElement("History unavailable");
        }
    }

    private void saveCorrection() {
        if (lastPredictionCount > 1) {
            predictionBadge.setText("Correction supports one digit at a time");
            predictionBadge.setForeground(UITheme.WARNING);
            return;
        }
        if (databaseManager == null || lastPredictionId < 0) {
            predictionBadge.setText("Click Predict before saving a correction");
            predictionBadge.setForeground(UITheme.WARNING);
            return;
        }

        try {
            int correctDigit = Integer.parseInt(correctionField.getText().trim());
            if (correctDigit < 0 || correctDigit > 9) {
                predictionBadge.setText("Correct digit must be between 0 and 9");
                predictionBadge.setForeground(UITheme.WARNING);
                return;
            }
            if (!consentCheckbox.isSelected()) {
                predictionBadge.setText("Check Consent before saving feedback");
                predictionBadge.setForeground(UITheme.WARNING);
                return;
            }

            databaseManager.saveFeedback(lastPredictionId, correctDigit, true);
            saveTrainingSample(processedImageForFeedback(), correctDigit);
            predictionBadge.setText("Correction saved for record #" + lastPredictionId);
            predictionBadge.setForeground(UITheme.SUCCESS);
            correctionField.setText("");
        } catch (NumberFormatException exception) {
            predictionBadge.setText("Enter a single digit from 0 to 9");
            predictionBadge.setForeground(UITheme.WARNING);
        } catch (SQLException exception) {
            predictionBadge.setText("Correction could not be saved");
            predictionBadge.setForeground(UITheme.DANGER);
            System.err.println("Feedback save failed: " + exception.getMessage());
        } catch (IOException exception) {
            predictionBadge.setText("Feedback saved, sample could not be stored");
            predictionBadge.setForeground(UITheme.WARNING);
            System.err.println("Training sample save failed: " + exception.getMessage());
        }
    }

    private BufferedImage processedImageForFeedback() {
        if (lastProcessedImage != null) {
            return lastProcessedImage;
        }
        return ImagePreprocessor.preprocess(digitCanvas.getDrawingImage());
    }

    private void saveTrainingSample(BufferedImage image, int digit) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        File file = new File("data/canvas-training/" + digit + "/sample-" + timestamp + ".png");
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        ImageIO.write(image, "png", file);
    }

    private void saveImage(BufferedImage image, File file) {
        try {
            writeImage(image, file);
        } catch (IOException exception) {
            predictionBadge.setText("Could not save image: " + exception.getMessage());
            predictionBadge.setForeground(UITheme.DANGER);
        }
    }

    private void writeImage(BufferedImage image, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        ImageIO.write(image, "png", file);
    }

    private record PredictionOutcome(String predictionText, String confidenceText, String latencyText,
                                     String alternativesText, String conversionText,
                                     long predictionId, int predictionCount,
                                     BufferedImage processedImage, String errorMessage) {
        private static PredictionOutcome error(String message) {
            return new PredictionOutcome(message, "–", "–", "", "", -1, 0, null, message);
        }
    }
}
