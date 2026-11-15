#!/usr/bin/env bash

set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
dataset_dir="$project_root/data/mnist"
base_url="https://storage.googleapis.com/cvdf-datasets/mnist"

mkdir -p "$dataset_dir"

files=(
  train-images-idx3-ubyte.gz
  train-labels-idx1-ubyte.gz
  t10k-images-idx3-ubyte.gz
  t10k-labels-idx1-ubyte.gz
)

for file in "${files[@]}"; do
  if [[ -f "$dataset_dir/$file" ]]; then
    echo "Already exists: $file"
  else
    echo "Downloading: $file"
    curl --fail --location --retry 3 --output "$dataset_dir/$file" "$base_url/$file"
  fi
done

echo "MNIST dataset is ready in $dataset_dir"
