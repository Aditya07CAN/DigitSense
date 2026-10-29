# DigitSense

DigitSense is a Java desktop application that recognizes handwritten digits using a CNN. It supports drawing on a canvas, importing PNG/JPG images, recognizing multi-digit numbers, and converting binary numbers into decimal using the `8 4 2 1` place-value method.

## Features

- Handwritten single-digit and multi-digit recognition.
- CNN model trained with the MNIST dataset.
- Drawing canvas with selectable pen colours.
- PNG, JPG, and JPEG image import.
- Normal, binary, and automatic recognition modes.
- Binary-to-decimal conversion with the calculation displayed.
- Prediction history stored in SQLite.
- CSV export for prediction history.
- Consent-based correction samples and CNN fine-tuning.
- Resizable prediction-history panel.

## Requirements

- Java JDK 25 or newer (Java 25 is the latest LTS release)
- Apache Maven 3.9 or newer
- Desktop environment with a graphical display

Supported platforms:

- Windows 10/11, 64-bit
- Linux desktop, 64-bit
- macOS desktop

Headless servers are not supported because the application uses Java AWT.

## Run from source

### Linux and macOS

```bash
cd /path/to/DigitSense
mvn clean compile
mvn exec:java
```

### Windows PowerShell

```powershell
cd C:\path\to\DigitSense
mvn clean compile
mvn exec:java
```

### VS Code

Open the project folder in VS Code, open the integrated terminal, and run:

```bash
mvn exec:java
```

## Use the application

1. Select a pen colour.
2. Write a digit or separated multi-digit number.
3. Select `Normal digit mode`, `Binary mode`, or `Auto detect`.
4. Click `Predict`.

To import an image, click `Open Image` and select a `.png`, `.jpg`, or `.jpeg` file.

For binary input such as `0001`, the application displays:

```text
(0 × 8) + (0 × 4) + (0 × 2) + (1 × 1) = 1
```

## Train the CNN

```bash
mvn exec:java -Dexec.mainClass=com.digitsense.training.MnistCnnTrainer
```

The trained models are saved in:

```text
models/digitsense-cnn.zip
models/digitsense-cnn-base.zip
```

The first training run may download MNIST and requires internet access.

## Fine-tuning

After collecting balanced, consented correction samples:

```bash
mvn exec:java -Dexec.mainClass=com.digitsense.training.CanvasFineTuner
```

At least five samples for every digit from `0` to `9` are required.

## Test and package

```bash
mvn test
mvn package -DskipTests
```

The project JAR is created at:

```text
target/digitsense-0.1.0-SNAPSHOT.jar
```

For the current version, run the application with `mvn exec:java` so Maven provides all dependencies.

## Share or download

Download or clone the repository, install Java and Maven, then run:

```bash
cd DigitSense
mvn exec:java
```

Include the `models` folder when sharing a trained version. The `data` folder contains local runtime files such as the SQLite database, exports, and user samples.

## Project structure

```text
DigitSense/
├── src/main/java/com/digitsense/app/        Application entry point
├── src/main/java/com/digitsense/ui/         AWT interface and canvas
├── src/main/java/com/digitsense/image/      Image processing
├── src/main/java/com/digitsense/model/      CNN prediction classes
├── src/main/java/com/digitsense/binary/     Binary conversion
├── src/main/java/com/digitsense/database/   SQLite storage
├── src/main/java/com/digitsense/training/   Training and fine-tuning
├── src/test/                                Automated tests
├── models/                                  Saved CNN models
├── data/                                    Runtime data
├── docs/                                    Architecture notes
└── pom.xml                                 Maven configuration
```

More technical details are available in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
