package com.example.health_app.dto;

import java.time.LocalDate;

public class HealthRecordRequestDto {

	private LocalDate recordDate;
    private double weight;
    private int systolic;
    private int diastolic;
    private double bodyTemperature;
    private int steps;

    public HealthRecordRequestDto() {}

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(double bodyTemperature) { this.bodyTemperature = bodyTemperature; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
}
