$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$datasetDir = Join-Path $projectRoot "data\mnist"
$baseUrl = "https://storage.googleapis.com/cvdf-datasets/mnist"
$files = @(
    "train-images-idx3-ubyte.gz",
    "train-labels-idx1-ubyte.gz",
    "t10k-images-idx3-ubyte.gz",
    "t10k-labels-idx1-ubyte.gz"
)

New-Item -ItemType Directory -Force -Path $datasetDir | Out-Null

foreach ($file in $files) {
    $destination = Join-Path $datasetDir $file
    if (Test-Path $destination) {
        Write-Host "Already exists: $file"
    } else {
        Write-Host "Downloading: $file"
        Invoke-WebRequest -Uri "$baseUrl/$file" -OutFile $destination
    }
}

Write-Host "MNIST dataset is ready in $datasetDir"
