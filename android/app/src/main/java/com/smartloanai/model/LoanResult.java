package com.smartloanai.model;

/**
 * Data model for loan prediction results.
 */
public class LoanResult {
    private boolean approved;
    private double probability;
    private String riskLevel;
    private String riskColor;
    private double healthScore;
    private String healthGrade;
    private String predictionId;

    public LoanResult() {}

    // Getters and setters
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskColor() { return riskColor; }
    public void setRiskColor(String riskColor) { this.riskColor = riskColor; }
    public double getHealthScore() { return healthScore; }
    public void setHealthScore(double healthScore) { this.healthScore = healthScore; }
    public String getHealthGrade() { return healthGrade; }
    public void setHealthGrade(String healthGrade) { this.healthGrade = healthGrade; }
    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }
}
