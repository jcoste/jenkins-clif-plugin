package org.ow2.clif.jenkins.chart;

/**
 * This class holds all configuration parmaeters to genertae charts
 */
public class ChartConfiguration {

	private int chartWidth = 1200;

	private int chartHeight = 600;

	private int distributionSliceSize = 20;

	private int distributionSliceNumber = 20;

	private int statisticalPeriod = 5;

	public ChartConfiguration(int chartHeight, int chartWidth, int distributionSliceNumber, int distributionSliceSize,
	                          int statisticalPeriod) {
		this.chartHeight = chartHeight;
		this.chartWidth = chartWidth;
		this.distributionSliceNumber = distributionSliceNumber;
		this.distributionSliceSize = distributionSliceSize;
		this.statisticalPeriod = statisticalPeriod;
	}

	public int getChartHeight() {
		return chartHeight;
	}

	public void setChartHeight(int chartHeight) {
		this.chartHeight = chartHeight;
	}

	public int getChartWidth() {
		return chartWidth;
	}

	public void setChartWidth(int chartWidth) {
		this.chartWidth = chartWidth;
	}

	public int getDistributionSliceNumber() {
		return distributionSliceNumber;
	}

	public void setDistributionSliceNumber(int distributionSliceNumber) {
		this.distributionSliceNumber = distributionSliceNumber;
	}

	public int getDistributionSliceSize() {
		return distributionSliceSize;
	}

	public void setDistributionSliceSize(int distributionSliceSize) {
		this.distributionSliceSize = distributionSliceSize;
	}

	public int getStatisticalPeriod() {
		return statisticalPeriod;
	}

	public void setStatisticalPeriod(int statisticalPeriod) {
		this.statisticalPeriod = statisticalPeriod;
	}
}
