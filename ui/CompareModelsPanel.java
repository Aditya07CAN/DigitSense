package com.digitsense.ui;

import com.digitsense.model.ModelBenchmark;
import com.digitsense.model.ModelMetrics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Modern panel that trains, evaluates, and benchmarks all six digit recognition models.
 */
public class CompareModelsPanel extends JPanel {

    private final JButton runBenchmarkButton;
    private final JButton viewConfusionButton;
    private final JButton viewPerClassButton;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private final JLabel selectedModelLabel;
    private List<ModelMetrics> benchmarkResults = new ArrayList<>();
    private final Frame parentFrame;

    public CompareModelsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // ── Header Panel ──
        JPanel headerPanel = new JPanel(new BorderLayout(10, 6));
        headerPanel.setBackground(UITheme.BG_SURFACE);
        headerPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel("⚡ Model Benchmark Arena");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLabel = new JLabel("Evaluate all 6 architectures (Logistic Regression, KNN, Random Forest, MLP, LeNet, Deeper CNN)");
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titles = new JPanel(new BorderLayout(2, 2));
        titles.setOpaque(false);
        titles.add(titleLabel, BorderLayout.NORTH);
        titles.add(subtitleLabel, BorderLayout.SOUTH);

        runBenchmarkButton = UITheme.createButton("Run Benchmark", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);

        headerPanel.add(titles, BorderLayout.CENTER);
        headerPanel.add(runBenchmarkButton, BorderLayout.EAST);

        // ── Progress Bar & Status ──
        progressLabel = new JLabel("Click 'Run Benchmark' to train and evaluate models on MNIST");
        progressLabel.setFont(UITheme.FONT_BODY);
        progressLabel.setForeground(UITheme.WARNING);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(UITheme.SUCCESS);
        progressBar.setBackground(UITheme.BG_INPUT);
        progressBar.setPreferredSize(new Dimension(100, 16));
        progressBar.setVisible(false);

        JPanel progressPanel = new JPanel(new BorderLayout(6, 4));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(4, 2, 6, 2));
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.SOUTH);

        // ── Table ──
        String[] columns = {"Model Architecture", "Accuracy", "Precision", "Recall", "F1-Score", "Time (s)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(32);
        resultsTable.setFont(UITheme.FONT_BODY);
        resultsTable.setBackground(UITheme.BG_CARD);
        resultsTable.setForeground(UITheme.TEXT_MAIN);
        resultsTable.setGridColor(UITheme.BORDER);
        resultsTable.setSelectionBackground(UITheme.BG_CARD_LIGHT);
        resultsTable.setSelectionForeground(UITheme.TEXT_MAIN);
        resultsTable.getTableHeader().setFont(UITheme.FONT_HEADER);
        resultsTable.getTableHeader().setBackground(UITheme.BG_SURFACE);
        resultsTable.getTableHeader().setForeground(UITheme.TEXT_MAIN);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0) {
                    setHorizontalAlignment(JLabel.LEFT);
                    setFont(UITheme.FONT_BOLD);
                    setForeground(isSelected ? Color.WHITE : UITheme.CYAN);
                } else if (column == 1) { // Accuracy
                    setHorizontalAlignment(JLabel.CENTER);
                    setFont(UITheme.FONT_BOLD);
                    setForeground(isSelected ? Color.WHITE : UITheme.SUCCESS);
                } else {
                    setHorizontalAlignment(JLabel.CENTER);
                    setFont(UITheme.FONT_BODY);
                    setForeground(isSelected ? Color.WHITE : UITheme.TEXT_MUTED);
                }
                setBackground(isSelected ? UITheme.BG_CARD_LIGHT : UITheme.BG_CARD);
                return this;
            }
        };

        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(new EmptyBorder(4, 0, 4, 0));

        // ── Bottom Action Panel ──
        viewConfusionButton = UITheme.createButton("View Confusion Matrix", UITheme.PURPLE, new Color(168, 85, 247));
        viewConfusionButton.setEnabled(false);

        viewPerClassButton = UITheme.createButton("Per-Class Metrics", UITheme.SUCCESS, UITheme.SUCCESS_HOVER);
        viewPerClassButton.setEnabled(false);

        selectedModelLabel = new JLabel("Select a model row from the table above");
        selectedModelLabel.setFont(UITheme.FONT_SMALL);
        selectedModelLabel.setForeground(UITheme.TEXT_FAINT);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bottomPanel.setBackground(UITheme.BG_SURFACE);
        bottomPanel.add(viewConfusionButton);
        bottomPanel.add(viewPerClassButton);
        bottomPanel.add(selectedModelLabel);

        // ── Assemble ──
        JPanel topContainer = new JPanel(new BorderLayout(4, 4));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(progressPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        registerEvents();
    }

    private void registerEvents() {
        runBenchmarkButton.addActionListener(event -> startBenchmark());

        resultsTable.getSelectionModel().addListSelectionListener(event -> {
            int selected = resultsTable.getSelectedRow();
            if (selected >= 0 && selected < benchmarkResults.size()) {
                String model = benchmarkResults.get(selected).getModelName();
                selectedModelLabel.setText("Selected: " + model);
                selectedModelLabel.setForeground(UITheme.TEXT_MAIN);
                viewConfusionButton.setEnabled(true);
                viewPerClassButton.setEnabled(true);
            } else {
                viewConfusionButton.setEnabled(false);
                viewPerClassButton.setEnabled(false);
            }
        });

        viewConfusionButton.addActionListener(event -> {
            int selected = resultsTable.getSelectedRow();
            if (selected >= 0 && selected < benchmarkResults.size()) {
                ModelMetrics metrics = benchmarkResults.get(selected);
                ConfusionMatrixDialog dialog = new ConfusionMatrixDialog(
                        parentFrame, metrics.getModelName(), metrics.getConfusionMatrix());
                dialog.setVisible(true);
            }
        });

        viewPerClassButton.addActionListener(event -> {
            int selected = resultsTable.getSelectedRow();
            if (selected >= 0 && selected < benchmarkResults.size()) {
                ModelMetrics metrics = benchmarkResults.get(selected);
                PerClassMetricsDialog dialog = new PerClassMetricsDialog(
                        parentFrame, metrics);
                dialog.setVisible(true);
            }
        });
    }

    private void startBenchmark() {
        runBenchmarkButton.setEnabled(false);
        tableModel.setRowCount(0);
        benchmarkResults.clear();
        viewConfusionButton.setEnabled(false);
        viewPerClassButton.setEnabled(false);
        progressLabel.setText("Initializing benchmark suite...");
        progressLabel.setForeground(UITheme.INFO);
        progressBar.setValue(0);
        progressBar.setVisible(true);

        Thread benchmarkThread = new Thread(() -> {
            try {
                List<ModelMetrics> results = ModelBenchmark.runBenchmark(
                        (modelName, status, percentComplete) -> EventQueue.invokeLater(() -> {
                            progressLabel.setText(modelName + ": " + status);
                            progressBar.setValue(percentComplete);
                        }));

                EventQueue.invokeLater(() -> {
                    benchmarkResults = results;
                    for (ModelMetrics m : results) {
                        tableModel.addRow(new Object[]{
                                m.getModelName(),
                                String.format("%.2f%%", m.getAccuracy() * 100),
                                String.format("%.2f%%", m.getMacroPrecision() * 100),
                                String.format("%.2f%%", m.getMacroRecall() * 100),
                                String.format("%.2f%%", m.getMacroF1() * 100),
                                String.format("%.1f s", m.getTrainingTimeMs() / 1000.0)
                        });
                    }
                    progressLabel.setText("Benchmark complete! All 6 architectures evaluated.");
                    progressLabel.setForeground(UITheme.SUCCESS);
                    progressBar.setValue(100);
                    runBenchmarkButton.setEnabled(true);
                });
            } catch (Exception exception) {
                EventQueue.invokeLater(() -> {
                    progressLabel.setText("Benchmark failed: " + exception.getMessage());
                    progressLabel.setForeground(UITheme.DANGER);
                    runBenchmarkButton.setEnabled(true);
                });
            }
        }, "benchmark-worker");
        benchmarkThread.start();
    }
}
