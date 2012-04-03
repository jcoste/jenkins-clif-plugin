package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;

/**
 * Calculate the moving max of time series data.
 */
public class MovingMaxStat extends AbstractMovingStat {

	double max = Double.MIN_VALUE;
	int n = 0;


	@Override
	public void resetMovingStat() {
		this.max = Double.MIN_VALUE;
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		max = Math.max(max, yy);
		n = n + 1;
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			result.add(x, max);
		}
		else {
			result.add(x, null);
		}
	}

}
