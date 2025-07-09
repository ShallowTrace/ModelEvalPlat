package com.modelevalplat.util;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class MnistDatasetLoader {
    private static final String DATASET_DIR = "dataset/mnist/";
    private static final String TEST_IMAGES_FILE = "t10k-images-idx3-ubyte";
    private static final String TEST_LABELS_FILE = "t10k-labels-idx1-ubyte";

    public float[][][] loadTestImages() throws IOException {
        // 实现MNIST图像文件解析逻辑
        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(Paths.get(DATASET_DIR, TEST_IMAGES_FILE).toFile())))
        {
            // ... 解析代码 ...
        }
    }

    public float[][] loadTestLabels() throws IOException {
        // 实现MNIST标签文件解析逻辑
        // ...
    }
}