package org.ow2.clif.jenkins.chart.movingstatistics;

import org.apache.commons.math.stat.descriptive.DescriptiveStatisticsImpl;
import org.jfree.data.xy.XYSeries;


/**
 * Calculate the moving stabdard deviation of time series data.
 */
public class MovingStdDevStat extends AbstractMovingStat
{

	DescriptiveStatisticsImpl stat = new DescriptiveStatisticsImpl();
	int n = 0;


	@Override
	public void resetMovingStat()
	{
		this.stat = new DescriptiveStatisticsImpl();
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod( Number yy )
	{
		stat.addValue( yy.doubleValue() );
		n = n + 1;
	}

	@Override
	protected void addMovingStatForPeriod( XYSeries result, double x )
	{
		if (n > 0)
		{
			result.add( x, stat.getStandardDeviation() );
		}
		else
		{
			result.add( x, null );
		}
	}

}
