package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;

/**
 * Calculate the moving min of time series data.
 */
public class MovingMinStat extends AbstractMovingStat
{

    double min = Double.MAX_VALUE;
    int n = 0;


    @Override
    public void resetMovingStat()
    {
        this.min = Double.MAX_VALUE;
        this.n = 0;
    }

    @Override
    protected void calculateMovingStatInPeriod( Number yy )
    {
        min = Math.min( min, yy.doubleValue() );
        n = n + 1;
    }

    @Override
    protected void addMovingStatForPeriod( XYSeries result, double x )
    {
        if (n > 0)
        {
            result.add( x, min );
        }
        else
        {
            result.add( x, null );
        }
    }

}
