package com.digitsense.ui;

import com.digitsense.model.ModelMetrics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Modern dialog showing per-class precision, recall, and F1 metrics for a selected model.
 */
public class PerClassMetricsDialog extends JDialog {

    public PerClassMetricsDialog(Frame parent, ModelMetrics metrics) {
        super(parent, "Per-Class Performance - " + metrics.getModelName(), true);

        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ── Header ──
        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setBackground(UITheme.BG_SURFACE);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("🎯 Per-Class Performance: " + metrics.getModelName());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLabel = new JLabel(
                String.format("Overall Accuracy: %.2f%%  •  Macro Precision: %.2f%%  •  Macro F1: %.2f%%",
                        metrics.getAccuracy() * 100,
                        metrics.getMacroPrecision() * 100,
                        metrics.getMacroF1() * 100));
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.SUCCESS);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // ── Metrics Table ──
        String[] columnNames = {"Digit Class", "Precision", "Recall", "F1-Score"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        double[] prec = metrics.getPrecisionPerClass();
        double[] rec = metrics.getRecallPerClass();
        double[] f1 = metrics.getF1PerClass();

        for (int digit = 0; digit < 10; digit++) {
            tableModel.addRow(new Object[]{
                    "Digit " + digit,
                    String.format("%.2f%%", prec[digit] * 100),
                    String.format("%.2f%%", rec[digit] * 100),
                    String.format("%.2f%%", f1[digit] * 100)
            });
        }

        // Add Macro row
        tableModel.addRow(new Object[]{
                "Macro Average",
                String.format("%.2f%%", metrics.getMacroPrecision() * 100),
                String.format("%.2f%%", metrics.getMacroRecall() * 100),
                String.format("%.2f%%", metrics.getMacroF1() * 100)
        });

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(UITheme.FONT_BODY);
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_MAIN);
        table.setGridColor(UITheme.BORDER);
        table.getTableHeader().setFont(UITheme.FONT_HEADER);
        table.getTableHeader().setBackground(UITheme.BG_SURFACE);
        table.getTableHeader().setForeground(UITheme.TEXT_MAIN);

        // Center cell renderer with custom coloring
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (row == 10) { // Macro Average row
                    setFont(UITheme.FONT_BOLD);
                    setForeground(UITheme.CYAN);
                    setBackground(UITheme.BG_SURFACE);
                } else if (column == 0) {
                    setFont(UITheme.FONT_BOLD);
                    setForeground(UITheme.WARNING);
                    setBackground(UITheme.BG_CARD);
                } else {
                    setFont(UITheme.FONT_BODY);
                    setForeground(UITheme.TEXT_MAIN);
                    setBackground(UITheme.BG_CARD);
                }
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(new EmptyBorder(8, 12, 8, 12));

        // ── Bottom Panel ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        bottomPanel.setBackground(UITheme.BG_SURFACE);
        JButton closeButton = UITheme.createButton("Close", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        closeButton.addActionListener(event -> dispose());
        bottomPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(480, 480);
        setLocationRelativeTo(parent);
    }
}
