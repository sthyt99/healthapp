package com.example.health_app.dto;

import java.time.LocalDate;

/**
 * 健康記録DTOクラス
 * 表示項目をDTOに集中管理。
 */
public class HealthRecordDto {

	private Long id;
	private LocalDate recordDate;
	private double weight;
	private int systolic;
	private int diastolic;
	private double bodyTemperature;
	private int steps;

	public HealthRecordDto() {
	}

	public HealthRecordDto(Long id, LocalDate recordDate, double weight, int systolic, int diastolic,
			double bodyTemperature, int steps) {
		this.id = id;
		this.recordDate = recordDate;
		this.weight = weight;
		this.systolic = systolic;
		this.diastolic = diastolic;
		this.bodyTemperature = bodyTemperature;
		this.steps = steps;
	}

	// Getter & Setter
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDate recordDate) {
		this.recordDate = recordDate;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getSystolic() {
		return systolic;
	}

	public void setSystolic(int systolic) {
		this.systolic = systolic;
	}

	public int getDiastolic() {
		return diastolic;
	}

	public void setDiastolic(int diastolic) {
		this.diastolic = diastolic;
	}

	public double getBodyTemperature() {
		return bodyTemperature;
	}

	public void setBodyTemperature(double bodyTemperature) {
		this.bodyTemperature = bodyTemperature;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}
}
