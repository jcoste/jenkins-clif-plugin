package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;


/**
 * Calculate the moving throughput of time series data.
 */
public class MovingThroughputStat extends AbstractMovingStat {

	int n = 0;

	long periodMs;

	public MovingThroughputStat(long periodMs) {
		super();
		this.periodMs = periodMs;
	}

	@Override
	public void resetMovingStat() {
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		this.n++;
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			result.add(x, ((this.n * 1000) / periodMs));
		}
		else {
			result.add(x, null);
		}
	}

}
