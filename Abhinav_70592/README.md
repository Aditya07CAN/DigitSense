# DigitSense - Abhinav_70592 Module

Modernized AI Studio desktop application for handwritten digit recognition, binary number calculation, and 6-model machine learning benchmarking.

Contributed by **Abhinav** ([@abhigorithm](https://github.com/abhigorithm)).

---

## Key Features & UI Enhancements

* **Modern Dark/Vibrant Design System (`UITheme`):**
  * Built on an elegant Slate-900 / Slate-800 surface architecture with high-contrast text and vibrant accents.
  * **Electric Indigo (`#6366F1`):** Predict button and primary navigation tabs.
  * **Emerald Green (`#10B981`):** Confidence meter, success indicators, and save actions.
  * **Neon Cyan (`#06B6D4`):** Predicted digit hero badge.
  * **Crimson / Rose (`#EF4444`):** Clear canvas button and classification errors.
  * **Cyber Purple (`#8B5CF6`):** CSV export and confusion matrix views.
  * **Vibrant Amber (`#F59E0B`):** Step-by-step binary place-value calculations.

* **Enhanced Drawing Studio Canvas:**
  * Double-buffered Swing rendering with sub-pixel anti-aliasing.
  * 7-color visual palette swatches (Black, Sky Blue, Crimson, Emerald, Purple, Orange, Pink).
  * Eraser tool with double stroke width.
  * Adjustable stroke widths (Thin 12px, Normal 18px, Bold 26px, Thick 36px).
  * Dotted alignment guide grid with center crosshairs.

* **Quick Predict Sidebar:**
  * Mini sketch canvas with real-time prediction.
  * Colored confidence progress bar.
  * Model architecture selector dropdown (CNN, Logistic Regression, MLP, LeNet, Deeper CNN).

* **Model Benchmark Arena:**
  * Evaluates all 6 architectures on the MNIST dataset.
  * Interactive table with accuracy, precision, recall, F1, and training time.
  * Real-time animated progress bar.
  * $10 \times 10$ Confusion Matrix heatmap visualizer and Per-Class Metrics inspector.

---

## How to Run

### Windows (Quick Launch)
Double-click `run.bat` or run in PowerShell:
```powershell
.\run.ps1
```

### Maven
```bash
mvn clean compile
mvn exec:java
```
*(Or use `.\mvnw.cmd exec:java`)*
