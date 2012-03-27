package org.ow2.clif.jenkins.chart.movingstatistics;

import org.apache.commons.math.util.ResizableDoubleArray;
import org.jfree.data.xy.XYSeries;

import java.util.Arrays;


/**
 * Calculate the moving median of time series data.
 */
public class MovingMedianStat extends AbstractMovingStat
{
    ResizableDoubleArray values;
    int n = 0;


    @Override
    public void resetMovingStat()
    {
        this.values = new ResizableDoubleArray();
        this.n = 0;
    }

    @Override
    protected void calculateMovingStatInPeriod( Number yy )
    {
        values.addElement( yy.doubleValue() );
        n = n + 1;
    }

    @Override
    protected void addMovingStatForPeriod( XYSeries result, double x )
    {
        if (n > 0)
        {
            double[] valuesArrayfromResisable = values.getValues();
            // ResizableDoubleArray has a bug (at least in v1.1 which is shipped with Clif-core)
            // . It returns an array with too many values
            double[] valuesArray = Arrays.copyOf( valuesArrayfromResisable, n );
            Arrays.sort( valuesArray );
            if (valuesArray.length % 2 == 0)
            {
                result.add( x, valuesArray[(valuesArray.length / 2) - 1] );
            }
            else
            {
                result.add( x, valuesArray[(valuesArray.length - 1) / 2] );
            }
        }
        else
        {
            result.add( x, null );
        }
    }

}
