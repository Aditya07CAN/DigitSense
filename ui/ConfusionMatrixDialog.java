package com.digitsense.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Modern dialog displaying an interactive 10x10 confusion matrix heatmap.
 */
public class ConfusionMatrixDialog extends JDialog {

    private final int[][] matrix;

    public ConfusionMatrixDialog(Frame parent, String modelName, int[][] confusionMatrix) {
        super(parent, "Confusion Matrix - " + modelName, true);
        this.matrix = confusionMatrix;

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ── Title Header ──
        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setBackground(UITheme.BG_SURFACE);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("📊 Confusion Matrix Heatmap: " + modelName);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLabel = new JLabel(
                "Rows = Actual Digit  |  Columns = Predicted Digit  •  Green = Correct, Red = Classification Errors");
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // ── Canvas Heatmap ──
        JPanel matrixCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMatrix(g, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 480);
            }
        };
        matrixCanvas.setBackground(UITheme.BG_CARD);

        JPanel matrixWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        matrixWrapper.setBackground(UITheme.BG_MAIN);
        matrixWrapper.add(matrixCanvas);

        // ── Bottom Panel with Close Button ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        bottomPanel.setBackground(UITheme.BG_SURFACE);
        JButton closeButton = UITheme.createButton("Close", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        closeButton.addActionListener(event -> dispose());
        bottomPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(matrixWrapper, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(540, 600));
        setLocationRelativeTo(parent);
    }

    private void drawMatrix(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int margin = 38;
        int cellWidth = Math.max(1, (width - margin * 2) / 11);
        int cellHeight = Math.max(1, (height - margin * 2) / 11);

        int maxValue = 1;
        for (int[] row : matrix) {
            for (int value : row) {
                maxValue = Math.max(maxValue, value);
            }
        }

        Font headerFont = new Font("Segoe UI", Font.BOLD, 12);
        Font cellFont = new Font("Consolas", Font.BOLD, 11);

        // Draw Predicted header
        g2.setFont(headerFont);
        g2.setColor(UITheme.CYAN);
        g2.drawString("Pred →", margin - 2, margin - 10);
        for (int col = 0; col < 10; col++) {
            int x = margin + (col + 1) * cellWidth;
            String colStr = String.valueOf(col);
            int sw = g2.getFontMetrics().stringWidth(colStr);
            g2.drawString(colStr, x + (cellWidth - sw) / 2, margin - 10);
        }

        // Draw Actual rows
        for (int row = 0; row < 10; row++) {
            int y = margin + (row + 1) * cellHeight;

            // Row header
            g2.setFont(headerFont);
            g2.setColor(UITheme.WARNING);
            String rowStr = String.valueOf(row);
            int sw = g2.getFontMetrics().stringWidth(rowStr);
            g2.drawString(rowStr, margin + (cellWidth - sw) / 2, y + cellHeight / 2 + 5);

            // Cells
            g2.setFont(cellFont);
            for (int col = 0; col < 10; col++) {
                int x = margin + (col + 1) * cellWidth;
                int value = matrix[row][col];
                float intensity = (float) value / (float) maxValue;

                Color cellColor;
                if (row == col) {
                    // Correct predictions: deep emerald to bright green
                    int gVal = (int) (60 + 170 * intensity);
                    int rVal = (int) (15 + 30 * intensity);
                    int bVal = (int) (30 + 80 * intensity);
                    cellColor = new Color(Math.min(255, rVal), Math.min(255, gVal), Math.min(255, bVal));
                } else if (value > 0) {
                    // Errors: dark ruby to neon red
                    int rVal = (int) (120 + 130 * intensity);
                    int gVal = (int) (20 + 40 * (1 - intensity));
                    int bVal = (int) (30 + 50 * (1 - intensity));
                    cellColor = new Color(Math.min(255, rVal), Math.min(255, gVal), Math.min(255, bVal));
                } else {
                    // Empty 0 cell
                    cellColor = UITheme.BG_CARD;
                }

                g2.setColor(cellColor);
                g2.fillRoundRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2, 4, 4);

                g2.setColor(new Color(255, 255, 255, 18));
                g2.drawRoundRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2, 4, 4);

                // Value text
                if (value > 0) {
                    g2.setColor(Color.WHITE);
                    String text = String.valueOf(value);
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text, x + (cellWidth - textWidth) / 2, y + cellHeight / 2 + 4);
                }
            }
        }

        // Left vertical label
        g2.setFont(headerFont);
        g2.setColor(UITheme.WARNING);
        g2.drawString("Act ↓", margin - 2, margin + cellHeight / 2 + 4);
        g2.dispose();
    }
}
