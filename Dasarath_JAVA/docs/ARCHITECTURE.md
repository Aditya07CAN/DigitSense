# DigitSense Architecture

DigitSense is a standalone Java desktop application. It uses Java AWT for the interface, Deeplearning4j for the CNN, SQLite through JDBC for persistence, and Java I/O for image and CSV files.

## Runtime flow

```text
User drawing
    -> AWT Canvas
    -> DigitSegmenter
    -> ImagePreprocessor
    -> 28 x 28 grayscale image
    -> DigitModel / Deeplearning4j CNN
    -> prediction, confidence, top-3 alternatives, latency
    -> SQLite prediction record
    -> history panel and optional CSV export
```

## Main packages

| Package | Responsibility |
|---|---|
| `com.digitsense.app` | Application entry point |
| `com.digitsense.ui` | AWT frame, canvas, buttons, events, history |
| `com.digitsense.image` | Segmentation and 28 x 28 preprocessing |
| `com.digitsense.model` | Model interface, CNN loader, prediction result |
| `com.digitsense.database` | JDBC connection, tables, predictions, feedback, exports |
| `com.digitsense.training` | MNIST training and safe canvas fine-tuning |

## Stored data

- `data/digitsense.db`: SQLite database
- `data/canvas-samples/`: original drawings saved by the user
- `data/canvas-training/<digit>/`: consented corrected samples
- `data/preprocessed/`: 28 x 28 input images
- `data/exports/predictions.csv`: exported prediction history
- `models/digitsense-cnn.zip`: active CNN
- `models/digitsense-cnn-base.zip`: protected balanced MNIST model

## Course concepts used

- Classes, objects, constructors, packages, inheritance, and interfaces
- AWT components, containers, layout managers, and event delegation
- `Graphics`, `Canvas`, mouse events, and `WindowAdapter`
- Java I/O with images, CSV files, and buffered writers
- JDBC with prepared statements and SQLite tables
- Threads and `EventQueue` for background prediction
- Exceptions and validation for user and database errors

## Commands

```bash
mvn test
mvn exec:java
mvn exec:java -Dexec.mainClass=com.digitsense.training.MnistCnnTrainer
```

