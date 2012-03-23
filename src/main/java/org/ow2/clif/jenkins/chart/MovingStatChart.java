/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2004, 2008 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.ow2.clif.jenkins.parser.clif.Messages;

import java.awt.*;

/**
 * Class used to generate and save a chart attach to a build
 *
 * @author Julien Coste
 */
public class MovingStatChart
        extends AbstractChart
{

    protected boolean scatterPlot;
    protected long statisticalPeriod;

    protected XYSeries eventSerie;

    public MovingStatChart( String testplan, String bladeId, String event, int statisticalPeriod )
    {
        super( "movingStat", bladeId, testplan, event );
        this.statisticalPeriod = statisticalPeriod;
        this.eventSerie = new XYSeries( event );
    }

    public void setScatterPlot( boolean scatterPlot )
    {
        this.scatterPlot = scatterPlot;
    }

    public void setStatisticalPeriod( long statisticalPeriod )
    {
        this.statisticalPeriod = statisticalPeriod;
    }

    public void addData( double x, double y )
    {
        this.eventSerie.add( x, y );
    }

    @Override
    protected JFreeChart createChart()
    {
        XYSeriesCollection coreDataset = new XYSeriesCollection();
        coreDataset.addSeries( this.eventSerie );

        XYSeries movingSeries = MovingAverage.createMovingAverage( coreDataset, 0, " Moving Average", statisticalPeriod,
                                                                   statisticalPeriod );
        XYSeries maxSeries = MovingAverage.createMovingMax( coreDataset, 0, " Moving Max", statisticalPeriod,
                                                            statisticalPeriod );
        XYSeries minSeries = MovingAverage.createMovingMin( coreDataset, 0, " Moving Min", statisticalPeriod,
                                                            statisticalPeriod );
        XYSeriesCollection movingDataset = new XYSeriesCollection();
        movingDataset.addSeries( movingSeries );
        movingDataset.addSeries( maxSeries );
        movingDataset.addSeries( minSeries );

        JFreeChart chart;
        if (this.scatterPlot)
        {
            chart = ChartFactory
                    .createScatterPlot( this.testplan + " - " + this.bladeId + " - " + this.event + "( moving period " +
                                                statisticalPeriod + "ms )",
                                        // chart title
                                        Messages.CallChart_Time(), // x axis label
                                        Messages.CallChart_ResponseTime(), // y axis label
                                        movingDataset, // data
                                        PlotOrientation.VERTICAL, true, // include legend
                                        true, // tooltips
                                        false // urls
                    );
        }
        else
        {
            chart = ChartFactory.createXYLineChart( this.testplan + " - " + this.bladeId + " - " + this.event,
                                                    // chart title
                                                    Messages.CallChart_Time(), // x axis label
                                                    this.event, // y axis label
                                                    movingDataset, // data
                                                    PlotOrientation.VERTICAL, false, // include legend
                                                    true, // tooltips
                                                    false // urls
            );
        }

        chart.setBackgroundPaint( Color.white );
        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint( Color.lightGray );
        plot.setAxisOffset( new RectangleInsets( 5.0, 5.0, 5.0, 5.0 ) );
        plot.setDomainGridlinePaint( Color.white );
        plot.setRangeGridlinePaint( Color.white );

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible( 0, false );
        renderer.setSeriesPaint( 0, Color.BLUE );
        renderer.setSeriesStroke( 0, new BasicStroke( 1 ) );
        renderer.setSeriesShapesVisible( 1, false );
        renderer.setSeriesPaint( 0, Color.RED );
        renderer.setSeriesStroke( 1, new BasicStroke( 2 ) );
        renderer.setSeriesShapesVisible( 2, false );
        renderer.setSeriesPaint( 2, Color.GREEN );
        renderer.setSeriesStroke( 2, new BasicStroke( 2 ) );
        plot.setRenderer( renderer );

        // Force the 0 on vertical axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero( true );
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );

        // Force the 0 on horizontal axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero( true );

        return chart;
    }


}
