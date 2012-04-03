package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;

/**
 * Calculate the moving average of time series data.
 */
public class MovingAverageStat extends AbstractMovingStat {

	double sum = 0.0;
	int n = 0;


	@Override
	public void resetMovingStat() {
		this.sum = 0.0;
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		sum = sum + yy;
		n = n + 1;
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			result.add(x, sum / n);
		}
		else {
			result.add(x, null);
		}
	}

}
