package org.ow2.clif.jenkins.chart;

import org.apache.commons.codec.digest.DigestUtils;

/**
 */
public class ChartId
{
    protected final String chartType;

    protected final String testplan;

    protected final String bladeId;

    protected final String event;

    public ChartId( String chartType, String testplan, String bladeId, String event )
    {
        this.chartType = chartType;
        this.testplan = testplan;
        this.bladeId = bladeId;
        this.event = event;
    }

    public String getId()
    {
        return DigestUtils.shaHex( chartType + testplan + bladeId + event );
    }

    public String getBladeId()
    {
        return bladeId;
    }

    public String getChartType()
    {
        return chartType;
    }

    public String getEvent()
    {
        return event;
    }

    public String getTestplan()
    {
        return testplan;
    }
}
