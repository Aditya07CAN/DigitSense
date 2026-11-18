package com.digitsense.ui;

import com.digitsense.image.ImagePreprocessor;
import com.digitsense.model.DigitModel;
import com.digitsense.model.ModelFactory;
import com.digitsense.model.PredictionResult;
import com.digitsense.letter.LetterClassifier;
import com.digitsense.roman.RomanNumeralConverter;
import com.digitsense.roman.RomanNumeralClassifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 * Compact quick-prediction sidebar panel with real-time confidence feedback.
 */
public class QuickPredictPanel extends JPanel {

    private final DigitCanvas miniCanvas;
    private final JLabel resultDigitLabel;
    private final JLabel resultConfLabel;
    private final JProgressBar confidenceBar;
    private final JLabel resultAltLabel;
    private final JLabel statusLabel;
    private final JComboBox<String> modelChoice;
    private final JButton predictButton;
    private final JButton clearButton;

    public QuickPredictPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BG_SURFACE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(210, 0));

        // ── Title Header ──
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UITheme.BG_SURFACE);
        JLabel titleLabel = new JLabel("⚡ Quick Predict", JLabel.CENTER);
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_MAIN);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // ── Mini Canvas Container ──
        miniCanvas = new DigitCanvas(160, 160);
        miniCanvas.setPreferredSize(new Dimension(160, 160));
        miniCanvas.setStrokeWidth(16);

        JPanel canvasCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4)) {
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
        canvasCard.setOpaque(false);
        canvasCard.add(miniCanvas);

        // ── Buttons ──
        predictButton = UITheme.createButton("Predict", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        clearButton = UITheme.createButton("Clear", UITheme.DANGER, UITheme.DANGER_HOVER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(predictButton);
        buttonPanel.add(clearButton);

        // ── Result Display Card ──
        JPanel resultCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        resultCard.setOpaque(false);
        resultCard.setLayout(new BorderLayout(4, 4));
        resultCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        resultDigitLabel = new JLabel("–", JLabel.CENTER);
        resultDigitLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        resultDigitLabel.setForeground(UITheme.CYAN);

        resultConfLabel = new JLabel("Confidence: –", JLabel.CENTER);
        resultConfLabel.setFont(UITheme.FONT_SMALL);
        resultConfLabel.setForeground(UITheme.TEXT_MUTED);

        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setValue(0);
        confidenceBar.setStringPainted(true);
        confidenceBar.setForeground(UITheme.SUCCESS);
        confidenceBar.setBackground(UITheme.BG_INPUT);
        confidenceBar.setFont(UITheme.FONT_SMALL);
        confidenceBar.setPreferredSize(new Dimension(100, 14));

        resultAltLabel = new JLabel("Alternatives: –", JLabel.CENTER);
        resultAltLabel.setFont(UITheme.FONT_SMALL);
        resultAltLabel.setForeground(UITheme.TEXT_FAINT);

        statusLabel = new JLabel("Sketch a digit & predict", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(UITheme.WARNING);

        JPanel resultDetails = new JPanel(new GridLayout(4, 1, 0, 3));
        resultDetails.setOpaque(false);
        resultDetails.add(resultConfLabel);
        resultDetails.add(confidenceBar);
        resultDetails.add(resultAltLabel);
        resultDetails.add(statusLabel);

        resultCard.add(resultDigitLabel, BorderLayout.NORTH);
        resultCard.add(resultDetails, BorderLayout.CENTER);

        // ── Model Selector ──
        JPanel modelPanel = new JPanel(new BorderLayout(2, 4));
        modelPanel.setOpaque(false);
        JLabel modelLabel = new JLabel("Architecture:");
        modelLabel.setFont(UITheme.FONT_SMALL);
        modelLabel.setForeground(UITheme.TEXT_MUTED);

        modelChoice = new JComboBox<>();
        modelChoice.setFont(UITheme.FONT_BODY);
        modelChoice.setBackground(UITheme.BG_INPUT);
        modelChoice.setForeground(UITheme.TEXT_MAIN);
        for (String name : ModelFactory.getAvailableModelNames()) {
            modelChoice.addItem(name);
        }

        modelPanel.add(modelLabel, BorderLayout.NORTH);
        modelPanel.add(modelChoice, BorderLayout.CENTER);

        // ── Assemble Center & Bottom ──
        JPanel centerContainer = new JPanel(new BorderLayout(6, 6));
        centerContainer.setOpaque(false);
        centerContainer.add(canvasCard, BorderLayout.NORTH);
        centerContainer.add(buttonPanel, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout(6, 6));
        bottomContainer.setOpaque(false);
        bottomContainer.add(resultCard, BorderLayout.NORTH);
        bottomContainer.add(modelPanel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        registerEvents();
    }

    private void registerEvents() {
        predictButton.addActionListener(event -> runPrediction());
        clearButton.addActionListener(event -> {
            miniCanvas.clear();
            resultDigitLabel.setText("–");
            resultDigitLabel.setForeground(UITheme.CYAN);
            resultConfLabel.setText("Confidence: –");
            confidenceBar.setValue(0);
            resultAltLabel.setText("Alternatives: –");
            statusLabel.setText("Sketch a digit & predict");
            statusLabel.setForeground(UITheme.TEXT_FAINT);
        });
    }

    private void runPrediction() {
        predictButton.setEnabled(false);
        statusLabel.setText("Predicting...");
        statusLabel.setForeground(UITheme.INFO);

        Thread worker = new Thread(() -> {
            try {
                BufferedImage processed = ImagePreprocessor.preprocess(miniCanvas.getDrawingImage());
                String selectedModel = (String) modelChoice.getSelectedItem();
                DigitModel model = ModelFactory.loadModelByName(selectedModel);
                PredictionResult result = model.predict(processed);

                EventQueue.invokeLater(() -> {
                    if (result.isValidDigit()) {
                        resultDigitLabel.setText(String.valueOf(result.digit()));
                        resultDigitLabel.setForeground(UITheme.SUCCESS);
                        int confPct = (int) Math.round(result.confidence() * 100);
                        resultConfLabel.setText("Confidence: " + confPct + "%");
                        confidenceBar.setValue(confPct);
                        if (confPct >= 80) {
                            confidenceBar.setForeground(UITheme.SUCCESS);
                        } else if (confPct >= 50) {
                            confidenceBar.setForeground(UITheme.WARNING);
                        } else {
                            confidenceBar.setForeground(UITheme.DANGER);
                        }
                        RomanNumeralClassifier.RomanCharResult rChar = RomanNumeralClassifier.classify(processed);
                        LetterClassifier.LetterResult lResult = LetterClassifier.classify(processed);
                        String romanStr = RomanNumeralConverter.toRoman(result.digit()).roman();
                        resultAltLabel.setText("Letter: " + lResult.letter() + " (" + Math.round(lResult.confidence() * 100) + "%) | Roman: " + rChar.character());
                        statusLabel.setText("Prediction complete");
                        statusLabel.setForeground(UITheme.SUCCESS);
                    } else {
                        resultDigitLabel.setText("?");
                        resultDigitLabel.setForeground(UITheme.WARNING);
                        resultConfLabel.setText("Confidence: –");
                        confidenceBar.setValue(0);
                        resultAltLabel.setText("Draw clearer stroke");
                        statusLabel.setText("Digit not recognized");
                        statusLabel.setForeground(UITheme.WARNING);
                    }
                    predictButton.setEnabled(true);
                });
            } catch (Exception ex) {
                EventQueue.invokeLater(() -> {
                    statusLabel.setText("Prediction error");
                    statusLabel.setForeground(UITheme.DANGER);
                    predictButton.setEnabled(true);
                });
            }
        }, "quick-predict-worker");
        worker.start();
    }

    public DigitCanvas getMiniCanvas() {
        return miniCanvas;
    }
}
