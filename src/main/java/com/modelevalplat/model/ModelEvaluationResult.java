package com.modelevalplat.model;

import java.util.Arrays;

public class ModelEvaluationResult {
    private String modelPath;
    private long inferenceTimeMs;
    private String inputShape;
    private String outputShape;
    private String status;
    private float accuracy;
    private float avgInferenceTime;
    private int totalSamples;
    private int correctSamples;
    private String errorMessage;

    // Getters and Setters
    public String getModelPath() { return modelPath; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }
    public long getInferenceTimeMs() { return inferenceTimeMs; }
    public void setInferenceTimeMs(long inferenceTimeMs) { this.inferenceTimeMs = inferenceTimeMs; }
    public String getInputShape() { return inputShape; }
    public void setInputShape(String inputShape) { this.inputShape = inputShape; }
    public String getOutputShape() { return outputShape; }
    public void setOutputShape(String outputShape) { this.outputShape = outputShape; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public float getAccuracy() { return accuracy; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    // Getters
    public String getStatus() { return status; }
    public float getAccuracy() { return accuracy; }
    public float getAvgInferenceTime() { return avgInferenceTime; }
    public int getTotalSamples() { return totalSamples; }
    public int getCorrectSamples() { return correctSamples; }
    public String getInputShape() { return inputShape; }
    public String getOutputShape() { return outputShape; }
    public String getErrorMessage() { return errorMessage; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }
    public void setAvgInferenceTime(float avgInferenceTime) { this.avgInferenceTime = avgInferenceTime; }
    public void setTotalSamples(int totalSamples) { this.totalSamples = totalSamples; }
    public void setCorrectSamples(int correctSamples) { this.correctSamples = correctSamples; }
    public void setInputShape(String inputShape) { this.inputShape = inputShape; }
    public void setOutputShape(String outputShape) { this.outputShape = outputShape; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
