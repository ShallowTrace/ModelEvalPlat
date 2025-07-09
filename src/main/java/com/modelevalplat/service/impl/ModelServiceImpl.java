package com.modelevalplat.service.impl;

import com.modelevalplat.model.ModelEvaluationResult;
import com.modelevalplat.service.ModelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

@Service
public class ModelServiceImpl implements ModelService {

    @Value("${model.upload.path}")
    private String uploadPath;

    @Override
    public String uploadModel(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("上传文件不能为空");
        }

        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (!fileName.endsWith(".h5") && !fileName.endsWith(".pb")) {
            throw new Exception("不支持的文件类型，仅支持 .h5 和 .pb 格式的模型文件");
        }

        // 创建上传目录（如果不存在）
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 保存文件
        Path filePath = Paths.get(uploadPath + File.separator + fileName);
        Files.write(filePath, file.getBytes());

        return filePath.toString();
    }

    @Override
    public ModelEvaluationResult evaluateModel(String modelPath) throws Exception {
        ModelEvaluationResult result = new ModelEvaluationResult();
        result.setModelPath(modelPath);

        try (SavedModelBundle model = SavedModelBundle.load(modelPath, "serve")) {
            Session session = model.session();

            // 创建MNIST测试数据（28x28像素的手写数字图像）
            float[][][][] testData = generateTestData(100);
            float[] testLabels = generateTestLabels(100);

            long totalInferenceTime = 0;
            int correctPredictions = 0;

            // 对每个测试样本进行推理
            for (int i = 0; i < testData.length; i++) {
                Tensor<?> input = Tensor.create(testData[i]);
                long startTime = System.currentTimeMillis();

                // 执行模型推理（注意：输入输出张量名称需与模型匹配）
                Tensor<?> output = session.runner()
                        .feed("input", input)
                        .fetch("output")
                        .run()
                        .get(0);

                totalInferenceTime += System.currentTimeMillis() - startTime;

                // 解析输出结果
                float[] predictions = new float[10];
                output.copyTo(predictions);
                int predictedLabel = getMaxIndex(predictions);

                if (predictedLabel == testLabels[i]) {
                    correctPredictions++;
                }

                input.close();
                output.close();
            }

            // 计算性能指标
            result.setInferenceTimeMs(totalInferenceTime / testData.length);
            result.setAccuracy((float) correctPredictions / testData.length);
            result.setInputShape(Arrays.toString(new int[]{testData[0].length, testData[0][0].length, testData[0][0][0].length}));
            result.setStatus("Evaluation completed successfully");

        } catch (Exception e) {
            result.setStatus("Evaluation failed: " + e.getMessage());
            throw e;
        }

        return result;
    }

    // 生成测试数据
    private float[][][][] generateTestData(int count) {
        float[][][][] data = new float[count][28][28][1];
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // 生成随机数字图像（实际应用中应使用真实测试数据）
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    data[i][x][y][0] = random.nextFloat();
                }
            }
        }
        return data;
    }

    // 生成测试标签
    private float[] generateTestLabels(int count) {
        float[] labels = new float[count];
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            labels[i] = random.nextInt(10);
        }
        return labels;
    }

    // 获取预测结果中概率最大的索引
    private int getMaxIndex(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}