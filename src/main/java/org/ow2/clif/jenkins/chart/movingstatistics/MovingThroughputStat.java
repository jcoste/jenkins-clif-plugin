package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;


/**
 * Calculate the moving throughput of time series data.
 */
public class MovingThroughputStat extends AbstractMovingStat {

	int n = 0;
	double firstCall = Long.MAX_VALUE;
	double lastCall = Long.MIN_VALUE;

	@Override
	public void resetMovingStat() {
		this.n = 0;
		this.firstCall = Long.MAX_VALUE;
		this.lastCall = Long.MIN_VALUE;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		this.n++;
		this.firstCall = Math.min(this.firstCall, xx);
		this.lastCall = Math.max(this.lastCall, xx);
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			result.add(x, ((this.n * 1000) / (lastCall - firstCall)));
		}
		else {
			result.add(x, null);
		}
	}

}
