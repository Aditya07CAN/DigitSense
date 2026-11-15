# Dataset

DigitSense uses the **MNIST handwritten-digit dataset** for training and evaluation.

MNIST contains 28×28 grayscale images of handwritten digits from `0` to `9`:

- Training set: 60,000 images
- Test set: 10,000 images

The original dataset files are stored in `data/mnist/` as IDX files compressed with gzip. The application currently uses Deeplearning4j's MNIST iterator, which downloads and prepares the same dataset automatically when training or benchmarking is started.

## Download the dataset

From the project root on Linux or macOS:

```bash
bash scripts/download-mnist.sh
```

On Windows PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/download-mnist.ps1
```

The files are downloaded from the official MNIST mirror maintained by Google Cloud's CVDF dataset archive.

## Dataset files

```text
data/mnist/
├── train-images-idx3-ubyte.gz
├── train-labels-idx1-ubyte.gz
├── t10k-images-idx3-ubyte.gz
└── t10k-labels-idx1-ubyte.gz
```

These files are used only as training/evaluation data. The generated SQLite database, exports, processed images, and user canvas samples remain ignored by Git.
